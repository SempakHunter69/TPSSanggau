package com.reyhan.tpssanggau.Algorithm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.reyhan.tpssanggau.Model.DestinyModel;
import com.reyhan.tpssanggau.Model.GraphModel;
import com.reyhan.tpssanggau.Model.NodeModel;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private String TAG = "SQLHelperTAG";
    private static final String DATABASE_NAME = "db_djikstra";
    private static final int DATABASE_VERSION = 1;
    private Context myContext;

    public static final String TABLE_SAMPAH = "sampah";
    public static final String  id = "id";
    public static final String sampah = "sampah";
    public static final String koordinat = "koordinat";

    public static final String TABLE_graph = "graph";
    public static final String simpul_awal = "simpul_awal";
    public static final String simpul_simpul_tujuanl = "simpul_tujuan";
    public static final String jalur = "jalur";
    public static final String bobot = "bobot";

    public static final String angkutan_umum = "angkutan_umum";
    public static final String no_trayek = "no_trayek";
    public static final String simpul = "simpul";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        myContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" CREATE TABLE IF NOT EXISTS " + TABLE_SAMPAH + " (" +
                id + " INTEGER NOT NULL UNIQUE, " +
                sampah + " TEXT NOT NULL, " +
                koordinat + " TEXT NOT NULL);");

        db.execSQL(" CREATE TABLE IF NOT EXISTS " + TABLE_graph + " (" +
                id + " INTEGER NOT NULL UNIQUE, " +
                simpul_awal + " INTEGER NOT NULL, " +
                simpul_simpul_tujuanl + " INTEGER NOT NULL, " +
                jalur + " TEXT NOT NULL, " +
                bobot + " DOUBLE NOT NULL);");

        db.execSQL(" CREATE TABLE IF NOT EXISTS " + angkutan_umum + " (" +
                id + " INTEGER NOT NULL UNIQUE, " +
                no_trayek + " TEXT NOT NULL, " +
                simpul + " TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAMPAH);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_graph);
        db.execSQL("DROP TABLE IF EXISTS " + angkutan_umum);
        this.onCreate(db);

    }

    public void addAngkot(ArrayList<NodeModel> nodeModels){
        if (readAllData(angkutan_umum).getCount()>0){
            deleteAllData(angkutan_umum);
        }
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i <nodeModels.size() ; i++) {
            ContentValues cv = new ContentValues();
            cv.put(id,nodeModels.get(i).getId());
            cv.put(no_trayek,nodeModels.get(i).getNo_trayek());
            cv.put(simpul,nodeModels.get(i).getSimpul());
            long result = db.insert(angkutan_umum,null, cv);

            if(result == -1){
                Log.d(TAG, "addNode: succes");
                //Toast.makeText(myContext, "Failed to Save", Toast.LENGTH_SHORT).show();
            }else {
                Log.d(TAG, "addNode: failed");
                //	Toast.makeText(myContext, "Successfully to Save", Toast.LENGTH_SHORT).show();


            }
            cv.clear();
        }

    }

    public void addGrap(ArrayList<GraphModel> graphModels){
        if (readAllData(TABLE_graph).getCount()>0){
            deleteAllData(TABLE_graph);

        }
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i <graphModels.size() ; i++) {
            ContentValues cv = new ContentValues();
            cv.put(id,graphModels.get(i).getId());
            cv.put(simpul_awal,graphModels.get(i).getSimpul_awal());
            cv.put(simpul_simpul_tujuanl,graphModels.get(i).getSimpul_simpul_tujuanl());
            cv.put(jalur, graphModels.get(i).getJalur());
            cv.put(bobot,graphModels.get(i).getBobot());
            long result = db.insert(TABLE_graph,null, cv);

            if(result == -1){
                Log.d(TAG, "addGrap: succes");
                //Toast.makeText(myContext, "Failed to Save", Toast.LENGTH_SHORT).show();
            }else {
                Log.d(TAG, "addGrap: failed");
                //	Toast.makeText(myContext, "Successfully to Save", Toast.LENGTH_SHORT).show();


            }
            cv.clear();
        }

    }

    public void addSampah(ArrayList<DestinyModel> destinyModels){
        if (readAllData(TABLE_SAMPAH).getCount()>0){
            deleteAllData(TABLE_SAMPAH);
        }
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i <destinyModels.size() ; i++) {
            ContentValues cv = new ContentValues();
            cv.put(id,destinyModels.get(i).getId());
            cv.put(sampah,destinyModels.get(i).getSampah());
            cv.put(koordinat,destinyModels.get(i).getKoordinat());
            long result = db.insert(TABLE_SAMPAH,null, cv);

            if(result == -1){
                Log.d(TAG, "addSampah: succes");
                //Toast.makeText(myContext, "Failed to Save", Toast.LENGTH_SHORT).show();
            }else {
                Log.d(TAG, "addSampah: failed");
                //	Toast.makeText(myContext, "Successfully to Save", Toast.LENGTH_SHORT).show();
            }
            cv.clear();
        }
    }

    public void deleteAllData(String table){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+table);
    }
    public Cursor readAllData(String table){
        String query = "SELECT * FROM " + table;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

}
