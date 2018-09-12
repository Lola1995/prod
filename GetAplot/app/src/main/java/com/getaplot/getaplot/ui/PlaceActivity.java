package com.getaplot.getaplot.ui;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.adapters.PlaceInfoSectionsPagerAdapter;
import com.getaplot.getaplot.utils.Handy;
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

        Log.d(TAG, "onCreate: all intent has "+getIntent().getStringExtra("from"));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.place_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.map) {
            Intent i = new Intent(mContext, MapActivity.class);
            i.putExtra("place_id", place_id);
            startActivity(i);

        }

        if (id == R.id.message) {
            Intent i = new Intent(mContext, ChatActivity.class);
            i.putExtra("user_id", place_id);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);

        }


        if (id == R.id.notioff) {
            Intent i = new Intent(mContext, UserInAppNotiSettingsActivity.class);
            i.putExtra("place_id", place_id);
            i.putExtra("from", "place");
            startActivity(i);

        }


        if (id == android.R.id.home) {
            if (getIntent().hasExtra("fromPlace")){
                startActivity(new Intent(this,MainActivity.class));
            }else {
                finish();
            }

        }
        if (id == R.id.shortcut) {


            previousTry();

        }

        return super.onOptionsItemSelected(item);


    }

    private void previousTry() {


        //Code Snippet to Add shortcut Icon.
        Intent shortcutAdd = new Intent(getApplicationContext(), PlaceActivity.class);
        //Instead of MainActivity you can add any activity from you application.
        shortcutAdd.setAction(Intent.ACTION_MAIN);
        shortcutAdd.putExtra("place_id",getIntent().getStringExtra("place_id"));
        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutAdd);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, pname);
        addIntent.putExtra("duplicate", false);
        addIntent.putExtra("from", "place");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher_foreground));
        //Above Line is for your application Shortcut Icon.
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);
        Toast.makeText(mContext, "Place shortcut created on homescreen", Toast.LENGTH_SHORT).show();
    }


    void returnShortcut(int rowId, String shortcutName) {
        Intent i = new Intent(this, PlaceActivity.class);
        i.setData(ContentUris.withAppendedId(Uri.parse("COM.GETAPLOT.ASGETAPLOT"), rowId));
        Intent shortcut = new Intent();
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
        setResult(RESULT_OK, shortcut);
        finish();
    }



//   todo remove a shortcut private void removeShortcutIcon() {
////Code Snippet to Remove shortcut Icon.
//        Intent shortcutRem = new Intent(getApplicationContext(), MainActivity.class);
//        shortcutRem.setAction(Intent.ACTION_MAIN);
//
//        Intent addIntent = new Intent();
//        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutRem);
//        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Shortcut");
//
//        addIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
//        getApplicationContext().sendBroadcast(addIntent);
//    }

    @Override
    public void onBackPressed() {
        if (getIntent().hasExtra("from")){
            startActivity(new Intent(this,MainActivity.class));
        }else {
            finish();
        }
    }

    public void goback(View view) {
        Log.d(TAG, "goback: ");
        finish();
    }
}
