package com.crycetruly.r2d2.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.crycetruly.r2d2.model.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GoogleMethods {
    private static final String TAG = "GoogleMethods";
    public static FirebaseAuth mAuth;

    //--------------------CHECK IF GOOGLE PLAY SERVICES AVAILABILITY------------------------//
    //returns three status codes
    public static boolean isGooglePlayServicesAvailable(Context context, Activity activity) {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(context);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(activity, isAvailable, 0);
            dialog.show();

        } else {
            Toast.makeText(context, "Cant connect to google play services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    //----------CHECKS USERNAME AVAILABILITY------------------------------//
    public static boolean isUsernameAvailable(final String username, final Context context) {
        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("name").getValue().toString().equals(username)) {
                    Toast.makeText(context, "Username is unavailable,its already in use", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return false;
    }

    public boolean checkIfUsernameExists(String username, DataSnapshot datasnapshot) {
        Log.d(TAG, "checkIfUsernameExists: checking if " + username + " already exists.");

        User user = new User();

        for (DataSnapshot ds : datasnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getChildren()) {
            Log.d(TAG, "checkIfUsernameExists: datasnapshot: " + ds);

            user.setUserName(ds.getValue(User.class).getUserName());
            Log.d(TAG, "checkIfUsernameExists: username: " + user.getUserName());

            if (StringManipulation.expandUsername(user.getUserName()).equals(username)) {
                Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + user.getUserName());
                return true;
            }
        }
        return false;
    }

}
