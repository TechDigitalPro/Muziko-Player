package com.whitebird.music.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.adapters.AlbumArtInternetAdapter;
import com.whitebird.music.api.GoogleCustomSearch.GoogleImageSearchFilters;
import com.whitebird.music.api.GoogleCustomSearch.models.GoogleImageSearchResults;
import com.whitebird.music.control.EndlessRecyclerOnScrollListener;
import com.whitebird.music.listeners.BasicRecyclerItemListener;
import com.whitebird.music.models.Song;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class AlbumArtInternetActivity extends BaseActivity implements BasicRecyclerItemListener, SearchView.OnQueryTextListener {
    private final ArrayList<GoogleImageSearchResults> images = new ArrayList<>();

    private RelativeLayout progressLayout;
    private TextView progress;
    private TextView search_no_results;
    private RecyclerView mrecyclerView;
    private AlbumArtInternetAdapter mAdapter;
    private Toolbar toolbar;
    private Song item;
    private MenuItem menuItemSearch;
    private SearchView searchView = null;
    private GoogleImageSearchFilters filters;
    private String searchQuery = "";
    private int selectedItemPosition;
    private GoogleImageSearchResults image;

    GridLayoutManager layoutManager;

    private Target picassoTarget = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            // loading of the bitmap was a success
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                    byte[] byteArray = stream.toByteArray();

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("item", item);
                    returnIntent.putExtra("index", selectedItemPosition);
                    returnIntent.putExtra("filepath", image.imageUrl);
                    returnIntent.putExtra("image", byteArray);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            });
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setStartActivity(false);
        setContentView(R.layout.activity_album_art_internet);

        findViewsById();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
           // window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_blue));
        }

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        item = (Song) getIntent().getSerializableExtra("item");
        selectedItemPosition = getIntent().getIntExtra("index", 0);
        if (item == null) {
            MuzikoApp.toast(this, getString(R.string.album_not_found));
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
            return;
        }
        actionBar.setTitle(item.albumName);


        progressLayout = findViewById(R.id.progressLayout);
        search_no_results = findViewById(R.id.search_no_results);
        progress = findViewById(R.id.progress);

        mAdapter = new AlbumArtInternetAdapter(this, images, this);
        /*GridLayoutManager gridlm = new GridLayoutManager(this, 2);
        mrecyclerView.setLayoutManager(gridlm);*/
        setLayoutManager();
        int spanCount = 2; // 3 columns
        int spacing = 30; // 50px
        boolean includeEdge = true;
//		mrecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
        mrecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(layoutManager) {

            @Override
            public void onLoadMore(int current_page) {

                if (MuzikoApp.isConnectingToInternet(AlbumArtInternetActivity.this)) {
                    do_search(searchQuery, current_page);
                } else {
                    MuzikoApp.toast(AlbumArtInternetActivity.this, getString(R.string.no_network_connection));
                }

            }
        });
        mrecyclerView.setAdapter(mAdapter);

        filters = new GoogleImageSearchFilters();
//		filters.setImageSiteFilter("Medium");

        progressLayout.setVisibility(View.VISIBLE);
        mrecyclerView.setVisibility(View.GONE);

        if (!MuzikoApp.isConnectingToInternet(AlbumArtInternetActivity.this)) {
            MuzikoApp.toast(this, getString(R.string.no_network_connection));
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
            return;
        }
        searchQuery = item.albumName;
        do_search(searchQuery, 0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLayoutManager();
    }
    @Override
    public void onBackPressed() {
        if (menuItemSearch != null) {
            SearchView searchView = (SearchView) menuItemSearch.getActionView();


            if (searchView != null && !searchView.isIconified()) {
                searchView.setIconified(true);
                return;
            }


        }

        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
		super.onBackPressed();

    }
    private void setLayoutManager() {

        int Orientation = getResources().getConfiguration().orientation;


            if (Orientation == Configuration.ORIENTATION_PORTRAIT)
                layoutManager = new GridLayoutManager(AlbumArtInternetActivity.this, MuzikoApp.getInstance().NoOfColumns());//2
            else
                layoutManager = new GridLayoutManager(AlbumArtInternetActivity.this,  MuzikoApp.getInstance().NoOfColumns());//4


        mrecyclerView.setLayoutManager(layoutManager);
    }
    private void findViewsById() {
        toolbar = findViewById(R.id.toolbar);
        mrecyclerView = findViewById(R.id.itemList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_albumartinternet, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        if (searchView != null)


        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery(item.albumName,false);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //do what you want  searchview is not expanded
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (MuzikoApp.isConnectingToInternet(AlbumArtInternetActivity.this)) {
            searchQuery = query;
            images.clear();
            mAdapter.notifyDataSetChanged();
            do_search(searchQuery, 0);
            searchView.clearFocus();
        } else {
            MuzikoApp.toast(AlbumArtInternetActivity.this, getString(R.string.no_network_connection));
        }

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onItemClicked(final int position) {
        progress.setText(R.string.saving_album_art);

        progressLayout.setVisibility(View.VISIBLE);
        mrecyclerView.setVisibility(View.GONE);

        image = mAdapter.getItem(position);

        mrecyclerView.setAdapter(null);

        Glide.with(this)
                .asBitmap()
                .load(image.imageUrl)
                //.resize(IMAGE_MEDIUM_SIZE, IMAGE_MEDIUM_SIZE)
                //.into((ImageView) picassoTarget);
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, com.bumptech.glide.request.target.Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        resource.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                        byte[] byteArray = stream.toByteArray();

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("item", item);
                        returnIntent.putExtra("index", selectedItemPosition);
                        returnIntent.putExtra("filepath", image.imageUrl);
                        returnIntent.putExtra("image", byteArray);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                        return false;
                    }
                }).submit();
    }

    private String generateQueryString(String query, int page) {
        String uri = "";
        String start = "";

        if (page > 0) {
            start = String.format("&start=%d", page * 20); // TODO: Make this use the nextpage value from the json
        }

        try {
            uri = String.format("%s?key=%s&cx=%s&searchType=image&q=%s%s%s",
                    "https://www.googleapis.com/customsearch/v1",
                    getString(R.string.google_search_key),
                    "018294540985465818514:-9ekt8uawu4",
                    URLEncoder.encode(query, "UTF-8"),
                    start,
                    filters.getFilterParams()
            );
        } catch (UnsupportedEncodingException exception) {
            // TODO: Handle this
            exception.printStackTrace();
        }

        return uri;
    }


    private void do_search(String query, int page) {
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        String url = generateQueryString(query, page);


        Log.d("blah", url);

        client.get(url, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Log.d("blah", response.toString());
                    images.addAll(GoogleImageSearchResults.parseJSON(response));
                    Log.d("blah", "Stream Size: " + images.size());
                    mAdapter.notifyDataSetChanged();

                    progressLayout.setVisibility(View.GONE);
                    mrecyclerView.setVisibility(View.VISIBLE);

                    if (images.isEmpty()) {
                        search_no_results.setVisibility(View.VISIBLE);
                    } else {
                        search_no_results.setVisibility(View.GONE);
                    }

                } catch (JSONException ex) {
                    Log.d("GIS JSONException", "JSON Exception: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
               // MuzikoApp.toast(AlbumArtInternetActivity.this, getString(R.string.problem_connecting_google));
                if (errorResponse != JSONObject.NULL)
//					Log.d("blah", "Obj: " + errorResponse.toString());
                    progressLayout.setVisibility(View.GONE);
                search_no_results.setVisibility(View.VISIBLE);
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONArray errorResponse) {
           //     MuzikoApp.toast(AlbumArtInternetActivity.this, getString(R.string.problem_connecting_google));
                progressLayout.setVisibility(View.GONE);
                search_no_results.setVisibility(View.VISIBLE);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
             //   MuzikoApp.toast(AlbumArtInternetActivity.this, getString(R.string.problem_connecting_google));
                progressLayout.setVisibility(View.GONE);
                search_no_results.setVisibility(View.VISIBLE);
                super.onFailure(statusCode, headers, responseString, throwable);
            }

        });
    }

}
