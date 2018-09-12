package com.crycetruly.r2d2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.crycetruly.r2d2.utils.Handy;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";
    private String pname, description;
    private double longtitude, latitude;
    private Context context;
    private GoogleMap mGoogleMap;
    private DatabaseReference mDatabase;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        context = MapActivity.this;
        toolbar = findViewById(R.id.mapbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Place_Users")
                .child(getIntent().getStringExtra("place_id"));
        mDatabase.keepSynced(true);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Log.d(TAG, "onDataChange: " + dataSnapshot);
                    pname = dataSnapshot.child("name").getValue().toString();
                    description = dataSnapshot.child("description").getValue().toString();
                    latitude = Double.parseDouble((String) dataSnapshot.child("latitude").getValue());
                    Log.d(TAG, "onDataChange: latitude" + latitude);
                    longtitude = Double.parseDouble((String) dataSnapshot.child("longtitude").getValue());
                    Log.d(TAG, "onDataChange: " + longtitude);
                    initMap(latitude, longtitude, pname, description);
                    getSupportActionBar().setTitle(Handy.getTrimmedName(pname));
                } catch (Exception e) {
                    Log.d(TAG, "onDataChange: " + e.getMessage());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void initMap(final double latitude, final double longtitude, final String pname, final String description) {
        Log.d(TAG, "initMap: cords" + latitude + longtitude);
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

                goToLocationZoom(latitude, longtitude, 15);

            }
        });
    }


    private void goToLocationZoom(double latitude, double longtitude, float zoom) {
        Log.d(TAG, "goToLocationZoom: ");
        LatLng ll = new LatLng(latitude, longtitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(cameraUpdate);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (mGoogleMap!=null) {
           if (id == android.R.id.home) {
                finish();
            }
        }else {
            Toast.makeText(context, "Error loading maps", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
