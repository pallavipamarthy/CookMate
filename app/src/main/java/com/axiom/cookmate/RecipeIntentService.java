package com.axiom.cookmate;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.axiom.cookmate.data.Recipe;
import com.axiom.cookmate.utilities.Constants;
import com.axiom.cookmate.utilities.NetworkUtils;
import com.axiom.cookmate.utilities.RecipeUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class RecipeIntentService extends IntentService {

    public static final String ACTION_NEXT_PAGE_DOWNLOAD = "com.axiom.cookmate.action.NEXT_PAGE_DOWNLOAD";
    private static final String FINAL_RECIPE_URL = "com.axiom.cookmate.extra.FINAL_RECIPE_URL";

    public RecipeIntentService() {
        super("RecipeIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_NEXT_PAGE_DOWNLOAD.equals(action)) {
                final String urlString = intent.getStringExtra(FINAL_RECIPE_URL);
                ArrayList<Recipe> recipeList = handleActionPageDownload(urlString);
                Intent recipeListIntent = new Intent(Constants.ACTION_DOWNLOAD_COMPLETE);
                recipeListIntent.putParcelableArrayListExtra("recipeList", recipeList);
                this.sendBroadcast(recipeListIntent);
            }
        }
    }

    private ArrayList<Recipe> handleActionPageDownload(String urlString) {
        URL recipeUrl = NetworkUtils.createUrl(urlString);
        String jsonResponse;
        try {
            jsonResponse = NetworkUtils.getResponseFromHttpUrl(recipeUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        try {
            ArrayList<Recipe> returnList = (ArrayList<Recipe>) RecipeUtils.extractRecipesFromJson(jsonResponse);
            return returnList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
