package com.axiom.cookmate.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

import com.axiom.cookmate.data.Recipe;
import com.axiom.cookmate.data.RecipeDbHelper;
import com.axiom.cookmate.data.RecipeContract.FavouriteRecipeEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavouriteRecipeLoader extends AsyncTaskLoader<List<Recipe>> {

    private Context mContext;

    public FavouriteRecipeLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Recipe> loadInBackground() {

        Cursor cursor = getContext().getContentResolver().query(FavouriteRecipeEntry.CONTENT_URI,
                null, null, null, null);
        List<Recipe> favouriteRecipes = new ArrayList<>();


        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(FavouriteRecipeEntry.COLUMN_RECIPE_NAME));
            String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(FavouriteRecipeEntry.COLUMN_IMAGE_URL));
            String instructionUrl = cursor.getString(cursor.getColumnIndexOrThrow(FavouriteRecipeEntry.COLUMN_INSTRUCTION_URL));
            String ingredientArrayString = cursor.getString(cursor.getColumnIndexOrThrow(FavouriteRecipeEntry.COLUMN_INGREDIENTS));
            String dietLabel = cursor.getString(cursor.getColumnIndexOrThrow(FavouriteRecipeEntry.COLUMN_DIET_LABEL));
            String healthLabel = cursor.getString(cursor.getColumnIndexOrThrow(FavouriteRecipeEntry.COLUMN_HEALTH_LABEL));
            String allergyLabel = cursor.getString(cursor.getColumnIndexOrThrow(FavouriteRecipeEntry.COLUMN_ALLERGY_LABEL));

            List<String> ingredientList = Arrays.asList
                    (ingredientArrayString.replace("[", "").replace("]", "").split(","));

            Recipe currentRecipe = new Recipe(name, imageUrl, instructionUrl,
                    ingredientList, healthLabel, dietLabel, allergyLabel);

            favouriteRecipes.add(currentRecipe);
        }
        cursor.close();
        return favouriteRecipes;
    }

}

