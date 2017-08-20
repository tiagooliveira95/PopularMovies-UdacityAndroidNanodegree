package com.udacity.tiagooliveira95.popularmovies.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by tiago on 22/02/2017.
 */

public class MovieProvider extends ContentProvider {
    public static final String TAG = MovieProvider.class.getSimpleName();
    public static final int CODE_MOVIES = 100;
    public static final int CODE_MOVIES_WITH_ID = 101;
    public static final int CODE_FAVORITES = 200;
    public static final int CODE_FAVORITES_WITH_ID = 201;
    public static final int CODE_TOP_RATED = 300;
    public static final int CODE_POPULAR = 400;

    private Database database;
    private static UriMatcher uriMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {
        database = new Database(getContext());
        return true;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (uriMatcher.match(uri)) {
            case CODE_MOVIES:
                cursor = database.getReadableDatabase().query(
                        Database.DatabaseContract.MOVIES_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return cursor;
            case CODE_MOVIES_WITH_ID:
                String id = uri.getLastPathSegment();

                String[] selectionArguments = new String[]{id};

                cursor = database.getReadableDatabase().query(
                        Database.DatabaseContract.MOVIES_TABLE_NAME,
                        projection,
                        Database.DatabaseContract.COLUMN_NAME_MOVIE_ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);

                return cursor;
            case CODE_FAVORITES_WITH_ID:
                id = uri.getLastPathSegment();

                selectionArguments = new String[]{id};

                cursor = database.getReadableDatabase().query(
                        Database.DatabaseContract.FAV_TABLE_NAME,
                        projection,
                        Database.DatabaseContract.COLUMN_NAME_MOVIE_ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                if(cursor.getCount() > 0)
                    return cursor;
                else
                    return null;
            case CODE_POPULAR:
                cursor = database.getReadableDatabase().query(
                        Database.DatabaseContract.MOST_POPULAR_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                cursor.moveToFirst();
                return cursor;
            case CODE_TOP_RATED:
                cursor = database.getReadableDatabase().query(
                        Database.DatabaseContract.TOP_RATED_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return cursor;
            case CODE_FAVORITES:
                cursor = database.getReadableDatabase().query(
                        Database.DatabaseContract.FAV_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return cursor;
            default:
                throw new UnsupportedOperationException("Unknown uri" + uri);
        }

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        switch (uriMatcher.match(uri)) {
            case CODE_MOVIES:
                SQLiteDatabase db = database.getWritableDatabase();
                long id = db.insert(
                        Database.DatabaseContract.MOVIES_TABLE_NAME,
                        null,
                        contentValues);
                if(id>0){
                    getContext().getContentResolver().notifyChange(uri, null);
                    return ContentUris.withAppendedId(Database.DatabaseContract.MOVIE_CONTENT_URI, id);
                }else{
                    throw new SQLException("Failed to insert row into " + uri);
                }
            case CODE_FAVORITES:
                db = database.getWritableDatabase();
                id = db.insert(
                        Database.DatabaseContract.FAV_TABLE_NAME,
                        null,
                        contentValues);
                if(id>0){
                    getContext().getContentResolver().notifyChange(uri, null);
                    return ContentUris.withAppendedId(Database.DatabaseContract.FAV_CONTENT_URI, id);
                }else{
                    throw new SQLException("Failed to insert row into " + uri);
                }
            case CODE_POPULAR:
                db = database.getWritableDatabase();
                db.delete( Database.DatabaseContract.MOST_POPULAR_TABLE_NAME,null,null);
                id = db.insert(
                        Database.DatabaseContract.MOST_POPULAR_TABLE_NAME,
                        null,
                        contentValues);
                if(id>0){
                    getContext().getContentResolver().notifyChange(uri, null);
                    return ContentUris.withAppendedId(Database.DatabaseContract.POPULAR_CONTENT_URI, id);
                }else{
                    throw new SQLException("Failed to insert row into " + uri);
                }
            case CODE_TOP_RATED:
                db = database.getWritableDatabase();
                db.delete( Database.DatabaseContract.TOP_RATED_TABLE_NAME,null,null);
                id = db.insert(
                        Database.DatabaseContract.TOP_RATED_TABLE_NAME,
                        null,
                        contentValues);
                if(id>0){
                    getContext().getContentResolver().notifyChange(uri, null);
                    return ContentUris.withAppendedId(Database.DatabaseContract.TOP_CONTENT_URI, id);
                }else{
                    throw new SQLException("Failed to insert row into " + uri);
                }
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String s, String[] strings) {
        int rowsDeleted;
        switch (uriMatcher.match(uri)) {
            case CODE_FAVORITES_WITH_ID:
                String id = uri.getPathSegments().get(1);
                rowsDeleted = database.getWritableDatabase().delete(
                        Database.DatabaseContract.FAV_TABLE_NAME, Database.DatabaseContract.COLUMN_NAME_MOVIE_ID + " = ?",
                        new String[]{id}
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
        if(uriMatcher.match(uri) == CODE_MOVIES_WITH_ID){
            String movieId = uri.getLastPathSegment();
            String[] selectionArguments = new String[]{movieId};

            int u = database.getWritableDatabase().update(
                    Database.DatabaseContract.MOVIES_TABLE_NAME,
                    contentValues,
                    Database.DatabaseContract.COLUMN_NAME_MOVIE_ID + " = ?",
                    selectionArguments
            );
            if(u>0){
                return u;
            }else{
                throw new SQLException("Failed to update row at: " + uri);
            }
        }else{
            throw new UnsupportedOperationException("Unsupported update uri: " + uri);
        }
    }

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = Database.CONTENT_AUTHORITY;
        matcher.addURI(authority, Database.PATH_MOVIES, CODE_MOVIES);
        matcher.addURI(authority, Database.PATH_MOVIES + "/#", CODE_MOVIES_WITH_ID);
        matcher.addURI(authority, Database.PATH_FAV, CODE_FAVORITES);
        matcher.addURI(authority, Database.PATH_FAV+ "/#", CODE_FAVORITES_WITH_ID);
        matcher.addURI(authority, Database.PATH_POPULAR, CODE_POPULAR);
        matcher.addURI(authority, Database.PATH_TOP, CODE_TOP_RATED);
        return matcher;
    }

}
