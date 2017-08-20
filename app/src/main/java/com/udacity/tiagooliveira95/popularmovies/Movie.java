package com.udacity.tiagooliveira95.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.udacity.tiagooliveira95.popularmovies.Data.Database;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by tiago on 15/01/2017.
 */

public class Movie implements Parcelable {
    public static final String TAG = Movie.class.getSimpleName();

    public static final String TOP_RATED = "top_rated";
    public static final String POPULAR = "popular";
    public static final String FAVORITE = "favorite";

    private final String ARG_FAVORITE = "favorite";
    private final String IMAGE_URL = "http://image.tmdb.org/t/p/w185/";
    private final String YOUTUBE_URL = "http://img.youtube.com/";
    private final String YOUTUBE_VIDEO_PATH = "vi";
    private final String YOUTUBE_DEFAULT_THUMBNAIL_NAME = "default.jpg";

    private boolean wasExtraDataSet = false;

    private String posterPath,overview,releaseDate,originalTitle,originalLanguage,title,backdropPath,popularity,voteCount,voteAverage;
    private boolean adult;
    private int movieID = -1;
    private String[] trailerKeys;
    private String[] trailerNames;
    private String[] reviewAuthors;
    private String[] reviewContent;
    private String[] reviewSources;
    private String rawJsonData;

    private ContentValues contentValues;

    private Movie(String img_path, String voteCount, String popularity, String backdropPath, String title, String originalLanguage, String originalTitle, String releaseDate, String overview, String video, String voteAverage, boolean adult, int movieID, @NotNull JSONObject raw) {
        this.posterPath = img_path;
        this.voteCount = voteCount;
        this.popularity = popularity;
        this.backdropPath = backdropPath;
        this.title = title;
        this.originalLanguage = originalLanguage;
        this.originalTitle = originalTitle;
        this.releaseDate = releaseDate;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.adult = adult;
        this.movieID = movieID;
        rawJsonData = raw.toString();
    }

    private Movie(Parcel parcel){
        posterPath = parcel.readString();
        overview = parcel.readString();
        releaseDate = parcel.readString();
        originalTitle = parcel.readString();
        originalLanguage = parcel.readString();
        title = parcel.readString();
        backdropPath = parcel.readString();
        voteCount = parcel.readString();
        voteAverage = parcel.readString();
        rawJsonData = parcel.readString();

        int trailerLenght = parcel.readInt();

        if(trailerLenght > 0) {
            trailerKeys = new String[trailerLenght];
            trailerNames = new String[trailerLenght];
            parcel.readStringArray(trailerKeys);
            parcel.readStringArray(trailerNames);
        }

        int reviewLenght = parcel.readInt();
        if(reviewLenght > 0) {
            reviewAuthors = new String[reviewLenght];
            reviewContent = new String[reviewLenght];
            reviewSources = new String[reviewLenght];
            parcel.readStringArray(reviewAuthors);
            parcel.readStringArray(reviewContent);
            parcel.readStringArray(reviewSources);
        }

        movieID = parcel.readInt();

        wasExtraDataSet = parcel.readByte() != 0;
    }

    public Movie(){}

    public void setTrailerKeys(String[] trailerKeys) {
        this.trailerKeys = trailerKeys;
    }

    String[] getReviewSources() {
        return reviewSources;
    }

    public void setReviewSources(String[] reviewSources) {
        this.reviewSources = reviewSources;
    }

    String[] getReviewContent() {
        return reviewContent;
    }

    public void setReviewContent(String[] review_content) {
        this.reviewContent = review_content;
    }

    String[] getReviewAuthors() {
        return reviewAuthors;
    }

    public void setReviewAuthors(String[] review_authors) {
        this.reviewAuthors = review_authors;
    }

    String[] getTrailerNames() {
        return trailerNames;
    }

    public void setTrailerNames(String[] trailerNames) {
        this.trailerNames = trailerNames;
    }

    String getPosterURL() {
        return IMAGE_URL + posterPath;
    }

    String getOverview() {
        return overview;
    }

    String getReleaseDate() {
        return releaseDate;
    }

    String getOriginalTitle() {
        return originalTitle;
    }

    String getOriginalLanguage() {
        return originalLanguage;
    }

    String getTitle() {
        return title;
    }

    String getBackdropPath() {
        return IMAGE_URL + backdropPath;
    }

    String getPopularity() {
        return popularity;
    }

    String getVoteCount() {
        return voteCount;
    }

    String[] getTrailerKeys() {
        return trailerKeys;
    }

    String getVoteAverage() {
        return voteAverage;
    }

    public boolean isAdult() {
        return adult;
    }

    int getMovieID() {
        return movieID;
    }

    URL[] getThumbnails(){
        URL[] urls = null;
        if(trailerKeys != null){
            urls = new URL[trailerKeys.length];
            for(int k = 0; k<trailerKeys.length;k++){
                try {
                    urls[k] = new URL(Uri.parse(YOUTUBE_URL).buildUpon()
                            .appendPath(YOUTUBE_VIDEO_PATH)
                            .appendPath(trailerKeys[k])
                            .appendPath(YOUTUBE_DEFAULT_THUMBNAIL_NAME)
                            .build().toString()
                    );
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    urls[k] = null;
                    Log.e(TAG, "Whattt??, what's happening? what did i did wrong?, We were unable to build the url for our thumbnail");
                }
            }
        }
        return urls;
    }

    void setWasExtraDataSet(boolean wasExtraDataSet) {
        this.wasExtraDataSet = wasExtraDataSet;
    }

    boolean wasExtraDataSet() {
        return wasExtraDataSet;
    }

    private String getRawData() {
        return rawJsonData;
    }

    void addToFavorite(Context context){
        if(contentValues == null) {
            contentValues = new ContentValues();
        }

        contentValues.put(Database.DatabaseContract.COLUMN_NAME_JSON_DATA, getRawData());
        contentValues.put(Database.DatabaseContract.COLUMN_NAME_MOVIE_ID, getMovieID());
        context.getContentResolver().insert(Database.DatabaseContract.FAV_CONTENT_URI, contentValues);
    }

    boolean isFavorite(Context context) {
        if(contentValues == null) {
            contentValues = new ContentValues();
        }

        return context.getContentResolver().query(
                Database.DatabaseContract.FAV_CONTENT_URI.buildUpon().appendPath(String.valueOf(getMovieID())).build(),
                null,
                null,
                null,
                null) != null;
    }

    void removeFromFavorites(Context context){
        if(contentValues == null) {
            contentValues = new ContentValues();
        }
        context.getContentResolver().delete(
                Database.DatabaseContract.FAV_CONTENT_URI.buildUpon().appendPath(String.valueOf(getMovieID())).build(),
                null,
                null
        );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel in, int i) {
        in.writeString(posterPath);
        in.writeString(overview);
        in.writeString(releaseDate);
        in.writeString(originalTitle);
        in.writeString(originalLanguage);
        in.writeString(title);
        in.writeString(backdropPath);
        in.writeString(voteCount);
        in.writeString(voteAverage);
        in.writeString(rawJsonData);

        /**
         * if this is the first time clicking on the poster this string arrays will be null
         * the second time the user clickes the poster the string arrays will contain all the data (if nothing wrong happens)
         *
         * since i am unable to know the array lenght i also pass the lenght as a int value
         *
         */

        int trailerKeySize = trailerKeys != null ? trailerKeys.length : 0; //trailerKeys size should be equal to trailerNames
        in.writeInt(trailerKeySize);

        if(trailerKeys != null && trailerNames != null) {
            in.writeStringArray(trailerKeys);
            in.writeStringArray(trailerNames);
        }

        int reviewAuthorsSize = reviewAuthors != null ? reviewAuthors.length : 0;
        in.writeInt(reviewAuthorsSize);


        if(reviewAuthors != null && reviewContent != null && reviewSources != null) {
            in.writeStringArray(reviewAuthors);
            in.writeStringArray(reviewContent);
            in.writeStringArray(reviewSources);
        }

        in.writeInt(movieID);

        in.writeByte((byte) (wasExtraDataSet ? 1 : 0)); //converting boolean to byte
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    /**
     * Movie Builder
     */
    public static class Builder{
        private String posterPath,overview,release_date,original_title,original_language,title,backdrop_path,popularity,vote_count,video,vote_average;
        private boolean adult;
        private int movieID = -1;
        JSONObject raw;

        public Builder setPosterPath(String posterPath) {
            this.posterPath = posterPath;
            return this;
        }

        public Builder setVoteAverage(String vote_average) {
            this.vote_average = vote_average;
            return this;
        }

        public Builder setVideo(String video) {
            this.video = video;
            return this;
        }

        public Builder setVoteCount(String vote_count) {
            this.vote_count = vote_count;
            return this;
        }

        public Builder setPopularity(String popularity) {
            this.popularity = popularity;
            return this;
        }

        public Builder setBackdropPath(String backdrop_path) {
            this.backdrop_path = backdrop_path;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setOriginalLanguage(String original_language) {
            this.original_language = original_language;
            return this;
        }

        public Builder setOriginalTitle(String original_title) {
            this.original_title = original_title;
            return this;
        }

        public Builder setReleaseDate(String release_date) {
            this.release_date = release_date;
            return this;
        }

        public Builder setOverview(String overview) {
            this.overview = overview;
            return this;
        }

        public Builder setAdult(boolean adult) {
            this.adult = adult;
            return this;
        }

        public Builder setMovieID(int movieID) {
            this.movieID = movieID;
            return this;
        }

        public Builder setRawData(JSONObject raw){
            this.raw = raw;
            return this;
        }

        public Movie build(){
            return new Movie(posterPath,vote_count,popularity,backdrop_path,title,original_language,original_title,release_date,overview,video,vote_average,adult,movieID,raw);
        }
    }

}
