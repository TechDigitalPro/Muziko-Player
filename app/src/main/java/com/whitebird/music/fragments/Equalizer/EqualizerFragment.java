package com.whitebird.music.fragments.Equalizer;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.afollestad.appthemeengine.Config;
import com.db.chart.model.LineSet;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.whitebird.music.MusicPlayer;

import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.custom.AnalogController;
import com.whitebird.music.models.EqualizerModel;
import com.whitebird.music.utils.Helpers;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class EqualizerFragment extends Fragment {

    public static Visualizer mVisualizer;
    public static short reverbPreset = -1;
    public static short bassStrength = -1;
    public static Equalizer mEqualizer;
    public static int presetPos;
    public static EqualizerModel equalizerModel;
    public static int[] seekbarpos = new int[5];
    public static BassBoost bassBoost;
    public static PresetReverb presetReverb;
    public static boolean isEqualizerEnabled = false;
    static FrameLayout equalizerBlocker;
    ImageView backBtn;
    TextView fragTitle;
    SwitchCompat equalizerSwitch;
    LineSet dataset;
    LineChartView chart;
    Paint paint;
    float[] points;
    int y = 0;
    ImageView spinnerDropDownIcon;
    short numberOfFrequencyBands;
    LinearLayout mLinearLayout;
    SeekBar[] seekBarFinal = new SeekBar[5];
    AnalogController bassController, reverbController;
    Spinner presetSpinner;
    Context ctx;
    onCheckChangedListener mCallback;
    boolean isEqualizerReloaded;

    public EqualizerFragment() {
        // Required empty public constructor
    }

    public static void setBlockerVisibility(int visibility) {
        equalizerBlocker.setVisibility(visibility);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ctx = context;
        mCallback = (onCheckChangedListener) context;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_equalizer, container, false);
        setupVisualizerFxAndUI();


        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.equalizer_action_container).setBackgroundColor(Config.primaryColor(getActivity(), Helpers.getATEKey(getActivity())));
        view.findViewById(R.id.spinner_holder).setBackgroundColor(Config.primaryColor(getActivity(), Helpers.getATEKey(getActivity())));

        AdView adView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("8401E8E9D1A58054CC5D94578030EA9E")
                .build();
        adView.loadAd(adRequest);

        backBtn = (ImageView) view.findViewById(R.id.equalizer_back_btn);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            backBtn.setBackgroundColor(Config.primaryColor(getActivity(), Helpers.getATEKey(getActivity())));
        }
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.isPressHome=true;
                getActivity().onBackPressed();
            }
        });

        fragTitle = (TextView) view.findViewById(R.id.equalizer_fragment_title);
    /*    if (SplashActivity.tf4 != null)
            fragTitle.setTypeface(SplashActivity.tf4);*/

        equalizerSwitch = (SwitchCompat) view.findViewById(R.id.equalizer_switch);
        equalizerSwitch.setChecked(true);

        equalizerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCallback.onCheckChanged(isChecked);
            }
        });

        spinnerDropDownIcon = (ImageView) view.findViewById(R.id.spinner_dropdown_icon);
        spinnerDropDownIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presetSpinner.performClick();
            }
        });

        presetSpinner = (Spinner) view.findViewById(R.id.equalizer_preset_spinner);

        equalizerBlocker = (FrameLayout) view.findViewById(R.id.equalizerBlocker);

        if (equalizerSwitch.isChecked()) {
            equalizerBlocker.setVisibility(View.GONE);
        } else {
            equalizerBlocker.setVisibility(View.VISIBLE);
        }

        chart = (LineChartView) view.findViewById(R.id.lineChart);
        paint = new Paint();
        dataset = new LineSet();

        bassController = (AnalogController) view.findViewById(R.id.controllerBass);
        reverbController = (AnalogController) view.findViewById(R.id.controller3D);

        bassController.setLabel("BASS");
        reverbController.setLabel("3D");

        bassController.circlePaint2.setColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
        bassController.linePaint.setColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
        bassController.textPaint.setColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
        bassController.invalidate();
        reverbController.circlePaint2.setColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
        reverbController.linePaint.setColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
        reverbController.textPaint.setColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
        reverbController.invalidate();

        if (!isEqualizerReloaded) {
            int x = 0;
            if (bassBoost != null) {
                try {
                    x = ((bassBoost.getRoundedStrength() * 19) / 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (presetReverb != null) {
                try {
                    y = (presetReverb.getPreset() * 19) / 6;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (x == 0) {
                bassController.setProgress(1);
            } else {
                bassController.setProgress(x);
            }

            if (y == 0) {
                reverbController.setProgress(1);
            } else {
                reverbController.setProgress(y);
            }
        } else {
            int x = ((bassStrength * 19) / 1000);
            y = (reverbPreset * 19) / 6;
            if (x == 0) {
                bassController.setProgress(1);
            } else {
                bassController.setProgress(x);
            }

            if (y == 0) {
                reverbController.setProgress(1);
            } else {
                reverbController.setProgress(y);
            }
        }

        bassController.setOnProgressChangedListener(new AnalogController.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                bassStrength = (short) (((float) 1000 / 19) * (progress));
                try {
                    bassBoost.setStrength(bassStrength);
                    equalizerModel.setBassStrength(bassStrength);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        reverbController.setOnProgressChangedListener(new AnalogController.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                reverbPreset = (short) ((progress * 6) / 19);
                equalizerModel.setReverbPreset(reverbPreset);
                try {
                    presetReverb.setPreset(reverbPreset);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                y = progress;
            }
        });

        mLinearLayout = (LinearLayout) view.findViewById(R.id.equalizerContainer);

        TextView equalizerHeading = new TextView(getContext());
        equalizerHeading.setText("Equalizer");
        equalizerHeading.setTextSize(20);
        equalizerHeading.setGravity(Gravity.CENTER_HORIZONTAL);

        numberOfFrequencyBands = 5;
        points = new float[numberOfFrequencyBands];

        final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];
        final short upperEqualizerBandLevel = mEqualizer.getBandLevelRange()[1];

        for (short i = 0; i < numberOfFrequencyBands; i++) {
            final short equalizerBandIndex = i;
            final TextView frequencyHeaderTextView = new TextView(getContext());
            frequencyHeaderTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            frequencyHeaderTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            frequencyHeaderTextView.setTextColor(Color.parseColor("#FFFFFF"));
            frequencyHeaderTextView.setText((mEqualizer.getCenterFreq(equalizerBandIndex) / 1000) + "Hz");

            LinearLayout seekBarRowLayout = new LinearLayout(getContext());
            seekBarRowLayout.setOrientation(LinearLayout.VERTICAL);

            TextView lowerEqualizerBandLevelTextView = new TextView(getContext());
            lowerEqualizerBandLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            lowerEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"));
            lowerEqualizerBandLevelTextView.setText((lowerEqualizerBandLevel / 100) + "dB");

            TextView upperEqualizerBandLevelTextView = new TextView(getContext());
            lowerEqualizerBandLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            upperEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"));
            upperEqualizerBandLevelTextView.setText((upperEqualizerBandLevel / 100) + "dB");

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.weight = 1;

            SeekBar seekBar = new SeekBar(getContext());
            TextView textView = new TextView(getContext());
            switch (i) {
                case 0:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar1);
                    textView = (TextView) view.findViewById(R.id.textView1);
                    break;
                case 1:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar2);
                    textView = (TextView) view.findViewById(R.id.textView2);
                    break;
                case 2:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar3);
                    textView = (TextView) view.findViewById(R.id.textView3);
                    break;
                case 3:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar4);
                    textView = (TextView) view.findViewById(R.id.textView4);
                    break;
                case 4:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar5);
                    textView = (TextView) view.findViewById(R.id.textView5);
                    break;
                /*case 5:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar6);
                    textView = (TextView) view.findViewById(R.id.textView6);
                    break;
                case 6:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar7);
                    textView = (TextView) view.findViewById(R.id.textView7);
                    break;
                case 7:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar8);
                    textView = (TextView) view.findViewById(R.id.textView8);
                    break;
                case 8:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar9);
                    textView = (TextView) view.findViewById(R.id.textView9);
                    break;
                case 9:
                    seekBar = (SeekBar) view.findViewById(R.id.seekBar10);
                    textView = (TextView) view.findViewById(R.id.textView10);

                    break;*/
            }
            seekBarFinal[i] = seekBar;
            seekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Config.primaryColorDark(getActivity(), Helpers.getATEKey(getActivity())), PorterDuff.Mode.SRC_IN));
            seekBar.getThumb().setColorFilter(new PorterDuffColorFilter(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())), PorterDuff.Mode.SRC_IN));
            seekBar.setId(i);




            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
                LayerDrawable progressBarDrawable = (LayerDrawable) seekBar.getProgressDrawable();
                Drawable backgroundDrawable = progressBarDrawable.getDrawable(0);
                Drawable progressDrawable = progressBarDrawable.getDrawable(1);

                backgroundDrawable.setColorFilter(ContextCompat.getColor(this.getContext(), R.color.gray), PorterDuff.Mode.SRC_IN);
                progressDrawable.setColorFilter(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())), PorterDuff.Mode.SRC_IN);
            }

            seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);

            textView.setText(frequencyHeaderTextView.getText());
            textView.setTextColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }

            if (isEqualizerReloaded) {
                points[i] = seekbarpos[i] - lowerEqualizerBandLevel;
                dataset.addPoint(frequencyHeaderTextView.getText().toString(), points[i]);
                seekBar.setProgress(seekbarpos[i] - lowerEqualizerBandLevel);
            } else {
                points[i] = mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel;
                dataset.addPoint(frequencyHeaderTextView.getText().toString(), points[i]);
                seekBar.setProgress(mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel);
                seekbarpos[i] = mEqualizer.getBandLevel(equalizerBandIndex);
                isEqualizerReloaded = true;
            }
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mEqualizer.setBandLevel(equalizerBandIndex, (short) (progress + lowerEqualizerBandLevel));
                    points[seekBar.getId()] = mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel;
                    seekbarpos[seekBar.getId()] = (progress + lowerEqualizerBandLevel);
                    equalizerModel.getSeekbarpos()[seekBar.getId()] = (progress + lowerEqualizerBandLevel);
                    dataset.updateValues(points);
                    chart.notifyDataUpdate();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    presetSpinner.setSelection(0);
                    presetPos = 0;
                    equalizerModel.setPresetPos(0);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

        }

        equalizeSound();

        paint.setColor(Color.parseColor("#555555"));
        paint.setStrokeWidth((float) (1.10 * AnalogController.ratio));

        dataset.setColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
        dataset.setSmooth(true);
        dataset.setThickness(5);

        chart.setXAxis(false);
        chart.setYAxis(false);

        chart.setYLabels(AxisRenderer.LabelPosition.NONE);
        chart.setXLabels(AxisRenderer.LabelPosition.NONE);
        chart.setGrid(ChartView.GridType.NONE, 7, 10, paint);

        chart.setAxisBorderValues(-300, 3300);

        chart.addData(dataset);
        chart.show();

        Button mEndButton = new Button(getContext());
        mEndButton.setBackgroundColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
        mEndButton.setTextColor(Color.WHITE);

   /*     showCase = new ShowcaseView.Builder(getActivity())
                .blockAllTouches()
                .singleShot(4)
                .setStyle(R.style.CustomShowcaseTheme)
                .useDecorViewAsParent()
                .replaceEndButton(mEndButton)
                .setContentTitlePaint(HomeActivity.tp)
                .setTarget(new ViewTarget(R.id.showcase_view_equalizer, getActivity()))
                .setContentTitle("Presets")
                .setContentText("Use one of the available presets")
                .build();
        showCase.setButtonText("Next");
        showCase.setButtonPosition(HomeActivity.lps);
        showCase.overrideButtonClick(new View.OnClickListener() {
            int count1 = 0;

            @Override
            public void onClick(View v) {
                count1++;
                switch (count1) {
                    case 1:
                        showCase.setTarget(new ViewTarget(R.id.equalizerContainer, getActivity()));
                        showCase.setContentTitle("Equalizer Controls");
                        showCase.setContentText("Use the seekbars to control the Individual frequencies");
                        showCase.setButtonPosition(HomeActivity.lps);
                        showCase.setButtonText("Next");
                        break;
                    case 2:
                        showCase.setTarget(new ViewTarget(R.id.controllerBass, getActivity()));
                        showCase.setContentTitle("Bass and Reverb");
                        showCase.setContentText("Use these controls to control Bass and Reverb");
                        showCase.setButtonPosition(HomeActivity.lps);
                        showCase.setButtonText("Done");
                        break;
                    case 3:
                        showCase.hide();
                        break;
                }
            }

        });*/

    }

    public void setupVisualizerFxAndUI() {

        try {
            if (equalizerModel == null) {
                equalizerModel = new EqualizerModel();
                isEqualizerEnabled = true;
                isEqualizerReloaded = false;
            } else {
                isEqualizerEnabled = equalizerModel.isEqualizerEnabled();
                isEqualizerReloaded = true;
                seekbarpos = equalizerModel.getSeekbarpos();
                presetPos = equalizerModel.getPresetPos();
                reverbPreset = equalizerModel.getReverbPreset();
                bassStrength = equalizerModel.getBassStrength();
            }
            mVisualizer = new Visualizer(MusicPlayer.getAudioSessionId());
            mEqualizer = new Equalizer(0, MusicPlayer.getAudioSessionId());
            mEqualizer.setEnabled(true);

            try {
                bassBoost = new BassBoost(0, MusicPlayer.getAudioSessionId());
                bassBoost.setEnabled(false);
                BassBoost.Settings bassBoostSettingTemp = bassBoost.getProperties();
                BassBoost.Settings bassBoostSetting = new BassBoost.Settings(bassBoostSettingTemp.toString());
                bassBoostSetting.strength = (1000 / 19);
                bassBoost.setProperties(bassBoostSetting);
              //  MusicPlayer.getPlayer().setAuxEffectSendLevel(1.0f);

                presetReverb = new PresetReverb(0, MusicPlayer.getAudioSessionId());
                presetReverb.setPreset(PresetReverb.PRESET_NONE);
                presetReverb.setEnabled(false);
             //   MusicPlayer.getPlayer().setAuxEffectSendLevel(1.0f);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isEqualizerEnabled) {
            try {
                bassBoost.setEnabled(true);
                BassBoost.Settings bassBoostSettingTemp = bassBoost.getProperties();
                BassBoost.Settings bassBoostSetting = new BassBoost.Settings(bassBoostSettingTemp.toString());
                if (bassStrength == -1) {
                    bassBoostSetting.strength = (1000 / 19);
                } else {
                    bassBoostSetting.strength = bassStrength;
                }
                bassBoost.setProperties(bassBoostSetting);
            //    MusicPlayer.getPlayer().setAuxEffectSendLevel(1.0f);

                if (reverbPreset == -1) {
                    presetReverb.setPreset(PresetReverb.PRESET_NONE);
                } else {
                    presetReverb.setPreset(reverbPreset);
                }
                presetReverb.setEnabled(true);
            //    MusicPlayer.getPlayer().setAuxEffectSendLevel(1.0f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (isEqualizerEnabled && isEqualizerReloaded) {
            try {
                isEqualizerEnabled = true;
                int pos = presetPos;
                if (pos != 0) {
                    mEqualizer.usePreset((short) (pos - 1));
                } else {
                    for (short i = 0; i < 5; i++) {
                        mEqualizer.setBandLevel(i, (short) seekbarpos[i]);
                    }
                }
                if (bassStrength != -1 && reverbPreset != -1) {
                    bassBoost.setEnabled(true);
                    bassBoost.setStrength(bassStrength);
                    presetReverb.setEnabled(true);
                    presetReverb.setPreset(reverbPreset);
                }
           //     MusicPlayer.getPlayer().setAuxEffectSendLevel(1.0f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        try {
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            mVisualizer.setDataCaptureListener(
                    new Visualizer.OnDataCaptureListener() {
                        public void onWaveFormDataCapture(Visualizer visualizer,
                                                          byte[] bytes, int samplingRate) {
                        }

                        public void onFftDataCapture(Visualizer visualizer,
                                                     byte[] bytes, int samplingRate) {
//                            updateVisualizer(bytes);
                        }
                    }, Visualizer.getMaxCaptureRate() / 2, false, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void equalizeSound() {
        ArrayList<String> equalizerPresetNames = new ArrayList<>();
        ArrayAdapter<String> equalizerPresetSpinnerAdapter = new ArrayAdapter<>(getContext(),
                R.layout.spinner_item,
                equalizerPresetNames);
        equalizerPresetSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        equalizerPresetNames.add("Custom");

        for (short i = 0; i < mEqualizer.getNumberOfPresets(); i++) {
            equalizerPresetNames.add(mEqualizer.getPresetName(i));
        }

        presetSpinner.setAdapter(equalizerPresetSpinnerAdapter);
        presetSpinner.setDropDownWidth((MuzikoApp.getDisplayWidth() * 3) / 4);
        if (isEqualizerReloaded && presetPos != 0) {
//            correctPosition = false;
            presetSpinner.setSelection(presetPos);
        }

        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (position != 0) {
                        mEqualizer.usePreset((short) (position - 1));
                        presetPos = position;
                        short numberOfFreqBands = 5;

                        final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];

                        for (short i = 0; i < numberOfFreqBands; i++) {
                            seekBarFinal[i].setProgress(mEqualizer.getBandLevel(i) - lowerEqualizerBandLevel);
                            points[i] = mEqualizer.getBandLevel(i) - lowerEqualizerBandLevel;
                            seekbarpos[i] = mEqualizer.getBandLevel(i);
                            equalizerModel.getSeekbarpos()[i] = mEqualizer.getBandLevel(i);
                        }
                        dataset.updateValues(points);
                        chart.notifyDataUpdate();
                    }
                } catch (Exception e) {
                    Toast.makeText(ctx, "Error while updating Equalizer", Toast.LENGTH_SHORT).show();
                }
                equalizerModel.setPresetPos(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public interface onCheckChangedListener {
        void onCheckChanged(boolean isChecked);
    }

}
