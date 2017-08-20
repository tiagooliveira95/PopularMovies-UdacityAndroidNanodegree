package com.udacity.tiagooliveira95.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.udacity.tiagooliveira95.popularmovies.Data.Database;
import com.udacity.tiagooliveira95.popularmovies.Factorys.MovieFactory;
import com.udacity.tiagooliveira95.popularmovies.Utils.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;


import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetails extends AppCompatActivity implements OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = MovieDetails.class.getSimpleName();

    @BindView(R.id.poster_details)
    ImageView moviePoster;
    @BindView(R.id.cover_bg_details)
    ImageView movieBg;
    @BindView(R.id.loadingProgressBar)
    ProgressBar mLoadingProgressBar;
    @BindView(R.id.txt_movie_title)
    TextView movieTitle;
    @BindView(R.id.txt_movie_details)
    TextView movieDetails;
    @BindView(R.id.txt_Movie_details_error)
    TextView mTextErrorMessage;
    @BindView(R.id.posterNoImageError)
    TextView noImagePosterTextError;
    @BindView(R.id.coverNoImageError)
    TextView noImageCoverTextError;
    @BindView(R.id.detailsRefresher)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.extraMovieDetails)
    TextView mExtraMovieDetails;
    @BindView(R.id.trailer_recycler)
    RecyclerView trailerRecycler;
    @BindView(R.id.review_recycler)
    RecyclerView reviewRecycler;
    TextView txtReviews;
    TextView txtTrailers;


    ReviewsAndTrailerAdapter trailerAdapter, reviewAdapter;


    int positionIndex = -1;
    String correntType;


    final int DEFAULT_STAR = R.drawable.ic_star_border_white_24dp;
    final int ACTIVE_STAR = R.drawable.ic_star_white_24dp;
    Movie movie;
    MenuItem menuItemStar;


    private static final int TASK_LOADER_ID = 3;
    private static final String TASK_ARG_JSON = "json";
    private static final String TASK_ARG_TYPE = "type";

    private static final int TYPE_INSERT = 1;
    private static final int TYPE_QUERY = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        ButterKnife.bind(this);
        swipeRefreshLayout.setOnRefreshListener(this);

        if (savedInstanceState == null || !savedInstanceState.containsKey(getString(R.string.positionKey)) && !savedInstanceState.containsKey(getString(R.string.movieKey)) && !savedInstanceState.containsKey(getString(R.string.typeKey))) {
            getIntentData();
        } else {
            movie = savedInstanceState.getParcelable(getString(R.string.movieKey));
            positionIndex = savedInstanceState.getInt(getString(R.string.positionKey));
            correntType = savedInstanceState.getString(getString(R.string.typeKey));
        }

        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, MovieDetails.this);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(movie.getTitle());
        if (!getResources().getBoolean(R.bool.tablet)) {
            txtReviews = (TextView) findViewById(R.id.txtReviews);
            txtTrailers = (TextView) findViewById(R.id.txtTrailers);
        }

        loadImages();
        loadMovieDetails();

        loadExtraMovieData();
    }

    void getIntentData() {
        Intent intent = getIntent();
        positionIndex = intent.getIntExtra(getString(R.string.positionKey), -1);
        correntType = intent.getStringExtra(getString(R.string.typeKey));
        movie = intent.getParcelableExtra(getString(R.string.movieKey));
    }

    /**
     * Loads images if image is null shows a view that will display No Image
     */
    void loadImages() {
        if (movie.getBackdropPath() != null) {
            Picasso.with(this).load(movie.getBackdropPath()).into(movieBg, picassoCallBack());
        } else {
            noImageCoverTextError.setVisibility(View.VISIBLE);
        }

        if (movie.getPosterURL() != null) {
            Picasso.with(this).load(movie.getPosterURL()).into(moviePoster, picassoCallBack(noImagePosterTextError, moviePoster));
        } else {
            noImagePosterTextError.setVisibility(View.VISIBLE);
        }
    }

    public Callback picassoCallBack(final View... views) {
        return new Callback() {
            @Override
            public void onSuccess() {
                if (views != null) {
                    for (int k = 0; k < views.length; k++) {
                        if (k == 0) {
                            views[0].setVisibility(View.INVISIBLE);
                        } else {
                            views[k].setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onError() {
                if (views != null) {
                    for (int k = 0; k < views.length; k++) {
                        if (k == 0) {
                            views[0].setVisibility(View.VISIBLE);
                        } else {
                            views[k].setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        };
    }

    void loadMovieDetails() {
        movieTitle.setText(movie.getOriginalTitle());
        movieDetails.setText(movie.getOverview());
        mExtraMovieDetails.setText(
                String.format(
                        getString(R.string.movieDetails),
                        movie.getReleaseDate(),
                        movie.getOriginalLanguage(),
                        movie.getVoteAverage()
                ));
    }

    /**
     * This will load or call getExtraMovieData() to get the extra data for the movie that we didn't get on the first call, like trailers and reviews
     */
    void loadExtraMovieData() {
        if (!movie.wasExtraDataSet()) {
            getExtraMovieData();
            return;
        }
        populateData();
    }

    void populateData() {
        if (movie.getTrailerKeys() != null && movie.getTrailerKeys().length > 0) {
            showProgressBar(false);
            showTrailerContainer();

            LinearLayoutManager trailerAdapterLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            trailerRecycler.setLayoutManager(trailerAdapterLinearLayoutManager);
            trailerAdapter = new ReviewsAndTrailerAdapter(this, ReviewsAndTrailerAdapter.TYPE_TRAILERS, movie);
            trailerRecycler.setAdapter(trailerAdapter);
            trailerAdapter.setOnItemClickListener(this);
        }

        if (movie.getReviewAuthors() != null && movie.getReviewAuthors().length > 0) {
            LinearLayoutManager reviewAdapterLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            reviewRecycler.setLayoutManager(reviewAdapterLinearLayoutManager);
            reviewAdapter = new ReviewsAndTrailerAdapter(this, ReviewsAndTrailerAdapter.TYPE_REVIEWS, movie);
            reviewRecycler.setAdapter(reviewAdapter);
            reviewAdapter.setOnItemClickListener(this);
            showReviewContainer();
            showProgressBar(false);
        }
    }

    void getExtraMovieData() {
        if (!MainActivity.isInternetAvailable()) {
            Bundle bundle = new Bundle();
            bundle.putInt(TASK_ARG_TYPE, TYPE_QUERY);
            getSupportLoaderManager().restartLoader(TASK_LOADER_ID, bundle, this);
            return;
        }
        NetworkUtils.getExtraMovieData(movie.getMovieID(), new NetworkUtils.OnServerResponce() {
            @Override
            public void onStart() {
                showProgressBar(true);
            }

            @Override
            public void onResponse(String responce) {
                try {
                    handleResponce(responce);
                    Bundle bundle = new Bundle();
                    bundle.putString(TASK_ARG_JSON, responce);
                    bundle.putInt(TASK_ARG_TYPE, TYPE_INSERT);
                    getSupportLoaderManager().restartLoader(TASK_LOADER_ID, bundle, MovieDetails.this);
                } catch (JSONException e) {
                    e.printStackTrace();
                    movie.setWasExtraDataSet(false);
                    showProgressBar(false);
                    showError();
                }
            }

            @Override
            public void onFinish() {
                showProgressBar(false);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onError() {
                showProgressBar(false);
                showError();
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    void handleResponce(String responce) throws JSONException {
        if (positionIndex == -1) {
            showError();
            Log.e(TAG, "positionIndex can't be negative!, at getExtraMovieData() onResponse");
            return;
        }


        movie = MovieFactory.completeMovie(movie, new JSONObject(responce));
        if (movie != null) {
            //Setting data to true, so next time we open this movie we will use this info instead of making a new network call
            movie.setWasExtraDataSet(true);

            //Updated data
            MainActivity.updateMovieData(positionIndex, correntType, movie);

            populateData();
        }


    }

    void showProgressBar(boolean state) {
        mLoadingProgressBar.setVisibility(state ? View.VISIBLE : View.GONE);
        mTextErrorMessage.setVisibility(View.GONE);
    }

    void showError() {
        mTextErrorMessage.setVisibility(View.VISIBLE);
        reviewRecycler.setVisibility(View.GONE);
        trailerRecycler.setVisibility(View.GONE);
    }

    void showTrailerContainer() {
        mTextErrorMessage.setVisibility(View.GONE);
        trailerRecycler.setVisibility(View.VISIBLE);
        if (!getResources().getBoolean(R.bool.tablet))
            txtTrailers.setVisibility(View.VISIBLE);
        setTrailerRecyclerHeight();
    }

    void showReviewContainer() {
        mTextErrorMessage.setVisibility(View.GONE);
        reviewRecycler.setVisibility(View.VISIBLE);
        if (!getResources().getBoolean(R.bool.tablet))
            txtReviews.setVisibility(View.VISIBLE);
        setReviewRecyclerHeight();
    }

    /**
     * I want 2 rows to be visible, this method will set the recycler to the right height
     * If we only have 1 trailer i will set the height to hold only 1 row otherwise i will set for 2 rows
     * <p>
     * Each row has 91dp
     * <p>
     * So if we want to show only 1 row we need to set the height to 91dp if we want 2 rows we ned 182dp
     */
    void setTrailerRecyclerHeight() {
        if (getResources().getBoolean(R.bool.tablet)) {
            return;
        }
        int trailerCount = movie.getTrailerKeys().length;

        trailerRecycler.getLayoutParams().height = Math.round(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        trailerCount == 1 ? 91 : 182,
                        getResources().getDisplayMetrics()
                ));
    }

    void setReviewRecyclerHeight() {
        if (getResources().getBoolean(R.bool.tablet)) {
            return;
        }
        int reviewCount = movie.getReviewSources().length;
        reviewRecycler.getLayoutParams().height = Math.round(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        reviewCount == 1 ? 91 : 182,
                        getResources().getDisplayMetrics()
                ));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detailsmenu, menu);
        menuItemStar = menu.findItem(R.id.action_star);
        if (movie.isFavorite(this)) {
            menuItemStar.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_star_white_24dp));
        } else {
            menuItemStar.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_star_border_white_24dp));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_star) {
            if (menuItemStar != null && movie.isFavorite(this)) {
                menuItemStar.setIcon(ContextCompat.getDrawable(this, DEFAULT_STAR));
                movie.removeFromFavorites(this);
            } else {
                menuItemStar.setIcon(ContextCompat.getDrawable(this, ACTIVE_STAR));
                movie.addToFavorite(this);
            }
            return true;
        } else if (id == android.R.id.home) {
            //if i don't do this clicking the arrow will restart the activity
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v, int position) {
        if (v instanceof ImageView) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, Uri.parse("https://www.youtube.com/watch").buildUpon()
                    .appendQueryParameter("v", movie.getTrailerKeys()[position])
                    .build().toString());
            startActivity(shareIntent);

        } else if (((int) v.getTag()) == ReviewsAndTrailerAdapter.TYPE_TRAILERS) {
            Uri uri = Uri.parse("https://www.youtube.com/watch").buildUpon()
                    .appendQueryParameter("v", movie.getTrailerKeys()[position])
                    .build();
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } else if (((int) v.getTag()) == ReviewsAndTrailerAdapter.TYPE_REVIEWS) {
            Intent intent = new Intent(this, ReviewsActivity.class);
            intent.putExtra(ReviewsActivity.ARG_AUTHOR_NAME, movie.getReviewAuthors()[position]);
            intent.putExtra(ReviewsActivity.ARG_AUTHOR_CONTENT, movie.getReviewContent()[position]);
            intent.putExtra(ReviewsActivity.ARG_SOURCE, movie.getReviewSources()[position]);
            intent.putExtra("title", getString(R.string.review) + movie.getTitle());
            startActivity(intent);
        }
    }

    @Override
    public void onRefresh() {
        if (MainActivity.isInternetAvailable()) {
            getIntentData();
            getExtraMovieData();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, R.string.noInternet, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(getString(R.string.movieKey), movie);
        outState.putString(getString(R.string.typeKey), correntType);
        outState.putInt(getString(R.string.positionKey), positionIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (args == null) {
                    return;
                }

                /**
                 * Se já tiveremos guardado a informação extra (trailers e reviews) não vale a pena fazer outra vez query
                 */
                if (args.getInt(TASK_ARG_TYPE, -1) == TYPE_QUERY && movie.wasExtraDataSet()) {
                    return;
                }

                forceLoad();
            }

            @Override
            public Cursor loadInBackground() {
                int type = args.getInt(TASK_ARG_TYPE, -1);
                if (type == TYPE_QUERY) {
                    return getContentResolver().query(
                            Database.DatabaseContract.MOVIE_CONTENT_URI.buildUpon().appendPath(String.valueOf(movie.getMovieID())).build(),
                            null,
                            null,
                            null,
                            null);
                } else if (type == TYPE_INSERT) {
                    /**
                     * Antes de fazer qualquer coisa vou verificar se já existe um filme com id x na base de dados.
                     * Se esse for o caso, vou utilizar o metudo update(), caso não exista vou utilizar o insert()
                     */
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Database.DatabaseContract.COLUMN_NAME_JSON_DATA, args.getString(TASK_ARG_JSON, ""));
                    contentValues.put(Database.DatabaseContract.COLUMN_NAME_MOVIE_ID, String.valueOf(movie.getMovieID()));

                    Cursor c = getContentResolver().query(
                            Database.DatabaseContract.MOVIE_CONTENT_URI.buildUpon().appendPath(String.valueOf(movie.getMovieID())).build(),
                            null,
                            null,
                            null,
                            null);

                    if (c != null && c.getCount() > 0) {
                        getContentResolver().update(Database.DatabaseContract.MOVIE_CONTENT_URI.buildUpon().appendPath(String.valueOf(movie.getMovieID())).build(),
                                contentValues,
                                null,
                                null);
                        c.close();
                        return null;
                    }
                    getContentResolver().insert(
                            Database.DatabaseContract.MOVIE_CONTENT_URI,
                            contentValues);
                    if (c != null)
                        c.close();
                    return null;

                } else {
                    throw new UnsupportedOperationException();
                }

            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            if (data.moveToFirst()) {
                String lastResponce = data.getString(data.getColumnIndex(Database.DatabaseContract.COLUMN_NAME_JSON_DATA));
                try {
                    handleResponce(lastResponce);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                /**
                 * Não temos acesso a internet e não temos nenhuma informação guardada na base de dados.
                 * Assim, vou mostrar uma mensagem de erro
                 */
                showError();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Here i will be using this adapter for both reviews and tailers since both require a Movie object i think is better to use
     * the same adaper instead of creating a new one, the only thing that changes is the layout and views.
     * <p>
     * I also thinking in using the same xml for both videos and reviews instead of creating 2 xml files with 2 identical layouts
     * <p>
     * Now im just using it for the trailers
     */
    class ReviewsAndTrailerAdapter extends RecyclerView.Adapter<ReviewsAndTrailerAdapter.ReviewMovieHolder> {
        /**
         * Adapter type, this will be use to set the type of the adapter
         */
        static final int TYPE_REVIEWS = 1;
        static final int TYPE_TRAILERS = 2;

        Movie movie;
        int type;
        Context context;
        OnItemClickListener onItemClickListener;
        ImageView share;

        class ReviewMovieHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView videoThumbnail;
            TextView videoTitle, noImageError;
            ConstraintLayout container;

            TextView reviewerName, reviewerContent;

            ReviewMovieHolder(View v) {
                super(v);
                if (type == TYPE_TRAILERS) {
                    videoThumbnail = (ImageView) v.findViewById(R.id.iv_video_thumbnail);
                    videoTitle = (TextView) v.findViewById(R.id.txt_video_title);
                    container = (ConstraintLayout) v.findViewById(R.id.video_container);
                    container.setOnClickListener(this);
                    container.setTag(type);
                    noImageError = (TextView) v.findViewById(R.id.videoNoImageError);
                    share = (ImageView) v.findViewById(R.id.share);
                    share.setOnClickListener(this);
                } else if (type == TYPE_REVIEWS) {
                    container = (ConstraintLayout) v.findViewById(R.id.review_row);
                    container.setOnClickListener(this);
                    container.setTag(type);
                    reviewerContent = (TextView) v.findViewById(R.id.reviewer_content);
                    reviewerName = (TextView) v.findViewById(R.id.reviewer_name);
                }
            }

            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(view, getAdapterPosition());
                }
            }
        }

        void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        /**
         * @param type This is the the type of list
         */
        ReviewsAndTrailerAdapter(Context context, int type, Movie movie) {
            this.movie = movie;
            this.type = type;
            this.context = context;
        }

        @Override
        public ReviewMovieHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (type == TYPE_TRAILERS)
                return new ReviewMovieHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.video_row, parent, false));
            else
                return new ReviewMovieHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.review_row, parent, false));
        }

        @Override
        public void onBindViewHolder(final ReviewMovieHolder holder, int position) {
            if (type == TYPE_TRAILERS) {
                holder.videoTitle.setText(movie.getTrailerNames()[position]);
                String urlLink = movie.getThumbnails()[position].toString();
                Picasso.with(context).load(urlLink).into(holder.videoThumbnail, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.noImageError.setVisibility(View.INVISIBLE);
                        holder.videoThumbnail.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                        holder.noImageError.setVisibility(View.VISIBLE);
                        holder.videoThumbnail.setVisibility(View.INVISIBLE);
                    }
                });
            } else if (type == TYPE_REVIEWS) {
                holder.reviewerName.setText(movie.getReviewAuthors()[position]);
                holder.reviewerContent.setText(movie.getReviewContent()[position]);
            }
        }

        @Override
        public int getItemCount() {
            if (type == TYPE_TRAILERS) {
                if (movie != null && movie.getThumbnails() != null) {
                    return movie.getThumbnails().length;
                } else {
                    return 0;
                }
            } else if (type == TYPE_REVIEWS) {
                if (movie != null && movie.getReviewAuthors() != null) {
                    return movie.getReviewAuthors().length;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }
    }
}


interface OnItemClickListener {
    void onClick(View v, int position);
}
