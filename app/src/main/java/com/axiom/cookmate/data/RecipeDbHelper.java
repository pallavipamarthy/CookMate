package com.axiom.cookmate.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.axiom.cookmate.data.RecipeContract.FavouriteRecipeEntry;
import com.axiom.cookmate.data.RecipeContract.MyRecipeEntry;

public class RecipeDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "recipestore.db";

    public RecipeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_FAV_RECIPE_TABLE = "CREATE TABLE " +
                FavouriteRecipeEntry.TABLE_NAME + " (" +
                FavouriteRecipeEntry._ID + " INTEGER PRIMARY KEY," +
                FavouriteRecipeEntry.COLUMN_IMAGE_URL + " TEXT, " +
                FavouriteRecipeEntry.COLUMN_RECIPE_NAME + " TEXT NOT NULL, " +
                FavouriteRecipeEntry.COLUMN_INSTRUCTION_URL + " TEXT, " +
                FavouriteRecipeEntry.COLUMN_INGREDIENTS + " TEXT, " +
                FavouriteRecipeEntry.COLUMN_DIET_LABEL + " TEXT, " +
                FavouriteRecipeEntry.COLUMN_HEALTH_LABEL + " TEXT, " +
                FavouriteRecipeEntry.COLUMN_ALLERGY_LABEL + " TEXT);";
        sqLiteDatabase.execSQL(SQL_CREATE_FAV_RECIPE_TABLE);

        final String SQL_CREATE_MY_RECIPE_TABLE = "CREATE TABLE " +
                MyRecipeEntry.TABLE_NAME + " (" +
                MyRecipeEntry._ID + " INTEGER PRIMARY KEY," +
                MyRecipeEntry.COLUMN_IMAGE + " TEXT, " +
                MyRecipeEntry.COLUMN_RECIPE_NAME + " TEXT NOT NULL, " +
                MyRecipeEntry.COLUMN_INSTRUCTION + " TEXT NOT NULL, " +
                MyRecipeEntry.COLUMN_INGREDIENTS + " TEXT NOT NULL);";
        sqLiteDatabase.execSQL(SQL_CREATE_MY_RECIPE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteRecipeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MyRecipeEntry.TABLE_NAME);
    }
}
