package com.whitebird.music.fragments.AllActivity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.google.android.material.tabs.TabLayout;
import com.whitebird.music.R;
import com.whitebird.music.utils.ATEUtils;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.PreferencesUtility;

import java.util.ArrayList;
import java.util.List;

public class AllActivityFragment extends Fragment {

    private PreferencesUtility mPreferences;
    private ViewPager viewPager;
    TabLayout tabs;
    public ActionBar ab;
    Toolbar toolbar;
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferencesUtility.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_activity, container, false);

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_back);
        ab.setDisplayHomeAsUpEnabled(true);



        viewPager =  rootView.findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
            viewPager.setOffscreenPageLimit(2);
        }
        tabs = (TabLayout) rootView.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        return rootView;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("dark_theme", false)) {
            ATE.apply(this, "dark_theme");
        } else {
            ATE.apply(this, "light_theme");
        }
        viewPager.setCurrentItem(mPreferences.getStartPageIndex());
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new RecentFre(), "RECENT");
        adapter.addFragment(new MostPLayedFre(), "MOST");
        adapter.addFragment(new NewFre(),"NEW");
//        adapter.addFragment(new ArtistFragment(), this.getString(R.string.artists));
//        adapter.addFragment(new GenreFragment() ,this.getString(R.string.genres));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPreferences.lastOpenedIsStartPagePreference()) {
            mPreferences.setStartPageIndex(viewPager.getCurrentItem());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helpers.getATEKey(getActivity());
        ATEUtils.setStatusBarColor(getActivity(), ateKey, Config.primaryColor(getActivity(), ateKey));

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
