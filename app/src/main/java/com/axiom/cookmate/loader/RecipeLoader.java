package com.axiom.cookmate.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.axiom.cookmate.data.Recipe;
import com.axiom.cookmate.utilities.NetworkUtils;
import com.axiom.cookmate.utilities.RecipeUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class RecipeLoader extends AsyncTaskLoader<List<Recipe>> {
    private String mUrlString = "";

    public RecipeLoader(Context context, String urlString) {
        super(context);
        mUrlString = urlString;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Recipe> loadInBackground() {
        URL recipeUrl = NetworkUtils.createUrl(mUrlString);
        String jsonResponse;
        try {
            jsonResponse = NetworkUtils.getResponseFromHttpUrl(recipeUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        try {
            return RecipeUtils.extractRecipesFromJson(jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
