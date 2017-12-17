package com.axiom.cookmate.loader;


import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

import com.axiom.cookmate.data.MyRecipe;
import com.axiom.cookmate.data.RecipeContract;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MyRecipeLoader extends AsyncTaskLoader<List<MyRecipe>> {
    private Context mContext;

    public MyRecipeLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<MyRecipe> loadInBackground() {

        Cursor cursor = getContext().getContentResolver().query(RecipeContract.MyRecipeEntry.CONTENT_URI,
                null, null, null, null);
        List<MyRecipe> myRecipes = new ArrayList<>();

        while (cursor.moveToNext()) {
            ArrayList<String> ingredientList = new ArrayList<>();
            ArrayList<String> instructionList = new ArrayList<>();
            String id = cursor.getString(cursor.getColumnIndexOrThrow(RecipeContract.MyRecipeEntry._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(RecipeContract.MyRecipeEntry.COLUMN_RECIPE_NAME));
            String image = cursor.getString(cursor.getColumnIndexOrThrow(RecipeContract.MyRecipeEntry.COLUMN_IMAGE));
            String instruction = cursor.getString(cursor.getColumnIndexOrThrow(RecipeContract.MyRecipeEntry.COLUMN_INSTRUCTION));
            String ingredient = cursor.getString(cursor.getColumnIndex(RecipeContract.MyRecipeEntry.COLUMN_INGREDIENTS));
            StringTokenizer st1 = new StringTokenizer(ingredient, ";");
            while (st1.hasMoreTokens()) {
                ingredientList.add(st1.nextToken());
            }

            instruction = instruction.substring(0, instruction.length() - 1);
            StringTokenizer st2 = new StringTokenizer(instruction, ";");
            while (st2.hasMoreTokens()) {
                instructionList.add(st2.nextToken());
            }

            MyRecipe myCurrentRecipe = new MyRecipe(id, name, image, instructionList, ingredientList);

            myRecipes.add(myCurrentRecipe);
        }

        cursor.close();
        return myRecipes;
    }

}
