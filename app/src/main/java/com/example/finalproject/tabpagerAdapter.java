package com.example.finalproject;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class tabpagerAdapter extends FragmentPagerAdapter {
    String[] tabarray = new String[] {"playlist","album"};
    int tabnumber = 2;

    public tabpagerAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabarray[position];
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                playlist play_list = new playlist();
                return play_list;
            case 1:
                album _album = new album();
                return _album;

        }
        return null;
    }


    @Override
    public int getCount() {
        return tabnumber;
    }
}
