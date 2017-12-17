package com.axiom.cookmate.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.axiom.cookmate.data.RecipeContract.FavouriteRecipeEntry;
import com.axiom.cookmate.data.RecipeContract.MyRecipeEntry;

public class RecipeContentProvider extends ContentProvider {
    private static final int RECIPES = 100;
    private static final int RECIPE_ID = 101;
    private static final int MYRECIPES = 102;
    private static final int MYRECIPE_ID = 103;
    private RecipeDbHelper mDbHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(RecipeContract.CONTENT_AUTHORITY, RecipeContract.PATH_FAVOURITES, RECIPES);
        sUriMatcher.addURI(RecipeContract.CONTENT_AUTHORITY, RecipeContract.PATH_FAVOURITES + "/#", RECIPE_ID);
        sUriMatcher.addURI(RecipeContract.CONTENT_AUTHORITY, RecipeContract.PATH_MYRECIPES, MYRECIPES);
        sUriMatcher.addURI(RecipeContract.CONTENT_AUTHORITY, RecipeContract.PATH_MYRECIPES + "/#", MYRECIPE_ID);
        return sUriMatcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new RecipeDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        Cursor cursor;
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        switch (match) {
            case RECIPES:
                cursor = database.query(FavouriteRecipeEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case RECIPE_ID:
                selection = FavouriteRecipeEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(FavouriteRecipeEntry.TABLE_NAME, null, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case MYRECIPES:
                cursor = database.query(MyRecipeEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case MYRECIPE_ID:
                selection = MyRecipeEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(MyRecipeEntry.TABLE_NAME, null, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        Uri insertUri;
        switch (match) {
            case RECIPES: {
                long id = database.insert(FavouriteRecipeEntry.TABLE_NAME, null, contentValues);
                if (id > 0) {
                    insertUri = ContentUris.withAppendedId(uri, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return insertUri;
            }
            case MYRECIPES: {
                long id = database.insert(MyRecipeEntry.TABLE_NAME, null, contentValues);
                if (id > 0) {
                    insertUri = ContentUris.withAppendedId(uri, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return insertUri;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case RECIPES:
                return 0;
            case MYRECIPES:
                return 0;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case RECIPES:
                rowsDeleted = database.delete(FavouriteRecipeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case RECIPE_ID:
                selection = FavouriteRecipeEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(FavouriteRecipeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MYRECIPES:
                rowsDeleted = database.delete(MyRecipeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MYRECIPE_ID:
                selection = MyRecipeEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(MyRecipeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case RECIPES:
                return null;
            case RECIPE_ID:
                return null;
            case MYRECIPES:
                return null;
            case MYRECIPE_ID:
                return null;
            default:
                throw new IllegalArgumentException("Type is not supported for " + uri);
        }
    }

}
