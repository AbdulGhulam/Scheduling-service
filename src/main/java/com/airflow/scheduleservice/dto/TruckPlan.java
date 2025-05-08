package com.airflow.scheduleservice.dto;

import java.util.List;

public record TruckPlan(String truckId,
                        List<AssignmentPlan> assignments) {}
