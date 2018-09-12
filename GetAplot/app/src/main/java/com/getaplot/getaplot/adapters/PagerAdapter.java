package com.getaplot.getaplot.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.getaplot.getaplot.fragments.home.ChatsFragment;
import com.getaplot.getaplot.fragments.home.EventsTodayFragment;
import com.getaplot.getaplot.fragments.home.InfoHiveFragment;

public class PagerAdapter extends FragmentPagerAdapter {
    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 2:
                return new ChatsFragment();
            case 0:
                return new EventsTodayFragment();


            case 1:
                return new InfoHiveFragment();


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
                return "CHATS";
            default:
                return super.getPageTitle(position);
        }


    }
}
