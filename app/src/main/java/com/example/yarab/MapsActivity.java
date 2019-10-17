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
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.SearchView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public  class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, ResultCallback<Status> {
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private GoogleMap mMap;

    private LocationCallback locCallback;
    private Location lastLoc;
    private GeofencingClient geofencingClient;
    private Marker locationMarker, geoFenceMarker;
    private SearchView searchView;
    private Circle geoFenceLimits;

    private ArrayList<Geofence> geofenceList;



    // For creating GeoFence


    // For PendingIntent
    private PendingIntent geoFencePendingIntent;
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
        geofenceList=new ArrayList<>();


        LatLng blue=new LatLng(31.906051,35.212643);
        LatLng ramallah=new LatLng(31.906051,35.212643);
        LatLng rafedia=new LatLng(31.906051,35.212643);
        Geofence geofence1 =  new Geofence.Builder()
                .setRequestId("BLUE")
                .setCircularRegion(blue.latitude,blue.longitude, 700)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        Geofence geofence2 =  new Geofence.Builder()
                .setRequestId("RAMALLAH")
                .setCircularRegion(ramallah.latitude,blue.longitude, 700)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        Geofence geofence3 =  new Geofence.Builder()
                .setRequestId("RAFEDIA")
                .setCircularRegion(rafedia.latitude,blue.longitude, 700)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        geofenceList.add(geofence1);
        geofenceList.add(geofence2);
        geofenceList.add(geofence3);

        startGeofence(geofenceList);
     //   startGeofence(ramallah,2);
       // startGeofence(rafedia,3);


        searchView=findViewById(R.id.search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location=searchView.getQuery().toString();
                List<Address> list;
                if(location!=null && !location.isEmpty()){
                    Geocoder geocoder=new Geocoder(MapsActivity.this);
                    try {
                        list=geocoder.getFromLocationName(location,1);
                        Address address=list.get(0);
                        LatLng latLng=new LatLng(address.getLatitude(),address.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                       markerForGeo(latLng);

                        //startGeofence(latLng.latitude,latLng.longitude);
                        //               geo.addNewGeo(latLng);
                        //             geo.registerAllGeos();
                     /*   String s="";
                        for(int i=0;i<geo.getMyGeos().size();i++)
                        {
                            s+=geo.getMyGeos().get(i).getRequestId()+"\t";
                        }
                        Toast.makeText(MapsActivity.this,s,Toast.LENGTH_LONG).show();*/
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                return false;            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mapFragment.getMapAsync(this);

    }


    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, MapsActivity.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }










    Circle circle;
    Marker marker;
    @Override
    public void onMapReady(GoogleMap googleMap) {

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
        request.setInterval(50000);
        request.setFastestInterval(10000);
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
                            if(lastLoc!=null) {
                                LatLng myLatLng = new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude());

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 17));
                               // markerForGeo(myLatLng);
                            }
                            else{
                                LocationRequest req=LocationRequest.create();
                                req.setInterval(50000);
                                req.setFastestInterval(10000);
                                req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                                locCallback=new LocationCallback(){
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if(locationResult==null)
                                            return;
                                        lastLoc=locationResult.getLastLocation();
                                        LatLng myLatLng = new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude());
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng,17));
                                      //  markerForGeo(myLatLng);

                                        //markerForCurrent(myLatLng);

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

    // create location marker
    private void markerLocation(LatLng latLng){
        Log.i("VisitPalestine", "markerLocation("+latLng+")");
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
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.animateCamera(cameraUpdate);
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("VisitPalestine", "onConnected()");

        getLastKnownLocation();
    }

    // Get last known location
    private void getLastKnownLocation(){
        Log.d("VisitPalestine", "getLastKnownLocation()");


            LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    lastLoc=location;
                }
            });
            if (lastLoc != null){
                Log.i("VisitPalestine", "LasKnown location. " +
                        "Long: " + lastLoc.getLongitude() +
                        " | Lat: " + lastLoc.getLatitude());

                startLocationUpdates();
            }else {
                Log.w("VisitPalestine", "No location retrieved yet");
                startLocationUpdates();
            }


    }

    // start location update
    private void startLocationUpdates() {
        Log.i("VisitPalestine", "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(50000)
                .setFastestInterval(10000);

      //  if (checkPermission()){
           // LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest,createGeofencePendingIntent());
          //  LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
       // }
    }

    @Override
    public void onConnectionSuspended(int i) {


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // called when click on any part of map
    @Override
    public void onMapClick(LatLng latLng) {


    }

    private void markerForGeo(LatLng alaLat){

        String title = alaLat.latitude + ", " + alaLat.longitude;

        // define marker option
        MarkerOptions markerOptions = new MarkerOptions()
                .position(alaLat)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));

        if (mMap != null) {
            //  remove the last geoFenceMarker
            if (geoFenceMarker != null) {
                geoFenceMarker.remove();
            }

            geoFenceMarker = mMap.addMarker(markerOptions);


        }

    }

    // called when Marker is touched
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("VisitPalestine", "onMarkerClickListener: " + marker.getPosition() );
        return false;
    }


    /*
     *
     *
     *
     * */

    // create geoFence
   /* private Geofence createGeofence(LatLng latLng, float radius){

        Geofence geofence =new Geofence.Builder()
                .setRequestId("My GEO")
                .setCircularRegion(31.906051,35.212643, 700)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
       // Toast.makeText(MapsActivity.this,"AAA2222",Toast.LENGTH_LONG).show();

        return geofence;
    }*/

    // create geoFence request
    private GeofencingRequest createGeoFenceRequest(ArrayList<Geofence> geofenceList){
        GeofencingRequest geofencingRequest=new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build();
      //  Toast.makeText(MapsActivity.this,"AAA3333",Toast.LENGTH_LONG).show();

        return geofencingRequest;
    }


    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request,int id){
        //if(!client.isConnected())
          //  client.connect();
        if (checkPermission()){
            LocationServices.getGeofencingClient(this).addGeofences(request,createGeofencePendingIntent(id));
           // Toast.makeText(MapsActivity.this, "AAA4444", Toast.LENGTH_SHORT).show();
       //     LocationServices.GeofencingApi.addGeofences(client,request,createGeofencePendingIntent()).setResultCallback(this);
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i("VisitPalestine", "onResult: " + status);
        if (status.isSuccess()){
            drawGeofence();
        }else{
        }
    }

    // draw geofence circle on google map
    private void drawGeofence(){

        if (geoFenceLimits != null){
            geoFenceLimits.remove();
        }

       CircleOptions circleOptions = new CircleOptions()
             .center(geoFenceMarker.getPosition())
               .fillColor(Color.argb(100, 135,39,154))
               .radius(700);

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
    private void startGeofence(ArrayList<Geofence> geofenceList ){

       // if (geoFenceMarker != null){

        //Geofence geofence = createGeofence(latLng, 700);
            GeofencingRequest geofencingRequest = createGeoFenceRequest(geofenceList);
            //addGeofences(geofencingRequest,id);
       // addGeofence(geofencingRequest);
     //   markerForGeo(latLng);

        //  Toast.makeText(getApplicationContext(),"AAAAAAAAA",Toast.LENGTH_LONG).show();


    }


 /*   public void startGeofence(View view) {
        Log.i("VisitPalestine", "startGeofence()");

        if (geoFenceMarker != null){
            Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
            GeofencingRequest geofencingRequest = createGeoFenceRequest(geofence);
            addGeofence(geofencingRequest);
        }else {
            Log.e("VisitPalestine", "Geofence marker is null");
        }
    }*/

    /*
     *
     *
     *
     * */

    // we use PendingIntent object to call Intent service
    private PendingIntent createGeofencePendingIntent(int id) {
        Log.d("VisitPalestine", "createGeofencePendingIntent");
        PendingIntent pendingIntent=null;

        if (geoFencePendingIntent != null) {
            pendingIntent=geoFencePendingIntent;

            return pendingIntent;

        }
                Intent intent = new Intent(MapsActivity.this, MyIntentService.class);
                pendingIntent = PendingIntent.getService(
                        this, 0, intent, PendingIntent.FLAG_ONE_SHOT
                );



        return pendingIntent;

    }

        // Toast.makeText(getApplicationContext(),"AAAA5555",Toast.LENGTH_LONG).show();


    /*
     *
     *
     *
     * */

    // check the permission to access to location
    private boolean checkPermission(){
        Log.d("VisitPalestine", "checkPermission");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    // Ask for permission
    private void askPermission(){
        Log.d("VisitPalestine", "askPermission()");

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                27
        );
    }


    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("VisitPalestine", "onRequestPermissionsResult()");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case 27: {
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
        Log.w("VisitPalestine", "permissionsDenied()");
    }


}
