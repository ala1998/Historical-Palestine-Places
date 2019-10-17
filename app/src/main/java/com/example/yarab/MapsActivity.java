package com.example.yarab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, ResultCallback<Status> {
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private GoogleMap mMap;

    private LocationCallback locCallback;
    private Location lastLoc;
    private GeofencingClient geofencingClient;
    private Marker locationMarker, geoFenceMarker;

    private Circle geoFenceLimits;

    private ArrayList<Geofence> geofenceList;

    private static final int REQ_PERMISSION = 1337;

    // These numbers in mili seconds in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL =  3 * 60 * 1000; // 3 minutes
    private final int FASTEST_INTERVAL = 3 * 60 * 1000; // 3 minutes


    // For creating GeoFence
    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters

    // For PendingIntent
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
        private FusedLocationProviderClient locClient;
        private View mView;

    // notifications
    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mView=mapFragment.getView();
        locClient=LocationServices.getFusedLocationProviderClient(MapsActivity.this);

    //    createGoogleApi();
       // if(!client.isConnected())
        //client.connect();
        // init the geofencingClient
        geofencingClient = LocationServices.getGeofencingClient (this);
        startGeofence();

    }// end of onCreate ..


    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, MapsActivity.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }


    // Create GoogleApiClient instance
    public void createGoogleApi(){
        Log.d("History", "createGoogleApi()");
        if ( client == null ) {
            client = new GoogleApiClient.Builder( this )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( LocationServices.API )
                    .build();

        }
     //   if(!client.isConnected())
       // client.connect();

    }


    // Call GoogleApiClient connection when starting the Activity
    @Override
    protected void onStart() {
        super.onStart();
       // client.connect();
    }


    // Disconnect GoogleApiClient when stopping Activity
    @Override
    protected void onStop() {
        super.onStop();
        //client.disconnect();
    }


    Circle circle;
    Marker marker;
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d("History", "onMapReady()");

        mMap = googleMap;

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if(mView!=null && mView.findViewById(Integer.parseInt("1"))!=null)
        {
            View locBtn=((View) mView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) locBtn.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
            params.setMargins(0,40,0,150);
        }
        LocationRequest request=LocationRequest.create();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder().addLocationRequest(request);
        SettingsClient settingsClient= LocationServices.getSettingsClient(MapsActivity.this);

        Task<LocationSettingsResponse> task= settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                locClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful())
                        {
                            lastLoc=task.getResult();
                            if(lastLoc!=null)
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLoc.getLatitude(),lastLoc.getLongitude()),17));
                            else{
                                LocationRequest req=LocationRequest.create();
                                req.setInterval(50000);
                                req.setFastestInterval(20000);
                                req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                                locCallback=new LocationCallback(){
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if(locationResult==null)
                                            return;
                                        lastLoc=locationResult.getLastLocation();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLoc.getLatitude(),lastLoc.getLongitude()),17));
                                    }
                                };

                                locClient.requestLocationUpdates(req,locCallback,null);
                            }
                        }
                        else
                            Toast.makeText(MapsActivity.this,"Unable to get your location!",Toast.LENGTH_LONG).show();
                    }
                });
            }

        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException)
                {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(MapsActivity.this,27);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });


        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);



    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.d("History", "onLocationChanged ["+location+"]");

      //  lastLoc = location;
       // writeActualLocation(location);

//


    }

    // Write location coordinates on UI
    private void writeActualLocation(Location location) {
       // markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    // create location marker
    private void markerLocation(LatLng latLng){
        Log.i("History", "markerLocation("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions =  new MarkerOptions()
                .position(latLng)
                .title(title);

        if (mMap != null){
            // Remove the anterior marker
            if (locationMarker != null){
                locationMarker.remove();
            }

            locationMarker = mMap.addMarker(markerOptions);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14f);
            mMap.animateCamera(cameraUpdate);
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("History", "onConnected()");

        getLastKnownLocation();
    }

    // Get last known location
    private void getLastKnownLocation(){
        Log.d("History", "getLastKnownLocation()");

        if (checkPermission()){
            LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    lastLoc=location;
                }
            });
            if (lastLoc != null){
                Log.i("History", "LasKnown location. " +
                        "Long: " + lastLoc.getLongitude() +
                        " | Lat: " + lastLoc.getLatitude());

             //   writelastLoc();
                startLocationUpdates();
            }else {
                Log.w("History", "No location retrieved yet");
                startLocationUpdates();
            }

        }else{
            askPermission();
        }
    }

    // start location update
    private void startLocationUpdates() {
        Log.i("History", "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (checkPermission()){
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest,createGeofencePendingIntent());
          //  LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    private void writelastLoc() {
       // writeActualLocation(lastLoc);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w("History", "onConnectionSuspended()");


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w("History", "onConnectionFailed()");

    }

    // called when click on any part of map
    @Override
    public void onMapClick(LatLng latLng) {
        Log.d("History", "onMapClick("+latLng +")");

        markerForGeofence(latLng);

    }

    // create marker for Geofence creation
    private void markerForGeofence(LatLng latLng){
        Log.i("History", "markerForGeofence("+latLng+")");

        String title = latLng.latitude + ", " + latLng.longitude;

        // define marker option
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

        if (mMap != null){
            // remove the last geoFenceMarker
            if (geoFenceMarker != null){
                geoFenceMarker.remove();
            }

            geoFenceMarker = mMap.addMarker(markerOptions);
        }



    }

    // called when Marker is touched
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("History", "onMarkerClickListener: " + marker.getPosition() );
        return false;
    }


    /*
     *
     *
     *
     * */

    // create geoFence
    private Geofence createGeofence(LatLng latLng, float radius){
        Log.d("History", "createGeofence");

        Geofence geofence =new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(32.225229,35.241456, radius)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        Toast.makeText(MapsActivity.this,"AAA2222",Toast.LENGTH_LONG).show();

        return geofence;
    }

    // create geoFence request
    private GeofencingRequest createGeoFenceRequest(Geofence geofence){
        Log.d("History", "createGeofenceRequest");
        GeofencingRequest geofencingRequest=new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
        Toast.makeText(MapsActivity.this,"AAA3333",Toast.LENGTH_LONG).show();

        return geofencingRequest;
    }


    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request){
        Log.d("History", "addGeofence");
        //if(!client.isConnected())
          //  client.connect();
        if (checkPermission()){
            LocationServices.getGeofencingClient(this).addGeofences(request,createGeofencePendingIntent());
            Toast.makeText(MapsActivity.this, "AAA4444", Toast.LENGTH_SHORT).show();
       //     LocationServices.GeofencingApi.addGeofences(client,request,createGeofencePendingIntent()).setResultCallback(this);
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i("History", "onResult: " + status);
        if (status.isSuccess()){
            drawGeofence();
        }else{
            // inform about fail
        }
    }

    // draw geofence circle on google map
    private void drawGeofence(){
        Log.d("History", "drawGeofence()");

        if (geoFenceLimits != null){
            geoFenceLimits.remove();
        }

       CircleOptions circleOptions = new CircleOptions()
             .center(geoFenceMarker.getPosition())
              .strokeColor(Color.argb(50, 70,70,70))
               .fillColor(Color.argb(100, 150,150,150))
               .radius(GEOFENCE_RADIUS);

       geoFenceLimits = mMap.addCircle(circleOptions);
    }


    // for geofence menu
   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.start: {
                startGeofence();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }*/

    // Start Geofence creation process
    private void startGeofence(){
        Log.i("History", "startGeofence");

       // if (geoFenceMarker != null){
            Geofence geofence = createGeofence(new LatLng(32.225229,35.241456), GEOFENCE_RADIUS);
            GeofencingRequest geofencingRequest = createGeoFenceRequest(geofence);
            addGeofence(geofencingRequest);
            Toast.makeText(getApplicationContext(),"AAAAAAAAA",Toast.LENGTH_LONG).show();

       // }else {
         //   Log.e("History", "Geofence marker is null");
           // Toast.makeText(getApplicationContext(),"Null",Toast.LENGTH_LONG).show();

        //}
    }


 /*   public void startGeofence(View view) {
        Log.i("History", "startGeofence()");

        if (geoFenceMarker != null){
            Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
            GeofencingRequest geofencingRequest = createGeoFenceRequest(geofence);
            addGeofence(geofencingRequest);
        }else {
            Log.e("History", "Geofence marker is null");
        }
    }*/

    /*
     *
     *
     *
     * */

    // we use PendingIntent object to call Intent service
    private PendingIntent createGeofencePendingIntent(){
        Log.d("History", "createGeofencePendingIntent");

        if (geoFencePendingIntent != null){
            return geoFencePendingIntent;
        }

        Intent intent = new Intent(MapsActivity.this, MyIntentService.class);
        PendingIntent pendingIntent=PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_ONE_SHOT
        );
        Toast.makeText(getApplicationContext(),"AAAA5555",Toast.LENGTH_LONG).show();

        return pendingIntent;
    }

    /*
     *
     *
     *
     * */

    // check the permission to access to location
    private boolean checkPermission(){
        Log.d("History", "checkPermission");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    // Ask for permission
    private void askPermission(){
        Log.d("History", "askPermission()");

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }


    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("History", "onRequestPermissionsResult()");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case REQ_PERMISSION: {
                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    private void permissionsDenied() {
        Log.w("History", "permissionsDenied()");
    }


}
