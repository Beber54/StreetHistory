package com.example.bertr.streethistory;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class MyPagerAdapter extends FragmentPagerAdapter {

    private String tabTitles[] = new String[]{"History", "Reviews", "Contact"};

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        System.out.println("position = " + position);

        switch(position) {
            case 0:
                return new HistoryFragment();

            case 1:
                return new ReviewsFragment();

            case 2:
                return new ContactFragment();

            default:
                return new HistoryFragment();

        }

    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

}
