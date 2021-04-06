// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.presentgeo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.presentgeo.model.JsonMarker;
import com.example.presentgeo.model.JsonRespon;
import com.example.presentgeo.model.PolygonMarker;
import com.example.presentgeo.service.Api;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.maps.android.PolyUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Inflater;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.AlertDialog.*;


/**
 * An activity that displays a Google map with polylines to represent paths or routes,
 * and polygons to represent areas.
 */
public class LandingActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnPolygonClickListener {

    private static final String TAG = "Landing Activity";
    private static final int REQUEST_CODE = 101;
    MaterialButton absen,logout;
    LatLng lastLocation;
    double currentLat = 0, currentLong = 0;
    Boolean first = false;
    SharedPreferences preferences;
    FusedLocationProviderClient fusedLocationProviderClient;
    MaterialTextView tvName;
    List<LatLng> mList = new ArrayList<>();
    Polygon polygon1;
    boolean contain;
    boolean isMock = true;
    boolean pausedYet = false;
    GoogleMap mGoogleMap;

    AlertDialog dialogLogout;
    View logoutDialog;
    Boolean dialogNotCreated = false;

    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        absen = findViewById(R.id.btnSendLoc);
        logout = findViewById(R.id.btnLogout);
        final LogoutDialog dLogout = new LogoutDialog(this);

        //Bottom sheet


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dLogout.starDialog(v,logout);
                logout.setClickable(false);
            }
        });
        View bottomsheet = findViewById(R.id.bottom_sheet);
        tvName = findViewById(R.id.tvName);
        String name = getSharedPreferences("UserData", MODE_PRIVATE).getString("nama", "No reference");
        tvName.setText(name);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheet);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });



        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


    }



    @Override
    protected void onResume() {
        super.onResume();
        if(pausedYet){
            currentLocation(mGoogleMap);

        }
    }


    private void present() {
        Call<JsonRespon> call;

        //parameternnya belum
        SharedPreferences sharedUserData = getSharedPreferences("UserData", MODE_PRIVATE);
        String id_pegawai = sharedUserData.getString("id_pegawai", null);
        call = Api.getClient().absen(id_pegawai, currentLat, currentLong,getSharedPreferences("UserData",MODE_PRIVATE).getString("imei_code",null));

        call.enqueue(new Callback<JsonRespon>() {
            @Override
            public void onResponse(Call<JsonRespon> call, Response<JsonRespon> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: Is Successfull");
                    if (response.body().getCode()) {
                        Toast.makeText(LandingActivity.this, "Berhasil absen", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LandingActivity.this, " ", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonRespon> call, Throwable t) {
                Toast.makeText(LandingActivity.this, " error : " + t, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    private void grantPermission(final GoogleMap googleMap) {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // permission is granted
                       currentLocation(googleMap);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied()) {
                            // navigate user to app settings
                            Toast.makeText(LandingActivity.this, "Permission Location Denied", Toast.LENGTH_SHORT).show();

                            googleMap.clear();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    public void onBackPressed() {

    }

    private void getMarkerPolygon(final GoogleMap googleMap) {
        Call<JsonMarker> call;

        call = Api.getClient().present();

        call.enqueue(new Callback<JsonMarker>() {
            @Override
            public void onResponse(Call<JsonMarker> call, Response<JsonMarker> response) {
                if (response.isSuccessful()) {
//                    Log.d(TAG, "onResponse: "+ response.body().getData());

                    List<PolygonMarker> polygons = response.body().getData();

                    for (PolygonMarker marker : polygons) {
                        LatLng position = new LatLng(Double.parseDouble(marker.getLat()), Double.parseDouble(marker.getLng()));
                        mList.add(position);
//                        Log.d(TAG, "onResponse: lat -> " + marker.getLat() + " && lng -> "+ marker.getLng());
                    }
                     polygon1 = googleMap.addPolygon(new PolygonOptions()
                            .clickable(false).addAll(mList));


//
//                            .add(
//                                    new LatLng(-27.457, 153.040),
//                                    new LatLng(-33.852, 151.211),
//                                    new LatLng(-37.813, 144.962),
//                                    new LatLng(-34.928, 138.599)));
//                    // Store a data object with the polygon, used here to indicate an arbitrary type.
                    polygon1.setTag("alpha");
//                    // Style the polygon.
                    stylePolygon(polygon1);
                } else {

                }
            }

            @Override
            public void onFailure(Call<JsonMarker> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    private boolean valid(final GoogleMap googleMap) {
        if(!inLocation()){
            currentLocation(googleMap);
            return false;
        }
        if (currentLat == 0) {
            Toast.makeText(this, "Error : Latitude tidak didapatkan", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "valid: Latitude tidak didapatkan");
            currentLocation(googleMap);
            return false;
        }
        if (currentLong == 0) {
            Toast.makeText(this, "Longitude tidak didapatkan", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "valid: Longitude tidak didapatkan");
            currentLocation(googleMap);
            return false;
        }
        return true;
    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this tutorial, we add polylines and polygons to represent routes and areas on the map.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;
        grantPermission(googleMap);


        Double nowLoginLat = Double.parseDouble(getSharedPreferences("UserData",MODE_PRIVATE).getString("latitude" , "0"));
        Double nowLoginLong = Double.parseDouble(getSharedPreferences("UserData",MODE_PRIVATE).getString("longitude" , "0"));
        if((nowLoginLat != 0) && (nowLoginLong != 0) ){
            CameraUpdate cameraUpdate;
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(nowLoginLat,nowLoginLong),18f);
            googleMap.animateCamera(cameraUpdate);
        }else{
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    CameraUpdate cameraUpdate;
                    cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),15f);
                    googleMap.animateCamera(cameraUpdate);
                }

            });

        }
        Log.d(TAG, "onMapReady:  Latitude " + nowLoginLat + " Longitude " + nowLoginLong);
        currentLocation(googleMap);
        absen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (valid(googleMap)) {
                    currentLocation(googleMap);
                    present();
                }
            }
        });
        getMarkerPolygon(googleMap);


    }

    private void currentLocation(final GoogleMap googleMap) {
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    googleMap.setMyLocationEnabled(false);
                    if(!location.isFromMockProvider()){
                        currentLat = location.getLatitude();
                        currentLong = location.getLongitude();
                        googleMap.setMyLocationEnabled(true);

                    }else{
                        warnShownUp();
                    }
                    Log.d(TAG, "currentLocation: " + currentLat  + " : " + currentLong + "isMOCK ? " + location.isFromMockProvider());

                }else{
                    Toast.makeText(LandingActivity.this, "No location found", Toast.LENGTH_SHORT).show();
                }

            }
        });
        //check if the position in contain polygon location
        contain = PolyUtil.containsLocation(currentLat,currentLong,mList,true);

//        Log.d(TAG, "currentLocation: " + contain);
    }

    private void warnShownUp() {
        final AlertDialog.Builder fakeGPS = new AlertDialog.Builder(LandingActivity.this);
        fakeGPS.setMessage("Make Sure to turned off the FAKE GPS!");
        fakeGPS.setCancelable(false);
        fakeGPS.setIcon(R.drawable.ic_baseline_gps_off_24);
        fakeGPS.setPositiveButton("OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentLocation(mGoogleMap);
            }
        });
        fakeGPS.create();
        fakeGPS.show();
    }

    private boolean inLocation(){
        if(contain){
            return true;
        }
        Toast.makeText(this, "Diluar Area", Toast.LENGTH_SHORT).show();
        return false;
    }


    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;

    /**
     * Styles the polyline, based on type.
     * @param polyline The polyline object that needs styling.
     */

    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    /**
     * Listens for clicks on a polygon.
     * @param polygon The polygon object that the user has clicked.
     */
    @Override
    public void onPolygonClick(Polygon polygon) {
        // Flip the values of the red, green, and blue components of the polygon's color.
        int color = polygon.getStrokeColor() ^ 0x00ffffff;
        polygon.setStrokeColor(color);
        color = polygon.getFillColor() ^ 0x00ffffff;
        polygon.setFillColor(color);

        Toast.makeText(this, "Area type " + polygon.getTag().toString(), Toast.LENGTH_SHORT).show();
    }


    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_PURPLE_ARGB = 0xff81C784;
    private static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_BLUE_ARGB = 0xffF9A825;

    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);

    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);

    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
    private static final List<PatternItem> PATTERN_POLYGON_BETA =
            Arrays.asList(DOT, GAP, DASH, GAP);

    /**
     * Styles the polygon, based on type.
     * @param polygon The polygon object that needs styling.
     */
    private void stylePolygon(Polygon polygon) {
        String type = "";
        // Get the data object stored with the polygon.
        if (polygon.getTag() != null) {
            type = polygon.getTag().toString();
        }

        List<PatternItem> pattern = null;
        int strokeColor = COLOR_BLACK_ARGB;
        int fillColor = COLOR_WHITE_ARGB;

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "alpha":
                // Apply a stroke pattern to render a dashed line, and define colors.
                pattern = PATTERN_POLYGON_ALPHA;
                strokeColor = COLOR_GREEN_ARGB;
                fillColor = COLOR_PURPLE_ARGB;
                break;
            case "beta":
                // Apply a stroke pattern to render a line of dots and dashes, and define colors.
                pattern = PATTERN_POLYGON_BETA;
                strokeColor = COLOR_ORANGE_ARGB;
                fillColor = COLOR_BLUE_ARGB;
                break;
        }

        polygon.setStrokePattern(pattern);
        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        polygon.setStrokeColor(strokeColor);
        polygon.setFillColor(fillColor);
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearMap();
    }

    private void clearMap() {
        mGoogleMap.clear();
        mGoogleMap.setMyLocationEnabled(false);
        pausedYet = true;
        Log.d(TAG, "clearMap: googlemap cleared");
    }

}

    