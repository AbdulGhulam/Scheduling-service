package com.airflow.scheduleservice.dto;

import java.util.List;

public class QueueResponseDTO {
    private String gateId;
    private List<QueueEntry> queue;

    public QueueResponseDTO(String gateId, List<QueueEntry> queue) {
        this.gateId = gateId;
        this.queue = queue;
    }

    public String getGateId() {
        return gateId;
    }

    public void setGateId(String gateId) {
        this.gateId = gateId;
    }

    public List<QueueEntry> getQueue() {
        return queue;
    }

    public void setQueue(List<QueueEntry> queue) {
        this.queue = queue;
    }

    public static class QueueEntry {
        private String truckerId;
        private Integer queueNumber;

        public QueueEntry(String truckerId, Integer queueNumber) {
            this.truckerId = truckerId;
            this.queueNumber = queueNumber;
        }

        public String getTruckerId() {
            return truckerId;
        }

        public void setTruckerId(String truckerId) {
            this.truckerId = truckerId;
        }

        public Integer getQueueNumber() {
            return queueNumber;
        }

        public void setQueueNumber(Integer queueNumber) {
            this.queueNumber = queueNumber;
        }
    }

}
