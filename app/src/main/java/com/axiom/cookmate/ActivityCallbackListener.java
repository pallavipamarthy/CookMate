package com.axiom.cookmate;


import android.os.Bundle;

import com.axiom.cookmate.data.MyRecipe;
import com.axiom.cookmate.data.Recipe;

public interface ActivityCallbackListener {

    void onListLoaded(Bundle b);
    void onRecipeClick(Recipe recipe,Bundle b);
    void onAddToShoppingList(String ingredientName);
    void onMyRecipeClick(MyRecipe myrecipe,Bundle b);
    void onRecipeDeleted();

}
