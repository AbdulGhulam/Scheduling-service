package com.airflow.scheduleservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Warehouse {
    @JsonProperty("warehouse_id")
    public String id;
    @JsonProperty("warehouse_location")
    public Location location;
    @JsonProperty("name")
    public String name;

    public String getId()              { return id; }
    public void setId(String id)       { this.id = id; }

    public Location getLocation()      { return location; }
    public void setLocation(Location l){ this.location = l; }

    public String getName()            { return name; }
    public void setName(String n)      { this.name = n; }
}
