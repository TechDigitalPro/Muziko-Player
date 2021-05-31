package com.whitebird.music.activities.splash;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.whitebird.music.R;
import com.whitebird.music.activities.BaseActivity;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.adapters.IntroViewPagerAdapter;
import com.whitebird.music.utils.PreferencesUtility;


public class IntroActivity extends BaseActivity {
    private final String TAG = IntroActivity.class.getSimpleName();

    private RelativeLayout footer;
    private int pagerCount = 0;
    private TextView nextText,previewText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        PreferencesUtility.setStatus("INTRO", true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_intro);


        RelativeLayout skipButton = findViewById(R.id.skipButton);
        RelativeLayout nextButton = findViewById(R.id.nextButton);
        RelativeLayout previewButton = findViewById(R.id.previewButton);
        footer = findViewById(R.id.footer);

        nextText = findViewById(R.id.nextText);
        previewText = findViewById(R.id.previewText);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(ContextCompat.getColor(IntroActivity.this, R.color.normal_blue));
//        }

        final ViewPager pager = findViewById(R.id.vpPager);
        pager.setAdapter(new IntroViewPagerAdapter(getSupportFragmentManager()));

        float scale = getResources().getDisplayMetrics().density;
        int padding = (int) (10 * scale + 0.5f);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                switch (position) {
                    case 0:
                        pagerCount = 0;
                        nextText.setText("Next");
                        previewText.setVisibility(View.GONE);
                        break;
                    case 1:
                        pagerCount = 1;
                        nextText.setText("Next");
                        previewText.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        pagerCount = 2;
                        nextText.setText("Next");
                        break;
                    case 3:
                        pagerCount = 3;
                        nextText.setText("Next");
                        break;
                    case 4:
                        pagerCount = 4;
                        nextText.setText("Next");
                        break;
                    case 5:
                        pagerCount = 5;
                        nextText.setText("Got it");

                        break;
                }

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pagerCount != 5) {
                    pager.setCurrentItem(pagerCount + 1);
                } else {

                    Log.e(TAG, "main start");

                    footer.animate()
                            .translationY(footer.getHeight())
                            .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));

                    startActivity(new Intent(IntroActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
        previewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    pager.setCurrentItem(pagerCount - 1);

            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                footer.animate()
                        .translationY(footer.getHeight())
                        .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));

                startActivity(new Intent(IntroActivity.this, MainActivity.class));
                finish();
            }
        });

    }
}
