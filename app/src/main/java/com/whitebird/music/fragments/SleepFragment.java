package com.whitebird.music.fragments;

import android.annotation.TargetApi;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.fragment.app.Fragment;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.control.WheelPicker.WheelPicker;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.PreferencesUtility;
import com.whitebird.music.utils.Utils;

public class SleepFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private FrameLayout set10Button;
    private FrameLayout set20Button;
    private FrameLayout set30Button;
    private FrameLayout set40Button;
    private FrameLayout set50Button;
    private FrameLayout set60Button;
    private MaterialDialog addDialog = null;
    private WheelPicker wheelHours;
    private WheelPicker wheelMinutes;
    private RelativeLayout startButton;
    private ImageButton addButton;
    private CheckBox playlastsong;
    private CoordinatorLayout coordinatorlayout;
    private ProgressBar progressBar;
    private TextView timerText;
    private TextView startText;


    private static int timerIncrement;
    private Toolbar toolbar;

    private InterstitialAd interstitialAd;
    boolean isActivityIsVisible = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @TargetApi(21)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(
                R.layout.fragment_sleep, container, false);
        findViewsById(rootView);

        startButton.setOnClickListener(this);
        set10Button.setOnClickListener(this);
        set20Button.setOnClickListener(this);
        set30Button.setOnClickListener(this);
        set40Button.setOnClickListener(this);
        set50Button.setOnClickListener(this);
        set60Button.setOnClickListener(this);
        timerText.setOnClickListener(this);
        addButton.setOnClickListener(this);

        playlastsong.setOnCheckedChangeListener(this);


        timerIncrement = (int) MuzikoApp.SLEEP_TIMER;
      //  progressBar.setMax(timerIncrement);
//        progressBar.setProgress(0);
        if (MuzikoApp.SLEEP_TIMER > 0) {
            setTimer();
            //progressBar.setMax((int) (MuzikoApp.SLEEP_TIMER - System.currentTimeMillis()));
            progressBar.setMax((int) MAX);
            progressBar.setProgress((int) (System.currentTimeMillis() - MuzikoApp.START_TIME));

        }
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

    private void setTimer() {
        if (PreferencesUtility.getInstance(getActivity()).isTimer())
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (MuzikoApp.SLEEP_TIMER > 0) {
                    progressBar.setProgress((int) (System.currentTimeMillis() - MuzikoApp.START_TIME));
                    timerText.setText(Utils.convertMillisecondstoDuration(MuzikoApp.SLEEP_TIMER - System.currentTimeMillis()));
                    setTimer();
                }else if (MuzikoApp.SLEEP_TIMER <= 0){
                    startButton.setBackgroundColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
                    startText.setText("START");
                }

            }
        }, 1000);
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
        register();
    }

    @Override
    public void onDestroy() {
        isActivityIsVisible = false;
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void setupToolbar() {

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Sleep Timer");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.isPressHome = true;
                getActivity().onBackPressed();
            }
        });

    }


    private void findViewsById(View view) {
        timerText = view.findViewById(R.id.timerText);
        progressBar = view.findViewById(R.id.progressBar);

        if (progressBar != null) {

            progressBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())), PorterDuff.Mode.SRC_IN));
            LayerDrawable progressBarDrawable = (LayerDrawable) progressBar.getProgressDrawable();
            Drawable backgroundDrawable = progressBarDrawable.getDrawable(1);
            Drawable progressDrawable = progressBarDrawable.getDrawable(0);
            backgroundDrawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.gray), PorterDuff.Mode.SRC_IN);
            progressDrawable.setColorFilter(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())), PorterDuff.Mode.SRC_IN);
        }

        set10Button = view.findViewById(R.id.set10Button);
        set20Button = view.findViewById(R.id.set20Button);
        set30Button = view.findViewById(R.id.set30Button);
        set40Button = view.findViewById(R.id.set40Button);
        set50Button = view.findViewById(R.id.set50Button);
        set60Button = view.findViewById(R.id.set60Button);
        startText = view.findViewById(R.id.startText);
        startButton = view.findViewById(R.id.startButton);
        addButton = view.findViewById(R.id.addButton);
        playlastsong = view.findViewById(R.id.playlastsong);

        if (PreferencesUtility.getInstance(getActivity()).isTimer()) {
            startButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.redColor));
            startText.setText("STOP");
        } else {
            startButton.setBackgroundColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
            startText.setText("START");
        }

         playlastsong.setChecked(PreferencesUtility.getInstance(getActivity()).getSleepTimeLastSong());
       // startButton.setBackgroundColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));


        if (Build.VERSION.SDK_INT < 21) {
            CompoundButtonCompat.setButtonTintList(playlastsong, ColorStateList.valueOf(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity()))));//Use android.support.v4.widget.CompoundButtonCompat when necessary else
        } else {
            playlastsong.setButtonTintList(ColorStateList.valueOf(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity()))));//setButtonTintList is accessible directly on API>19
        }
        addButton.setColorFilter(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));

        toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setupToolbar();
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.set10Button:
                timerIncrement = 600;
                timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
                progressBar.setMax(timerIncrement);
                progressBar.setProgress(0);
                updateTimer();
                break;
            case R.id.set20Button:
                timerIncrement = 1200;
                timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
                progressBar.setMax(timerIncrement);
                progressBar.setProgress(0);
                updateTimer();
                break;
            case R.id.set30Button:
                timerIncrement = 1800;
                timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
                progressBar.setMax(timerIncrement);
                progressBar.setProgress(0);
                updateTimer();
                break;
            case R.id.set40Button:
                timerIncrement = 2400;
                timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
                progressBar.setMax(timerIncrement);
                progressBar.setProgress(0);
                updateTimer();
                break;
            case R.id.set50Button:
                timerIncrement = 3000;
                timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
                progressBar.setMax(timerIncrement);
                progressBar.setProgress(0);
                updateTimer();
                break;
            case R.id.set60Button:
                timerIncrement = 3600;
                timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
                progressBar.setMax(timerIncrement);
                progressBar.setProgress(0);
                updateTimer();
                break;

            case R.id.set70Button:
                addDialog.dismiss();
                timerIncrement = 4200;
                timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
                progressBar.setMax(timerIncrement);
                progressBar.setProgress(0);
                updateTimer();
                break;

            case R.id.set80Button:
                addDialog.dismiss();
                timerIncrement = 4800;
                timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
                progressBar.setMax(timerIncrement);
                progressBar.setProgress(0);
                updateTimer();
                break;

            case R.id.set90Button:
                addDialog.dismiss();
                timerIncrement = 5400;
                timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
                progressBar.setMax(timerIncrement);
                progressBar.setProgress(0);
                updateTimer();
                break;

            case R.id.set100Button:
                addDialog.dismiss();
                timerIncrement = 6000;
                timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
                progressBar.setMax(timerIncrement);
                progressBar.setProgress(0);
                updateTimer();
                break;

            case R.id.set110Button:
                addDialog.dismiss();
                timerIncrement = 6600;
                timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
                progressBar.setMax(timerIncrement);
                progressBar.setProgress(0);
                updateTimer();
                break;

            case R.id.set120Button:
                addDialog.dismiss();
                timerIncrement = 7200;
                timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
                progressBar.setMax(timerIncrement);
                progressBar.setProgress(0);
                updateTimer();
                break;

            case R.id.timerText:
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                int selectedHours = hourOfDay * 3600;
                                int selectedMins = minute * 60;
                                timerIncrement = selectedHours + selectedMins;
                                timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
                                progressBar.setMax(timerIncrement);
                                progressBar.setProgress(0);
                                updateTimer();
                            }
                        }, 0, 0, true);


                timePickerDialog.show();
               /* long hours = 0;
                long minutes;
                int seconds = timerIncrement;
                if (seconds > 3600) {
                    hours = seconds / 3600;
                    minutes = (seconds % 3600) / 60;
                } else {
                    minutes = seconds / 60;
                }

                ArrayList<String> hoursList = new ArrayList<>();
                for (int a = 0; a < 24; a++) {
                    hoursList.add(String.format("%02d", a));
                }

                ArrayList<String> minutesList = new ArrayList<>();
                for (int a = 0; a < 60; a++) {
                    minutesList.add(String.format("%02d", a));
                }

                ArrayList<String> middle = new ArrayList<>();
                middle.add(":");

                View customView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_sleep_custom, null, false);
                wheelHours = customView.findViewById(R.id.wheelHours);
                wheelHours.setData(hoursList);
                wheelHours.setSelectedItemPosition((int) hours);
                wheelMinutes = customView.findViewById(R.id.wheelMinutes);
                wheelMinutes.setData(minutesList);
                wheelMinutes.setSelectedItemPosition((int) minutes);
                WheelPicker wheelMiddle = customView.findViewById(R.id.wheelMiddle);
                wheelMiddle.setData(middle);

                MaterialDialog customDialog = new MaterialDialog.Builder(getActivity())
                        .theme(Theme.LIGHT)
                        .customView(customView, false)
                        .positiveText("DONE").onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                int selectedHours = wheelHours.getCurrentItemPosition() * 3600;
                                int selectedMins = wheelMinutes.getCurrentItemPosition() * 60;
                                timerIncrement = selectedHours + selectedMins;
                                timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement * 1000));
                                progressBar.setMax(timerIncrement);
                                progressBar.setProgress(0);
                                updateTimer();
                            }
                        }).negativeText("Cancel")
                        .build();
                customDialog.show();*/
                break;

            case R.id.addButton:

                View addView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_sleep_extra, null, false);
                FrameLayout set70Button = addView.findViewById(R.id.set70Button);
                FrameLayout set80Button = addView.findViewById(R.id.set80Button);
                FrameLayout set90Button = addView.findViewById(R.id.set90Button);
                FrameLayout set100Button = addView.findViewById(R.id.set100Button);
                FrameLayout set110Button = addView.findViewById(R.id.set110Button);
                FrameLayout set120Button = addView.findViewById(R.id.set120Button);

                set70Button.setOnClickListener(this);
                set80Button.setOnClickListener(this);
                set90Button.setOnClickListener(this);
                set100Button.setOnClickListener(this);
                set110Button.setOnClickListener(this);
                set120Button.setOnClickListener(this);

                addDialog = new MaterialDialog.Builder(getActivity())
                        .theme(Theme.LIGHT)
                        .customView(addView, false)
                        .build();
                addDialog.show();

                break;

            case R.id.startButton:

            {
                if (startText.getText().equals("START")) {
                    if (timerIncrement==0){

                        return;
                    }
                    startText.setText("STOP");
                    v.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.redColor));
                    MuzikoApp.SLEEP_TIMER = System.currentTimeMillis() + timerIncrement * 1000;
                    MuzikoApp.START_TIME = System.currentTimeMillis();

                    progressBar.setMax((int) (MuzikoApp.SLEEP_TIMER - System.currentTimeMillis()));
                    MAX=progressBar.getMax();
                    PreferencesUtility.getInstance(getActivity()).setTimer(true);
                    setTimer();
                    MuzikoApp.checkTime();
                } else {
                    startText.setText("START");
                    v.setBackgroundColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
                    MuzikoApp.SLEEP_TIMER = 0;
                    MuzikoApp.START_TIME = 0;
                    PreferencesUtility.getInstance(getActivity()).setTimer(false);
                    timerIncrement=0;
                    progressBar.setProgress(0);
                    timerText.setText(Utils.convertMillisecondstoDuration(timerIncrement));

                }
            }

            break;
        }

    }
private static long MAX;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


    }


    private void updateTimer() {
        if (startText.getText().equals("STOP")) {
            MuzikoApp.SLEEP_TIMER = System.currentTimeMillis() + timerIncrement * 1000;
            MuzikoApp.START_TIME = System.currentTimeMillis();
            progressBar.setMax((int) (MuzikoApp.SLEEP_TIMER - System.currentTimeMillis()));
            PreferencesUtility.getInstance(getActivity()).setTimer(true);
        }
    }

    private void register() {
       /* receiver = new stopTimerReceiver();
        IntentFilter timerfilter = new IntentFilter(SLEEP_TIMER_STOP);
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppController.INTENT_EXIT);
        filter.addAction(AppController.INTENT_CLEAR);
        filter.addAction(AppController.INTENT_TRACK_EDITED);
        filter.addAction(AppController.INTENT_TRACK_SEEKED);
        filter.addAction(AppController.INTENT_QUEUE_STOPPED);
        filter.addAction(AppController.INTENT_TRACK_SHUFFLE);
        filter.addAction(AppController.INTENT_TRACK_REPEAT);
        filter.addAction(AppController.INTENT_QUEUE_CHANGED);
        filter.addAction(AppController.INTENT_QUEUE_CLEARED);
        mainReceiver = new MainReceiver();
       registerReceiver(mainReceiver, filter);*/
    }

  /*  private void unregister() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }

        if (mainReceiver != null) {
            unregisterReceiver(mainReceiver);
            mainReceiver = null;
        }
    }*/

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            PreferencesUtility.getInstance(getActivity()).setSleepTimeLastSong(true);
        } else {
            PreferencesUtility.getInstance(getActivity()).setSleepTimeLastSong(false);
        }
    }

/*    public class stopTimerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(SLEEP_TIMER_STOP)) {

                if (intent.getLongExtra(ACTION_SLEEP_TIME, 0) > 0) {
                    long progress = intent.getLongExtra(ACTION_SLEEP_TIME, 0);
                    progressBar.setProgress((int) (timerIncrement - progress / 1000));
                    timerText.setText(Utils.convertMillisecondstoDuration(progress));
                    startText.setText(R.string.STOP);
                    startButton.setBackgroundColor(ContextCompat.getColor(SleepActivity.this, R.color.redColor));
                } else if (intent.getStringExtra(ACTION_SLEEP_STOP).equals("1")) {
                    AppController.toast(SleepActivity.this, "Sleep Timer Stoped Player");
                    startText.setText(R.string.START);
                    startButton.setBackgroundColor(ContextCompat.getColor(SleepActivity.this, R.color.normal_blue));

                }
            }
        }
    }*/

}