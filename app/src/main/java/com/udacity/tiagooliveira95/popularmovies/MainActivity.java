package com.udacity.tiagooliveira95.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.udacity.tiagooliveira95.popularmovies.Data.Database;
import com.udacity.tiagooliveira95.popularmovies.Factorys.MovieFactory;
import com.udacity.tiagooliveira95.popularmovies.Utils.NetworkUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final String JSON_ARG_RESULTS = "results";

    MovieAdapter mMovieAdapter;
    public static ArrayList<Movie> moviesTopRated = new ArrayList<>();
    public static ArrayList<Movie> moviesPopular = new ArrayList<>();
    public static ArrayList<Movie> moviesFavorites = new ArrayList<>();

    private static final int TASK_LOADER_ID = 0;

    private static final String TASK_ARG_JSON = "json";
    private static final String TASK_ARG_CORRENT_TYPE = "type";
    private static final String TASK_ARG_REQUEST = "request";
    private static final String INSERT = "insert";
    private static final String QUERY = "query";


    /**
     * Default sort type
     */
    private final String SORT_DEFAULT = Movie.POPULAR;

    public String correntType = SORT_DEFAULT;

    @BindView(R.id.movieProgressBar) ProgressBar mLoadingProgressBar;
    @BindView(R.id.errorMessage) TextView mErrorMessage;
    @BindView(R.id.mainRefresher) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_movies) RecyclerView mMovieRecyclerView;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        swipeRefreshLayout.setOnRefreshListener(this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, calculateNoOfColumns(this));
        mMovieRecyclerView.setLayoutManager(gridLayoutManager);
        mMovieAdapter = new MovieAdapter(this, this);
        mMovieRecyclerView.setHasFixedSize(true);
        mMovieRecyclerView.setAdapter(mMovieAdapter);

        if (savedInstanceState == null ||
                !savedInstanceState.containsKey(getString(R.string.moviesFavoritesKey)) &&
                        !savedInstanceState.containsKey(getString(R.string.moviesPopularKey)) &&
                        !savedInstanceState.containsKey(getString(R.string.moviesTopRatedKey)) &&
                        !savedInstanceState.containsKey(getString(R.string.sortTypeKey))) {

            if(isInternetAvailable()) {
                setMovie(correntType);
                getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);
            }else{
                Bundle bundle = new Bundle();
                bundle.putString(TASK_ARG_CORRENT_TYPE,correntType);
                bundle.putString(TASK_ARG_REQUEST,QUERY);
                getSupportLoaderManager().initLoader(TASK_LOADER_ID, bundle, this);
            }
        } else {
            moviesTopRated = savedInstanceState.getParcelableArrayList(getString(R.string.moviesTopRatedKey));
            moviesPopular = savedInstanceState.getParcelableArrayList(getString(R.string.moviesPopularKey));
            moviesFavorites = savedInstanceState.getParcelableArrayList(getString(R.string.moviesFavoritesKey));
            correntType = savedInstanceState.getString(getString(R.string.sortTypeKey));
            if(!savedInstanceState.containsKey(getString(R.string.errorMessageKey))) {
                populateRecycler(correntType);
            }else{
                showError(savedInstanceState.getString(getString(R.string.errorMessageKey)));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(getString(R.string.moviesPopularKey), moviesPopular);
        outState.putParcelableArrayList(getString(R.string.moviesTopRatedKey), moviesTopRated);
        outState.putParcelableArrayList(getString(R.string.moviesFavoritesKey),moviesFavorites);
        outState.putString(getString(R.string.sortTypeKey), correntType);
        if(mErrorMessage.getVisibility() == View.VISIBLE){
            outState.putString(getString(R.string.errorMessageKey), mErrorMessage.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sort, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_fav:
                if(correntType.equals(Movie.FAVORITE))
                    break;
                correntType = Movie.FAVORITE;
                setMovie(Movie.FAVORITE);
                return true;
            case R.id.action_sort_popular:
                if (correntType.equals(Movie.POPULAR))
                    return true;

                correntType = Movie.POPULAR;
                if (moviesPopular.isEmpty()) {
                    setMovie(Movie.POPULAR);
                } else {
                    mMovieAdapter.setMovies(moviesPopular);
                    showRecycler();
                }
                return true;
            case R.id.action_sort_topRated:
                if (correntType.equals(Movie.TOP_RATED))
                    return true;

                correntType = Movie.TOP_RATED;
                if (moviesTopRated.isEmpty()) {
                    setMovie(Movie.TOP_RATED);
                } else {
                    mMovieAdapter.setMovies(moviesTopRated);
                    showRecycler();
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v, int position) {
        Intent intent = new Intent(MainActivity.this, MovieDetails.class);
        switch (correntType){
            case Movie.FAVORITE:
                intent.putExtra(getString(R.string.movieKey), moviesFavorites.get(position));
                break;
            case Movie.POPULAR:
                intent.putExtra(getString(R.string.movieKey), moviesPopular.get(position));
                break;
            case Movie.TOP_RATED:
                intent.putExtra(getString(R.string.movieKey), moviesTopRated.get(position));
                break;
        }
        intent.putExtra(getString(R.string.typeKey), correntType);
        intent.putExtra(getString(R.string.positionKey), position);
        startActivity(intent);
    }

    /**
     * This is used to update the movie data with extra information, when we click in the poster for the first time
     * it will get the extra information for that particular movie and since we don't want to make a request
     * every time we click the poster i will update the movie data, so the second time we press the poster
     * it will noticed that the data is already there and it won't perform a new request
     */
    public static void updateMovieData(int position, String type, Movie movie) {
        if (type.equals(Movie.POPULAR)) {
            moviesPopular.set(position, movie);
        } else if (type.equals(Movie.TOP_RATED)) {
            moviesTopRated.set(position, movie);
        }
    }

    /**
     * This will set the movies data and it will display the movies in the recyclerview
     *
     * @param type sort type
     */
    public void setMovie(final String type) {
        if (!type.equals(Movie.FAVORITE)) {
            NetworkUtils.getMovieData(type, new NetworkUtils.OnServerResponce() {
                @Override
                public void onStart() {
                    showProgressBar(true);
                }

                @Override
                public void onResponse(String responce) {
                    handleResponce(type,responce,true);
                }

                @Override
                public void onFinish() {
                    showProgressBar(false);
                    showRecycler();
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void onError() {
                    showError();
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }else{
            Bundle bundle = new Bundle();
            bundle.putString(TASK_ARG_CORRENT_TYPE,Movie.FAVORITE);
            bundle.putString(TASK_ARG_REQUEST,QUERY);
            getSupportLoaderManager().restartLoader(TASK_LOADER_ID,bundle,this);
        }
    }

    /**
     * Processa os favoritos
     *
     * @param json array de strings json
     */
    public void handleFavorites(@NotNull String... json){
        moviesFavorites.clear();
        for (String aJson : json) {
            try {
                JSONObject results = new JSONObject(aJson);
                moviesFavorites.add(MovieFactory.createMovie(results));
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }
        populateRecycler(Movie.FAVORITE);
    }

    /**
     *  Processa a resposta do servidor ou da base de dados
     *
     * @param type favoritos, top rated ou populares
     * @param responce resposta do servidor ou base de dados
     * @param saveData devemos guardar a resposta na base de dados?
     */
    public void handleResponce(String type,String responce,boolean saveData){
        try {
            //Returns the array of movies
            JSONArray results = new JSONObject(responce).getJSONArray(JSON_ARG_RESULTS);

            /**
             * For each movie i will call createMovie and pass the jsonObject that will contain a single movie data
             * And we will add the movie to the corresponding ArrayList
             */
            for (int k = 0; k < results.length(); k++) {
                if (type.equals(Movie.POPULAR)) {
                    moviesPopular.add(MovieFactory.createMovie(results.getJSONObject(k)));
                } else if (type.equals(Movie.TOP_RATED)) {
                    moviesTopRated.add(MovieFactory.createMovie(results.getJSONObject(k)));
                }
            }



        if(saveData) {
            Bundle bundle = new Bundle();
            bundle.putString(TASK_ARG_JSON, responce);
            bundle.putString(TASK_ARG_CORRENT_TYPE, type);
            bundle.putString(TASK_ARG_REQUEST,INSERT);
            getSupportLoaderManager().restartLoader(TASK_LOADER_ID, bundle, MainActivity.this);

        }

            /**
             * Now that we have all the information, i will call populateRecycler
             */

            populateRecycler(type);
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    /**
     * Populates the recycler depending on the sortType passed
     */
    public void populateRecycler(String sortType) {
        showRecycler();
        switch (sortType){
            case Movie.POPULAR:
                mMovieAdapter.setMovies(moviesPopular);
                break;
            case Movie.FAVORITE:
                mMovieAdapter.setMovies(moviesFavorites);
                break;
            case Movie.TOP_RATED:
                mMovieAdapter.setMovies(moviesTopRated);
                break;
        }
    }

    /**
     * Shows progress bar if @param state is true, otherwise it will hide the progress bar
     */
    void showProgressBar(boolean state) {
        if (!swipeRefreshLayout.isRefreshing()) {
            mLoadingProgressBar.setVisibility(state ? View.VISIBLE : View.INVISIBLE);
            mErrorMessage.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Shows recyclerView
     */
    void showRecycler() {
        mMovieRecyclerView.setVisibility(View.VISIBLE);
        mErrorMessage.setVisibility(View.INVISIBLE);
    }

    /**
     * Hides ProgressBar, shows error message and hides the recycler
     */
    void showError() {
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
        mErrorMessage.setText(getString(R.string.error_no_server_responce));
        mMovieRecyclerView.setVisibility(View.INVISIBLE);
    }

    void showError(String error) {
        mLoadingProgressBar.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
        mErrorMessage.setText(error);
        mMovieRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onRefresh() {
        if(MainActivity.isInternetAvailable())
            setMovie(correntType);
        else {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, R.string.noInternet, Toast.LENGTH_SHORT).show();
        }
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / 180);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                if(args == null){
                    return;
                }

                forceLoad();
            }

            @Override
            public Cursor loadInBackground() {
                String type = args.getString(TASK_ARG_CORRENT_TYPE,"");
                String rq = args.getString(TASK_ARG_REQUEST,"");
                switch (rq) {
                    case INSERT:
                        final String jsonData = args.getString(TASK_ARG_JSON, "");
                        switch (type) {
                            case Movie.POPULAR:
                                ContentValues content = new ContentValues();
                                content.put(Database.DatabaseContract.COLUMN_NAME_JSON_DATA, jsonData);
                                getContentResolver().insert(Database.DatabaseContract.POPULAR_CONTENT_URI, content);
                                return null;
                            case Movie.TOP_RATED:
                                content = new ContentValues();
                                content.put(Database.DatabaseContract.COLUMN_NAME_JSON_DATA, jsonData);
                                getContentResolver().insert(Database.DatabaseContract.TOP_CONTENT_URI, content);
                                return null;
                            default:
                                throw new UnsupportedOperationException(String.format(Locale.getDefault(), "Unknown type: '%s'", type));
                        }
                    case QUERY:
                        switch (type) {
                            case Movie.POPULAR:
                                return getContentResolver().query(Database.DatabaseContract.POPULAR_CONTENT_URI, null, null, null, null);
                            case Movie.TOP_RATED:
                                return getContentResolver().query(Database.DatabaseContract.TOP_CONTENT_URI, null, null, null, null);
                            case Movie.FAVORITE:
                                return getContentResolver().query(Database.DatabaseContract.FAV_CONTENT_URI, null, null, null, null);
                            default:
                                throw new UnsupportedOperationException(String.format(Locale.getDefault(), "Unknown type: '%s'", type));
                        }
                    default:
                        throw new UnsupportedOperationException(String.format(Locale.getDefault(), "Unknown request id: '%d'", id));
                }
            }
        };

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null && !correntType.equals(Movie.FAVORITE)){
            if(data.moveToFirst()) {
                handleResponce(correntType, data.getString(data.getColumnIndex(Database.DatabaseContract.COLUMN_NAME_JSON_DATA)), false);
            }else{
                showError();
            }
        }else if (data != null && correntType.equals(Movie.FAVORITE)){
            int count = data.getCount();
            if(count > 0) {
                if(count != moviesFavorites.size()) {
                    String[] json = new String[data.getCount()];
                    data.moveToFirst();
                    for (int p = 0; p < data.getCount(); p++) {
                        json[p] = data.getString(data.getColumnIndex(Database.DatabaseContract.COLUMN_NAME_JSON_DATA));
                        data.moveToNext();
                    }
                    handleFavorites(json);
                }else{
                    mMovieAdapter.setMovies(moviesFavorites);
                }
            }else{
                showError(getString(R.string.noFavorites));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public static boolean isInternetAvailable() {
        try {
            return (Runtime.getRuntime().exec ("ping -c 1 google.com").waitFor() == 0);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }


}
