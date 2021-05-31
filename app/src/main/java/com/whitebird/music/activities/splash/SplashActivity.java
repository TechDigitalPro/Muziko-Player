package com.whitebird.music.activities.splash;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.BaseActivity;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.dataloaders.SongLoaderNew;
import com.whitebird.music.permissions.Nammu;
import com.whitebird.music.permissions.PermissionCallback;
import com.whitebird.music.utils.ExceptionHandler;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.PreferencesUtility;

import static com.whitebird.music.MuzikoApp.SLEEP_TIMER;


public class SplashActivity extends BaseActivity {
    private FirebaseAnalytics mFirebaseAnalytics;
    private final PermissionCallback permissionReadstorageCallbackN = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            SplashActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadEverything();
                }
            });

        }

        @Override
        public void permissionRefused() {
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_splash);


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (MuzikoUtils.isMarshmallow()) {
            checkPermissionAndThenLoad();
            //checkWritePermissions();
        } else {
            loadEverything();
        }


        PreferencesUtility.getInstance(SplashActivity.this).setTimer(false);
        SLEEP_TIMER = 0;

        MuzikoApp.START_TIME = 0;
        MuzikoApp.isActiveLast = false;


    }

    private void checkPermissionAndThenLoad() {
        //check for permission
        if (Nammu.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            loadEverything();
        } else {
            if (Nammu.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(findViewById(R.id.img_splash), "Muziko will need to read external storage to display songs on your device.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Nammu.askForPermission(SplashActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissionReadstorageCallbackN);
                            }
                        }).show();
            } else {
                Nammu.askForPermission(SplashActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissionReadstorageCallbackN);
            }
        }
    }

    private void loadEverything() {
        new SongLoaderNew(SplashActivity.this).execute();
        String crash = getIntent()
                .getStringExtra(ExceptionHandler.CRASH_REPORT);
        // thread for displaying the SplashScreen
        if (crash == null) {
            try {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (new PreferencesUtility(SplashActivity.this).getStatus("INTRO"))
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        else
                            startActivity(new Intent(SplashActivity.this, IntroActivity.class));
                        finish();
                    }
                }, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                showCrashDialog(crash);
            } catch (Exception e) {
                finish();
            }
        }


    }

    public void showCrashDialog(final String report) {


        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("App Crashed");
        b.setMessage("Oops! The app crashed due to below reason:\n\n" + report);

        DialogInterface.OnClickListener ocl = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == DialogInterface.BUTTON_POSITIVE) {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/html");
                    i.putExtra(Intent.EXTRA_EMAIL,
                            new String[]{"lokeshaddonvision@gmail.com"});
                    i.putExtra(Intent.EXTRA_TEXT, report);
                    i.putExtra(Intent.EXTRA_SUBJECT, "App Crashed");
                    startActivity(Intent.createChooser(i, "Send Mail via:"));
                    finish();
                } else {
                    try {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                                finish();
                            }
                        }, 2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        b.setCancelable(false);
        b.setPositiveButton("Send Report", ocl);
        b.setNegativeButton("Restart", ocl);
        b.create().show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
