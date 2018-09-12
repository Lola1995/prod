package com.getaplot.getaplot.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.getaplot.getaplot.fragments.users.FindUsersFragment;
import com.getaplot.getaplot.fragments.users.TopUsersFragment;

public class UsersPagerAdapter extends FragmentPagerAdapter {
    public UsersPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                TopUsersFragment topUsersFragment = new TopUsersFragment();
                return topUsersFragment;
            case 1:
                FindUsersFragment findUsersFragment = new FindUsersFragment();
                return findUsersFragment;


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
                return "top users";
            case 1:
                return "Find People";


            default:
                return super.getPageTitle(position);
        }


    }
}
