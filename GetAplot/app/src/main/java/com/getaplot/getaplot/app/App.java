package com.getaplot.getaplot.app;

import android.app.Application;
import android.util.Log;

import com.getaplot.getaplot.utils.UserPreferences;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;


public class App extends Application {
    private static final String TAG = "App";
    String user;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        if (FirebaseApp.getApps(this) != null) {
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            } catch (IllegalStateException e) {


            }


        }
        try {
            mAuth = FirebaseAuth.getInstance();


            if (mAuth.getCurrentUser() != null) {
                user = mAuth.getCurrentUser().getUid();
                mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user);
            }


            //--------------------------listen for connections to monitor logged in users---------------------//
            if (mAuth.getCurrentUser() != null) {
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            mDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                        } else {
                            mDatabase.child("online").onDisconnect().setValue("true");
                        }

                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            //--------------------TURNING NOTICATIONS ON/OFF------------------------------
            try {
                UserPreferences.HandleNotificationsUserPreferences(this);
            } catch (NullPointerException ex) {
                Log.d(TAG, "onCreate: NullPointerException :" + ex.getLocalizedMessage());
            }
        }

    catch (NullPointerException e) {
        Log.d(TAG, "onCreate: ");
    }
    }
}
