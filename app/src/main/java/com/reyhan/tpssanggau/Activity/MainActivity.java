package com.reyhan.tpssanggau.Activity;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.reyhan.tpssanggau.Algorithm.DatabaseHelper;
import com.reyhan.tpssanggau.Algorithm.Dijkstra;
import com.reyhan.tpssanggau.Algorithm.GetKoordinatAwalAkhir;
import com.reyhan.tpssanggau.Algorithm.GraphToArray;
import com.reyhan.tpssanggau.Algorithm.MyDialogFragment;
import com.reyhan.tpssanggau.Algorithm.TambahSimpul;
import com.reyhan.tpssanggau.Model.DestinyModel;
import com.reyhan.tpssanggau.Model.GraphModel;
import com.reyhan.tpssanggau.Model.ListTerminalModel;
import com.reyhan.tpssanggau.Model.NodeModel;
import com.reyhan.tpssanggau.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private String TAG = "MainActivity";


    // DB
    DatabaseHelper dbHelper;
    Cursor cursor;

    // Google Maps
    GoogleMap googleMap;
    public String __global_endposition = null;
    public String __global_startposition = null;
    public int __global_simpul_awal;
    public int __global_simpul_akhir;
    public String __global_old_simpul_awal = "";
    public String __global_old_simpul_akhir = "";
    public int __global_maxRow0;
    public int __global_maxRow1;
    private String[][] __global_graphArray;
    private LatLng __global_yourCoordinate_exist = null;
    private MapFragment mapFragment;
    private DatabaseReference mDatabase;
    //
    int PERMISSION_ID = 44;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private final int UPDATE_INTERVAL = 1100;
    private final int FASTEST_INTERVAL = 500;
    private ArrayList<ListTerminalModel> listTerminalModels;
    private ArrayList<DestinyModel> listDestiny;
    private DatabaseReference mUsers;
    Marker marker;

    MaterialCardView popUpWidow;
    Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        popUpWidow = findViewById(R.id.popUpWidow);
        popUpWidow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialogFragment myDialogFragment = new MyDialogFragment();
                myDialogFragment.show(getSupportFragmentManager(), "MyFragment");
            }
        });
        mDatabase = FirebaseDatabase.getInstance().getReference();
        listTerminalModels = new ArrayList<>();
        // addData();
        // create DB
        dbHelper = new DatabaseHelper(this);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        getNode();
        getGraph();
        getDestiny();

        //untuk menambahkan marker list destiny/tujuan tps
        mUsers = FirebaseDatabase.getInstance().getReference("sampah");
        mUsers.push().setValue(marker);


    }

    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            //lakukan koneksi saat googleclint sudah di init
            googleApiClient.connect();


        }
    }


    //mendapatkan lokasi saat ini
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            Log.i("getLastKnownLocation", "getLastKnownLocation");
            if (lastLocation != null) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                __global_yourCoordinate_exist = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                //writeLastLocation();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(__global_yourCoordinate_exist)
                        .zoom(15)
                        .bearing(0)
                        .tilt(45)
                        .build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        } else {
            requestPermissions();
        }
    }

    //cek permission ijin mendapatkan lokasi
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation();
            }
        }
    }

    // Start location Updates
    private void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
//

        //mendeteksi wajib gps menyala
        if (!isGPSON()) {
            dialogEnableGPS();
        } else {
            if (checkPermission())
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        }
    }

    //menampilkan dialog enable gps
    int REQUEST_CHECK_SETTINGS_GPS = 1;

    private void dialogEnableGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi
                .requestLocationUpdates(googleApiClient, locationRequest, (LocationListener) this);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi
                        .checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied.
                    // You can initialize location requests here.
                    int permissionLocation = ContextCompat
                            .checkSelfPermission(MainActivity.this,
                                    Manifest.permission.ACCESS_FINE_LOCATION);
                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                        lastLocation = LocationServices.FusedLocationApi
                                .getLastLocation(googleApiClient);
                    }
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied.
                    // But could be fixed by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        // Ask to turn on GPS automatically
                        status.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS_GPS);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied.
                    // However, we have no way
                    // to fix the
                    // settings so we won't show the dialog.
                    // finish();
                    break;
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case Activity.RESULT_OK:
                if (requestCode == REQUEST_CHECK_SETTINGS_GPS) {
                    getLastKnownLocation();
                    //Toast.makeText(this, "okay On", Toast.LENGTH_SHORT).show();
                }
                break;
            case Activity.RESULT_CANCELED:
                finish();
                break;
        }
    }


    private void getDestiny() {
        mDatabase.child("sampah")
                .addValueEventListener(new ValueEventListener() {
                    ArrayList<DestinyModel> destinyModels = new ArrayList<>();

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            DestinyModel destinyModel = dataSnapshot.getValue(DestinyModel.class);

                            destinyModels.add(destinyModel);
                        }
                        dbHelper.addSampah(destinyModels);
                        // BUAT MAP
                        //if(mapFragment == null){
                        mapFragment.getMapAsync(MainActivity.this);
                        createGoogleApi();
                        //  }

                        setUpSpinner();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getNode() {
        mDatabase.child("list_node")
                .addValueEventListener(new ValueEventListener() {
                    ArrayList<NodeModel> nodeModels = new ArrayList<>();

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            NodeModel nodeModel = dataSnapshot.getValue(NodeModel.class);
                            nodeModels.add(nodeModel);
                        }
                        dbHelper.addAngkot(nodeModels);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getGraph() {
        mDatabase.child("graph")
                .addValueEventListener(new ValueEventListener() {
                    ArrayList<GraphModel> graphModels = new ArrayList<>();

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            GraphModel graphModel = dataSnapshot.getValue(GraphModel.class);
                            graphModels.add(graphModel);
                        }

                        dbHelper.addGrap(graphModels);
                        setTerminal();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setTerminal() {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor csr = db.rawQuery("SELECT * FROM graph", null);
        while (csr.moveToNext()) {
            String json = csr.getString(3).toString();
            JSONObject jObject = null;
            LatLng simpulAngkot = null;
            try {
                jObject = new JSONObject(json);
                JSONArray jArrCoordinates = jObject.getJSONArray("coordinates");
                JSONArray latlngs = jArrCoordinates.getJSONArray(0);
                Double lats = latlngs.getDouble(0);
                Double lngs = latlngs.getDouble(1);
                simpulAngkot = new LatLng(lats, lngs);
                Log.d(TAG, "setTerminal: lat " + simpulAngkot.latitude + " lng" + simpulAngkot.longitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (listTerminalModels.size() == 0) {
                listTerminalModels.add(new ListTerminalModel(Integer.valueOf(csr.getString(1)), simpulAngkot));
            } else {
                if (!isAvaiTerminal(simpulAngkot)) {
                    listTerminalModels.add(new ListTerminalModel(Integer.valueOf(csr.getString(1)), simpulAngkot));
                }
            }
        }

    }

    private boolean isAvaiTerminal(LatLng simpulAngkot) {
        for (int i = 0; i < listTerminalModels.size(); i++) {
            if (listTerminalModels.get(i).getLatLng().latitude == simpulAngkot.latitude
                    && listTerminalModels.get(i).getLatLng().longitude == simpulAngkot.longitude) {
                return true;
            }

        }
        return false;
    }


    private void setUpSpinner() {

        // Query DB to show all sampah
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM sampah", null);
        cursor.moveToFirst();

        // tampung nama sampah
        ArrayList<String> spinner_list_sampah = new ArrayList<String>();
        // Adapter spinner sampah


        // nama-nama sampah dimasukkan ke array
        spinner_list_sampah.add("-- Pilih TPS --");

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            spinner_list_sampah.add(cursor.getString(1).toString());
        }

        // masukkan list Sampah ke spinner (dropdown)
        Spinner spinner = (Spinner) findViewById(R.id.spinner_list_sampah);
        ArrayAdapter<String> adapter_spinner_sampah = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinner_list_sampah);

        adapter_spinner_sampah.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter_spinner_sampah);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                // TODO Auto-generated method stub
//                ProgressDialog pd = new ProgressDialog(MainActivity.this);
                if (arg0.getItemAtPosition(arg2).toString() != "-- Pilih TPS --") {
                    String tujuan = arg0.getItemAtPosition(arg2).toString();
                    Toast.makeText(arg0.getContext(), "Harap tunggu sebentar aplikasi sedang mencari rute terbaik ke" + tujuan, Toast.LENGTH_LONG).show();
                    cursor = db.rawQuery("SELECT koordinat FROM sampah where sampah = '" + tujuan + "'", null);
//                    pd.setMessage("loading");
//                    pd.show();
                    cursor.moveToFirst();
                    cursor.moveToPosition(0);

                    // get coordinate Sampah from field koordinat
                    __global_endposition = cursor.getString(0).toString();

                    // user men-tap peta
                    if (__global_yourCoordinate_exist != null) {

                        // your coordinate
                        double latUser = __global_yourCoordinate_exist.latitude;
                        double lngUser = __global_yourCoordinate_exist.longitude;

                        // destination coordinate Sampah
                        String[] exp_endCoordinate = __global_endposition.split(",");
                        double lat_endposition = Double.parseDouble(exp_endCoordinate[0]);
                        double lng_endposition = Double.parseDouble(exp_endCoordinate[1]);


                        // ========================================================================
                        // CORE SCRIPT
                        // fungsi cari simpul awal dan tujuan, buat graph sampai algoritma dijkstra
                        // ========================================================================
                        try {
                            startingScript(latUser, lngUser, lat_endposition, lng_endposition);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
//                        pd.dismiss();

                    } else {
                        Toast.makeText(getApplicationContext(), "Tap pada peta untuk menentukan posisi Anda", Toast.LENGTH_LONG).show();
                    }

                }// if -- pilih Sampah --
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });//setOnItemSelectedListener
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        // event map
        this.googleMap.setOnMapClickListener(this);
        this.googleMap.setOnMapLongClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);

//        for (int i = 0; i <listTerminalModels.size() ; i++) {
//            Log.d(TAG, "setTerminal: "+listTerminalModels.get(i).getLatLng());
//            googleMap.addMarker(new MarkerOptions()
//                    .position(listTerminalModels.get(i).getLatLng())
//                    .title(String.valueOf(listTerminalModels.get(i).getGraphId()))
//                    .snippet(String.valueOf(listTerminalModels.get(i).getLatLng()))
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
//        }

//        for (int i = 0; i <listDestiny.size() ; i++) {
//            Log.d(TAG, "setTerminal: "+listDestiny.get(i).getKoordinat());
//            String[] koordinat = listDestiny.get(i).getKoordinat().split(",");
//            double lat_endposition = Double.parseDouble(koordinat[0]);
//            double lng_endposition = Double.parseDouble(koordinat[1]);
//            googleMap.addMarker(new MarkerOptions()
//                    .position(new LatLng(lat_endposition,lng_endposition))
//                    .title(String.valueOf(listDestiny.get(i).getSampah()))
//                    .snippet(String.valueOf(listDestiny.get(i).getId()))
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
//        }
        //setting the size of marker in map by using Bitmap Class
        mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot s : dataSnapshot.getChildren()){
                    DestinyModel user = s.getValue(DestinyModel.class);
                    String[] gpsVal = user.getKoordinat().split(",");
                    double lat = Double.parseDouble(gpsVal[0]);
                    double lon = Double.parseDouble(gpsVal[1]);
                    LatLng location=new LatLng(lat,lon);
                    googleMap.addMarker(new MarkerOptions().position(location).title(user.getSampah()))
                            .setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





    }

    @Override
    public void onMapClick(LatLng arg0) {
// TODO Auto-generated method stub

        // your coordinate position
        double latUser = arg0.latitude;
        double lngUser = arg0.longitude;

        __global_yourCoordinate_exist = arg0;

        // destination coordinate position
        String endposition = __global_endposition;

        if(endposition != null){

            // pecah coordinate sampah
            String[] exp_endposition = endposition.split(",");
            double lat_endposition = Double.parseDouble(exp_endposition[0]);
            double lng_endposition = Double.parseDouble(exp_endposition[1]);

            // ========================================================================
            // CORE SCRIPT
            // fungsi cari simpul awal dan tujuan, buat graph sampai algoritma dijkstra
            // ========================================================================
            try {
                startingScript(latUser, lngUser, lat_endposition, lng_endposition);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }else{
            Toast.makeText(getApplicationContext(), "pilih lokasi TPS dulu", Toast.LENGTH_LONG).show();
        }
    }

    public void startingScript(double latUser, double lngUser, double lat_endposition, double lng_endposition) throws JSONException{

        // delete temporary record DB
        deleteTemporaryRecord();

        // reset google map
        googleMap.clear();

        // convert graph from DB to Array; graph[][]
        GraphToArray DBGraph = new GraphToArray();
        __global_graphArray = DBGraph.convertToArray(this); // return graph[][] Array

        // get max++ row temporary DB
        maxRowDB();

        // GET COORDINATE AWAL DI SEKITAR SIMPUL
        // coordinate awal lalu di konversi ke simpul awal
        // return __global_simpul_awal, __global_graphArray[][]
        // ==========================================
        GetKoordinatAwalAkhir start_coordinate_jalur = new GetKoordinatAwalAkhir();
        getSimpulAwalAkhirJalur(start_coordinate_jalur, latUser, lngUser, "awal");

        // GET COORDINATE AKHIR DI SEKITAR SIMPUL
        // coordinate akhir lalu di konversi ke simpul akhir
        // return __global_simpul_akhir, __global_graphArray[][]
        // ==========================================
        GetKoordinatAwalAkhir destination_coordinate_jalur = new GetKoordinatAwalAkhir();
        getSimpulAwalAkhirJalur(destination_coordinate_jalur, lat_endposition, lng_endposition, "akhir");

        // ALGORITMA DIJKSTRA
        // ==========================================
        Dijkstra algo = new Dijkstra();
        //TODO
        Log.i(TAG, "startingScript: "+__global_simpul_awal + " "+__global_simpul_akhir);
        algo.jalurTerpendek(__global_graphArray, __global_simpul_awal, __global_simpul_akhir);

        // no result for algoritma dijkstra
        if(algo.status == "die"){

            Toast.makeText(getApplicationContext(), "Lokasi Anda sudah dekat dengan lokasi tujuan", Toast.LENGTH_LONG).show();

        }else{
            // return jalur terpendek; example 1->5->6->7
            String[] exp = algo.jalur_terpendek1.split("->");

            // DRAW JALUR UMUM
            // =========================================
            drawJalur(algo.jalur_terpendek1, exp);
        }

    }

    private int naikKe =1;
    public void drawJalur(String alg, String[] exp) throws JSONException{
        naikKe=1;
        int start = 0;

        // GAMBAR JALURNYA
        // ======================
        dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        for(int i = 0; i < exp.length-1; i++){

            ArrayList<LatLng> lat_lng = new ArrayList<LatLng>();

            cursor = db.rawQuery("SELECT jalur FROM graph where simpul_awal ="+exp[start]+" and simpul_tujuan ="+exp[(++start)], null);
            cursor.moveToFirst();

            // dapatkan koordinat Lat,Lng dari field koordinat (3)
            String json = cursor.getString(0).toString();
            // get JSON
            JSONObject jObject = new JSONObject(json);
            JSONArray jArrCoordinates = jObject.getJSONArray("coordinates");

            // get coordinate JSON
            for(int w = 0; w < jArrCoordinates.length(); w++){

                JSONArray latlngs = jArrCoordinates.getJSONArray(w);
                Double lats = latlngs.getDouble(0);
                Double lngs = latlngs.getDouble(1);

                LatLng latLng = new LatLng(latlngs.getDouble(0) ,latlngs.getDouble(1) );
                if (checkTerminal(latLng)){
                    googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Urutan "+String.valueOf(naikKe++))
                            .icon(bitmapDescriptorFromVector(this, R.drawable.ic_baseline_directions_run_24)) );
                }

                lat_lng.add( new LatLng(lats, lngs) );

            }

            // buat rute
            PolylineOptions jalurBiasa = new PolylineOptions();
            jalurBiasa.addAll(lat_lng).width(5).color(0xff4b9efa).geodesic(true);
            googleMap.addPolyline(jalurBiasa);

        }


        // BUAT MARKER UNTUK YOUR POSITION AND DESTINATION POSITION
        // ======================
        // your position
        googleMap.addMarker(new MarkerOptions()
                .position(__global_yourCoordinate_exist)
                .title("Your position")
                .snippet("Your position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        String[] exp_endCoordinate = __global_endposition.split(",");
        double lat_endPosition = Double.parseDouble(exp_endCoordinate[0]);
        double lng_endPosition = Double.parseDouble(exp_endCoordinate[1]);
        LatLng endx = new LatLng(lat_endPosition, lng_endPosition);

        // destination position
        googleMap.addMarker(new MarkerOptions()
                .position(endx)
                .title("Destination position")
                .snippet("Destination position")
                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_baseline_delete_24)));


        // TENTUKAN YANG MELEWATI JALUR TERSEBUT
        // ==========================================================
        // misal exp[] = 1->5->6->7
        int m = 0;


        String[] awal = __global_old_simpul_awal.split("-"); // misal 4-5
        String[] akhir = __global_old_simpul_akhir.split("-"); // misal 8-7

        int ganti_a = 0;
        int ganti_b = 0;
        int simpulAwalDijkstra = 0;
        if (!exp[0].equals("")){
            simpulAwalDijkstra = Integer.parseInt(exp[0]);
        }


        String gabungSimpul_all = "";
        Map<String, ArrayList> listAngkutanUmum = new HashMap<String, ArrayList>();
        ArrayList<Integer> listSimpulAngkot = new ArrayList<Integer>();

        // cari simpul_old sebelum koordinat dipecah
        // misal 4-5 dipecah menjadi 4-6-5, berarti simpul_old awal = 5, simpul_old akhir = 4
        for(int e = 0; e < (exp.length - 1); e++){

            if(e == 0){ // awal

                // dijalankan jika hasil algo hanya 2 simpul, example : 4->5
                if(exp.length == 2 /* 2 simpul (4-5)*/){

                    // ada simpul baru di awal (10) dan di akhir (11), example 10->11
                    if( exp[0].equals(String.valueOf(__global_maxRow0)) && exp[1].equals(String.valueOf(__global_maxRow1)) ){

                        if(String.valueOf(__global_maxRow0).equals(akhir[0])){
                            ganti_b = Integer.parseInt(akhir[1]);
                        }else{
                            ganti_b = Integer.parseInt(akhir[0]);
                        }

                        if(String.valueOf(ganti_b).equals(awal[0])){
                            ganti_a = Integer.parseInt(awal[1]);
                        }else{
                            ganti_a = Integer.parseInt(awal[0]);
                        }
                    }
                    else{
                        // ada simpul baru di awal (10), example 10->5
                        // maka cari simpul awal yg oldnya
                        if( exp[0].equals(String.valueOf(__global_maxRow0)) ){

                            if(exp[1].equals(awal[1])){
                                ganti_a = Integer.parseInt(awal[0]);
                            }else{
                                ganti_a = Integer.parseInt(awal[1]);
                            }
                            ganti_b = Integer.parseInt(exp[1]);
                        }
                        // ada simpul baru di akhir (10), example 5->10
                        // maka cari simpul akhir yg oldnya
                        else if( exp[1].equals(String.valueOf(__global_maxRow0)) ){

                            if(exp[0].equals(akhir[0])){
                                ganti_b = Integer.parseInt(akhir[1]);
                            }else{
                                ganti_b = Integer.parseInt(akhir[0]);
                            }
                            ganti_a = Integer.parseInt(exp[0]);
                        }
                        // tidak ada penambahan simpul sama sekali
                        else{
                            ganti_a = Integer.parseInt(exp[0]);
                            ganti_b = Integer.parseInt(exp[1]);
                        }
                    }

        			/*
        			// 4 == 4
        			if(exp[0].equals(awal[0])){
            			ganti_a = Integer.parseInt(awal[0]);
            			//ganti_b = Integer.parseInt(awal[1]);
        			}else{
            			ganti_a = Integer.parseInt(awal[1]);
            			//ganti_b = Integer.parseInt(awal[0]);
        			}

        			if(String.valueOf(ganti_a).equals(akhir[0])){
            			ganti_b = Integer.parseInt(akhir[1]);
            			//ganti_b = Integer.parseInt(awal[1]);
        			}else{
            			ganti_b = Integer.parseInt(akhir[0]);
            			//ganti_b = Integer.parseInt(awal[0]);
        			}
        			*/

        			/*
        			 *         			// 4 == 4
        			if(exp[0].equals(awal[0])){
            			ganti_a = Integer.parseInt(akhir[0]);
            			ganti_b = Integer.parseInt(awal[1]);
        			}else{
            			ganti_a = Integer.parseInt(awal[1]);
            			ganti_b = Integer.parseInt(akhir[0]);
        			}
        			 */

                }
                // hasil algo lebih dr 2 : 4->5->8->7-> etc ..
                else{
                    if(exp[1].equals(awal[1])){ // 5 == 5
                        ganti_a = Integer.parseInt(awal[0]); // hasil 4
                    }else{
                        ganti_a = Integer.parseInt(awal[1]); // hasil 5
                    }

                    ganti_b = Integer.parseInt( exp[++m] );
                }
            }
            else if(e == (exp.length - 2)){ // akhir

                if(exp[ (exp.length - 2) ].equals(akhir[1])){ // 7 == 7
                    ganti_b = Integer.parseInt(akhir[0]); // hasil 8
                }else{
                    ganti_b = Integer.parseInt(akhir[1]); // hasil 7
                }

                ganti_a = Integer.parseInt( exp[m] );

            }else{ // tengah tengah
                ganti_a = Integer.parseInt( exp[m] );
                ganti_b = Integer.parseInt( exp[++m] );
            }

            gabungSimpul_all += "," + ganti_a + "-" + ganti_b + ","; // ,1-5,
            String gabungSimpul = "," + ganti_a + "-" + ganti_b + ","; // ,1-5,

            cursor = db.rawQuery("SELECT * FROM angkutan_umum where simpul like '%" + gabungSimpul + "%'", null);
            cursor.moveToFirst();

            ArrayList<String> listAngkutan = new ArrayList<String>();

            for(int ae = 0; ae < cursor.getCount(); ae++){
                cursor.moveToPosition(ae);
                listAngkutan.add( cursor.getString(1).toString() );
            }

            listAngkutanUmum.put("angkutan" + e, listAngkutan);

            // add simpul angkot
            listSimpulAngkot.add( Integer.parseInt(exp[e]) );

        }


        String replace_jalur = gabungSimpul_all.replace(",,", ","); //  ,1-5,,5-6,,6-7, => ,1-5,5-6,6-7,
        cursor = db.rawQuery("SELECT * FROM angkutan_umum where simpul like '%" + replace_jalur + "%'", null);
        cursor.moveToFirst();
        cursor.moveToPosition(0);

        // ada 1 rute yg melewati jalur dari awal sampek akhir
        if(cursor.getCount() > 0){

            String siAngkot = cursor.getString(1).toString();

            // get coordinate
            cursor = db.rawQuery("SELECT jalur FROM graph where simpul_awal = '" + simpulAwalDijkstra + "'", null);
            cursor.moveToFirst();
            String json_coordinate = cursor.getString(0).toString();

            // manipulating JSON
            JSONObject jObject = new JSONObject(json_coordinate);
            JSONArray jArrCoordinates = jObject.getJSONArray("coordinates");
            JSONArray latlngs = jArrCoordinates.getJSONArray(0);

            // first latlng
            Double lats = latlngs.getDouble(0);
            Double lngs = latlngs.getDouble(1);

            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lats, lngs))
                    .title("")
                    .snippet(siAngkot)
                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_baseline_directions_run_24)) );

            // die()
            return;
        }

        // ada 2 atau lebih node yg melewati jalur dari awal sampek akhir
        int banyakAngkot = 0;
        int indexUrut = 0;
        int indexSimpulAngkot = 1;
        int lengthAngkutan = listAngkutanUmum.size();
        Map<String, ArrayList> angkotFix = new HashMap<String, ArrayList>();

        for(int en = 0; en < lengthAngkutan; en++ ){

            // temporary sementara sebelum di retainAll()
            ArrayList<String> temps = new ArrayList<String>();
            for(int u = 0; u < listAngkutanUmum.get("angkutan0").size(); u++){
                temps.add( listAngkutanUmum.get("angkutan0").get(u).toString() );
            }

            if(en > 0 ){
                ArrayList listSekarang1 = listAngkutanUmum.get("angkutan0");
                ArrayList listSelanjutnya1 = listAngkutanUmum.get("angkutan" + en);

                // intersection
                listSekarang1.retainAll(listSelanjutnya1);

                if(listSekarang1.size() > 0){

                    listSimpulAngkot.remove(indexSimpulAngkot);
                    --indexSimpulAngkot;

                    listAngkutanUmum.remove("angkutan" + en);

                    if(en == (lengthAngkutan - 1)){

                        ArrayList<String> tempDalam = new ArrayList<String>();
                        for(int es = 0; es < listSekarang1.size(); es++){
                            tempDalam.add( listSekarang1.get(es).toString() );
                        }

                        angkotFix.put("angkutanFix" + indexUrut, tempDalam);
                        ++indexUrut;
                    }
                }
                else if(listSekarang1.size() == 0){

                    angkotFix.put("angkutanFix" + indexUrut, temps);

                    ArrayList<String> tempDalam = new ArrayList<String>();
                    for(int es = 0; es < listSelanjutnya1.size(); es++){
                        tempDalam.add( listSelanjutnya1.get(es).toString() );
                    }

                    //if(en == 1) break;
                    listAngkutanUmum.get("angkutan0").clear();
                    listAngkutanUmum.put("angkutan0", tempDalam);

                    //if(en != (listAngkutanUmum.size() - 1)){
                    listAngkutanUmum.remove("angkutan" + en);
                    //}

                    ++indexUrut;

                    if(en == (lengthAngkutan - 1)){

                        ArrayList<String> tempDalam2 = new ArrayList<String>();
                        for(int es = 0; es < listSelanjutnya1.size(); es++){
                            tempDalam2.add( listSelanjutnya1.get(es).toString() );
                        }

                        angkotFix.put("angkutanFix" + indexUrut, tempDalam2);
                        ++indexUrut;
                    }
                }

                ++indexSimpulAngkot;
            }
        }

        for(int r = 0; r < listSimpulAngkot.size(); r++){
            String simpulx = listSimpulAngkot.get(r).toString();
            Log.d(TAG, "simpulx: "+simpulx);
            // get coordinate simpulAngkutan
            cursor = db.rawQuery("SELECT jalur FROM graph where simpul_awal = '" + simpulx + "'", null);
            cursor.moveToPosition(0);

            // dapatkan koordinat Lat,Lng dari field koordinat (3)
            String json = cursor.getString(0).toString();
            //TODO
            // get JSON
            JSONObject jObject = new JSONObject(json);
            JSONArray jArrCoordinates = jObject.getJSONArray("coordinates");

            // get first coordinate JSON
            for (int i = 0; i <jArrCoordinates.length() ; i++) {
                JSONArray latlngs = jArrCoordinates.getJSONArray(i);

//                if (checkTerminal(latLng)){
//                    if (String.valueOf(latLng.latitude).startsWith("-6.1")) {
//                        Log.d(TAG, "drawJalur: adadaa" +latLng.latitude+","+latLng.longitude);
//                    }
//                    googleMap.addMarker(new MarkerOptions()
//                            .position(latLng)
//                            .title("Angkot "+simpulx)
//                            .snippet("www")
//                            . icon(bitmapDescriptorFromVector(MainActivity.this, R.drawable.car)));
//                }
                Log.d(TAG, "drawJalurloop: lat "+latlngs.getDouble(0) +" lng "+latlngs.getDouble(1));
            }
            JSONArray latlngs = jArrCoordinates.getJSONArray(0);

            Double lats = latlngs.getDouble(0);
            Double lngs = latlngs.getDouble(1);

            Log.d(TAG, "drawJalurww: "+lats+" " +lngs);
            LatLng simpulAngkot = new LatLng(lats, lngs);
            String siAngkot = angkotFix.get("angkutanFix" + r).toString();
//            if(r == 0){
//                googleMap.addMarker(new MarkerOptions()
//                        .position(simpulAngkot)
//                        .title("Angkot "+simpulx)
//                        .snippet(siAngkot)
//                        . icon(bitmapDescriptorFromVector(MainActivity.this, R.drawable.car)));
//            }else{
//                googleMap.addMarker(new MarkerOptions()
//                        .position(simpulAngkot)
//                        .title("Angkot "+simpulx)
//                        .snippet(siAngkot)
//                        . icon(bitmapDescriptorFromVector(MainActivity.this, R.drawable.car)));
//            }
        }

    }

    float distanceInMeters =-1;
    int pos; //fix position array paling dekat
    private void checkLocation(double lat, double lng) {

    }

    private boolean checkTerminal(LatLng latLng) {
        for (int i = 0; i < listTerminalModels.size(); i++) {
            if (listTerminalModels.get(i).getLatLng().latitude == latLng.latitude &&
                    listTerminalModels.get(i).getLatLng().longitude == latLng.longitude){
                return true;
            }
        }
        return false;
    }

    public void getSimpulAwalAkhirJalur(GetKoordinatAwalAkhir objects, double latx, double lngx, String statusObject) throws JSONException{

        // return JSON index posisi koordinat, nodes0, nodes1
        JSONObject jStart = objects.Get_simpul(latx, lngx, this);

        // index JSON
        String status = jStart.getString("status");
        int node_simpul_awal0 = jStart.getInt("node_simpul_awal0");
        int node_simpul_awal1 = jStart.getInt("node_simpul_awal1");
        int index_coordinate_json = jStart.getInt("index_coordinate_json");


        int fix_simpul_awal = 0;

        // jika koordinat tepat di atas posisi simpul/node
        // maka tidak perlu menambahkan simpul baru
        if(status.equals("jalur_none")){

            //tentukan simpul awal atau akhir yg dekat dgn posisi user
            if(index_coordinate_json == 0){ // awal
                fix_simpul_awal = node_simpul_awal0;
            }else{ // akhir
                fix_simpul_awal = node_simpul_awal1;
            }

            if(statusObject == "awal"){

                // return
                __global_old_simpul_awal = node_simpul_awal0 + "-" + node_simpul_awal1;
                __global_simpul_awal = fix_simpul_awal; // misal 0
            }else{

                // return
                __global_old_simpul_akhir = node_simpul_awal0 + "-" + node_simpul_awal1;
                __global_simpul_akhir = fix_simpul_awal; // misal 0
            }


        }
        // jika koordinat berada diantara simpul 5 dan simpul 4 atau simpul 4 dan simpul 5
        // maka perlu menambahkan simpul baru
        else if(status.equals("jalur_double")){

            // return
            if(statusObject == "awal"){

                // cari simpul (5,4) dan (4-5) di Tambah_simpul.java
                TambahSimpul obj_tambah = new TambahSimpul();
                obj_tambah.dobelSimpul(node_simpul_awal0, node_simpul_awal1, index_coordinate_json,
                        this, __global_graphArray, 401
                ); // 401 : row id yg baru


                // return
                __global_old_simpul_awal = obj_tambah.simpul_lama;
                __global_simpul_awal = obj_tambah.simpul_baru; // misal 6
                __global_graphArray = obj_tambah.modif_graph; // graph[][]

            }else{

                // cari simpul (5,4) dan (4-5) di Tambah_simpul.java
                TambahSimpul obj_tambah = new TambahSimpul();
                obj_tambah.dobelSimpul(node_simpul_awal0, node_simpul_awal1, index_coordinate_json,
                        this, __global_graphArray, 501
                ); // 501 : row id yg baru


                // return
                __global_old_simpul_akhir = obj_tambah.simpul_lama;
                __global_simpul_akhir = obj_tambah.simpul_baru; // misal 4
                __global_graphArray = obj_tambah.modif_graph; // graph[][]

            }

        }
        // jika koordinat hanya berada diantara simpul 5 dan simpul 4
        // maka perlu menambahkan simpul baru
        else if(status.equals("jalur_single")){

            if(statusObject == "awal"){

                // cari simpul (5,4) di Tambah_simpul.java
                TambahSimpul obj_tambah1 = new TambahSimpul();
                obj_tambah1.singleSimpul(node_simpul_awal0, node_simpul_awal1, index_coordinate_json,
                        this, __global_graphArray, 401
                ); // 401 : row id yg baru


                // return
                __global_old_simpul_awal = obj_tambah1.simpul_lama;
                __global_simpul_awal = obj_tambah1.simpul_baru; // misal 6
                __global_graphArray = obj_tambah1.modif_graph; // graph[][]

            }else{

                // cari simpul (5,4) di Tambah_simpul.java
                TambahSimpul obj_tambah1 = new TambahSimpul();
                obj_tambah1.singleSimpul(node_simpul_awal0, node_simpul_awal1, index_coordinate_json,
                        this, __global_graphArray, 501
                ); // 501 : row id yg baru


                // return
                __global_old_simpul_akhir = obj_tambah1.simpul_lama;
                __global_simpul_akhir = obj_tambah1.simpul_baru; // misal 4
                __global_graphArray = obj_tambah1.modif_graph; // graph[][]
            }
        }
    }


    /*
     * @fungsi
     *  delete temporary record DB
     *  (temporary ini digunakan untuk menampung sementara simpul baru)
     * @parameter
     *  no parameter
     * @return
     *  no returen
     */
    public void deleteTemporaryRecord(){

        // delete DB
        final SQLiteDatabase dbDelete = dbHelper.getWritableDatabase();

        // delete temporary record DB
        for(int i = 0; i < 4; i++){
            //hapus simpul awal tambahan, mulai dr id 401,402,403,404
            String deleteQuery_ = "DELETE FROM graph where id ='"+ (401+i) +"'";
            dbDelete.execSQL(deleteQuery_);

            //hapus simpul tujuan tambahan, mulai dr id 501,502,503,504
            String deleteQuery = "DELETE FROM graph where id ='"+ (501+i) +"'";
            dbDelete.execSQL(deleteQuery);
        }
    }

    public void maxRowDB(){

        dbHelper = new DatabaseHelper(this);
        SQLiteDatabase dbRead = dbHelper.getReadableDatabase();

        cursor = dbRead.rawQuery("SELECT max(simpul_awal), max(simpul_tujuan) FROM graph", null);
        cursor.moveToFirst();
        int max_simpul_db		= 0;
        int max_simpulAwal_db 	= Integer.parseInt(cursor.getString(0).toString());
        int max_simpulTujuan_db = Integer.parseInt(cursor.getString(1).toString());

        if(max_simpulAwal_db >= max_simpulTujuan_db){
            max_simpul_db = max_simpulAwal_db;
        }else{
            max_simpul_db = max_simpulTujuan_db;
        }

        // return
        __global_maxRow0 = (max_simpul_db+1);
        __global_maxRow1 = (max_simpul_db+2);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    //menandai marker dengan bitmap/drawable
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, vectorDrawableResourceId);//bisa ganti dengan drawable
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }





    //cek gps menyala atau tidak
    private boolean isGPSON (){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLastKnownLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }
}