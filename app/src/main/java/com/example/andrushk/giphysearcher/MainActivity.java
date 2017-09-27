package com.example.andrushk.giphysearcher;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final String TRENDING_NOW = "Trending Now";
    final String API_TRENDING = "https://api.giphy.com/v1/gifs/trending?api_key=kC1KzXCpBzLPah1oGfck4769u3nDisHM&limit=15";
    final String API_SEARCH = "https://api.giphy.com/v1/gifs/search?api_key=kC1KzXCpBzLPah1oGfck4769u3nDisHM&limit=15&";

    String apiUrl;
    String searchText;
    List<Gif> gifs = new ArrayList<>();
    int screenWidth;

    EditText searchGifText;
    ImageButton btnSearch;
    ImageButton btnDeleteSearchText;
    TextView captionText;
    ProgressBar progressBar;

    private android.support.v7.widget.RecyclerView mRecyclerView;
    private android.support.v7.widget.RecyclerView.Adapter mAdapter;
    private android.support.v7.widget.RecyclerView.LayoutManager mLayoutManager;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean isTrending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        screenWidth = getScreenWidth();

        mRecyclerView = (android.support.v7.widget.RecyclerView) findViewById(R.id.rv);
        searchGifText = (EditText) findViewById(R.id.searchGifText);
        btnSearch = (ImageButton) findViewById(R.id.btnSearch);
        btnDeleteSearchText = (ImageButton) findViewById(R.id.btnDeleteSearchText);
        captionText = (TextView) findViewById(R.id.captionText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new RecyclerViewAdapter(gifs, this, screenWidth);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        btnSearch.setOnClickListener(this);
        btnDeleteSearchText.setOnClickListener(this);

        mRecyclerView.addOnScrollListener(new PaginationScrollListener((LinearLayoutManager) mLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                progressBar.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextPage();
                    }
                }, 1000);
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                trendingGifs(0);
            }
        }, 1000);

        searchGifText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    clickSearchGifs();
                    return true;
                }
                return false;
            }
        });

        searchGifText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    searchGifText.setHint("");
                else
                    searchGifText.setHint("Search all the GIFs");
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSearch:
                clickSearchGifs();
                break;
            case R.id.btnDeleteSearchText:
                clickDeleteSearch();
                break;
        }
    }

    public int getScreenWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        return display.getWidth();
    }

    public void clickSearchGifs() {
        if (!String.valueOf(searchGifText.getText()).equals("")) {
            searchGifs(0);
        } else if (isTrending) {
            clearFocusAndHideKeyboard();
        } else {
            nothingEntered();
        }
    }

    public void clickDeleteSearch() {
        clearFocusAndHideKeyboard();
        searchGifText.setText("");
        gifs.clear();
        trendingGifs(0);
        captionText.setText(TRENDING_NOW);
    }

    public void trendingGifs(int offset) {
        isTrending = true;
        if (offset != 0) {
            apiUrl = API_TRENDING + "&offset=" + offset;
        } else {
            apiUrl = API_TRENDING;
        }
        new RetrieveFeedTask(apiUrl).execute();
    }

    public void searchGifs(int offset) {
        isTrending = false;
        clearFocusAndHideKeyboard();
        searchText = String.valueOf(searchGifText.getText());
        if (offset != 0) {
            apiUrl = API_SEARCH + "&q=" + searchText.replace(" ", "+") + "&offset=" + offset;
        } else {
            gifs.clear();
            apiUrl = API_SEARCH + "&q=" + searchText.replace(" ", "+");
        }
        new RetrieveFeedTask(apiUrl).execute();
        captionText.setText("Gifs with \"" + searchText + "\"");
    }

    public void clearFocusAndHideKeyboard() {
        searchGifText.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void nothingEntered() {
        clearFocusAndHideKeyboard();
        gifs.clear();
        trendingGifs(0);
        captionText.setText(TRENDING_NOW);
    }

    public void loadNextPage() {
        if (isTrending) {
            trendingGifs(gifs.size());
        } else {
            searchGifs(gifs.size());
        }
    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        private String apiUrl;

        public RetrieveFeedTask(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        protected void onPreExecute() {
        }

        protected String doInBackground(Void... urls) {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            isLoading = false;
            isLastPage = false;

            String urlGif;
            int width;
            int height;
            JSONObject object;

            if (response == null) {
                response = "THERE WAS AN ERROR";
            }

            Log.i("INFO", response);
            try {
                object = new JSONObject(response);
                JSONArray gifs = object.getJSONArray("data");
                if (gifs.length() == 0) {
                    isLastPage = true;
                    Toast.makeText(getApplicationContext(), "Nothing found", Toast.LENGTH_SHORT).show();
                }
                int sizeGifs = MainActivity.this.gifs.size();
                if (sizeGifs != 0) {
                    MainActivity.this.gifs.remove(sizeGifs - 1);
                }
                for (int i = 0; i < gifs.length(); i++) {
                    JSONObject gif = gifs.getJSONObject(i);
                    JSONObject images = gif.getJSONObject("images");
                    JSONObject downsized = images.getJSONObject("downsized");

                    urlGif = downsized.getString("url");
                    width = downsized.getInt("width");
                    height = downsized.getInt("height");

                    MainActivity.this.gifs.add(new Gif(urlGif, width, height));
                }
                if (!isLastPage) {
                    MainActivity.this.gifs.add(new Gif(null, screenWidth, progressBar.getHeight()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }
    }
}