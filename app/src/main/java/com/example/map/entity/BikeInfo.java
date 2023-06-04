package com.example.map.entity;

public class BikeInfo {
    private int id;
    private String currentLocation;
    private boolean available;

    private boolean underRepair;

    public BikeInfo() {
    }

    public BikeInfo(int id, String currentLocation, boolean available, boolean underRepair) {
        this.id = id;
        this.currentLocation = currentLocation;
        this.available = available;
        this.underRepair = underRepair;
    }

    @Override
    public String toString() {
        return "BikeInfo{}";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isUnderRepair() {
        return underRepair;
    }

    public void setUnderRepair(boolean underRepair) {
        this.underRepair = underRepair;
    }
}
