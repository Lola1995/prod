package com.getaplot.getaplot.ui.settings;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.getaplot.getaplot.R;

public class ApsActivity extends AppCompatActivity {
    private static final String TAG = "ApsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aps);

        FragmentManager fragmentManager1 = getFragmentManager();
        FragmentTransaction fragmentTransaction1 = fragmentManager1.beginTransaction();
        AppSettingsFragment appSettingsFragment1 = new AppSettingsFragment();
        fragmentTransaction1.replace(R.id.view, appSettingsFragment1);
        fragmentTransaction1.commit();

    }


    //----------------------LOADS IN PLACE PREFERENCES---------------------------//
    public static class AppSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.d(TAG, "onCreate: Place fragment called");
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.main);
        }

    }

}
