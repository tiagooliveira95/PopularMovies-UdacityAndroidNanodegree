package com.udacity.tiagooliveira95.popularmovies.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import java.util.Locale;


/**
 * Created by tiago on 22/02/2017.
 */

public class Database extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "movies.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SMALLINT = " SMALLINT";
    private static final String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";
    private static final String INTEGER_PRIMARY_KEY = " INTEGER PRIMARY KEY ";
    private static final String UNIQUE = " UNIQUE ";

    static final String CONTENT_AUTHORITY = "com.udacity.tiagooliveira95.popularmovies";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    static final String PATH_MOVIES = "movies";
    static final String PATH_FAV = "fav";
    static final String PATH_POPULAR = "popular";
    static final String PATH_TOP = "top";

    private static final String SQL_CREATE_TABLE =
            CREATE_TABLE_IF_NOT_EXISTS  + "%s (" +
                    DatabaseContract._ID + INTEGER_PRIMARY_KEY + COMMA_SEP +
                    DatabaseContract.COLUMN_NAME_JSON_DATA + TEXT_TYPE + COMMA_SEP +
                    DatabaseContract.COLUMN_NAME_MOVIE_ID + SMALLINT  + UNIQUE +
                    " )";

    public static abstract class DatabaseContract implements BaseColumns {
        static final String MOVIES_TABLE_NAME = "movies";
        static final String FAV_TABLE_NAME = "favMovies";
        static final String TOP_RATED_TABLE_NAME = "topRated";
        static final String MOST_POPULAR_TABLE_NAME = "mostPopular";

        /**
         * Para simplicidade em vez de criar uma coluna para cada valor, vou guardar a informação json na base de dados
         * que sera utilizada mais tarde para criar um objecto Movie
         */
        public static final String COLUMN_NAME_JSON_DATA = "jsonData";


        public static final String COLUMN_NAME_MOVIE_ID = "movie_id";

        public static final Uri MOVIE_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES)
                .build();
        public static final Uri FAV_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAV)
                .build();
        public static final Uri POPULAR_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_POPULAR)
                .build();

        public static final Uri TOP_CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_TOP)
                .build();
    }

    Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format(Locale.getDefault(),SQL_CREATE_TABLE,DatabaseContract.FAV_TABLE_NAME));
        db.execSQL(String.format(Locale.getDefault(),SQL_CREATE_TABLE,DatabaseContract.MOST_POPULAR_TABLE_NAME));
        db.execSQL(String.format(Locale.getDefault(),SQL_CREATE_TABLE,DatabaseContract.TOP_RATED_TABLE_NAME));
        db.execSQL(String.format(Locale.getDefault(),SQL_CREATE_TABLE,DatabaseContract.MOVIES_TABLE_NAME));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

}
