package com.getaplot.getaplot.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.getaplot.getaplot.R;

public class UserInAppNotiSettingsActivity extends AppCompatActivity {
    private static final String TAG = "UserInAppSettingsActivi";
    private String plac, event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_in_app_settings);
        Toolbar toolbar=findViewById(R.id.from);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Options");

        switch (getIntent().getStringExtra("from")) {
            case "place":
                plac = getIntent().getStringExtra("place_id");
                Log.d(TAG, "onCreate: fromplace");
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                AppSettingsFragment appSettingsFragment = new AppSettingsFragment();
                fragmentTransaction.replace(R.id.view, appSettingsFragment);
                fragmentTransaction.commit();

                break;

            case "comments":
                event = getIntent().getStringExtra("event_id");
                Log.d(TAG, "onCreate: fromplace");
                FragmentManager fragmentManager1 = getFragmentManager();
                FragmentTransaction fragmentTransaction1 = fragmentManager1.beginTransaction();
                CommentsSettingsFragment appSettingsFragment1 = new CommentsSettingsFragment();
                fragmentTransaction1.replace(R.id.view, appSettingsFragment1);
                fragmentTransaction1.commit();

                break;


        }
//todo looks like when you turn on notifications for one you have turned off for aall and vicevesa

        //fixme chill awhile n think

    }

    @Override
    public void onStop() {
//maintain user place preferences
        Log.d(TAG, "onStop: Settings good");
        SharedPreferences sharedprefences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isnotion = sharedprefences.getBoolean("nots", false);
        try {
            if (isnotion) {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic(
                        plac);

                Log.d(TAG, "onStop: onnots");

            } else {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic(
                        plac);
                Log.d(TAG, "onStop: offnts");

            }
        } catch (Exception e) {

        }

        SharedPreferences sharedpref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean arecommentson = sharedpref.getBoolean("notscomm", false);

        try {
            if (arecommentson) {

                com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic(
                        event);
                Log.d(TAG, "onStop: commentson");

            } else {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic(
                        event);
                Log.d(TAG, "onStop: commentsoff");

            }
        } catch (Exception e) {
            //ilegal agument for invalid top,null pointer from emty entent
        }

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    //----------------------LOADS IN PLACE PREFERENCES---------------------------//
    public static class AppSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.d(TAG, "onCreate: Place fragment called");
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.place_prefs);
        }

    }

    public static class CommentsSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.d(TAG, "onCreate: Place fragment called");
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.event_prefs);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
