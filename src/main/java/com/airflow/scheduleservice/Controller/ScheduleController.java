// src/main/java/com/airflow/scheduleservice/Controller/ScheduleController.java
package com.airflow.scheduleservice.Controller;

import com.airflow.scheduleservice.dto.*;
import com.airflow.scheduleservice.model.*;
import com.airflow.scheduleservice.service.SchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    private final SchedulerService scheduler;
    private static final Location DFW_EXTERNAL_NORTH =
            new Location(32.931446, -97.040762);

    public ScheduleController(SchedulerService svc) {
        this.scheduler = svc;
    }

    @PostMapping("/run")
    public ResponseEntity<ScheduleResponse> run(
            @RequestBody ScheduleRequest req) {

        Map<String,Warehouse> whMap = req.warehouses().stream()
                .collect(Collectors.toMap(Warehouse::getId, w->w));

        ScheduleResponse resp = scheduler.buildSchedule(
                req.manifests(),
                req.trucks(),
                whMap,
                DFW_EXTERNAL_NORTH,
                req.currentTime(),
                req.events()
        );
        return ResponseEntity.ok(resp);
    }
}
