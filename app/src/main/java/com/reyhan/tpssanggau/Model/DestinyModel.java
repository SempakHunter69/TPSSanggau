package com.reyhan.tpssanggau.Model;

public class DestinyModel {
    private int id;
    private String sampah;
    private String koordinat;

    public DestinyModel() {
    }

    public DestinyModel(int id, String sampah, String koordinat) {
        this.id = id;
        this.sampah = sampah;
        this.koordinat = koordinat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSampah() {
        return sampah;
    }

    public void setSampah(String sampah) {
        this.sampah = sampah;
    }

    public String getKoordinat() {
        return koordinat;
    }

    public void setKoordinat(String koordinat) {
        this.koordinat = koordinat;
    }
}

