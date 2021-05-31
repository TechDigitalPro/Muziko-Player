package com.whitebird.music.adapters;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.whitebird.music.fragments.Intro.Intro1;
import com.whitebird.music.fragments.Intro.Intro2;
import com.whitebird.music.fragments.Intro.Intro3;
import com.whitebird.music.fragments.Intro.Intro4;
import com.whitebird.music.fragments.Intro.Intro5;
import com.whitebird.music.fragments.Intro.Intro6;


public class IntroViewPagerAdapter extends FragmentPagerAdapter {

    // Tab Titles
    private final String[] tabTitles = new String[]{"splash1", "splash2", "splash3", "splash4", "splash5", "splash6"};

    public IntroViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return Intro1.newInstance();
            case 1:
                return Intro2.newInstance();
            case 2:
                return Intro3.newInstance();
            case 3:
                return Intro4.newInstance();
            case 4:
                return Intro5.newInstance();
            case 5:
                return Intro6.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        int PAGE_COUNT = 6;
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

}
