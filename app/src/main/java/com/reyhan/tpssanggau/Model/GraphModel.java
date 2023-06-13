package com.reyhan.tpssanggau.Model;

public class GraphModel {
    int id,simpul_awal,simpul_simpul_tujuanl ;
    String jalur;
    double bobot;

    public GraphModel(int id, int simpul_awal, int simpul_simpul_tujuanl, String jalur, double bobot) {
        this.id = id;
        this.simpul_awal = simpul_awal;
        this.simpul_simpul_tujuanl = simpul_simpul_tujuanl;
        this.jalur = jalur;
        this.bobot = bobot;
    }

    public GraphModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSimpul_awal() {
        return simpul_awal;
    }

    public void setSimpul_awal(int simpul_awal) {
        this.simpul_awal = simpul_awal;
    }

    public int getSimpul_simpul_tujuanl() {
        return simpul_simpul_tujuanl;
    }

    public void setSimpul_simpul_tujuanl(int simpul_simpul_tujuanl) {
        this.simpul_simpul_tujuanl = simpul_simpul_tujuanl;
    }

    public String getJalur() {
        return jalur;
    }

    public void setJalur(String jalur) {
        this.jalur = jalur;
    }

    public double getBobot() {
        return bobot;
    }

    public void setBobot(double bobot) {
        this.bobot = bobot;
    }
}

