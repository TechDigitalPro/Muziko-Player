package com.whitebird.music.fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.tabs.TabLayout;
import com.whitebird.music.R;
import com.whitebird.music.fragments.homeList.SongsFragment;
import com.whitebird.music.utils.ATEUtils;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.PreferencesUtility;

import java.util.ArrayList;
import java.util.List;

public class Mp3CutterList extends Fragment {

    public ActionBar ab;
    TabLayout tabs;
    Toolbar toolbar;
    private PreferencesUtility mPreferences;
    private ViewPager viewPager;
    private InterstitialAd interstitialAd;
    boolean isActivityIsVisible = true;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferencesUtility.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_mp3cutter_list, container, false);

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_back);
        ab.setDisplayHomeAsUpEnabled(true);


        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
            viewPager.setOffscreenPageLimit(2);
        }

        tabs = (TabLayout) rootView.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        try {
            interstitialAd = new InterstitialAd(getContext());
            interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstial_id));

            AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("8401E8E9D1A58054CC5D94578030EA9E")
                    .build();

            interstitialAd.loadAd(adRequest);
            interstitialAd.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    if (interstitialAd.isLoaded() && isActivityIsVisible) {
                        interstitialAd.show();
                    } else {
                        Log.d("Interstitial", "Interstitial ad was not ready to be shown.");
                    }
                }

                @Override
                public void onAdFailedToLoad(int error_code) {

                }

            });
        }catch (Exception e){
            e.printStackTrace();
        }

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

    }

    private void setupViewPager(ViewPager viewPager) {
        MainFragment.Adapter adapter = new MainFragment.Adapter(getChildFragmentManager());

        SongsFragment fre=new SongsFragment();
         fre.setMp3Frament(true);
        adapter.addFragment(fre, this.getString(R.string.track));
        adapter.addFragment(new RingToneFragment(), "Ringtone");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        isActivityIsVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isActivityIsVisible = true;
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
