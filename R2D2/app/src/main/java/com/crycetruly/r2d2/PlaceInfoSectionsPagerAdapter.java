package com.crycetruly.r2d2;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.crycetruly.r2d2.fragments.AboutPlaceFragment;
import com.crycetruly.r2d2.fragments.PlaceEventsFragment;
import com.crycetruly.r2d2.fragments.PlacePostsFragment;


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
            case 1:
                PlaceEventsFragment placeEventsFragment = new PlaceEventsFragment();
                return placeEventsFragment;
            case 0:
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
                return "Plots";
            case 0:
                return "POSTS";
            case 2:
                return "ABOUT";
        }
        return super.getPageTitle(position);
    }
}
