package com.example.map.entity;

import androidx.annotation.NonNull;

public class BikeInfo {
    private int id;
    private BikePosition currentLocation;
    private boolean available;

    private boolean underRepair;

    public BikeInfo(int id, BikePosition currentLocation, boolean available, boolean underRepair) {
        this.id = id;
        this.currentLocation = currentLocation;
        this.available = available;
        this.underRepair = underRepair;
    }

    @NonNull
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

    public BikePosition getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(BikePosition currentLocation) {
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
