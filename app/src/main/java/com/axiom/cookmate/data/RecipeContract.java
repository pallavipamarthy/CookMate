package com.axiom.cookmate.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class RecipeContract {
    public static final String CONTENT_AUTHORITY = "com.axiom.cookmate";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVOURITES = "favouriterecipes";
    public static final String PATH_MYRECIPES = "myrecipes";

    public static final class FavouriteRecipeEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITES).build();

        public static final String TABLE_NAME = "favouriterecipes";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_IMAGE_URL = "image_url";
        public final static String COLUMN_RECIPE_NAME = "label";
        public final static String COLUMN_INSTRUCTION_URL = "url";
        public final static String COLUMN_INGREDIENTS = "ingredients";
        public final static String COLUMN_DIET_LABEL = "dietLabels";
        public final static String COLUMN_HEALTH_LABEL = "healthLabels";
        public final static String COLUMN_ALLERGY_LABEL = "cautions";

    }

    public static final class MyRecipeEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MYRECIPES).build();

        public static final String TABLE_NAME = "myrecipes";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_IMAGE = "recipe_image";
        public final static String COLUMN_RECIPE_NAME = "recipe_name";
        public final static String COLUMN_INSTRUCTION = "instructions";
        public final static String COLUMN_INGREDIENTS = "ingredients";

    }
}