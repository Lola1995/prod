package com.getaplot.getaplot.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.getaplot.getaplot.fragments.Profile.HomeFragment;
import com.getaplot.getaplot.fragments.Profile.PostsFragment;

public class UsersProfilePagerAdapter extends FragmentPagerAdapter {
    public UsersProfilePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                HomeFragment topUsersFragment = new HomeFragment();
                return topUsersFragment;
            case 1:
                PostsFragment findUsersFragment = new PostsFragment();
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
                return "Home";
            case 1:
                return "Posts";


            default:
                return super.getPageTitle(position);
        }


    }
}
