package com.reyhan.tpssanggau.Model;

import com.google.android.gms.maps.model.LatLng;

public class ListTerminalModel {
    private int graphId;
    private LatLng latLng;

    public ListTerminalModel(int graphId, LatLng latLng) {
        this.graphId = graphId;
        this.latLng = latLng;
    }

    public ListTerminalModel() {
    }

    public int getGraphId() {
        return graphId;
    }

    public void setGraphId(int graphId) {
        this.graphId = graphId;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
