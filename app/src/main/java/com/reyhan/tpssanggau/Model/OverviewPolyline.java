package com.reyhan.tpssanggau.Model;

import com.google.maps.android.PolyUtil;

import java.util.List;

public class OverviewPolyline {
    private String points;

    public OverviewPolyline(String points) {
        this.points = points;
    }

    public List getPoints(){
        return PolyUtil.decode(points);
    }
}
