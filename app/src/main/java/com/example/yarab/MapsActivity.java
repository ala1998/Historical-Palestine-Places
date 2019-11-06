package com.example.yarab;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewGroupCompat;
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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public  class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, ResultCallback<Status> {
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private GoogleMap mMap;
    private int flag=-1;
    private LocationCallback locCallback;
    private Location lastLoc;
    private GeofencingClient geofencingClient;
    private SearchView searchView;
    public static MediaPlayer player;
  //  private Circle geoFenceLimits;
    private ImageView imageView;
    private View myView=null;

    private ArrayList<Geofence> geofenceList;
    ArrayList<Geo> geos;
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
        String json = "";
        try {
            InputStream is = this.getAssets().open("places.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();

        }
         geos = JsonUtils.parseGeoJson(json);
        for(int i=0;i<geos.size();i++){
            //Toast.makeText(this, geos.get(i).getRequest_id()+"\n"+geos.get(i).getLatLng().latitude+"\n"+geos.get(i).getLatLng().longitude+"\n"
                //    +geos.get(i).getRadius()+"\n"+geos.get(i).getAudio(),Toast.LENGTH_LONG).show();
            createWithoutDraw(geos.get(i).getLatLng(),geos.get(i).getRequest_id(),geos.get(i).getRadius());
        }

        if(geofenceList!=null && !geofenceList.isEmpty()) {
            startGeofence(geofenceList);
     //   Toast.makeText(MapsActivity.this,"Mayar",Toast.LENGTH_LONG).show();
        }
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
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,14));
                       //markerForGeo(latLng);

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

    //TODO: Show notifications

    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, MapsActivity.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }









    Circle circle;
    Marker marker;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(geofenceList!=null && !geofenceList.isEmpty()) {
            startGeofence(geofenceList);
     //   Toast.makeText(MapsActivity.this,"OnMapReady",Toast.LENGTH_LONG).show();
        }
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        //TODO: Uncomment this
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

//                                Bitmap bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.ic_car2);
                                Bitmap bitmap=getBitmapFromVectorDrawable(MapsActivity.this,R.drawable.ic_car2);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 14));
                                MarkerOptions myMarker = new MarkerOptions()
                                  .position(myLatLng)

                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                                mMap.addMarker(myMarker);
//                               Marker marker= mMap.addMarker(myMarker);
//                               marker.showInfoWindow();
                              //  if(geofenceList!=null && !geofenceList.isEmpty())
                                 //   startGeofence(geofenceList);

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
                                        Bitmap bitmap=getBitmapFromVectorDrawable(MapsActivity.this,R.drawable.ic_car2);
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng,14));
                                        MarkerOptions myMarker = new MarkerOptions()
                                                .position(myLatLng)

                                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                                        mMap.addMarker(myMarker);
//                                        Marker marker=mMap.addMarker(myMarker);
//                                        marker.showInfoWindow();
                                        if(geofenceList!=null && !geofenceList.isEmpty())
                                            startGeofence(geofenceList);
                                        Toast.makeText(MapsActivity.this,"Start from location",Toast.LENGTH_LONG).show();
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
        for(int i=0;i<geos.size();i++){
               createAndDrawCircle(geos.get(i).getLatLng(),geos.get(i).getRequest_id(),geos.get(i).getRadius()
               ,geos.get(i).getDesc());
        }
     /*   LatLng blue=new LatLng(31.906051,35.212643);
        LatLng ramallah=new LatLng(31.908652, 35.203135);
        LatLng rafedia=new LatLng(32.227521,35.223483);
        LatLng hospital=new LatLng(32.225456, 35.241531);
        LatLng yabad=new LatLng(32.448028, 35.167231);

        createAndDrawCircle(blue,"BLUE",700, "Blue is the first web-development company in Palestine,");
        createAndDrawCircle(ramallah,"RAMALLAH",700,"Ramallah is a big city in Palestine,");
        createAndDrawCircle(rafedia,"RAFEEDIA",700,"Refeedia is a beautiful region in Palestine,");
        createAndDrawCircle(hospital,"HOSPITAL",700,"Rafeedia hospital is one of famous hospitals in Palestine,");
        createAndDrawCircle(yabad,"YA'BAD",700,"Ya'bad is a big town in Palestine,");
*/
       // Toast.makeText(MapsActivity.this,"ALA",Toast.LENGTH_LONG).show();
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);



     /*   geofenceList.add(geofence1);
        geofenceList.add(geofence2);
        geofenceList.add(geofence3);
        geofenceList.add(geofence4);
        geofenceList.add(geofence5);

   */

      // Toast.makeText(this, "before", Toast.LENGTH_LONG).show();
      //  startGeofence(geofenceList);
        //Toast.makeText(this, "after", Toast.LENGTH_LONG).show();



    }

  /*  // create location marker
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
*/

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("VisitPalestine", "onConnected()");

        getLastKnownLocation();
    }

    // Get last known location
    private void getLastKnownLocation(){
        //Log.d("VisitPalestine", "getLastKnownLocation()");


            LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    lastLoc=location;
                    if(geofenceList!=null && !geofenceList.isEmpty())
                    startGeofence(geofenceList);
                }
            });
            if (lastLoc != null){
               // Log.i("VisitPalestine", "LasKnown location. " +
                 //       "Long: " + lastLoc.getLongitude() +
                   //     " | Lat: " + lastLoc.getLatitude());

                startLocationUpdates(lastLoc);
            }else {
              //  Log.w("VisitPalestine", "No location retrieved yet");
                startLocationUpdates(lastLoc);
            }


    }

    // start location update
    private void startLocationUpdates(Location location) {
        Log.i("VisitPalestine", "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(50000)
                .setFastestInterval(10000);
        if(location!=null){
            LatLng latLng= new LatLng(location.getLatitude(),location.getLongitude());
            Bitmap bitmap=getBitmapFromVectorDrawable(MapsActivity.this,R.drawable.ic_car2);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
            MarkerOptions myMarker = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap));
            mMap.addMarker(myMarker);

        }
        if (geofenceList != null && !geofenceList.isEmpty())
        {
            startGeofence(geofenceList);
            Toast.makeText(this,"ALA",Toast.LENGTH_LONG).show();

        }
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

    /*private void markerForGeo(LatLng alaLat){

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
*/
    // called when Marker is touched
  @Override
    public boolean onMarkerClick(Marker marker) {
   //   if(!marker.isInfoWindowShown())
     //     marker.showInfoWindow();
     // else
       //   marker.hideInfoWindow();
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
   /* private void addGeofence(GeofencingRequest request,int id){
        //if(!client.isConnected())
          //  client.connect();
        if (checkPermission()){
            LocationServices.getGeofencingClient(this).addGeofences(request,createGeofencePendingIntent());
           // Toast.makeText(MapsActivity.this, "AAA4444", Toast.LENGTH_SHORT).show();
       //     LocationServices.GeofencingApi.addGeofences(client,request,createGeofencePendingIntent()).setResultCallback(this);
        }
    }*/

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()){
            if(geofenceList!=null && !geofenceList.isEmpty()) {
                Toast.makeText(MapsActivity.this, "Result Success", Toast.LENGTH_LONG).show();
                startGeofence(geofenceList);
            }
            //createAndDrawCircle();
           // drawGeofence();
        }else{
            Toast.makeText(MapsActivity.this,"Result Failed",Toast.LENGTH_LONG).show();
        }
    }

    // draw geofence circle on google map
/*    private void drawGeofence(){
       // if (geoFenceLimits != null){
      //      geoFenceLimits.remove();
        //}
    }*/


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
         //   if(checkPermission()) {
                GeofencingRequest geofencingRequest = createGeoFenceRequest(geofenceList);
                LocationServices.getGeofencingClient(this).addGeofences(geofencingRequest, createGeofencePendingIntent());
//        Toast.makeText(getApplicationContext(),"AAAAAAAAA",Toast.LENGTH_LONG).show();

        //
        //    }

        //        Toast.makeText(this, "start geofence", Toast.LENGTH_LONG).show();


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

    private void createAndDrawCircle(LatLng latLng,String requestID,float r,String desc ){
        Geofence geofence =  new Geofence.Builder()
                .setRequestId(requestID)
                .setCircularRegion(latLng.latitude,latLng.longitude, r)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                )
                .build();
        geofenceList.add(geofence);
        CircleOptions circleOptions = new CircleOptions() .center(latLng)
                .radius(r)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .strokeWidth(5.0f);
//        Bitmap m=BitmapFactory.decodeResource(this.getResources(), R.drawable.play);
//        Canvas canvas = new Canvas(m);
      //  canvas.drawText("CITY", 0, 50,new Paint()); // paint defines the text color, stroke width, size
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents( Marker marker) {


                LinearLayout info = new LinearLayout(MapsActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);
                LinearLayout horiz=new LinearLayout(MapsActivity.this);
                horiz.setOrientation(LinearLayout.HORIZONTAL);
                imageView=new ImageView(MapsActivity.this);
                imageView.setTag("MyImage");
                if(flag==1)
                    imageView.setImageResource(R.drawable.ic_pause);
                else if(flag==0)
                    imageView.setImageResource(R.drawable.ic_play);
                else
                    imageView.setImageResource(R.drawable.ic_play);

                imageView.setPadding(20,20,20,20);
                //LinearLayout linearLayout=findViewById(R.id.layout);

                //ImageView imageView=findViewById(R.id.myPlay);
                //linearLayout.removeView(imageView);

                //   imageView.setVisibility(View.VISIBLE);
                // imageView.setImageResource(R.drawable.play);
                //imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                // imageView.requestLayout();
                // LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(2,2);
                //   imageView.setLayoutParams(params);
                // imageView.getLayoutParams().width=2;
                //       imageView.setMaxWidth(5);
                //     imageView.setMaxHeight(5);
                //   imageView.setScaleX(0.9f);
                // imageView.setScaleY(0.9f);

                imageView.setOnClickListener(new View.OnClickListener() {
                    //Why doesn't enter here
                    @Override
                    public void onClick(View v) {
                 /*   Toast.makeText(MapsActivity.this,"OnClick",Toast.LENGTH_LONG).show();
                    if(flag==true)
                    {
                        if(player!=null)
                            // if(player.isPlaying())
                            player.stop();
                        Toast.makeText(MapsActivity.this,"STOP",Toast.LENGTH_LONG).show();
                        flag=false;
                        return;
                    }
                    //if(player!=null)
                    //  if(player.isPlaying())
                    //player.start();
//                int audio=Integer.parseInt("R.raw."+marker.getTitle().toLowerCase());
                    int audio=getResources().getIdentifier(marker.getTitle().toLowerCase(),"raw",getPackageName());

                    player = MediaPlayer.create(MapsActivity.this, audio);
                    Toast.makeText(MapsActivity.this,"Hi",Toast.LENGTH_LONG).show();
                    player.setLooping(false);
                    player.start();
                    flag=true;
               */ }
                });
                //LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                //params.width = 20;
                //imageView.setLayoutParams(params);
                TextView title = new TextView(MapsActivity.this);
                title.setTextColor(Color.BLACK);
                title.setPadding(10,20,20,20);
                //  title.setGravity(Gravity.CENTER_VERTICAL);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());
                horiz.addView(imageView);
                horiz.addView(title);
                TextView snippet = new TextView(MapsActivity.this);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());
                info.addView(horiz);
                //info.addView(imageView);
                //info.addView(title);
                info.addView(snippet);
                //View temp=info;
                //if(imageView!=null)
                //return temp;
                //else{
                //horiz.removeView(imageView);

                //linearLayout.addView(imageView);
                myView=info;
                return info;
                //}
            }
        });
        MarkerOptions markerOptions=new MarkerOptions()
                .position(latLng)
                .title(requestID)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
               // .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.drawable.play, "your text goes here")))
                .snippet(desc+" Play for more info!");
        mMap.addCircle(circleOptions);
        mMap.addMarker(markerOptions);

        //Marker marker=mMap.addMarker(markerOptions);
        //marker.showInfoWindow();
        //if(!marker.isInfoWindowShown())
          //  marker.showInfoWindow();
        //else
          //  marker.hideInfoWindow();
        if(geofenceList!=null && !geofenceList.isEmpty()) {
            startGeofence(geofenceList);
     //   Toast.makeText(MapsActivity.this,"ALA",Toast.LENGTH_LONG).show();
        }// Toast.makeText(MapsActivity.this,"After Creating",Toast.LENGTH_LONG).show();

       mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
           @Override
           public void onInfoWindowClick(Marker marker) {
              // if (marker.isInfoWindowShown())
              //     marker.hideInfoWindow();
               //else
                 //  marker.hideInfoWindow();

               if (flag == 1) {
                   if (player != null && player.isPlaying())
                       // if(player.isPlaying())
                   {
                       player.stop();
//                   imageView.setImageResource(R.drawable.ic_play);
//                       ((ImageView) myView.findViewWithTag("MyImage")).setImageResource(R.drawable.ic_play);
//                   Toast.makeText(MapsActivity.this, "STOP", Toast.LENGTH_LONG).show();

                       flag = 0;
                       marker.showInfoWindow();


                   }

                   return;
               }
               //if(player!=null)
               //  if(player.isPlaying())
               //player.start();
//                int audio=Integer.parseInt("R.raw."+marker.getTitle().toLowerCase());
         else if(flag==0){
             if(player!=null && player.isPlaying())
                 player.stop();
                   int audio = getResources().getIdentifier(marker.getTitle().toLowerCase(), "raw", getPackageName());
                   player = MediaPlayer.create(MapsActivity.this, audio);
                   player.setLooping(false);
                   player.start();
//                   imageView.setImageResource(R.drawable.ic_pause);
//                   ((ImageView) myView.findViewWithTag("MyImage")).setImageResource(R.drawable.ic_pause);

                   flag = 1;
                   marker.showInfoWindow();

               }
else if(player!=null)
    if(flag==-1 && player.isPlaying())
    {
        player.stop();
        int audio = getResources().getIdentifier(marker.getTitle().toLowerCase(), "raw", getPackageName());
        player = MediaPlayer.create(MapsActivity.this, audio);
        player.setLooping(false);
        player.start();
//                   imageView.setImageResource(R.drawable.ic_pause);
//                   ((ImageView) myView.findViewWithTag("MyImage")).setImageResource(R.drawable.ic_pause);
        flag=1;
        marker.showInfoWindow();

    }
else if(player==null && flag==-1)
    {
        int audio = getResources().getIdentifier(marker.getTitle().toLowerCase(), "raw", getPackageName());
        player = MediaPlayer.create(MapsActivity.this, audio);
        player.setLooping(false);
        player.start();
        flag=1;
        marker.showInfoWindow();
    }

               }

        });
               //return geofence;


           }

    private void createWithoutDraw(LatLng latLng,String requestID,float r ){
        Geofence geofence =  new Geofence.Builder()
                .setRequestId(requestID)
                .setCircularRegion(latLng.latitude,latLng.longitude, r)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                )
                .build();
        geofenceList.add(geofence);
     /*   CircleOptions circleOptions = new CircleOptions() .center(latLng)
                .radius(r)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .strokeWidth(5.0f);
        MarkerOptions markerOptions=new MarkerOptions()
                .position(latLng)
                .title(requestID)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                .snippet("Play info!");
        mMap.addCircle(circleOptions);
        mMap.addMarker(markerOptions);
       */
     if(geofenceList!=null && !geofenceList.isEmpty()) {
            startGeofence(geofenceList);
            //   Toast.makeText(MapsActivity.this,"ALA",Toast.LENGTH_LONG).show();
        }// Toast.makeText(MapsActivity.this,"After Creating",Toast.LENGTH_LONG).show();
/*        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                int audio=-1;
                if(marker.getTitle().equals("BlUE"))
                    audio=R.raw.blue;
                else
                if(marker.getTitle().equals("RAMALLAH"))
                    audio=R.raw.ramallah;
                else  if(marker.getTitle().equals("RAFEEDIA"))
                    audio=R.raw.rafedia;
                else  if(marker.getTitle().equals("YA'BAD"))
                    audio=R.raw.yabad;
                else  if(marker.getTitle().equals("HOSPITAL"))
                    audio=R.raw.hospital;
                if(audio!=-1){
                    MediaPlayer player = MediaPlayer.create(MapsActivity.this, audio);
                    player.setLooping(false);
                    player.start();
                }

            }
        });
  */      //return geofence;
    }
    // we use PendingIntent object to call Intent service
    private PendingIntent createGeofencePendingIntent() {
        PendingIntent pendingIntent=null;

        if (geoFencePendingIntent != null) {
            pendingIntent=geoFencePendingIntent;

            return pendingIntent;

        }
                Intent intent = new Intent(MapsActivity.this, MyIntentService.class);
                pendingIntent = PendingIntent.getService(
                        this, 1989, intent,PendingIntent.FLAG_ONE_SHOT
                );
//        Toast.makeText(getApplicationContext(),"AAAAAAAAA",Toast.LENGTH_LONG).show();

        //Toast.makeText(MapsActivity.this,"After calling pending & Service",Toast.LENGTH_LONG).show();




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
   /* private Bitmap writeTextOnDrawable(int drawableId, String text) {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(this, 11));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(this, 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);

        return  bm;
    }



    public static int convertToPixels(Context context, int nDP)
    {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }
*/
//TODO: Add LifeCycle methods to control background


    @Override
    protected void onStop() {
        super.onStop();
        if(player!=null)
        player.stop();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(player!=null)
            player.pause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    /*    if(player!=null)
            player.start();*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if(player!=null)
            player.start();*/

    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
  /*  private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
//        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_map_pin_filled_blue_48dp);
//        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
//        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }*/
}
