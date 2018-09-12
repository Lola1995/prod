package com.getaplot.getaplot.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.getaplot.getaplot.fragments.place.AboutPlaceFragment;
import com.getaplot.getaplot.fragments.place.PlaceEventsFragment;
import com.getaplot.getaplot.fragments.place.PlacePostsFragment;

/**
 * Created by Elia on 9/8/2017.
 */

public class PlaceInfoSectionsPagerAdapter extends FragmentPagerAdapter {
    public PlaceInfoSectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                PlaceEventsFragment placeEventsFragment = new PlaceEventsFragment();
                return placeEventsFragment;
            case 1:
                PlacePostsFragment placePostsFragment = new PlacePostsFragment();
                return placePostsFragment;
            case 2:
                AboutPlaceFragment aboutPlaceFragment = new AboutPlaceFragment();
                return aboutPlaceFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 1:
                return "POSTS";
            case 0:
                return "PLOTS";
            case 2:
                return "ABOUT";
        }
        return super.getPageTitle(position);
    }
}
