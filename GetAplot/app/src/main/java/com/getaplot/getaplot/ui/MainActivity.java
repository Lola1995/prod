package com.getaplot.getaplot.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.getaplot.getaplot.BuildConfig;
import com.getaplot.getaplot.FavoritePlacesActivity;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.adapters.PagerAdapter;
import com.getaplot.getaplot.ui.settings.AllSettingsActivity;
import com.getaplot.getaplot.ui.users.UsersActivity;
import com.getaplot.getaplot.utils.GoogleMethods;
import com.getaplot.getaplot.utils.Handy;
import com.getaplot.getaplot.utils.Location;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;

import static com.getaplot.getaplot.utils.Location.LOCATION_PERMISSION_REQUEST_CODE;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int ACTIVITY_NUM = 2;
    private static final int RS_SIGN_IN = 1;
    private static final int REQUEST_PERMISSION = 10;
    public Boolean mLocationPermissionsGranted = false;
    private FirebaseAuth mAuth;
    private ViewPager tabsPager;
    protected PagerAdapter pagerAdapter;
    private TabLayout mtablayout;
    private DatabaseReference mUserRef;
    private Context mContext = MainActivity.this;
    private CoordinatorLayout main_rel_layout;
    private FirebaseAuth.AuthStateListener authStateListener;
    private android.support.v7.widget.Toolbar toolbar;
    private TabLayout appBarLayout;
    FloatingActionButton fab_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (GoogleMethods.isGooglePlayServicesAvailable(this, this)) {
            Log.d(TAG, "onCreate: Google play services Available");

        } else {
            setContentView(R.layout.activity_place_maps_not_supported);

        }
        Log.d(TAG, "onCreate: started on xreate");
        
        

        //----------------------------AUTHENTICATION --------------------------------------//
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) { 
try {
    startActivityForResult(
            AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(
                            Arrays.asList(
                                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build())
                    ).setIsSmartLockEnabled(!BuildConfig.DEBUG)

                    .build(),
            RS_SIGN_IN);
}catch (NullPointerException e){
    Log.d(TAG, "onCreate: "+e.getMessage());
}

        } else {


            DatabaseReference d = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            d.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        String Name = dataSnapshot.child("name").getValue().toString();
                        if (Name.equals("Change Me")) {
                            Intent i = new Intent(mContext, EditProfileActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);


                            Toast.makeText(mContext, "You need to add your display name", Toast.LENGTH_LONG).show();
                            startActivity(i);
                            finish();

                        }
                    } catch (NullPointerException e) {

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            DatabaseReference userPoints = FirebaseDatabase.getInstance().getReference().child("userPoints")
                    .child(mAuth.getCurrentUser().getUid());
            userPoints.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        try {
                            double points = Double.parseDouble(dataSnapshot.child("points").getValue().toString());

                            Log.d(TAG, "onDataChange: POINTS NOW " + points);
                        } catch (NumberFormatException e) {

                        }
                    } else {
                        Log.d(TAG, "onDataChange::NO POINTS YET");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            setContentView(R.layout.activity_main);
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(mAuth.getCurrentUser().getUid());


            main_rel_layout = findViewById(R.id.main_rel_layout);
            tabsPager = findViewById(R.id.main_tabPager);
            pagerAdapter = new PagerAdapter(getSupportFragmentManager());
            mtablayout = findViewById(R.id.main_tabs);
            fab_add=findViewById(R.id.fab_add);
            tabsPager.setAdapter(pagerAdapter);
            fab_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = "";
                    switch (tabsPager.getCurrentItem()) {
                        case 0:
                            startActivity(new Intent(getBaseContext(), SerchActivity.class));
                            break;

                        case 2:

                            startActivity(new Intent(getBaseContext(), NewMessageActivity.class));
                            break;
                        default:
                            return;
                    }
                }
            });


            mtablayout.setupWithViewPager(tabsPager);
            tabsPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    changeFabIcon(position);
                    switch (position){
                        case 0:
                            getSupportActionBar().setTitle("PLOTS");
                            break;

                        case 1:
                            getSupportActionBar().setTitle("POSTS");
                            break;
                        case 2:
                            getSupportActionBar().setTitle("CHATS");
                            break;

                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
           mUserRef.child("device_token").setValue(FirebaseInstanceId.getInstance().getToken()).addOnSuccessListener(new OnSuccessListener<Void>() {
               @Override
               public void onSuccess(Void aVoid) {
                   Log.d(TAG, "onSuccess: token updated");
               }
           });
            setUpToolbar();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Location location = new Location();
                    location.getLocationPermission(MainActivity.this, MainActivity.this);

                    Location.getDeviceLocation(MainActivity.this, MainActivity.this);
                }
            });
            thread.start();

        }

    }



    private void setUpToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_home);



    }


    //firebase

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RS_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            Log.d(TAG, "onActivityResult: response" + response);
            if (resultCode == RESULT_OK) {
                Intent i = new Intent(MainActivity.this, FinishActivity.class);
                startActivity(i);
                finish();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(mContext, "cancelled", Toast.LENGTH_SHORT).show();
                    //Snackbar.make(main_rel_layout,R.string.sign_in_cancelled,Snackbar.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    // Snackbar.make(main_rel_layout,R.string.no_internet_connection,Snackbar.LENGTH_SHORT).show();
                    Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Connect to the Internet,The first time GetAplot runs You need to be connected to the internet").setTitle("Not connected")
                            .setCancelable(false)
                            .setPositiveButton("Turn on Internet", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                                }
                            })
                            .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    finish();
                                    return;
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    // Snackbar.make(main_rel_layout,R.string.unknown_error,Snackbar.LENGTH_SHORT).show();
                    Toast.makeText(mContext, "Unknown Response", Toast.LENGTH_SHORT).show();

                    finish();
                    return;
                }
            }


        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                }
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart:code "+ Handy.getVersionCode(getApplicationContext()));

        //--------------------------------------------
        try {
            DatabaseReference usernow = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            if (mAuth.getCurrentUser() != null) {
                usernow.child("online").setValue("true");


                //--------------------------------------------
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "onStop: user null");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            DatabaseReference usernow = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(mAuth.getCurrentUser().getUid());
            if (mAuth.getCurrentUser() != null) {
                usernow.child("online").setValue(ServerValue.TIMESTAMP);
                usernow.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                //--------------------------------------------
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "onStop: user null");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fav:
                startActivity(new Intent(mContext, FavoritePlacesActivity.class));
                break;
            case R.id.places:
                startActivity(new Intent(mContext, PlacesActivity.class));
                break;
            case R.id.add_user:
                startActivity(new Intent(mContext, UsersActivity.class));
                break;
            case R.id.setss:
                startActivity(new Intent(mContext, AllSettingsActivity.class));
                break;

            case R.id.requests:
                startActivity(new Intent(mContext, RequestsActivity.class));
                break;
            case R.id.share:
                startActivity(new Intent(mContext, ChangeStatusActivity.class));
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        finish();

    }

    public void sendNewMessage(View view) {
        startActivity(new Intent(mContext, NewMessageActivity.class));
    }

    public void gotosearchplaces(View view) {
        startActivity(new Intent(mContext, PlacesActivity.class));
    }
    private void changeFabIcon(final int index) {
        fab_add.hide();
        if (index==1) {
            fab_add.setVisibility(View.GONE);


        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (index) {
                    case 0: {
                        fab_add.setImageResource(R.drawable.ic_searc);
                        break;
                    }
                    case 2: {
                        fab_add.setImageResource(R.drawable.ic_add_white);
                        break;
                    }
                }
                fab_add.show();
            }
        }, 400);
    }


}
