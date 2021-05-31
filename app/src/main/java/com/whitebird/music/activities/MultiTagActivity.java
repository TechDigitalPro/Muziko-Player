package com.whitebird.music.activities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.dataloaders.DataHelper;
import com.whitebird.music.models.Song;
import com.whitebird.music.tageditor.metadata.MusicMetadata;
import com.whitebird.music.tageditor.metadata.MusicMetadataSet;
import com.whitebird.music.tageditor.myid3.MyID3;
import com.whitebird.music.utils.ATEUtils;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.Utils;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MultiTagActivity extends BaseActivity implements View.OnClickListener, ATEActivityThemeCustomizer {
    private final String TAG = MultiTagActivity.class.getSimpleName();
    private final ArrayList<Song> multiTagList = new ArrayList<>();
    private final ArrayList<MusicMetadataSet> multiTagMetaDataSetList = new ArrayList<>();
    private final ArrayList<MusicMetadata> multiTagMetaDataList = new ArrayList<>();
    private final ArrayList<MusicMetadataSet> finalMultiTagMetaDataSetList = new ArrayList<>();
    private final ArrayList<MusicMetadata> finalMultiTagMetaDataList = new ArrayList<>();
    private RelativeLayout progressLayout;
    private ProgressBar progressBar;
    private AppBarLayout appBarLayout;
    private EditText editArtist;
    private EditText editAlbum;
    private AutoCompleteTextView editGenre;
    private EditText editYear;
    private boolean saved = false;
    private FloatingActionButton fab;
    private MyID3 id3 = new MyID3();
    private TagsUpdater task = null;
    private boolean saveRunning = false;
    private File newfile;
    private boolean lyricsChanged = false;
    private boolean isLrc = false;
    private Toolbar toolbar;
    private int primaryColor = -1;


    public static void setInputTextLayoutColor(EditText editText, @ColorInt int color) {

        try {
            TextInputLayout til = (TextInputLayout) editText.getParent().getParent();
            Field fDefaultTextColor = TextInputLayout.class.getDeclaredField("mDefaultTextColor");
            fDefaultTextColor.setAccessible(true);
            fDefaultTextColor.set(til, new ColorStateList(new int[][]{{0}}, new int[]{color}));

            Field fFocusedTextColor = TextInputLayout.class.getDeclaredField("mFocusedTextColor");
            fFocusedTextColor.setAccessible(true);
            fFocusedTextColor.set(til, new ColorStateList(new int[][]{{0}}, new int[]{color}));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_tag);
        if (MuzikoApp.multiTagList.size() == 0) {
            MuzikoApp.toast(this, "Song not found!");
            finish();
            return;
        }
        FindViewsById();
        primaryColor = Config.primaryColor(MultiTagActivity.this, Helpers.getATEKey(MultiTagActivity.this));
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(this);
        editArtist.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.equals("")) {
                    fab.setVisibility(View.VISIBLE);
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });

        editAlbum.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.equals("")) {
                    fab.setVisibility(View.VISIBLE);
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });

        editGenre.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.equals("")) {
                    fab.setVisibility(View.VISIBLE);
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });


        editYear.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.equals("")) {
                    fab.setVisibility(View.VISIBLE);
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });


        KeyboardVisibilityEvent.setEventListener(
                this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {

                        if (isOpen) {
                            appBarLayout.setExpanded(false, true);
                        } else {
                            appBarLayout.setExpanded(true, true);
                        }
                    }
                });


    }

    private MusicMetadataSet readData(Song queueItem) {
        MusicMetadataSet data = null;
        try {
            File from = new File(queueItem.data);
            data = id3.read(from);      //read metadata
        } catch (Exception ex) {
            String fileNameWithoutExt = FilenameUtils.getBaseName(queueItem.data);
            MuzikoApp.toast(this, "Unable to read song metadata - " + fileNameWithoutExt);

        }

        return data;
    }

    private void loadSongs() {
        ArrayList<Song> removelist = new ArrayList<>();

        for (int i = 0; i < MuzikoApp.multiTagList.size(); i++) {

            Song multiQueueItem = MuzikoApp.multiTagList.get(i);
            MusicMetadataSet itemDataset = readData(multiQueueItem);

            if (itemDataset == null) {
                MuzikoApp.toast(this, "Unable to read song metadata from " + multiQueueItem.data);
                removelist.add(multiQueueItem);
                continue;
            }

            MusicMetadata itemMetadata = (MusicMetadata) itemDataset.getSimplified();

            if (itemMetadata == null) {
                MuzikoApp.toast(this, "Unable to read song metadata from " + multiQueueItem.data);
                removelist.add(multiQueueItem);
                continue;
            }

            multiTagMetaDataSetList.add(itemDataset);
            multiTagMetaDataList.add(itemMetadata);

        }

        for (Song removeItem : removelist) {
            MuzikoApp.multiTagList.remove(removeItem);
        }
    }

    @Override
    public void onBackPressed() {

        finish();
    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    private void FindViewsById() {

        appBarLayout = findViewById(R.id.appBarLayout);
        toolbar = findViewById(R.id.toolbar);


        progressLayout = findViewById(R.id.progressLayout);

        progressBar = findViewById(R.id.progressBar);


        editArtist = findViewById(R.id.editArtist);
        editAlbum = findViewById(R.id.editAlbum);
        editGenre = findViewById(R.id.editGenre);
        editYear = findViewById(R.id.editYear);

        setInputTextLayoutColor(editArtist, Config.accentColor(MultiTagActivity.this, Helpers.getATEKey(MultiTagActivity.this)));
        setInputTextLayoutColor(editAlbum, Config.accentColor(MultiTagActivity.this, Helpers.getATEKey(MultiTagActivity.this)));
        setInputTextLayoutColor(editGenre, Config.accentColor(MultiTagActivity.this, Helpers.getATEKey(MultiTagActivity.this)));
        setInputTextLayoutColor(editYear, Config.accentColor(MultiTagActivity.this, Helpers.getATEKey(MultiTagActivity.this)));


        fab = findViewById(R.id.fab);


        List<String> entries = new ArrayList<>();
        ArrayList<Song> genres = DataHelper.getAllGenre();
        for (Song genre : genres) {
            entries.add(genre.title);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, entries);
        editGenre.setAdapter(adapter);
        editGenre.setThreshold(1);


        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(MuzikoApp.multiTagList.size() + " tracks");
    }

    @Override
    public void onClick(View view) {
        if (view == fab) {
            save();
        }
    }


    private void save() {
        saved = false;

        multiTagList.clear();

        if (saveRunning) {
            return;
        }

        saveRunning = true;

        for (int i = 0; i < MuzikoApp.multiTagList.size(); i++) {
            MusicMetadataSet itemDataset;
            MusicMetadata itemMetadata;
            byte[] bitmap = null;

            Song queueItem = MuzikoApp.multiTagList.get(i);
            itemMetadata = multiTagMetaDataList.get(i);
            itemDataset = multiTagMetaDataSetList.get(i);


            if (!editArtist.getText().toString().isEmpty()) {
                queueItem.artistName = editArtist.getText().toString();
                itemMetadata.setArtist(editArtist.getText().toString());
                saved = true;
            }

            if (!editAlbum.getText().toString().trim().isEmpty()) {
                queueItem.albumName = editAlbum.getText().toString().trim();
                itemMetadata.setAlbum(editAlbum.getText().toString().trim());
                itemMetadata.setAlbum(editAlbum.getText().toString().trim());
                saved = true;
            }

            if (!editGenre.getText().toString().trim().isEmpty()) {
                queueItem.genre_name = editGenre.getText().toString().trim();
                itemMetadata.setGenre(editGenre.getText().toString().trim());

                saved = true;

                long genreid = Utils.checkGenre(this, queueItem);

                itemMetadata.remove("genre_id");
                itemMetadata.put("genre_id", genreid);


            }

            if (!editYear.getText().toString().trim().isEmpty()) {
                queueItem.year = Utils.getInt(editYear.getText().toString().trim(), -1);
                itemMetadata.put("year", Utils.getInt(editYear.getText().toString().trim(), -1));
                saved = true;
            }

            multiTagList.add(queueItem);
            finalMultiTagMetaDataSetList.add(itemDataset);
            finalMultiTagMetaDataList.add(itemMetadata);

        }

        done();
    }

    private void done() {

        if (task != null) {
            task.cancel(true);
            task = null;
        }

        task = new TagsUpdater(this);
        task.execute();

        progressLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        saveRunning = false;
//        EventBus.getDefault().post(new RefreshEvent(1000));


    }

    @Override
    public int getActivityTheme() {

        return /*isDarkTheme ? R.style.AppThemeNormalDark :*/ R.style.AppThemeNormalLight;
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helpers.getATEKey(MultiTagActivity.this);
        toolbar.setBackgroundColor(primaryColor);
        if (MultiTagActivity.this != null) {
            MaterialDrawableBuilder builder = MaterialDrawableBuilder.with(MultiTagActivity.this)
                    .setIcon(MaterialDrawableBuilder.IconValue.CONTENT_SAVE)
                    .setSizeDp(30);
            ATEUtils.setFabBackgroundTint(fab, Config.accentColor(MultiTagActivity.this, Helpers.getATEKey(MultiTagActivity.this)));
            builder.setColor(MuzikoUtils.getBlackWhiteColor(Config.accentColor(MultiTagActivity.this, Helpers.getATEKey(MultiTagActivity.this))));
            fab.setImageDrawable(builder.build());
        }
        if (primaryColor != -1 && MultiTagActivity.this != null) {
//            collapsingToolbarLayout.setContentScrimColor(primaryColor);
            ATEUtils.setFabBackgroundTint(fab, primaryColor);

        }
        loadSongs();

    }


    public class TagsUpdater extends AsyncTask<String, String, Boolean> {
        private final Context ctx;
        private File dst;
        private MaterialDialog materialDialog;
        private boolean running = true;
        private int tagsCount = 0;

        public TagsUpdater(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                for (int i = 0; i < multiTagList.size(); i++) {

                    if (!running) break;
                    final Song queueItem = multiTagList.get(i);

                    publishProgress(multiTagList.get(i).title);

                    MusicMetadataSet itemDataset = multiTagMetaDataSetList.get(i);
                    MusicMetadata itemMetadata = multiTagMetaDataList.get(i);

                    File newfile = new File(queueItem.data);
                    File tempfile;
                    id3.update(dst, itemDataset, itemMetadata, false);

                    if (queueItem.storage == 0) {
                      /*  if (PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
                            break;
                        }
                        tempfile = id3.update(newfile, itemDataset, itemMetadata, true);
                        DocumentFile targetDocument = SAFHelpers.getDocumentFile(newfile, false);
                        if (targetDocument == null) {
                            break;
                        }

                        OutputStream out = getContentResolver().openOutputStream(targetDocument.getUri());

                        try {
                            byte[] bytes = FileUtils.readFileToByteArray(tempfile);
                            out.write(bytes);
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        tempfile.delete();*/
                    } else {
                        id3.update(newfile, itemDataset, itemMetadata, false);
                    }

                    DataHelper.updateSong(ctx,queueItem);
                    Utils.updateSongTags(ctx, queueItem);
                   // sendBroadcast(new Intent(MuzikoApp.INTENT_TRACK_CHANGED));
                    /*MultiTagActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MuzikoApp.toast(MultiTagActivity.this, "MP3 tags saved!");

                        }
                    });*/


                }
                return true;
            } catch (Exception ex) {

                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            materialDialog = new MaterialDialog.Builder(MultiTagActivity.this)
                    .title("Saving Tags")
                    .content("Saving")
                    .progress(false, multiTagList.size(), true)
                    .autoDismiss(false)
                    .negativeText("Cancel").onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            running = false;
                            dialog.dismiss();
                        }
                    })
                    .neutralText("Background").onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

        @Override
        protected void onPostExecute(Boolean result) {

            saveRunning = false;
            materialDialog.dismiss();

            if (!result) {
                MuzikoApp.toast(ctx, "There was a problem saving the tags");
            } else {

                if (running) {
                    MuzikoApp.toast(MultiTagActivity.this, "MP3 tags saved.");
                } else {
                    MuzikoApp.toast(MultiTagActivity.this, "Cancelled. MP3 tags for " + tagsCount + " songs were saved.");

                }
                finish();
//                sendBroadcast(new Intent(AppController.INTENT_RECENT_CHANGED));
//
//                sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
//
//                sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
//                sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));
//
//                AppController.Instance().serviceDirty();
//
//                EventBus.getDefault().post(new RefreshEvent(1000));

            }


        }

        @Override
        protected void onProgressUpdate(String... values) {

            tagsCount++;
            materialDialog.setContent(values[0]);
            materialDialog.incrementProgress(1);

        }
    }
}
