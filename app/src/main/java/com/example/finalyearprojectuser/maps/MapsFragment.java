package com.example.finalyearprojectuser.maps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.finalyearprojectuser.R;
import com.example.finalyearprojectuser.viewinfo.ViewCandidate;
import com.example.finalyearprojectuser.party.ViewParty;
import com.example.finalyearprojectuser.pdf.ViewPDF;
import com.example.finalyearprojectuser.results.ViewRealTimeResults;
import com.example.finalyearprojectuser.vote.CheckStatus;
import com.example.finalyearprojectuser.voter.Profile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class MapsFragment extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private TabLayout tabLayout;
    private Fragment fragment;
    private LinearLayout linearLayout;
    private Intent intent;
    private final int[] ICONS = new int[]{
            R.drawable.ic_baseline_how_to_vote_24,
            R.drawable.ic_baseline_supervised_user_circle_24,
            R.drawable.ic_baseline_flag_circle_24,
            R.drawable.ic_baseline_confirmation_number_24,
            R.drawable.ic_baseline_picture_as_pdf_24,
            R.drawable.ic_baseline_map_24,
            R.drawable.ic_baseline_self_improvement_24
    };

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Marker fixedLocation1;
    Marker fixedLocation2;
    Marker fixedLocation3;
    LocationRequest mLocationRequest;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_map);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        tabLayout  = findViewById(R.id.tabLayout);
        tabLayout.getTabAt(0).setIcon(ICONS[0]);
        tabLayout.getTabAt(1).setIcon(ICONS[1]);
        tabLayout.getTabAt(2).setIcon(ICONS[2]);
        tabLayout.getTabAt(3).setIcon(ICONS[3]);
        tabLayout.getTabAt(4).setIcon(ICONS[4]);
        tabLayout.getTabAt(5).setIcon(ICONS[5]);
        tabLayout.getTabAt(6).setIcon(ICONS[6]);

        //tabLayout On Click
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) { switchCase(tab.getPosition()); }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switchCase(tab.getPosition());
            }
        });
    }

    //tabLayout onClick
    private void switchCase(int position){
        switch (position){
            case 0:
                intent = new Intent(MapsFragment.this, CheckStatus.class);
                break;

            case 1:
                intent = new Intent(MapsFragment.this, ViewCandidate.class);
                break;

            case 2:
                intent = new Intent(MapsFragment.this,ViewParty.class);
                break;

            case 3:
                intent = new Intent(MapsFragment.this, ViewRealTimeResults.class);
                break;

            case 4:
                intent = new Intent(MapsFragment.this, ViewPDF.class);
                break;

            case 5:
                intent = new Intent(MapsFragment.this, MapsFragment.class);
                break;

            case 6:
                intent = new Intent(MapsFragment.this, Profile.class);
                break;
        }
        startActivity(intent);
    }

    //Do if Map Fragment is Open
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    //Google API to Obtain Map Services
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    //Conencted to Google API
    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
    }


    //If the Location Is Changing (Realtime)
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Showing Current Location Marker on Map
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng fixedLatLng1 = new LatLng(3.091938, 101.559515);
        LatLng fixedLatLng2 = new LatLng(3.092361, 101.560011);
        LatLng fixedLatLng3 = new LatLng(3.092958, 101.558922);

        //marker
        MarkerOptions markerOptions = new MarkerOptions();
        MarkerOptions fixedMarker1 = new MarkerOptions();
        MarkerOptions fixedMarker2 = new MarkerOptions();
        MarkerOptions fixedMarker3 = new MarkerOptions();

        //set position
        markerOptions.position(latLng);
        fixedMarker1.position(fixedLatLng1);
        fixedMarker2.position(fixedLatLng2);
        fixedMarker3.position(fixedLatLng3);


        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location locations = locationManager.getLastKnownLocation(provider);
        List<String> providerList = locationManager.getAllProviders();
        if (null != locations && null != providerList && providerList.size() > 0) {
            double longitude = locations.getLongitude();
            double latitude = locations.getLatitude();

            Geocoder geocoder = new Geocoder(getApplicationContext(),
                    Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude,
                        longitude, 1);
                if (null != listAddresses && listAddresses.size() > 0) {
                    String state = listAddresses.get(0).getAdminArea();
                    String country = listAddresses.get(0).getCountryName();
                    String subLocality = listAddresses.get(0).getSubLocality();
                    Log.d("location", "" + latLng + "," + subLocality + "," + state
                            + "," + country);
                    markerOptions.title("" + latLng + "," + subLocality + "," + state
                            + "," + country);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //current location
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

        fixedMarker1.title("Vote Station 1");
        fixedMarker2.title("Vote Station 2");
        fixedMarker3.title("Vote Station 3");
        //mCurrLocationMarker = mMap.addMarker(markerOptions);
        fixedLocation1 = mMap.addMarker(fixedMarker1);
        fixedLocation2 = mMap.addMarker(fixedMarker2);
        fixedLocation3 = mMap.addMarker(fixedMarker3);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                    this);
        }
    }

    //If Connection Failed to Connect
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    //Check Permission Profile
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "permission denied",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    public int getLayoutSize() {
        // Get the layout id
        final LinearLayout root = (LinearLayout) findViewById(R.id.mapLinearLayout);
        final AtomicInteger layoutHeight = new AtomicInteger();

        root.post(new Runnable() {
            public void run() {
                Rect rect = new Rect();
                Window win = getWindow();  // Get the Window
                win.getDecorView().getWindowVisibleDisplayFrame(rect);

                // Get the height of Status Bar
                int statusBarHeight = rect.top;

                // Get the height occupied by the decoration contents
                int contentViewTop = win.findViewById(Window.ID_ANDROID_CONTENT).getTop();

                // Calculate titleBarHeight by deducting statusBarHeight from contentViewTop
                int titleBarHeight = contentViewTop - statusBarHeight;
                Log.i("MY", "titleHeight = " + titleBarHeight + " statusHeight = " + statusBarHeight + " contentViewTop = " + contentViewTop);

                // By now we got the height of titleBar & statusBar
                // Now lets get the screen size
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int screenHeight = metrics.heightPixels;
                int screenWidth = metrics.widthPixels;
                Log.i("MY", "Actual Screen Height = " + screenHeight + " Width = " + screenWidth);

                // Now calculate the height that our layout can be set
                // If you know that your application doesn't have statusBar added, then don't add here also. Same applies to application bar also
                layoutHeight.set(screenHeight - (titleBarHeight + statusBarHeight));
                Log.i("MY", "Layout Height = " + layoutHeight);

                // Lastly, set the height of the layout
                FrameLayout.LayoutParams rootParams = (FrameLayout.LayoutParams)root.getLayoutParams();
                rootParams.height = layoutHeight.get();
                root.setLayoutParams(rootParams);
            }
        });

        return layoutHeight.get();
    }

}