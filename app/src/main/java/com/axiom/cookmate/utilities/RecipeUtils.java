package com.axiom.cookmate.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.axiom.cookmate.data.Recipe;
import com.axiom.cookmate.data.RecipeBookmark;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class RecipeUtils {

    public static final String SHOPPING_LIST_PREFERENCE = "shopping_list_pref";
    public static final String SHOPPING_LIST = "shopping_list";
    public static final String BOOKMARK_LIST_PREFERENCE = "bookmark_list_pref";
    public static final String BOOKMARK_LIST = "bookmark_list";

    public static List<Recipe> extractRecipesFromJson(String output) throws JSONException {
        if (output == null) {
            return null;
        }

        List<Recipe> recipeList = new ArrayList<>();
        try {
            JSONObject recipesObj = new JSONObject(output);
            JSONArray hitsArray = recipesObj.getJSONArray("hits");

            for (int i = 0; i < hitsArray.length(); i++) {
                JSONObject recipeObj = hitsArray.getJSONObject(i);
                JSONObject recipe = recipeObj.getJSONObject("recipe");
                String recipeName = recipe.getString("label");
                String imageUrl = recipe.getString("image");
                String instructionUrl = recipe.getString("url");
                String healthLabel = recipe.getString("healthLabels");
                JSONArray ingredientArray = recipe.getJSONArray("ingredientLines");
                ArrayList<String> ingredientList = new ArrayList<>();
                for (int j = 0; j < ingredientArray.length(); j++) {
                    String ingredient = ingredientArray.getString(j);
                    ingredientList.add(ingredient);
                }

                String dietLabel = recipe.getString("dietLabels");
                String allergyLabel = recipe.getString("cautions");


                recipeList.add(new Recipe(recipeName, imageUrl, instructionUrl, ingredientList, dietLabel, healthLabel,
                        allergyLabel));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return recipeList;
    }

    public static void addIngredientToRecipeShoppingList(Context context, String recipeName, String ingredient) {
        addRecipeToList(context, recipeName);
        SharedPreferences sharedPref = context.getSharedPreferences(SHOPPING_LIST_PREFERENCE, Context.MODE_PRIVATE);
        HashSet<String> shoppingList = (HashSet<String>) sharedPref.getStringSet(recipeName, new HashSet<String>());
        shoppingList.add(ingredient);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(recipeName, shoppingList);
        editor.commit();
    }

    public static ArrayList<String> getRecipeShoppingList(Context context, String recipeName) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHOPPING_LIST_PREFERENCE, Context.MODE_PRIVATE);
        HashSet<String> shoppingList1 = (HashSet<String>) sharedPref.getStringSet(recipeName, new HashSet<String>());
        ArrayList<String> returnList = new ArrayList<>();
        for (String ingredient : shoppingList1) {
            returnList.add(ingredient);
        }
        return returnList;
    }

    public static void removeIngredientFromRecipeShoppingList(Context context, String recipeName, String ingredient) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHOPPING_LIST_PREFERENCE, Context.MODE_PRIVATE);
        HashSet<String> shoppingList = (HashSet<String>) sharedPref.getStringSet(recipeName, null);
        if (shoppingList != null) {
            shoppingList.remove(ingredient);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putStringSet(recipeName, shoppingList);
            editor.commit();
        }
    }

    public static void addRecipeToList(Context context, String recipeName) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHOPPING_LIST_PREFERENCE, Context.MODE_PRIVATE);
        HashSet<String> shoppingList1 = (HashSet<String>) sharedPref.getStringSet(SHOPPING_LIST, new HashSet<String>());
        shoppingList1.add(recipeName);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(SHOPPING_LIST, shoppingList1);
        editor.commit();
    }

    public static ArrayList<String> getShoppingRecipeList(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHOPPING_LIST_PREFERENCE, Context.MODE_PRIVATE);
        HashSet<String> recipeList = (HashSet<String>) sharedPref.getStringSet(SHOPPING_LIST, new HashSet<String>());
        ArrayList<String> returnList = new ArrayList<>();
        for (String recipe : recipeList) {
            returnList.add(recipe);
        }
        return returnList;
    }

    public static void removeRecipeFromList(Context context, String recipeName) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHOPPING_LIST_PREFERENCE, Context.MODE_PRIVATE);
        HashSet<String> recipeList = (HashSet<String>) sharedPref.getStringSet(SHOPPING_LIST, null);
        if (recipeList != null) {
            recipeList.remove(recipeName);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putStringSet(SHOPPING_LIST, recipeList);
            editor.commit();
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(recipeName);
        editor.commit();
    }

    public static void removeAllRecipesFromShopList(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHOPPING_LIST_PREFERENCE, Context.MODE_PRIVATE);
        HashSet<String> recipeList = (HashSet<String>) sharedPref.getStringSet(SHOPPING_LIST, null);
        if (recipeList != null) {
            for (Iterator<String> iterator = recipeList.iterator(); iterator.hasNext(); ) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove(iterator.next());
                editor.commit();
            }
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(SHOPPING_LIST,null);
        editor.commit();
    }

    /*Recipe Bookmark Shared Preferences */

    public static void addRecipeBookmark(Context context, RecipeBookmark bookmarkObj) {
        SharedPreferences mPrefs = context.getSharedPreferences(BOOKMARK_LIST_PREFERENCE, Context.MODE_PRIVATE);
        HashSet<String> bookmarkList = (HashSet<String>) mPrefs.getStringSet(BOOKMARK_LIST, new HashSet<String>());
        SharedPreferences.Editor editor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(bookmarkObj);
        bookmarkList.add(json);
        editor.putStringSet(BOOKMARK_LIST, bookmarkList);
        editor.commit();
    }

    public static ArrayList<RecipeBookmark> getRecipeBookmarks(Context context) {
        ArrayList<RecipeBookmark> recipeBookmarkArrayList = new ArrayList<>();
        SharedPreferences mPrefs = context.getSharedPreferences(BOOKMARK_LIST_PREFERENCE, Context.MODE_PRIVATE);
        HashSet<String> bookmarkList = (HashSet<String>) mPrefs.getStringSet(BOOKMARK_LIST, new HashSet<String>());
        for (String json : bookmarkList) {
            Gson gson = new Gson();
            RecipeBookmark recipeBookmark = gson.fromJson(json, RecipeBookmark.class);
            recipeBookmarkArrayList.add(recipeBookmark);
        }
        return recipeBookmarkArrayList;
    }

    public static void deleteRecipeBookmark(Context context, RecipeBookmark bookmark) {
        SharedPreferences mPrefs = context.getSharedPreferences(BOOKMARK_LIST_PREFERENCE, Context.MODE_PRIVATE);
        HashSet<String> bookmarkList = (HashSet<String>) mPrefs.getStringSet(BOOKMARK_LIST, new HashSet<String>());
        SharedPreferences.Editor editor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(bookmark);
        bookmarkList.remove(json);
        if (!bookmarkList.isEmpty()) {
            editor.putStringSet(BOOKMARK_LIST, bookmarkList);
        } else {
            editor.remove(BOOKMARK_LIST);
        }
        editor.commit();
    }

    public static void removeAllBookmarkFromList(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(BOOKMARK_LIST_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(BOOKMARK_LIST);
        editor.commit();
    }
}
