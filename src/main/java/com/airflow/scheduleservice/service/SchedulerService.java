// src/main/java/com/airflow/scheduleservice/service/SchedulerService.java
package com.airflow.scheduleservice.service;

import com.airflow.scheduleservice.dto.*;
import com.airflow.scheduleservice.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Service
public class SchedulerService {
    private static final Logger logger =
            Logger.getLogger(SchedulerService.class.getName());

    private final GoogleDistanceService distanceService;
    private final int shiftBuffer;
    private final int warehouseHandlingTime;
    private final int airportHandlingTime;

    // event-driven state
    private final Map<String, List<TimeWindow>> truckBlocked     = new HashMap<>();
    private final Map<String, List<TimeWindow>> dockBlocked      = new HashMap<>();
    private final List<DelayWindow>             routeDelays      = new ArrayList<>();
    private boolean                              systemOutage     = false;

    public SchedulerService(
            GoogleDistanceService distanceService,
            @Value("${scheduler.shiftBufferMinutes}") int shiftBuffer,
            @Value("${scheduler.warehouseHandlingMinutes:30}") int warehouseHandlingTime,
            @Value("${scheduler.airportHandlingMinutes:60}") int airportHandlingTime
    ) {
        this.distanceService     = distanceService;
        this.shiftBuffer         = shiftBuffer;
        this.warehouseHandlingTime = warehouseHandlingTime;
        this.airportHandlingTime = airportHandlingTime;
        logger.info(String.format(
                "SchedulerService init: buffer=%d, whHandle=%d, apHandle=%d",
                shiftBuffer, warehouseHandlingTime, airportHandlingTime));
    }

    public ScheduleResponse buildSchedule(
            List<Manifest> manifests,
            List<Truck> trucks,
            Map<String,Warehouse> warehouses,
            Location dfwExternal,
            LocalDateTime now,
            List<ScheduleEvent> events
    ) {
        // 1) preprocess all events into time-windows/delays
        preprocessEvents(events, now);

        // 2) sort by urgency
        manifests.sort(Comparator.comparing(this::earliestDockWindow));

        // 3) prepare result‐holders
        Map<String, List<AssignmentPlan>> assignmentMap = new HashMap<>();
        trucks.forEach(t -> {
            t.getStops().clear();
            assignmentMap.put(t.getId(), new ArrayList<>());
        });
        List<NotDoable> notDoable = new ArrayList<>();

        // 4) greedy assignment
        for (Manifest m : manifests) {
            try {
                Truck chosen = findFeasibleTruckOrThrow(
                        m, trucks, warehouses, dfwExternal, now);
                int seq = chosen.getStops().size()/2 + 1;

                LocalTime truckNow = determineTruckNow(chosen, now.toLocalTime());
                Location origin = lastLocation(chosen, warehouses);

                List<Stop> legs = buildStops(
                        m,
                        warehouses.get(m.getWarehouseId()).getLocation(),
                        warehouses.get(chosen.getHomeWarehouseId()).getLocation(),
                        dfwExternal,
                        truckNow,
                        origin,
                        now.toLocalDate()
                );

                chosen.getStops().addAll(legs);
                assignmentMap.get(chosen.getId()).add(
                        new AssignmentPlan(
                                m.getManifestId(),
                                seq,
                                m.getTaskTypeRaw(),
                                m.getFlightNumber(),
                                m.getWarehouseId(),
                                legs
                        )
                );

            } catch (SchedulingException ex) {
                notDoable.add(new NotDoable(m.getManifestId(), ex.getMessage()));
            }
        }

        List<TruckPlan> plans = new ArrayList<>();
        trucks.forEach(t ->
                plans.add(new TruckPlan(t.getId(), assignmentMap.get(t.getId())))
        );

        return new ScheduleResponse(plans, notDoable);
    }

    //────────────────────────────────────────────────────────────
    // 1) Event preprocessing
    //────────────────────────────────────────────────────────────

    private void preprocessEvents(List<ScheduleEvent> events, LocalDateTime now) {
        truckBlocked.clear();
        dockBlocked.clear();
        routeDelays.clear();
        systemOutage = false;

        for (var e : events) {
            switch (e.getType()) {
                case "truck_breakdown" -> {
                    // block until repair done
                    LocalDateTime until = e.getStartTime()
                            .plusMinutes(e.getExpectedRepairDurationMinutes());
                    addBlock(truckBlocked, e.getTruckId(),
                            new TimeWindow(e.getStartTime(), until));
                }
                case "maintenance_due" -> {
                    addBlock(truckBlocked, e.getTruckId(),
                            new TimeWindow(e.getStartTime(), LocalDateTime.MAX));
                }
                case "dock_unavailable" -> {
                    addBlock(dockBlocked, e.getWarehouseId(),
                            new TimeWindow(e.getStartTime(), e.getEndTime()));
                }
                case "traffic_jam", "road_closure", "flight_gate_change" -> {
                    routeDelays.add(new DelayWindow(
                            new TimeWindow(e.getStartTime(), e.getEndTime()),
                            e.getDelayMinutes()
                    ));
                }
                case "system_outage" -> {
                    if (!now.isBefore(e.getStartTime()) && !now.isAfter(e.getEndTime())) {
                        systemOutage = true;
                    }
                }
                default -> {
                    logger.warning("Unknown event type: " + e.getType());
                }
            }
        }
    }

    private <K> void addBlock(Map<K,List<TimeWindow>> map, K key, TimeWindow w) {
        map.computeIfAbsent(key, k->new ArrayList<>()).add(w);
    }

    //────────────────────────────────────────────────────────────
    // 2) Find & validate a truck
    //────────────────────────────────────────────────────────────

    private Truck findFeasibleTruckOrThrow(
            Manifest m,
            List<Truck> fleet,
            Map<String,Warehouse> warehouses,
            Location dfwExt,
            LocalDateTime now
    ) throws SchedulingException {
        if (systemOutage) throw new SchedulingException("System outage in effect");

        return Stream.concat(
                        fleet.stream().filter(t->!t.isSpare()),
                        fleet.stream().filter(Truck::isSpare)
                )
                .filter(t -> !isTruckBlocked(t.getId(), now))
                .filter(t -> canFinishWithinShift(t, m, warehouses, dfwExt, now))
                .findFirst()
                .orElseThrow(() ->
                        new SchedulingException("No available truck can complete manifest in shift window")
                );
    }

    private boolean canFinishWithinShift(
            Truck truck,
            Manifest m,
            Map<String,Warehouse> warehouses,
            Location dfwExt,
            LocalDateTime now
    ) {
        LocalTime start = determineTruckNow(truck, now.toLocalTime());
        Location origin = lastLocation(truck, warehouses);

        List<Stop> hypo;
        try {
            hypo = buildStops(m,
                    warehouses.get(m.getWarehouseId()).getLocation(),
                    warehouses.get(truck.getHomeWarehouseId()).getLocation(),
                    dfwExt,
                    start,
                    origin,
                    now.toLocalDate()
            );
        } catch (SchedulingException e) {
            return false;
        }

        Stop leg2 = hypo.get(1);
        int driveHome = totalTravelMinutes(
                leg2.endLocation(),
                warehouses.get(truck.getHomeWarehouseId()).getLocation(),
                now
        );
        LocalTime finish = leg2.departureAtEndLocation()
                .plusMinutes(driveHome + shiftBuffer);
        return !finish.isAfter(truck.shiftEnd());
    }

    private boolean isTruckBlocked(String truckId, LocalDateTime at) {
        return truckBlocked.getOrDefault(truckId, List.of())
                .stream().anyMatch(w -> w.includes(at));
    }

    private boolean isDockBlocked(String whId, LocalDateTime at) {
        return dockBlocked.getOrDefault(whId, List.of())
                .stream().anyMatch(w -> w.includes(at));
    }

    private LocalTime determineTruckNow(Truck truck, LocalTime scheduleNow) {
        var last = truck.getLastStop();
        if (last != null) {
            var dep = last.departureAtEndLocation();
            return dep.isAfter(scheduleNow) ? dep : scheduleNow;
        }
        return truck.shiftStart().isAfter(scheduleNow)
                ? truck.shiftStart()
                : scheduleNow;
    }

    private Location lastLocation(Truck truck, Map<String,Warehouse> wh) {
        var last = truck.getLastStop();
        return last != null
                ? last.endLocation()
                : wh.get(truck.getHomeWarehouseId()).getLocation();
    }

    //────────────────────────────────────────────────────────────
    // 3) Build stops (throws SchedulingException on any conflict)
    //────────────────────────────────────────────────────────────

    private List<Stop> buildStops(
            Manifest m,
            Location whLoc,
            Location homeLoc,
            Location dfwExt,
            LocalTime now,
            Location origin,
            LocalDate today
    ) throws SchedulingException {
        boolean dropOff = m.taskType() == TaskType.DROP_OFF_AT_AIRPORT;
        // for dropOff: leave 2h before flightDeparture
        // for pickup: arrive 2h after flightArrival
        LocalTime fixedEta = dropOff
                ? m.departureTime().minusHours(2)
                : m.arrivalTime().plusHours(2);
        LocalDateTime fixedEtaDT = LocalDateTime.of(today, fixedEta);

        var legs = new ArrayList<Stop>(2);

        if (dropOff) {
            // 1) → warehouse
            int t1 = totalTravelMinutes(origin, whLoc, fixedEtaDT.minusHours(2));
            LocalTime dep1 = now;
            LocalTime arr1 = dep1.plusMinutes(t1);
            LocalTime end1 = arr1.plusMinutes(warehouseHandlingTime);
            legs.add(new Stop(origin, whLoc, "Pick-up @ warehouse", dep1, arr1, end1));

            // 2) warehouse → airport
            LocalDateTime dep2DT = distanceService.computeDepartureTime(
                    whLoc, dfwExt, fixedEtaDT);
            LocalTime dep2 = dep2DT.toLocalTime();
            if (dep2.isBefore(end1)) throw new SchedulingException("Cannot depart warehouse in time");
            if (isDockBlocked(m.getWarehouseId(), dep2DT))
                throw new SchedulingException("Warehouse dock unavailable at departure");
            LocalTime arr2 = fixedEta;
            LocalTime end2 = arr2.plusMinutes(airportHandlingTime);
            legs.add(new Stop(whLoc, dfwExt, "Unload @ DFW ("+m.getFlightNumber()+")", dep2, arr2, end2));

        } else {
            // 1) origin → airport
            LocalDateTime dep1DT = distanceService.computeDepartureTime(
                    origin, dfwExt, fixedEtaDT);
            LocalTime dep1 = dep1DT.toLocalTime();
            if (dep1.isBefore(now))
                throw new SchedulingException("Missed departure window for pickup");
            LocalTime arr1 = fixedEta;
            LocalTime end1 = arr1.plusMinutes(airportHandlingTime);
            legs.add(new Stop(origin, dfwExt, "Load @ DFW ("+m.getFlightNumber()+")", dep1, arr1, end1));

            // 2) airport → warehouse
            int t2 = totalTravelMinutes(dfwExt, whLoc, fixedEtaDT.plusMinutes(airportHandlingTime));
            LocalTime dep2 = end1;
            LocalTime arr2 = dep2.plusMinutes(t2);
            LocalTime end2 = arr2.plusMinutes(warehouseHandlingTime);
            if (isDockBlocked(m.getWarehouseId(),
                    LocalDateTime.of(today, arr2))) {
                throw new SchedulingException("Warehouse dock unavailable at arrival");
            }
            legs.add(new Stop(dfwExt, whLoc, "Deliver to warehouse", dep2, arr2, end2));
        }

        return legs;
    }

    //────────────────────────────────────────────────────────────
    // 4) Helpers
    //────────────────────────────────────────────────────────────

    private int totalTravelMinutes(Location a, Location b, LocalDateTime depart) {
        int base = distanceService.travelMinutes(a, b);
        int extra = routeDelays.stream()
                .filter(d -> d.window.includes(depart))
                .mapToInt(d -> d.delay)
                .sum();
        return base + extra;
    }

    // event window
    private static class TimeWindow {
        final LocalDateTime start, end;
        TimeWindow(LocalDateTime s, LocalDateTime e) { start = s; end = e; }
        boolean includes(LocalDateTime t) {
            return !t.isBefore(start) && !t.isAfter(end);
        }
    }
    // delay on route
    private static class DelayWindow {
        final TimeWindow window;
        final int delay;
        DelayWindow(TimeWindow w, int d) { window = w; delay = d; }
    }

    // exception for scheduling failures
    public static class SchedulingException extends Exception {
        public SchedulingException(String msg) { super(msg); }
    }
    private LocalTime earliestDockWindow(Manifest m) {
        return m.taskType() == TaskType.DROP_OFF_AT_AIRPORT
                // drop-offs need to leave warehouse 2h30m before flight departs
                ? m.departureTime().minusHours(2).minusMinutes(30)
                // pickups come in 1h after flight arrives
                : m.arrivalTime().plusHours(1);
    }

}
