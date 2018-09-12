package com.getaplot.getaplot.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.getaplot.getaplot.fragments.requests.RecievedRequestsFragment;
import com.getaplot.getaplot.fragments.requests.SentRequestsFragment;

/**
 * Created by Elia on 9/8/2017.
 */

public class RequestPagerAdapter extends FragmentPagerAdapter {
    public RequestPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                RecievedRequestsFragment recievedRequestsFragment = new RecievedRequestsFragment();
                return recievedRequestsFragment;
            case 1:
                SentRequestsFragment sentRequestsFragment = new SentRequestsFragment();
                return sentRequestsFragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "RECIEVED REQUESTS";
            case 1:
                return "SENT REQUESTS";
        }
        return super.getPageTitle(position);
    }
}
