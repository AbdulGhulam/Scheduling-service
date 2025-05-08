package com.airflow.scheduleservice.dto;

public class TruckRequestDTO {
    private String id;
    private String priority;
    private Integer requestOrder;
    private Integer currSeqNo;
    private Integer alreadyspendtimeParking;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getRequestOrder() {
        return requestOrder;
    }

    public void setRequestOrder(Integer requestOrder) {
        this.requestOrder = requestOrder;
    }

    public Integer getCurrSeqNo() {
        return currSeqNo;
    }

    public void setCurrSeqNo(Integer currSeqNo) {
        this.currSeqNo = currSeqNo;
    }

    public Integer getAlreadyspendtimeParking() {
        return alreadyspendtimeParking;
    }

    public void setAlreadyspendtimeParking(Integer alreadyspendtimeParking) {
        this.alreadyspendtimeParking = alreadyspendtimeParking;
    }
}
