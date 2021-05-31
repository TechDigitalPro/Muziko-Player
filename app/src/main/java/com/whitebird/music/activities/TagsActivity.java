package com.whitebird.music.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.palette.graphics.Palette;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
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
import com.whitebird.music.utils.ImageUtils;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.Utils;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TagsActivity extends BaseActivity implements View.OnClickListener, ATEActivityThemeCustomizer {
    private final String TAG = TagsActivity.class.getSimpleName();
    private Song item;
    private RelativeLayout progressLayout;
    private ProgressBar progressBar;
    private ImageView coverArtImage;
    private TextView progress;
    private TextView titletext;
    private TextView subtitletext;
    private CoordinatorLayout coordinatorlayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private EditText editTitle;
    private EditText editArtist;
    private EditText editAlbum;
    private AutoCompleteTextView editGenre;
    private EditText editNumber;
    private EditText editYear;
    private EditText editLyrics;
    private TextView lyricsSyncedTextView;
    private FloatingActionButton fab;
    private MyID3 id3 = new MyID3();

    private MusicMetadataSet dataset;
    private MusicMetadata metadata;
    private String artistName;
    private String albumName;
    private String genreName;
    private TagsUpdater task = null;
    private boolean saveRunning = false;
    private File newfile;
    private boolean lyricsChanged = false;
    private boolean isLrc = false;
    private Toolbar toolbar;
    private boolean isFav;
    private boolean isLibrary;
    private boolean loadFailed = false;
    private int primaryColor = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_tags);
        item = (Song) getIntent().getSerializableExtra("item");

        if (item == null) {
            MuzikoApp.toast(this, "Song not found!");
            finish();
            return;
        }
        FindViewsById();


        /*collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, R.color.normal_blue));
        collapsingToolbarLayout.setStatusBarScrimColor(ContextCompat.getColor(this, R.color.normal_blue));*/


        fab.setVisibility(View.GONE);
        fab.setOnClickListener(this);

        editTitle.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.equals("")) {
                    fab.setVisibility(View.VISIBLE);
                }

            }


            public void afterTextChanged(Editable s) {

            }
        });

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

        editNumber.addTextChangedListener(new TextWatcher() {

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

        editLyrics.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.equals("")) {
                    fab.setVisibility(View.VISIBLE);
                    lyricsChanged = true;
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


        LoadRunnable loadRunnable = new LoadRunnable();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
//            pendingRunnable = runnable;
//            new MaterialDialog.Builder(getActivity()).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Grant SD card permissions").content(message).positiveText("OK").onPositive((dialog, which) -> triggerStorageAccessFramework()).negativeText("Cancel").show();
//
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
//            checkStoragePermissions("Do you want to grant SD card permissions so tags can be changed?", loadRunnable);
//        } else {
//        hck here
        loadRunnable.run();
//        }
    }

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
    public void onBackPressed() {

        finish();
    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    private void FindViewsById() {
        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        appBarLayout = findViewById(R.id.appBarLayout);
        toolbar = findViewById(R.id.toolbar);
        titletext = findViewById(R.id.titletext);
        subtitletext = findViewById(R.id.subtitletext);

        progressLayout = findViewById(R.id.progressLayout);
        progress = findViewById(R.id.progress);
        progressBar = findViewById(R.id.progressBar);
        coverArtImage = findViewById(R.id.coverArtImage);
        editTitle = findViewById(R.id.editTitle);
        editArtist = findViewById(R.id.editArtist);
        editAlbum = findViewById(R.id.editAlbum);
        editGenre = findViewById(R.id.editGenre);
        editNumber = findViewById(R.id.editNumber);
        editYear = findViewById(R.id.editYear);
        editLyrics = findViewById(R.id.editLyrics);
        lyricsSyncedTextView = findViewById(R.id.lyricsSyncedTextView);
        fab = findViewById(R.id.fab);


        setInputTextLayoutColor(editTitle, Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this)));
        setInputTextLayoutColor(editArtist, Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this)));
        setInputTextLayoutColor(editNumber, Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this)));
        setInputTextLayoutColor(editLyrics, Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this)));
        setInputTextLayoutColor(editAlbum, Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this)));
        setInputTextLayoutColor(editGenre, Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this)));
        setInputTextLayoutColor(editYear, Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this)));

        artistName = item.artistName;
        albumName = item.albumName;
        genreName = item.genre_name;

        titletext.setText(item.title);
        subtitletext.setText(item.albumName);

        List<String> entries = new ArrayList<>();
        ArrayList<Song> genres = DataHelper.getAllGenre();
        for (Song genre : genres) {
            entries.add(genre.title);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, entries);
        editGenre.setAdapter(adapter);
        editGenre.setThreshold(1);

        editTitle.clearFocus();
        loadcoverArt();

        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setTitle(item.title);
    }

    private void loadcoverArt() {
        ImageUtils.loadAlbumArtIntoView(item.albumId, coverArtImage, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        loadFailed = true;
                        MaterialDrawableBuilder builder = MaterialDrawableBuilder.with(TagsActivity.this)
                                .setIcon(MaterialDrawableBuilder.IconValue.CONTENT_SAVE)
                                .setColor(MuzikoUtils.getBlackWhiteColor(Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this))));
                        ATEUtils.setFabBackgroundTint(fab, Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this)));
                        fab.setImageDrawable(builder.build());
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        try {
                            new Palette.Builder(loadedImage).generate(new Palette.PaletteAsyncListener() {
                                                                          @Override
                                                                          public void onGenerated(Palette palette) {
                                                                              Palette.Swatch swatch = palette.getVibrantSwatch();
                                                                              if (swatch != null) {
                                                                                  primaryColor = swatch.getRgb();
                                                                                  collapsingToolbarLayout.setContentScrimColor(primaryColor);
                                                                                  if (TagsActivity.this != null)
                                                                                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                                                          getWindow().setStatusBarColor(primaryColor);
                                                                                      }
                                                                              } else {
                                                                                  Palette.Swatch swatchMuted = palette.getMutedSwatch();
                                                                                  if (swatchMuted != null) {
                                                                                      primaryColor = swatchMuted.getRgb();
                                                                                      collapsingToolbarLayout.setContentScrimColor(primaryColor);
                                                                                      if (TagsActivity.this != null)
                                                                                          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                                                              getWindow().setStatusBarColor(primaryColor);
                                                                                          }
                                                                                  }
                                                                              }

                                                                              if (TagsActivity.this != null) {
                                                                                  MaterialDrawableBuilder builder = MaterialDrawableBuilder.with(TagsActivity.this)
                                                                                          .setIcon(MaterialDrawableBuilder.IconValue.CONTENT_SAVE)
                                                                                          .setSizeDp(30);
                                                                                  if (primaryColor != -1) {
                                                                                      builder.setColor(MuzikoUtils.getBlackWhiteColor(primaryColor));
                                                                                      ATEUtils.setFabBackgroundTint(fab, primaryColor);
                                                                                      fab.setImageDrawable(builder.build());
                                                                                  } else {
                                                                                      if (TagsActivity.this != null) {
                                                                                          ATEUtils.setFabBackgroundTint(fab, Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this)));
                                                                                          builder.setColor(MuzikoUtils.getBlackWhiteColor(Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this))));
                                                                                          fab.setImageDrawable(builder.build());
                                                                                      }
                                                                                  }
                                                                              }
                                                                          }
                                                                      }

                            );
                        } catch (
                                Exception ignored
                                )

                        {

                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                    }

                }

        );

//        toolbar.setItemColor(Color.WHITE);
    }

    private void InvalidFileExtensionError(String extension) {
        new MaterialDialog.Builder(TagsActivity.this).theme(Theme.LIGHT)
                .negativeColor(Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this)))
                .titleColor(Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this)))
                .neutralColorRes(R.color.dialog_negetive_button)
                .positiveColor(Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this))).title("Invalid file format")
                .content("This is a ." + extension + " file. Only mp3 files are supported.").positiveText("OK").onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(TagsActivity.this);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("prefStoragePerms", false);
                editor.apply();
                finish();
            }
        }).show();

    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_tags, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                save();
                return true;

            case R.id.action_goto:
                AppController.Instance().search(editTitle.getText().toString());
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

            case R.id.action_share:
                AppController.Instance().shareApp();
                return true;
            case R.id.sharing_wifi:
                Intent shareIntent =
                        new Intent(TagsActivity.this, ShareWifiActivity.class);
                startActivity(shareIntent);
                return true;
            case R.id.action_exit:
                AppController.Instance().exit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    private void GeneralTagReadError() {
        new MaterialDialog.Builder(TagsActivity.this).theme(Theme.LIGHT)
                .negativeColor(Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this)))
                .titleColor(Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this)))
                .neutralColorRes(R.color.dialog_negetive_button)
                .positiveColor(Config.accentColor(TagsActivity.this, Helpers.getATEKey(TagsActivity.this))).title("Error Reading Tags")
                .content("Unable to read tags from this track").positiveText("OK").onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(TagsActivity.this);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("prefStoragePerms", false);
                editor.apply();
                finish();
            }
        }).show();

    }

    @Override
    public void onClick(View view) {
        if (view == fab) {
            save();
        }
    }

    private void showData() {
        String title = item.title;
        if (title != null) editTitle.setText(item.title);

        String artist = item.artistName;
        if (artist != null) editArtist.setText(item.artistName);

        String album = item.albumName;
        if (album != null) editAlbum.setText(item.albumName);

        String genre = item.genre_name;
        if (genre != null) editGenre.setText(item.genre_name);

        Number number = item.trackNumber;
        if (number != null) editNumber.setText(item.trackNumber + "");

        Object year = item.year;
        if (year != null) {
            if (year instanceof Number)
                editYear.setText(year.toString());
            else if (year instanceof String)
                editYear.setText(((String) year));
        }


   /*     if (item.lyrics != null) {
            if (item.lRC) {
                isLrc = true;
                lyricsSyncedTextView.setVisibility(View.VISIBLE);
                String staticLyrics = LyricsHelper.getStaticLyrics(item.data);
                editLyrics.setText(Utils.fromHtml(staticLyrics));
            } else {
                isLrc = false;
                lyricsSyncedTextView.setVisibility(View.GONE);
                editLyrics.setText(Utils.fromHtml(item.lyrics));
            }
        }*/
    }

    private void save() {

        if (saveRunning) {
            return;
        }

        saveRunning = true;

        progressLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        item.title = editTitle.getText().toString().trim();

        item.artistName = editArtist.getText().toString().trim();

        item.albumName = editAlbum.getText().toString().trim();

        item.trackNumber = Utils.getInt(editNumber.getText().toString().trim(), 0);
        if (editYear.getText() != null) {
            item.year = Utils.getInt(editYear.getText().toString().trim(), -1);
        }
        item.genre_name = editGenre.getText().toString().trim();

        if (lyricsChanged) {
            isLrc = false;

            String lyrics = Utils.toHtml(editLyrics.getText());

            lyrics = lyrics.replaceAll("<(.*?)\\>", " ");//Removes all items in brackets
            lyrics = lyrics.replaceAll("<(.*?)\\\n", " ");//Must be undeneath
            lyrics = lyrics.replaceFirst("(.*?)\\>", " ");//Removes any connected item to the last bracket
            lyrics = lyrics.replaceAll("&nbsp;", " ");
            lyrics = lyrics.replaceAll("&amp;", " ");

            // clean up junk from html
//			String removedStuff = lyrics.replace("dir=\"ltr\">", "");

//            item.lyrics = lyrics;
        }

        if (item.title.length() == 0) {
            item.title = "Unknown";
        }

        if (item.artistName.length() == 0) {
            item.artistName = "Unknown Artist";
        }

        if (item.albumName.length() == 0) {
            item.albumName = "Unknown Album";
        }

        try {

            if (item.genre_name.length() > 0) {

                long genreid = Utils.checkGenre(this, item);
                metadata.remove("genre_id");
                metadata.put("genre_id", genreid);


            } else {
                item.genre_name = "Unknown Genre";
                metadata.remove("genre_id");
                metadata.put("genre_id", -1);

            }


            metadata.setSongTitle(item.title);
            metadata.setArtist(item.artistName);
            metadata.setAlbum(item.albumName);

            metadata.setGenre(item.genre_name);
            metadata.setTrackNumber(item.trackNumber);

            if (item.year != -1) {
                metadata.put("year", item.year);
            }

        } catch (Exception e) {
            e.printStackTrace();
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
        String ateKey = Helpers.getATEKey(TagsActivity.this);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        if (primaryColor != -1 && TagsActivity.this != null) {
            collapsingToolbarLayout.setContentScrimColor(primaryColor);
            ATEUtils.setFabBackgroundTint(fab, primaryColor);

        }


    }

    private class LoadRunnable implements Runnable {
        @Override
        public void run() {

            File from = new File(item.data);

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && item.storage == 2) {
//                boolean getperms = false;
//                if (!PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
//                    DocumentFile targetDocument = SAFHelpers.getDocumentFile(openedExternalFile, false);
//                    if (targetDocument == null) {
//                        getperms = true;
//                    }
//                }
//            }

            final String extension = FilenameUtils.getExtension(from.getAbsolutePath());
            try {
                dataset = id3.read(from);      //read metadata
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (dataset != null) {
                metadata = (MusicMetadata) dataset.getSimplified();
            }

            // Create a fake result (MUST be final)
            final boolean result = true;

            // Send the result to the UI thread and show it on a Toast

            TagsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (metadata == null || dataset == null) {
                        if (extension.equals("mp3")) {
                            GeneralTagReadError();
                        } else {
                            InvalidFileExtensionError(extension);
                        }
                    } else {
                        showData();
                    }
                }
            });


        }
    }

    public class TagsUpdater extends AsyncTask<String, String, Boolean> {
        private Context ctx;
        private ArrayList<Song> list = new ArrayList<>();

        public TagsUpdater(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {

                newfile = new File(item.data);
                File tempfile = null;

                if (item.storage == 2) {
               /*     if (PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
                        return false;
                    }
                    tempfile = id3.update(newfile, dataset, metadata, true);
                    DocumentFile targetDocument = Utils.getDocumentFile(newfile, false);
                    if (targetDocument == null) {
                        return false;
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
                    id3.update(newfile, dataset, metadata, false);
                }

               /* TrackRealmHelper.updateTrackTags(item, lyricsChanged, isLrc);

                if (isLibrary) {
                    FirebaseManager.Instance().deleteLibrary(item);
                }

                if (isFav) {
                    FirebaseManager.Instance().deleteFav(item);
                }

                item.md5 = MD5.calculateMD5(new File(item.data));
                TrackRealmHelper.updateMD5Hash(item);

                if (isLibrary) {
                    TrackRealmHelper.toggleLibrary(item, isLibrary);
                    FirebaseManager.Instance().uploadLibrary(item);
                }

                if (isFav) {
                    TrackRealmHelper.toggleSync(item, true);
                    TrackRealmHelper.toggleFavorite(item, true);
                    FirebaseManager.Instance().uploadFav(item);
                }

                MyApplication.ignoreNextMediaScan = true;

                // Update queue list
                for (Song queue : PlayerConstants.QUEUE_LIST) {
                    if (item.data.equals(queue.data)) {
                        queue.setTags(item);
                    }
                }
                // Update queue song
                if (PlayerConstants.QUEUE_SONG.data.equals(item.data)) {
                    PlayerConstants.QUEUE_SONG = item;
                }

                AlbumArtRealmHelper.updateStatus(item.artist_name, item.album_name);
                MediaStoreHelper.updateSongTags(ctx, item);
//                Utils.scanMedia(ctx, item.data);



                AppController.Instance().serviceDirty();

                Intent trackEditIntent = new Intent(AppController.INTENT_TRACK_EDITED);
                if (albumName.equals(item.album_name)) {
                    trackEditIntent.putExtra("tag", getIntent().getStringExtra("tag"));
                    trackEditIntent.putExtra("index", getIntent().getIntExtra("index", -1));
                    trackEditIntent.putExtra("item", item);
                }
                sendBroadcast(trackEditIntent);
                sendBroadcast(new Intent(AppController.INTENT_RECENT_CHANGED));
                sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));*/



                DataHelper.updateSong(ctx,item);

                Utils.updateSongTags(ctx, item);



                TagsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MuzikoApp.toast(TagsActivity.this, "MP3 tags saved!");

                    }
                });
                finish();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //progressBar.setVisibility(trackList.size() > 0 ? View.GONE : View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                MuzikoApp.toast(ctx, "There was a problem saving the tags");



            }
        }
    }
}
