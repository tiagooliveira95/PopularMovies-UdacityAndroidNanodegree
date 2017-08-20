/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.udacity.tiagooliveira95.popularmovies.Utils;

import android.net.Uri;
import android.os.AsyncTask;
import com.udacity.tiagooliveira95.popularmovies.Movie;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public final class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String API_KEY = "YOUR_API_KEY_HERE";
    private static final String MOVIE_URL = "http://api.themoviedb.org/3";

    private final static String API_KEY_PARAM = "api_key";
    private final static String VIDEOS_AND_REVIEWS = "reviews,videos";
    private final static String ARG_APPEND_RESPONCE = "append_to_response";
    private final static String MOVIE_PATH = "movie";

    /**
     * This method builds the url that we will use to get movies from the popular or toprated list
     *
     * @param type type of list
     * @return generated url
     */
    private static URL buildUrl(String type) {
        if(!type.equals(Movie.TOP_RATED) && !type.equals(Movie.POPULAR))
            return null;

        Uri builtUri = Uri.parse(MOVIE_URL).buildUpon()
                .appendPath("movie")
                .appendPath(type)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .build();
        try {
            return new URL(builtUri.toString());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method builds the url that will we will use to retirve the videos and trailers
     *
     * @param movieID the id of the movie
     * @return generated url
     */
    private static URL buildMovieUrl(String movieID) {
        if(movieID == null)
            return null;

        Uri builtUri = Uri.parse(MOVIE_URL).buildUpon()
                .appendPath(MOVIE_PATH)
                .appendPath(movieID)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .appendQueryParameter(ARG_APPEND_RESPONCE,VIDEOS_AND_REVIEWS)
                .build();
        try {
            return new URL(builtUri.toString());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    private static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * This will get the movies and reviews
     * @param id movie id
     */
    public static void getExtraMovieData(int id, OnServerResponce onServerResponce){
        getMovies(onServerResponce).execute(buildMovieUrl(String.valueOf(id)));
    }


    /**
     * This will get call getMovies and pass the type of movies to get
     * then getMovies will call use the interface to pass the data or report an error
     */
    public static void getMovieData(String type, OnServerResponce onServerResponce){
        if(type != null && (type.equals(Movie.TOP_RATED) || type.equals(Movie.POPULAR))){
            URL url = buildUrl(type);
            if(url != null){
                getMovies(onServerResponce).execute(url);
            }else{
                onServerResponce.onError();
            }
        }else{
            onServerResponce.onError();
        }
    }

    /**
     * This will perform the network call to get popular or toprated movies
     */
    private static AsyncTask<URL,Void,String> getMovies(final OnServerResponce onServerResponce){
        return new AsyncTask<URL, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                onServerResponce.onStart();
            }

            @Override
            protected String doInBackground(URL... urls) {
                if(urls == null || urls[0] == null)
                    return null;

                try {
                    return getResponseFromHttpUrl(urls[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                onServerResponce.onFinish();
                if(s != null && !s.equals("")){
                    onServerResponce.onResponse(s);
                }else{
                    onServerResponce.onError();
                }
            }
        };
    }

    public interface OnServerResponce{
        void onStart();
        void onResponse(String responce);
        void onFinish();
        void onError();
    }
}