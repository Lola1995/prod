package com.getaplot.getaplot.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.InflateException;
import android.view.Menu;
import android.view.MenuItem;

import com.getaplot.getaplot.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;

public class LocationActivity extends AppCompatActivity {
    private static final String TAG = "LocationActivity";
    private String pname, description;
    private double longtitude, latitude;
    private Context context;
    private GoogleMap mGoogleMap;
    private DatabaseReference mDatabase;
    private Toolbar toolbar;
    private LatLng latLng;

    public LocationActivity() throws IOException, IndexOutOfBoundsException, InflateException, OutOfMemoryError, NullPointerException {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        Toolbar toolbar = findViewById(R.id.mapbar);
        setSupportActionBar(toolbar);
        context = LocationActivity.this;

        longtitude = Double.parseDouble(getIntent().getStringExtra("longtitude"));
        latitude = Double.parseDouble(getIntent().getStringExtra("latitude"));
        Log.d(TAG, "onCreate: " + latitude);
        Log.d(TAG, "onCreate: " + longtitude);
        pname = "My Location";
        description = "Am CHILLING OUT HERE";
        initMap(latitude, longtitude, pname, description);


    }


    private void initMap(final double latitude, final double longtitude, final String pname, final String description) {
        Log.d(TAG, "initMap: cords" + latitude + " " + longtitude);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                MarkerOptions options = new MarkerOptions()
                        .title(pname)
                        .visible(true).snippet(description)
                        .position(new LatLng(latitude, longtitude));
                mGoogleMap.addMarker(options);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                } else {

                }
                mGoogleMap.setMyLocationEnabled(true);
                goToLocationZoom(latitude, longtitude, 15);

            }
        });
    }


    private void goToLocationZoom(double latitude, double longtitude, float zoom) {
        LatLng ll = new LatLng(latitude, longtitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(cameraUpdate);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.location_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.satellite) {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (id == R.id.hybrid) {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (id == R.id.terrain) {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else if (id == R.id.home) {
            startActivity(new Intent(context, MainActivity.class));
        } else if (id == R.id.normal) {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        } else if (id == R.id.back) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        try {
            Intent intent = new Intent(context, EventCommentsActivity.class);
            intent.putExtra("event_id", getIntent().getStringExtra("event_id"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } catch (NullPointerException e) {

        }
        super.onBackPressed();
    }

}
