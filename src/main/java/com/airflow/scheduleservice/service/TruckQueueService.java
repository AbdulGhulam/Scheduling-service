package com.airflow.scheduleservice.service;

import com.airflow.scheduleservice.dto.QueueResponseDTO;
import com.airflow.scheduleservice.dto.TruckRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TruckQueueService {
    @Value("${queue.enoughTime}")
    private Integer enoughTime;


    private static final Map<String, Integer> PRIORITY_MAP = Map.of(
            "critical", 4,
            "high", 3,
            "medium", 2,
            "low", 1
    );


    public QueueResponseDTO generateTruckQueue(String gateId, List<TruckRequestDTO> truckers) {

        // Separate trucks already queued and trucks requesting new access
        List<TruckRequestDTO> alreadyQueued = truckers.stream()
                .filter(t -> t.getCurrSeqNo() != null)
                .collect(Collectors.toList());

        List<TruckRequestDTO> requestingNew = truckers.stream()
                .filter(t -> t.getRequestOrder() != null)
                .collect(Collectors.toList());

        // Adjust already queued trucks only if necessary
        alreadyQueued.sort((t1, t2) -> {
            if (!t1.getCurrSeqNo().equals(t2.getCurrSeqNo())) {
                return t1.getCurrSeqNo().compareTo(t2.getCurrSeqNo());
            }
            return Integer.compare(PRIORITY_MAP.getOrDefault(t2.getPriority(), 0),
                    PRIORITY_MAP.getOrDefault(t1.getPriority(), 0));
        });

        // Adjust sequence considering waiting time and priority
        List<TruckRequestDTO> adjustedQueue = new ArrayList<>(alreadyQueued);

        for (TruckRequestDTO newTrucker : requestingNew) {
            boolean inserted = false;

            for (int i = 0; i < adjustedQueue.size(); i++) {
                TruckRequestDTO queuedTruck = adjustedQueue.get(i);

                boolean canReplace = queuedTruck.getAlreadyspendtimeParking() < enoughTime &&
                        PRIORITY_MAP.getOrDefault(newTrucker.getPriority(), 0) >
                                PRIORITY_MAP.getOrDefault(queuedTruck.getPriority(), 0);

                if (canReplace) {
                    adjustedQueue.add(i, newTrucker);
                    inserted = true;
                    break;
                }
            }

            if (!inserted) {
                adjustedQueue.add(newTrucker);
            }
        }

        // Prepare response queue
        List<QueueResponseDTO.QueueEntry> finalQueue = new ArrayList<>();
        int seq = 1;
        for (TruckRequestDTO truck : adjustedQueue) {
            finalQueue.add(new QueueResponseDTO.QueueEntry(truck.getId(), seq++));
        }

        return new QueueResponseDTO(gateId, finalQueue);
    }

}
