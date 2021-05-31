package com.whitebird.music.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;
import android.util.StateSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.ActionBarContextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.BaseActivity;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.dataloaders.SongLoader;
import com.whitebird.music.dialogs.AddPlaylistDialog;
import com.whitebird.music.dialogs.PlayFrom;
import com.whitebird.music.dialogs.PreviewSong;
import com.whitebird.music.dialogs.SetRingtone;
import com.whitebird.music.dialogs.ShareRingtone;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.utils.StaticData;
import com.whitebird.music.widgets.PopupImageView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class DirectoryFragment extends Fragment implements ActionMode.Callback {
    Receiver receiverNew;
    private String title_ = "";
    private Toolbar toolbar;
    private View fragmentView;
    private File currentDir;
    private HorizontalScrollView scrollview;
    private LinearLayout folderlayout;
    private List<Song> mSongs = new ArrayList<>();
    private ListView listView;
    private ListAdapter listAdapter;
    private TextView emptyView;
    private ActionBar actionBar;
    private ArrayList<ListItem> items = new ArrayList<>();
    private ArrayList<HistoryEntry> history = new ArrayList<>();

    private ArrayList<Integer> selectedPosition = new ArrayList<>();
    private long sizeLimit = 1024 * 1024 * 1024;
    private ActionMode actionMode;
    private boolean isMultiSelect = false;
    private String[] chhosefileType = {".mp3", ".acc", ".wav", ".ogg"};

    private InterstitialAd interstitialAd;
    boolean isActivityIsVisible = true;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        if (currentDir == null) {
                            listRoots();
                        } else {
                            listFiles(currentDir);
                        }
                    } catch (Exception e) {
                        Log.e("t messages", e.toString());
                    }
                }
            };
            if (Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) {
                listView.postDelayed(r, 1000);
            } else {
                r.run();
            }
        }
    };

    public static String formatFileSize(long size) {
        if (size < 1024) {
            return String.format("%d B", size);
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0f);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / 1024.0f / 1024.0f);
        } else {
            return String.format("%.1f GB", size / 1024.0f / 1024.0f / 1024.0f);
        }
    }

    public static void clearDrawableAnimation(View view) {
        if (Build.VERSION.SDK_INT < 21 || view == null) {
            return;
        }
        Drawable drawable = null;
        if (view instanceof ListView) {
            drawable = ((ListView) view).getSelector();
            if (drawable != null) {
                drawable.setState(StateSet.NOTHING);
            }
        } else {
            drawable = view.getBackground();
            if (drawable != null) {
                drawable.setState(StateSet.NOTHING);
                drawable.jumpToCurrentState();
            }
        }
    }

    private void register() {
        receiverNew = new Receiver();
        IntentFilter filter = new IntentFilter();

        filter.addAction(MuzikoApp.INTENT_MEDIA_CHANGE);
        getActivity().registerReceiver(receiver, filter);
    }

    private void unregister() {
        try {
            if (receiver != null) {
                getActivity().unregisterReceiver(receiver);
                receiver = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean onBackPressed_() {

        if (history.size() > 0) {
            HistoryEntry he = history.remove(history.size() - 1);
            title_ = he.title;

            if (he.dir != null) {
                listFiles(he.dir);
            } else {
                listRoots();
            }
            listView.setSelectionFromTop(he.scrollItem, he.scrollOffset);
            updateStoragePath();
            return false;
        } else {
            return true;
        }
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
    }

    @Override
    public void onDestroy() {
        isActivityIsVisible = false;
        onFragmentDestroy();
        unregister();
        super.onDestroy();
    }

    private void updateStoragePath() {
        try {
            selectedPosition.clear();
            //Destroy action mode
            if (actionMode != null)
                actionMode.finish();

        } catch (Exception e) {
            e.printStackTrace();
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                folderlayout.removeAllViews();

                for (int i = 0; i < history.size(); i++) {

                    HistoryEntry queueItem = history.get(i);
                    TextView valueTV = new TextView(getActivity());
                    valueTV.setText(queueItem.title);
                    valueTV.setId(i + 1);
                    valueTV.setTextColor(getActivity().getResources().getColor(R.color.gray));
                    valueTV.setTag(i);
                    valueTV.setTextSize(16f);
                    valueTV.setBackgroundColor(Color.TRANSPARENT);
                    valueTV.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    valueTV.setGravity(Gravity.CENTER);

                /*    if (i != history.size() - 1) {
                        valueTV.setText(queueItem.title );

                    } else*/
                    {
                        if (!queueItem.title.equalsIgnoreCase(""))
                            valueTV.setText("   >   " + queueItem.title);
                        else {
                            valueTV.setText("Drives");
                            //  valueTV.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_home, 0, 0);
                        }
                    }


                    valueTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int no = (int) v.getTag();
                            HistoryEntry he = history.get(no);

                            for (int k = no; k < history.size(); k++) {
                                history.remove(k);
                                k--;
                            }

                            title_ = he.title;

                            if (he.dir != null) {
                                listFiles(he.dir);
                            } else {
                                listRoots();
                            }
                            listView.setSelectionFromTop(he.scrollItem, he.scrollOffset);
                            updateStoragePath();

                        }
                    });


                    folderlayout.addView(valueTV);

                }


                final TextView valueTV = new TextView(getActivity());
                if (!title_.equalsIgnoreCase(""))
                    valueTV.setText("   >   " + title_);
                else {
                    valueTV.setText("Drives");
                    //valueTV.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_home, 0, 0);
                }
                valueTV.setId(0);
                valueTV.setTextColor(Color.WHITE);
                valueTV.setTag(0);
                valueTV.setTextSize(16f);
                valueTV.setBackgroundColor(Color.TRANSPARENT);
                valueTV.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
                valueTV.setGravity(Gravity.CENTER);
//                CardView cardView = createCard();
//                cardView.addView(valueTV);
                folderlayout.addView(valueTV);

                scrollview.postDelayed(new Runnable() {
                    public void run() {
                        scrollview.smoothScrollTo(valueTV.getRight(), valueTV.getTop());
                    }
                }, 100L);
            }
        });


    }


    public void onFragmentDestroy() {
        try {
            if (receiver != null) {
                getActivity().unregisterReceiver(receiver);
            }
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        {
            //      receiverRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            filter.addAction(Intent.ACTION_MEDIA_CHECKING);
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_NOFS);
            filter.addAction(Intent.ACTION_MEDIA_REMOVED);
            filter.addAction(Intent.ACTION_MEDIA_SHARED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);

            filter.addDataScheme("file");
            getActivity().registerReceiver(receiver, filter);
            register();
        }
        history.clear();
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.document_select_layout,
                    container, false);

            listAdapter = new ListAdapter(getActivity());
            emptyView = (TextView) fragmentView
                    .findViewById(R.id.searchEmptyView);
            emptyView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

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

            AdView adView = (AdView) fragmentView.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("8401E8E9D1A58054CC5D94578030EA9E")
                    .build();
            adView.loadAd(adRequest);

            toolbar = fragmentView.findViewById(R.id.toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

            listView = (ListView) fragmentView.findViewById(R.id.listView);
            scrollview = (HorizontalScrollView) fragmentView.findViewById(R.id.scrollview);
            scrollview.setBackgroundColor(Config.primaryColor(getActivity(), Helpers.getATEKey(getActivity())));
            folderlayout = (LinearLayout) fragmentView.findViewById(R.id.folderlayout);

            scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);


            listView.setEmptyView(emptyView);
            listView.setAdapter(listAdapter);


            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        if (items.get(position).file.isDirectory())
                            return true;
                    } catch (Exception e) {
                        return true;
                    }

                    if (!isMultiSelect) {
                        ((MainActivity) getActivity()).startSupportActionMode(DirectoryFragment.this);
                        isMultiSelect = true;
                        toogleSelection(position);

                    }

                    return true;
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view,
                                        final int i, long l) {


                    if (i < 0 || i >= items.size()) {
                        return;
                    }
                    if (isMultiSelect) {
                        try {
                            if (items.get(i).file.isDirectory())
                                return;
                        } catch (Exception e) {
                            return;
                        }

                        toogleSelection(i);
                        return;
                    }


                    final ListItem item = items.get(i);
                    final File file = item.file;
                    if (file == null) {
                        HistoryEntry he = history.remove(history.size() - 1);
                        title_ = he.title;

                        if (he.dir != null) {
                            listFiles(he.dir);
                        } else {
                            listRoots();
                        }
                        listView.setSelectionFromTop(he.scrollItem,
                                he.scrollOffset);

                    } else if (file.isDirectory()) {
                        HistoryEntry he = new HistoryEntry();
                        he.scrollItem = listView.getFirstVisiblePosition();
                        he.scrollOffset = listView.getChildAt(0).getTop();
                        he.dir = currentDir;
                        he.title = title_.toString();

                        if (!listFiles(file)) {
                            return;
                        }
                        history.add(he);
                        title_ = item.title;

                        listView.setSelection(0);


                    } else {
                        if (!file.canRead()) {
                            showErrorBox("AccessError");
                            return;
                        }
                        if (sizeLimit != 0) {
                            if (file.length() > sizeLimit) {
                                showErrorBox("FileUploadLimit");
                                return;
                            }
                        }
                        if (file.length() == 0) {
                            return;
                        }
                        if (file.toString().contains(chhosefileType[0]) ||
                                file.toString().contains(chhosefileType[1]) ||
                                file.toString().contains(chhosefileType[2]) ||
                                file.toString().contains(chhosefileType[3])) {


                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int current = -1;
                                    long songId = SongLoader.getSongFromPath(file.getAbsolutePath(), getActivity()).id;
                                    int count = 0;
                                    for (Song song : mSongs) {
                                        if (song.id != -1) {
                                            count++;
                                        }
                                    }
                                    ArrayList<Song> ret = new ArrayList<>();
                                    int j = 0;
                                    for (int k = 0; k < items.size(); k++) {
                                        try {
                                            if (mSongs.get(k).id != -1) {
                                                ret.add(mSongs.get(k));
                                                if (mSongs.get(k).id == songId) {
                                                    current = j;
                                                }
                                                j++;
                                            }
                                        } catch (Exception e) {

                                        }

                                    }
                                    playAll(getActivity(), ret, current, -1, MuzikoUtils.IdType.NA,
                                            false, mSongs.get(i - 1), false);
                                }
                            }, 100);
                        } else {
                            showErrorBox("Choose correct file.");
                            return;
                        }

                    }
                    updateStoragePath();
                }


            });

            listRoots();
        } else {
            ViewGroup parent = (ViewGroup) fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        setupToolbar();
        return fragmentView;
    }


    private void toogleSelection(int position) {

        if (selectedPosition.contains(position)) {
            selectedPosition.remove(selectedPosition.indexOf(position));

        } else
            selectedPosition.add(position);
        listAdapter.notifyDataSetChanged();

        actionMode.setTitle(String.format("%d Song%s", selectedPosition.size(), selectedPosition.size() != 1 ? "s" : ""));
    }

    private void setupToolbar() {

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Storage");


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.isPressHome = true;
                getActivity().onBackPressed();
            }
        });


    }


    private CardView createCard() {
        CardView card = new CardView(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = 15;
        params.topMargin = 15;
        params.bottomMargin = 15;
        card.setLayoutParams(params);
        // card.setRadius(9);
        card.setContentPadding(15, 15, 15, 15);
        card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
        card.setMaxCardElevation(15);
        card.setCardElevation(9);
        return card;
    }

    private void listRoots() {
        currentDir = null;
        items.clear();
        TextView valueTV = new TextView(getActivity());
        valueTV.setText("Drives");
        valueTV.setId(0);
        valueTV.setTextColor(Color.WHITE);
        valueTV.setTag(0);
        valueTV.setTextSize(16f);
        valueTV.setBackgroundColor(Color.TRANSPARENT);
        // valueTV.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_home, 0, 0);
        // valueTV.setBackgroundResource(R.drawable.lable);
        valueTV.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        valueTV.setGravity(Gravity.CENTER);
//        CardView cardView = createCard();
//        cardView.addView(valueTV);
        folderlayout.addView(valueTV);

        String extStorage = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        ListItem ext = new ListItem();
        if (Build.VERSION.SDK_INT < 9
                || Environment.isExternalStorageRemovable()) {
            ext.title = "SdCard";
        } else {
            ext.title = "InternalStorage";
        }
        ext.icon = Build.VERSION.SDK_INT < 9
                || Environment.isExternalStorageRemovable() ? R.drawable.icon_storage
                : R.drawable.icon_storage;
        ext.subtitle = getRootSubtitle(extStorage);
        ext.file = Environment.getExternalStorageDirectory();
        items.add(ext);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    "/proc/mounts"));
            String line;
            HashMap<String, ArrayList<String>> aliases = new HashMap<String, ArrayList<String>>();
            ArrayList<String> result = new ArrayList<String>();
            String extDevice = null;
            while ((line = reader.readLine()) != null) {
                if ((!line.contains("/mnt") && !line.contains("/storage") && !line
                        .contains("/sdcard"))
                        || line.contains("asec")
                        || line.contains("tmpfs") || line.contains("none") || line.equals("/")) {
                    continue;
                }
                String[] info = line.split(" ");
                if (!aliases.containsKey(info[0])) {
                    aliases.put(info[0], new ArrayList<String>());
                }
                aliases.get(info[0]).add(info[1]);
                if (info[1].equals(extStorage)) {
                    extDevice = info[0];
                }
                result.add(info[1]);
            }
            reader.close();
            if (extDevice != null) {
                result.removeAll(aliases.get(extDevice));
                for (String path : result) {
                    try {
                        ListItem item = new ListItem();
                        if (path.toLowerCase().contains("sd")) {
                            ext.title = "SdCard";
                        } else {
                            ext.title = "ExternalStorage";
                        }
                        item.icon = R.drawable.icon_storage;
                        item.subtitle = getRootSubtitle(path);
                        item.file = new File(path);
                        items.add(item);
                    } catch (Exception e) {
                        Log.e("t messages", e.toString());
                    }
                }
            }
        } catch (Exception e) {
            Log.e("t messages", e.toString());
        }

        listAdapter.notifyDataSetChanged();
    }

    private boolean listFiles(File dir) {
        if (!dir.canRead()) {
            if (dir.getAbsolutePath().startsWith(
                    Environment.getExternalStorageDirectory().toString())
                    || dir.getAbsolutePath().startsWith("/sdcard")
                    || dir.getAbsolutePath().startsWith("/mnt/sdcard")) {
                if (!Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)
                        && !Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED_READ_ONLY)) {
                    currentDir = dir;
                    items.clear();
                    String state = Environment.getExternalStorageState();
                    if (Environment.MEDIA_SHARED.equals(state)) {
                        emptyView.setText("UsbActive");
                    } else {
                        emptyView.setText("NotMounted");
                    }
                    clearDrawableAnimation(listView);
                    // scrolling = true;
                    listAdapter.notifyDataSetChanged();
                    return true;
                }
            }
            showErrorBox("AccessError");
            return false;
        }
        emptyView.setText("NoFiles");
        File[] files = null;
        try {
            files = dir.listFiles();
        } catch (Exception e) {
            showErrorBox(e.getLocalizedMessage());
            return false;
        }
        if (files == null) {
            showErrorBox("UnknownError");
            return false;
        }
        currentDir = dir;
        items.clear();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                if (lhs.isDirectory() != rhs.isDirectory()) {
                    return lhs.isDirectory() ? -1 : 1;
                }
                return lhs.getName().compareToIgnoreCase(rhs.getName());
                /*
                 * long lm = lhs.lastModified(); long rm = lhs.lastModified();
                 * if (lm == rm) { return 0; } else if (lm > rm) { return -1; }
                 * else { return 1; }
                 */
            }
        });
        for (File file : files) {
            if (file.getName().startsWith(".")) {
                continue;
            }
            ListItem item = new ListItem();
            item.title = file.getName();
            item.file = file;
            if (file.isDirectory()) {
                item.icon = R.drawable.icon_folder;
                item.subtitle = "Folder";
            } else {
                String fname = file.getName();
                String[] sp = fname.split("\\.");
                item.ext = sp.length > 1 ? sp[sp.length - 1] : "?";
                item.subtitle = formatFileSize(file.length());
                fname = fname.toLowerCase();
                if (fname.endsWith(".jpg") || fname.endsWith(".png")
                        || fname.endsWith(".gif") || fname.endsWith(".jpeg")) {
                    item.thumb = file.getAbsolutePath();
                }
            }
            if (file.toString().contains(chhosefileType[0]) ||
                    file.toString().contains(chhosefileType[1]) ||
                    file.toString().contains(chhosefileType[2]) ||
                    file.toString().contains(chhosefileType[3]) || file.isDirectory())
                items.add(item);
        }
        ListItem item = new ListItem();
        item.title = "..";
        item.subtitle = "Folder";
        item.icon = R.drawable.icon_folder;
        item.file = null;
        items.add(0, item);
        clearDrawableAnimation(listView);
        // scrolling = true;
        listAdapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getSongsForFiles(items);
            }
        }).start();

        return true;
    }

    private void getSongsForFiles(List<ListItem> files) {
        try {
            mSongs.clear();
            for (ListItem file : files) {
                try {
                    mSongs.add(SongLoader.getSongFromPath(file.file.getAbsolutePath(), getActivity()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {

        }

    }

    public void showErrorBox(String error) {
        if (getActivity() == null) {
            return;
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.app_name))
                .setMessage(error).setPositiveButton("OK", null).show();
    }

    private String getRootSubtitle(String path) {
        StatFs stat = new StatFs(path);
        long total = (long) stat.getBlockCount() * (long) stat.getBlockSize();
        long free = (long) stat.getAvailableBlocks()
                * (long) stat.getBlockSize();
        if (total == 0) {
            return "";
        }
        return "Free " + formatFileSize(free) + " of " + formatFileSize(total);
    }

    public void finishFragment() {

    }

    public void playAll(final Activity context, final ArrayList<Song> list, int position,
                        final long sourceId, final MuzikoUtils.IdType sourceType,
                        final boolean forceShuffle, final Song currentSong, boolean navigateNowPlaying) {

        if (context instanceof BaseActivity) {
           /* CastSession castSession = ((BaseActivity) context).getCastSession();
            if (castSession != null) {
                navigateNowPlaying = false;
                MuzikoCastHelper.startCasting(castSession, currentSong);
            } else*/
            {
                MusicPlayer.playAll(  list, position,   false);
            }
        } else {
            MusicPlayer.playAll(  list, position,   false);
        }

        if (navigateNowPlaying) {
            NavigationUtils.navigateToNowplaying(context, true);
        }


    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(Config.primaryColorDark(getActivity(), Helpers.getATEKey(getActivity())));
        }
        getActivity().getMenuInflater().inflate(R.menu.context_default, menu);
        ActionBarContextView actionBar = getActivity().getWindow().getDecorView().findViewById(R.id.action_mode_bar);
        actionBar.setBackgroundColor(Config.primaryColor(getActivity(), Helpers.getATEKey(getActivity())));
        actionMode = mode;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {


        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {


        ArrayList<Song> selectedSong = new ArrayList<>();

        for (int i : selectedPosition) {
            selectedSong.add(SongLoader.getSongFromPath(items.get(i).file.getAbsolutePath(), getActivity()));

        }
       // long[] ids = MuzikoApp.getSongIds(selectedSong);


        if (selectedSong.size() > 0) {

            switch (item.getItemId()) {
                case R.id.play:
                    MusicPlayer.playAll(selectedSong, 0,   false);
                    break;
                case R.id.play_next:

                    MusicPlayer.playNext(selectedSong);
                    break;
                case R.id.add_to_queue:

                    MusicPlayer.addToQueue(selectedSong);
                    break;
                case R.id.add_to_fav:

                    MuzikoApp.moveToFavorite(selectedSong, getActivity());
                    break;
                case R.id.share:
                    MuzikoApp.getInstance().shareSongs(getActivity(), selectedSong);
                    break;
                case R.id.add_to_playlist:
                    AddPlaylistDialog.newInstance(selectedSong).show(getActivity().getSupportFragmentManager(), "ADD_PLAYLIST");
                    break;

                case R.id.multi_tag_edit:
                    NavigationUtils.multiTagEdit(getActivity(), selectedSong);
                    break;
                case R.id.delete:
                    MuzikoUtils.deleteMultipleSong(getActivity(), selectedSong, "Songs");
                    break;
                default:

                    return false;
            }
        }

        mode.finish();
        actionMode = null;
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

        isMultiSelect = false;
        selectedPosition.clear();
        listAdapter.notifyDataSetChanged();

      /*  new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (!recyclerView.isComputingLayout()) {
                    mAdapter.setMultiSelect(false);
                    actionMode = null;
                } else {
                    onDestroyActionMode(SongsFragment.this.actionMode);
                }
            }
        });*/

    }

    private class HistoryEntry {
        int scrollItem, scrollOffset;
        File dir;
        String title;
    }

    public class ListItem {
        public int icon;
        public String title;
        public String subtitle = "";
        public String ext = "";
        public String thumb;
        public File file;
    }

    private class ListAdapter extends BaseAdapter {
        private Activity mContext;

        public ListAdapter(Activity context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public int getItemViewType(int pos) {
            return items.get(pos).subtitle.length() > 0 ? 0 : 1;
        }

        @Override
        public View getView(int position, View convertViewN, ViewGroup parent) {
            TextView song_title, song_artist;
            ImageView albumArt;
            PopupImageView popup_menu;
            View viewOverlay;

            // if (convertViewN == null)
            {
                //convertViewN = new TextDetailDocumentsCell(mContext);
                LayoutInflater inflater = getLayoutInflater();

                convertViewN = inflater.inflate(R.layout.item_folder, parent, false);
                song_title = (TextView) convertViewN.findViewById(R.id.song_title);
                song_artist = (TextView) convertViewN.findViewById(R.id.song_artist);
                albumArt = (ImageView) convertViewN.findViewById(R.id.albumArt);

                albumArt.getBackground().setColorFilter(new PorterDuffColorFilter(Config.accentColor(mContext, Helpers.getATEKey(mContext)), PorterDuff.Mode.SRC_IN));
                popup_menu = (PopupImageView) convertViewN.findViewById(R.id.popup_menu);
                viewOverlay = convertViewN.findViewById(R.id.viewOverlay);
                popup_menu.setTag(position);
                if (selectedPosition.contains(position)) {
                    viewOverlay.setVisibility(View.VISIBLE);
                    viewOverlay.setAlpha(.5f);
                    viewOverlay.setBackgroundColor(Config.primaryColor(mContext, Helpers.getATEKey(mContext)));
                } else {
                    viewOverlay.setVisibility(View.GONE);
                }


                try {
                    if (items.get(position).file.isDirectory())
                        setOnPopupMenuFolder(popup_menu, position);
                    else
                        setOnPopupMenuSong(popup_menu, position);
                } catch (Exception e) {

                }

            }
            //TextDetailDocumentsCell textDetailCell = (TextDetailDocumentsCell) convertView;
            final ListItem item = items.get(position);

            if (item.title.equalsIgnoreCase("..") || item.icon == R.drawable.icon_storage || item.icon == R.drawable.icon_storage) {
                popup_menu.setVisibility(View.GONE);
            } else {
                popup_menu.setVisibility(View.VISIBLE);
            }


            if (item.icon != 0) {
                song_title.setText(item.title);
                song_artist.setText(item.subtitle);
                albumArt.setImageResource(item.icon);
        /*        ((TextDetailDocumentsCell) convertView)
                        .setTextAndValueAndTypeAndThumb(item.title,
                                item.subtitle, null, null, item.icon);*/
            } else {
/*                String type = item.ext.toUpperCase().substring(0,
                        Math.min(item.ext.length(), 4));
                ((TextDetailDocumentsCell) convertView)
                        .setTextAndValueAndTypeAndThumb(item.title,
                                item.subtitle, type, item.thumb, 0);*/

                song_title.setText(item.title);
                song_artist.setText(item.subtitle);
                // albumArt.setImageResource(R.drawable.ic_empty_music2);
                MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(SongLoader.getSongFromPath(item.file.getAbsolutePath(), getActivity()).albumId).toString(),
                        albumArt, MuzikoApp.OPTIONS);
                albumArt.setBackgroundColor(0);
                albumArt.setPadding(0, 0, 0, 0);

            }

            return convertViewN;
        }


        private void setOnPopupMenuFolder(View view, final int position) {

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final PopupMenu menu = new PopupMenu(mContext, v);
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            ArrayList<Song> songs = StaticData.getSongsForFiles(StaticData.listFiles(items.get(position).file), getActivity());
                            if (songs.size() == 0) {
                                MuzikoApp.toast(getActivity(), "No Song found!");
                                return false;
                            }

                           // long[] ids = MuzikoApp.getSongIds(songs);

                            switch (item.getItemId()) {

                                case R.id.play:
                                    MusicPlayer.playAll(songs, 0, false);
                                    break;
                                case R.id.play_next:
                                    MusicPlayer.playNext(songs);
                                    break;
                                case R.id.add_to_queue:
                                    MusicPlayer.addToQueue(songs);
                                    break;
                                case R.id.add_to_playlist:
                                    AddPlaylistDialog.newInstance(songs).show(getActivity().getSupportFragmentManager(), "ADD_PLAYLIST");
                                    break;

                                case R.id.popup_delete:
//                                     long[] deleteIds = {arraylist.get(position).id};
//                                     MuzikoUtils.showDeleteDialog(mContext,arraylist.get(position).title, deleteIds, AlbumSongsAdapter.this, position);
                                    break;

                            }
                            return false;
                        }
                    });
                    menu.inflate(R.menu.menu_storage_folder);
                    menu.show();
                }
            });
        }


        private void setOnPopupMenuSong(View view, final int position) {

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final PopupMenu menu = new PopupMenu(mContext, v);

                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            Song song = SongLoader.getSongFromPath(items.get(position).file.getAbsolutePath(), getActivity());
ArrayList<Song> songs=new ArrayList<>();
songs.add(song);
                            //long[] id = new long[1];
                           // id[0] = song.id;
                            switch (item.getItemId()) {

                                case R.id.popup_song_play:
                                    MusicPlayer.playAll(songs, 0,   false);
                                    break;
                                case R.id.popup_move_ignore:
                                    ArrayList<Song> ignore = new ArrayList();
                                    ignore.add(song);
                                    MuzikoApp.moveToIgnore(ignore, mContext);
                                    break;
                                case R.id.add_to_fav:
                                    ArrayList<Song> fav = new ArrayList();
                                    fav.add(song);
                                    MuzikoApp.moveToFavorite(fav, mContext);
                                    break;
                                case R.id.popup_song_play_next:

                                    MusicPlayer.playNext(songs);
                                    break;
                                case R.id.popup_song_goto_album:
                                    NavigationUtils.goToAlbum(mContext, song.albumName);
                                    break;
                                case R.id.popup_song_goto_artist:
                                    NavigationUtils.goToArtist(mContext, song.artistId);
                                    break;
                                case R.id.popup_song_addto_queue:

                                    MusicPlayer.addToQueue(songs);
                                    break;
                                case R.id.popup_song_addto_playlist:
                                    AddPlaylistDialog.newInstance(song).show(((FragmentActivity) mContext).getSupportFragmentManager(), "ADD_PLAYLIST");
                                    break;
                                case R.id.preview_song:
                                    PreviewSong previewSong = new PreviewSong();
                                    previewSong.open(mContext, song);
                                    break;
                                case R.id.start_time:
                                    PlayFrom play = new PlayFrom();
                                    play.open(mContext, song);
                                    break;
                                case R.id.popup_cut:
                                    MuzikoApp.getInstance().cutSong(song);
                                    break;
                                case R.id.send_au:
                                    ShareRingtone shareRingtone = new ShareRingtone();
                                    shareRingtone.open(mContext, song);
                                    break;
                                case R.id.popup_song_share:
                                    MuzikoUtils.shareTrack(mContext, song.id);
                                    break;
                                case R.id.tag_edit:
                                   // ArrayList<Song> songs = new ArrayList<>();
                                   // songs.add(song);
                                    NavigationUtils.multiTagEdit(mContext, songs);
                                    break;
                                case R.id.popup_song_delete:
                                  //  long[] deleteIds = {song.id};
                                    MuzikoUtils.showDeleteDialog(mContext, song.title, songs);
                                    break;
                                case R.id.popup_song_details:

                                    MuzikoApp.details(mContext, song);
                                    break;
                                case R.id.popup_set_ring:
                                    boolean permission;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        permission = Settings.System.canWrite(mContext);
                                    } else {
                                        permission =
                                                ContextCompat.checkSelfPermission(
                                                        mContext, Manifest.permission.WRITE_SETTINGS)
                                                        == PackageManager.PERMISSION_GRANTED;
                                    }
                                    if (!permission) {

                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                            new MaterialDialog.Builder(mContext)
                                                    .theme(Theme.LIGHT)
                                                    .negativeColor(Config.accentColor(mContext, Helpers.getATEKey(mContext)))
                                                    .titleColor(Config.accentColor(mContext, Helpers.getATEKey(mContext)))
                                                    .neutralColorRes(R.color.dialog_negetive_button)
                                                    .positiveColor(Config.accentColor(mContext, Helpers.getATEKey(mContext)))
                                                    .title("Permission required")
                                                    .content(
                                                            "Muziko requires the write settings permission to set your default ringtone. You need to enable it on the settings screen.")
                                                    .positiveText("Ok")
                                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                                                intent.setData(Uri.parse("package:com.whitebird.music"));
                                                                mContext.startActivityForResult(intent, 1020);
                                                            } else {
                                                                ActivityCompat.requestPermissions(
                                                                        mContext,
                                                                        new String[]{Manifest.permission.WRITE_SETTINGS},
                                                                        1020);
                                                            }
                                                        }
                                                    })
                                                    .negativeText("Cancel")
                                                    .show();
                                        } else {
                                            SetRingtone createRingtone = new SetRingtone();
                                            createRingtone.open(mContext, song);
                                        }
                                    } else {
                                        SetRingtone createRingtone = new SetRingtone();
                                        createRingtone.open(mContext, song);
                                    }

                                    break;
                            }
                            return false;
                        }
                    });
                    menu.inflate(R.menu.popup_song);
                    menu.show();

                }
            });
        }


    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();
                    {
                        try {
                            if (currentDir == null) {
                                listRoots();
                            } else {
                                listFiles(currentDir);
                            }
                        } catch (Exception e) {
                            Log.e("t messages", e.toString());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
