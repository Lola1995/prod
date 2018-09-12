package com.getaplot.getaplot.fragments.users;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getaplot.getaplot.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyPlaceSettingsFragment extends Fragment {
    private static final String TAG = "MyPlaceSettingsFragment";

    public MyPlaceSettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PlacePrefFragment appSettingsFragment = new PlacePrefFragment();
        fragmentTransaction.add(appSettingsFragment, "SETTINGS FRAGMENT");
        fragmentTransaction.commit();

        return inflater.inflate(R.layout.fragment_my_place_settings, container, false);

    }

    public static class PlacePrefFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.place_prefs);
        }
    }

}
