package com.airflow.scheduleservice.Controller;


import com.airflow.scheduleservice.dto.QueueResponseDTO;
import com.airflow.scheduleservice.dto.TruckRequestDTO;
import com.airflow.scheduleservice.service.TruckQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/queue")
public class GateQueueController {
    private final TruckQueueService truckQueueService;

    public GateQueueController(TruckQueueService truckQueueService) {
        this.truckQueueService = truckQueueService;
    }

    @PostMapping("/{gateId}/generate")
    public ResponseEntity<QueueResponseDTO> generateQueue(
            @PathVariable String gateId,
            @RequestBody Map<String, List<TruckRequestDTO>> request) {

        List<TruckRequestDTO> truckers = request.get("truckers");
        QueueResponseDTO response = truckQueueService.generateTruckQueue(gateId, truckers);
        return ResponseEntity.ok(response);
    }
}
