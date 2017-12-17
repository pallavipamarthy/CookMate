package com.axiom.cookmate.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.axiom.cookmate.R;
import com.axiom.cookmate.data.RecipeBookmark;
import com.axiom.cookmate.data.RecipeContract;
import com.axiom.cookmate.utilities.AccountUtils;
import com.axiom.cookmate.utilities.RecipeUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class SyncActivity extends Activity {

    private static final String TAG = SyncActivity.class.getSimpleName();
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.sync_dialog_text));
        mProgressDialog.show();

        syncDataFromRealTimeDB();
    }

    private void syncDataFromRealTimeDB() {
        String userId = AccountUtils.getFirebaseUserId(this);
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        DatabaseReference mFirebaseDBRef = mFirebaseInstance.getReference("users");

        DatabaseReference mUserRef = mFirebaseDBRef.child(userId);
        mUserRef.addValueEventListener(userListener);
    }

    ValueEventListener userListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.hasChild("favorite_recipe_list")) {
                addFavoritesListToDB(dataSnapshot);
            }
            if(dataSnapshot.hasChild("my_recipe_list")) {
                addMyRecipesListToDB(dataSnapshot);
            }
            if(dataSnapshot.hasChild("bookmarks_list")) {
                saveBookmarkList(dataSnapshot);
            }
            if(dataSnapshot.hasChild("shopping_list")) {
                saveShoppingList(dataSnapshot);
            }
            Intent intent = new Intent(SyncActivity.this, SearchMainActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "loadUserData:onCancelled", databaseError.toException());
        }
    };

    private void addFavoritesListToDB(DataSnapshot dataSnapshot) {
        Iterable<DataSnapshot> mFavoriteList = dataSnapshot.child("favorite_recipe_list").getChildren();
        for (DataSnapshot favorite : mFavoriteList) {
            HashMap<Object, Object> recipe = (HashMap<Object, Object>) favorite.getValue();
            String recipeName = (String) recipe.get("recipeName");
            String recipeImageUrl = (String) recipe.get("imageUrl");
            String instructionUrl = (String) recipe.get("instructionUrl");
            List<String> ingredients = (List<String>) recipe.get("ingredientList");

            ContentValues values = new ContentValues();
            values.put(RecipeContract.FavouriteRecipeEntry.COLUMN_IMAGE_URL, recipeImageUrl);
            values.put(RecipeContract.FavouriteRecipeEntry.COLUMN_RECIPE_NAME, recipeName);
            values.put(RecipeContract.FavouriteRecipeEntry.COLUMN_INSTRUCTION_URL, instructionUrl);
            values.put(RecipeContract.FavouriteRecipeEntry.COLUMN_INGREDIENTS, ingredients.toString());

            Uri newUri = getContentResolver().insert(RecipeContract.FavouriteRecipeEntry.CONTENT_URI, values);
        }
    }

    private void addMyRecipesListToDB(DataSnapshot dataSnapshot) {
        Iterable<DataSnapshot> myRecipeList = dataSnapshot.child("my_recipe_list").getChildren();
        for (DataSnapshot data : myRecipeList) {
            HashMap<Object, Object> myRecipe = (HashMap<Object, Object>) data.getValue();
            String mPicturePath = (String) myRecipe.get("image");
            String mRecipeTitle = (String) myRecipe.get("recipeName");
            List<String> ingredient = (List<String>) myRecipe.get("ingredientList");
            List<String> instruction = (List<String>) myRecipe.get("instructionList");

            // Saving the recipe to the database.
            ContentValues values = new ContentValues();
            values.put(RecipeContract.MyRecipeEntry.COLUMN_IMAGE, mPicturePath);
            values.put(RecipeContract.MyRecipeEntry.COLUMN_RECIPE_NAME, mRecipeTitle);
            values.put(RecipeContract.MyRecipeEntry.COLUMN_INSTRUCTION, instruction.toString());
            values.put(RecipeContract.MyRecipeEntry.COLUMN_INGREDIENTS, ingredient.toString());
            Uri newUri = getContentResolver().insert(RecipeContract.MyRecipeEntry.CONTENT_URI, values);
        }
    }

    private void saveBookmarkList(DataSnapshot dataSnapshot) {
        Iterable<DataSnapshot> mBookmarkList = dataSnapshot.child("bookmarks_list").getChildren();
        for (DataSnapshot bookmark : mBookmarkList) {
            String recipeName = bookmark.getKey();
            String recipeUrl = (String) bookmark.getValue();
            RecipeBookmark recipeBookmark = new RecipeBookmark(recipeName, recipeUrl);
            RecipeUtils.addRecipeBookmark(this, recipeBookmark);
        }
    }

    private void saveShoppingList(DataSnapshot dataSnapshot) {
        Iterable<DataSnapshot> mShoppingList = dataSnapshot.child("shopping_list").getChildren();
        for (DataSnapshot recipe : mShoppingList) {
            String recipeName = (String) recipe.getKey();
            Iterable<DataSnapshot> mIngredientList = recipe.getChildren();
            for (DataSnapshot ingredientItem : mIngredientList) {
                String ingredient = (String) ingredientItem.getValue();
                RecipeUtils.addIngredientToRecipeShoppingList(this, recipeName, ingredient);
            }
        }
    }
}
