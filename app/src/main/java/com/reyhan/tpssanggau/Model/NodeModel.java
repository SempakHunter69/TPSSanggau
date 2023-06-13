package com.reyhan.tpssanggau.Model;

public class NodeModel {
    int id;
    String no_trayek,simpul;

    public NodeModel(int id, String no_trayek, String simpul) {
        this.id = id;
        this.no_trayek = no_trayek;
        this.simpul = simpul;
    }

    public NodeModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNo_trayek() {
        return no_trayek;
    }

    public void setNo_trayek(String no_trayek) {
        this.no_trayek = no_trayek;
    }

    public String getSimpul() {
        return simpul;
    }

    public void setSimpul(String simpul) {
        this.simpul = simpul;
    }
}
