package com.example.cw1_trafficapplication_s2534002.model;

public class Situation {

    // General Situation information
    private String id;
    private String type;
    private String description;
    private String status;


    // Location information
    private String road;
    private String direction;
    private Double latitude;
    private Double longitude;

    // Time information
    private String startTime;
    private String endTime;

    // Impact information
    private String impact;

    // Record specific information;
    private String roadManagementType;
    private String abnormalTrafficType;
    private String delaysType;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public String getRoadManagementType() {
        return roadManagementType;
    }

    public void setRoadManagementType(String roadManagementType) {
        this.roadManagementType = roadManagementType;
    }

    public String getAbnormalTrafficType() {
        return abnormalTrafficType;
    }

    public void setAbnormalTrafficType(String abnormalTrafficType) {
        this.abnormalTrafficType = abnormalTrafficType;
    }

    public String getDelaysType() {
        return delaysType;
    }

    public void setDelaysType(String delaysType) {
        this.delaysType = delaysType;
    }

    public String getSeverity()
    {
        if (roadManagementType == null)
        {
            return "UNKNOWN";
        }

        if (roadManagementType.equals("roadClosed"))
        {
            return "HIGH";
        }

        return "MODERATE";
    }

    public Situation() { }

}

