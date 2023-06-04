package com.example.map.entity;

import androidx.annotation.NonNull;

import com.amap.api.maps2d.model.LatLng;

public enum BikePosition {
    WEST_GATE("西门", new LatLng(39.95133, 116.336834)),
    SOUTH_GATE("南门", new LatLng(39.94922,116.341495)),
    EAST_GATE_1("东一门", new LatLng(39.952587,116.343882)),
    EAST_GATE_2("东二门", new LatLng(39.95253,116.347691)),
    STADIUM("体育场", new LatLng(39.952132,116.340084));

    private final String positionName;
    private final LatLng latLng;

    BikePosition(String positionName, LatLng latLng) {
        this.positionName = positionName;
        this.latLng = latLng;
    }

    public String getPositionName() { return positionName; }
    public LatLng getLatLng() { return latLng; }

    @NonNull
    @Override
    public String toString() { return getPositionName(); }
}
