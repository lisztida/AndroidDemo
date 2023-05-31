package com.example.map.entity;

import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;

public class Point {
    private LatLng latLng;
    private String title;
    private int quantity;
    private Marker marker;

    public Point(LatLng latLng, String title, int quantity) {
        this.latLng = latLng;
        this.title = title;
        this.quantity = quantity;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getTitle() {
        return title;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}

