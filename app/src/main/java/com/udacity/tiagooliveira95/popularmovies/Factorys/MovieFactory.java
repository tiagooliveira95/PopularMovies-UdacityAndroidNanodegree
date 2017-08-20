package com.udacity.tiagooliveira95.popularmovies.Factorys;

import com.udacity.tiagooliveira95.popularmovies.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tiago on 15/01/2017.
 */

/**
 * To make this clean
 */
public class MovieFactory {
    private static String ARG_POSTER_PATH = "poster_path";
    private static String ARG_ADULT = "adult";
    private static String ARG_OVERVIEW = "overview";
    private static String ARG_REALEASE_DATE = "release_date";
    private static String ARG_ORIGINAL_TITLE = "original_title";
    private static String ARG_LANGUAGE = "original_language";
    private static String ARG_TITLE = "title";
    private static String ARG_BACKDROP_PATH = "backdrop_path";
    private static String ARG_POPULARITY = "popularity";
    private static String ARG_VOTE_COUNT = "vote_count";
    private static String ARG_VIDEO = "";
    private static String ARG_VOTE_AVERAGE = "vote_average";
    private static String ARG_MOVIE_ID = "id";
    private static String ARG_VIDEOS = "videos";
    private static String ARG_REVIEWS = "reviews";
    private static String ARG_RESULTS = "results";
    private static String ARG_CONTENT = "content";
    private static String ARG_AUTHOR = "author";
    private static String ARG_AUTHOR_SOURCE_URL = "url";
    private static String ARG_VIDEO_KEY = "key";
    private static String ARG_VIDEO_NAME = "name";
    /**
     * Creates the basic infomation of a certain movie and returns a Movie object
     */
    public static Movie createMovie(JSONObject movieData){
        return new Movie.Builder()
                .setPosterPath(getString(movieData,ARG_POSTER_PATH))
                .setAdult(true)
                .setOverview(getString(movieData,ARG_OVERVIEW))
                .setReleaseDate(getString(movieData,ARG_REALEASE_DATE))
                .setOriginalTitle(getString(movieData,ARG_ORIGINAL_TITLE))
                .setOriginalLanguage(getString(movieData,ARG_LANGUAGE))
                .setTitle(getString(movieData,ARG_TITLE))
                .setBackdropPath(getString(movieData,ARG_BACKDROP_PATH))
                .setPopularity(getString(movieData,ARG_POPULARITY))
                .setVoteCount(getString(movieData,ARG_VOTE_COUNT))
                .setVoteAverage(getString(movieData,ARG_VOTE_AVERAGE))
                .setMovieID(getInt(movieData,ARG_MOVIE_ID))
                .setRawData(movieData)
                .build();
    }

    /**
     * This method completes the movie data
     * Adds Video and ReviewsActivity to the movie object
     */
    public static Movie completeMovie(Movie movie,JSONObject movieData){
        try {
            if (movieData.has(ARG_REVIEWS)) {
                JSONObject reviews = movieData.getJSONObject(ARG_REVIEWS);

                if (reviews.has(ARG_RESULTS)) {
                    JSONArray results = reviews.getJSONArray(ARG_RESULTS);
                    int lenght = results.length();
                    String reviewContent[] = new String[lenght];
                    String reviewAuthors[] = new String[lenght];
                    String reviewSources[] = new String[lenght];
                    for (int k = 0; k < results.length(); k++) {
                        JSONObject review = results.getJSONObject(k);
                        reviewContent[k] = review.getString(ARG_CONTENT);
                        reviewAuthors[k] = review.getString(ARG_AUTHOR);
                        reviewSources[k] = review.getString(ARG_AUTHOR_SOURCE_URL);
                    }
                    movie.setReviewAuthors(reviewAuthors);
                    movie.setReviewContent(reviewContent);
                    movie.setReviewSources(reviewSources);
                }
            }

            if (movieData.has(ARG_VIDEOS)) {
                JSONObject videos = movieData.getJSONObject(ARG_VIDEOS);
                if (videos.has(ARG_RESULTS)) {
                    JSONArray results = videos.getJSONArray(ARG_RESULTS);
                    int lenght = results.length();
                    String trailersKeys[] = new String[lenght];
                    String trailersNames[] = new String[lenght];
                    for (int k = 0; k < lenght; k++) {
                        JSONObject trailers = results.getJSONObject(k);

                        trailersKeys[k] = trailers.getString(ARG_VIDEO_KEY);
                        trailersNames[k] = trailers.getString(ARG_VIDEO_NAME);
                    }
                    movie.setTrailerKeys(trailersKeys);
                    movie.setTrailerNames(trailersNames);
                }
            }
        }catch (JSONException je){
            je.printStackTrace();
            return null;
        }
        return movie;
    }

    /**
     * If the json object dosen't contain the key it returns an empty string
     *
     * @param object corrent jsonData
     * @param ARG key to search
     * @return
     * @throws JSONException
     */
    private static String getString(JSONObject object,String ARG){
        if(object.has(ARG)){
            try {
                return object.getString(ARG);
            }catch (Exception e){
                e.printStackTrace();
                return "";
            }
        }else{
            return "";
        }
    }

    private static int getInt(JSONObject object,String ARG){
        if(object.has(ARG)){
            try {
                return object.getInt(ARG);
            }catch (Exception e){
                e.printStackTrace();
                return -1;
            }
        }else{
            return -1;
        }
    }


}
