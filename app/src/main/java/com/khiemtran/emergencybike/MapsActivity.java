package com.khiemtran.emergencybike;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.khiemtran.emergencybike.Application.EmergencyBikeApplication;
import com.khiemtran.emergencybike.Models.GarageModel;
import com.khiemtran.emergencybike.Utils.General;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    public static final int PERMISSION_LOCATION_REQ_CODE = 1;
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int LARGE_UPDATE_INTERVAL_IN_SECONDS = 30;
    public static final int FAST_CEILING_IN_SECONDS = 1;
    public static final long LARGE_UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * LARGE_UPDATE_INTERVAL_IN_SECONDS;
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private double mLongitude = 0;//106.64737701;
    private double mLatitude = 0;//10.79992092;
    private LocationRequest mLocationRequest;
    private GoogleMap googleMap;
    private LocationManager mLocationManager;
    private LatLng currentLocation = new LatLng(mLatitude, mLongitude);
    private Marker currentMarker;
    private boolean isFirst = false;
    private Context mContext;
    private List<GarageModel> lstGarageModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mContext = this;
        setUpLocation();
        setUpMapIfNeeded();
    }

    private void setUpLocation() {
        mLocationRequest = LocationRequest.create();

        // Set the update interval
        mLocationRequest.setInterval(LARGE_UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                addGarageMarker();
            }
        }
    }

    /**
     * Fake data
     */
    private void addGarageMarker(){
        String strGarageJson = General.loadJSONFromAsset(this, "tiem_sua_xe.json");
        lstGarageModel = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(strGarageJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                GarageModel mGarageModel = new GarageModel();
                mGarageModel.setName(jsonObject.getString("Name"));
                mGarageModel.setAddress(jsonObject.getString("Address"));
                mGarageModel.setPhone(jsonObject.getString("Phone"));
                mGarageModel.setTag(jsonObject.getString("Tag"));
                mGarageModel.setLat(jsonObject.getLong("Lat"));
                mGarageModel.setLong(jsonObject.getLong("Long"));
                lstGarageModel.add(mGarageModel);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MarkerOptions marker = new MarkerOptions();
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_red));
        for (int i = 0; i < lstGarageModel.size(); i++) {
            final GarageModel mGarageModel = lstGarageModel.get(i);
            marker.position(new LatLng(mGarageModel.getLat(), mGarageModel.getLong()));
            marker.title(mGarageModel.getName());
            mMap.addMarker(marker);
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    startDialogGarage(getGarageModelClick(marker));
                }
            });
        }
    }

    private GarageModel getGarageModelClick(Marker marker){
        for (int i = 0; i < lstGarageModel.size(); i++) {
            if(lstGarageModel.get(i).getLat() == marker.getPosition().latitude &&
                    lstGarageModel.get(i).getLong() == marker.getPosition().longitude){
                return lstGarageModel.get(i);
            }
        }
        return null;
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if(currentMarker != null){
            currentMarker.remove();
        }
        currentMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Bạn đang ở đây"));
    }

    @Override
    public void onConnected(Bundle bundle) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED){// && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQ_CODE);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(lastLocation != null){
            currentLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        }else{
            currentLocation = new LatLng(10.701335, 106.782303);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if(!isFirst) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));
            isFirst = true;
        }
        setUpMap();
    }

    private void startDialogGarage(GarageModel mGarageModel){
        Dialog mDialog = new Dialog(mContext, android.R.style.Theme_Holo_Dialog_NoActionBar);
        mDialog.setContentView(R.layout.dialog_garage);

        ((TextView) mDialog.findViewById(R.id.txt_name)).setText(mGarageModel.getName());
        ((TextView) mDialog.findViewById(R.id.txt_address)).setText(String.format(getString(R.string.txt_garage_adress), mGarageModel.getAddress()));
        ((TextView) mDialog.findViewById(R.id.txt_phone)).setText(String.format(getString(R.string.txt_garage_phone), mGarageModel.getPhone()));
        ((LinearLayout) mDialog.findViewById(R.id.root_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mDialog.show();
    }

}
