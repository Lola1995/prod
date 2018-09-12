package com.crycetruly.r2d2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.crycetruly.r2d2.utils.Handy;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class PlaceActivity extends AppCompatActivity {
    private static final String TAG = "PlaceActivity";
    private static final int STORAGE_REQUEST = 1;
    DatabaseReference subScribedTo;
    ImageView satellite, hybrid, terrain;
    TextView novents;
    private ImageView placeImage;
    private TextView nameText;
    private TextView descriptionText;
    private String place_id;
    private DatabaseReference mDatabaseo, mDatabase;
    private DatabaseReference mCurrentUserDb;
    private FirebaseAuth mAuth;
    private String currentUser, phone, group, district;
    private String category, pname, description, image, email;
    private DatabaseReference rootRef, users;
    private double latitude, longtitude;
    private Context mContext;
    private GoogleMap mGoogleMap;
    private CoordinatorLayout relativeLayout;
    private RecyclerView mEventsList;
    private DatabaseReference eventsDatabase;
    private LinearLayout noevents, loadevents;
    private Query query;
    private ViewPager viewPager;
    private TabLayout tabs;
    private ImageView backImageView;
    private TextView NameTextView;
    private PlaceInfoSectionsPagerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);


        viewPager = findViewById(R.id.mviepager);
        tabs = findViewById(R.id.tabs);

        PlaceInfoSectionsPagerAdapter adapter = new PlaceInfoSectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
        try {
            initWidgets();
        } catch (NullPointerException e) {
        }
        initFirebaseDatabase();
        mContext = PlaceActivity.this;
        rootRef.keepSynced(true);
        mDatabaseo.keepSynced(true);
        Toolbar toolbar = findViewById(R.id.placeToolBar);

        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setTitle(Handy.getTrimmedName(getIntent().getStringExtra("place_name")));
        } catch (NullPointerException e) {

        }
        //nameText.setText(getIntent().getStringExtra("place_name"));
        mDatabaseo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot);
                try {
                    pname = dataSnapshot.child("name").getValue().toString();
                    image = dataSnapshot.child("image").getValue().toString();
                    //nameText.setText(pname);
                    getSupportActionBar().setTitle(Handy.getTrimmedName(pname.toUpperCase()));


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        try {
            final ProgressBar progressBar = findViewById(R.id.pe);

            Toolbar mToolbar = findViewById(R.id.placeToolBar);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        } catch (Exception e) {
            Log.d(TAG, "onCreate: " + e.getMessage());
        }
    }


    private void initFirebaseDatabase() {
        //--------------------------------------FIREBASE------------------------------------------//
        //TODO PULL THIS PLACE ID TO A SAFER PLACE
        place_id = getIntent().getStringExtra("place_id");
        //for the current place
        mDatabaseo = FirebaseDatabase.getInstance().getReference().child("Place_Users").child(place_id);
        //to check if thy r subscribers first
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();
        mCurrentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser);

        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    private void initWidgets() {
        Log.d(TAG, "initWidgets: Wigdets initilised");
        //nameText = findViewById(R.id.placeName);
        mContext = PlaceActivity.this;
        descriptionText = findViewById(R.id.pdescription);
        place_id = getIntent().getStringExtra("place_id");
        descriptionText.setText(getIntent().getStringExtra("description"));

        Glide.with(mContext).load(getIntent().getStringExtra("image_url"))
                .into(placeImage);


    }


    @Override
    protected void onStop() {
        super.onStop();
        mCurrentUserDb.child("online").setValue(ServerValue.TIMESTAMP);
    }




    @Override
    public void onBackPressed() {
        finish();
    }

    public void goback(View view) {
        Log.d(TAG, "goback: ");
        finish();
    }
}
