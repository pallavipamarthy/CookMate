package com.axiom.cookmate.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.axiom.cookmate.ActivityCallbackListener;
import com.axiom.cookmate.R;
import com.axiom.cookmate.data.MyRecipe;
import com.axiom.cookmate.data.Recipe;
import com.axiom.cookmate.fragment.DetailFragment;
import com.axiom.cookmate.fragment.MyRecipeDetailFragment;
import com.axiom.cookmate.utilities.Constants;

public class RecipeDetailActivity extends AppCompatActivity implements ActivityCallbackListener {

    private static final int MSG_ADD_TO_SHOPPING_LIST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        getWindow().setEnterTransition(null);
        getWindow().setSharedElementEnterTransition(enterTransition());

        String activity = getIntent().getStringExtra(Constants.ACTIVITY);
        Bundle b = new Bundle();
        if (activity.equals(getResources().getString(R.string.my_recipe_string))) {
            MyRecipe myrecipe = getIntent().getParcelableExtra(Constants.MY_RECIPE_OBJ);
            setTitle(myrecipe.getRecipeName());
            b.putParcelable(Constants.MY_RECIPE_OBJ, myrecipe);
            MyRecipeDetailFragment myRecipeDetailFragment;
            myRecipeDetailFragment = (MyRecipeDetailFragment)
                    getFragmentManager().findFragmentByTag(Constants.MY_RECIPE_DETAIL_FRAGMENT_TAG);
            if (myRecipeDetailFragment == null) {
                myRecipeDetailFragment = new MyRecipeDetailFragment();
                myRecipeDetailFragment.setRetainInstance(true);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, myRecipeDetailFragment, Constants.MY_RECIPE_DETAIL_FRAGMENT_TAG);
                ft.commit();
                myRecipeDetailFragment.setArguments(b);
            }
        } else if (activity.equals(getResources().getString(R.string.recipes_string))) {
            Recipe recipe = getIntent().getParcelableExtra(Constants.RECIPE_OBJ);
            setTitle(recipe.getRecipeName());
            b.putParcelable(Constants.RECIPE_OBJ, recipe);
            DetailFragment detailFragment;
            detailFragment = (DetailFragment)
                    getFragmentManager().findFragmentByTag(Constants.DETAIL_FRAGMENT_TAG);
            if (detailFragment == null) {
                detailFragment = new DetailFragment();
                FragmentManager fm = getFragmentManager();
                detailFragment.setRetainInstance(true);
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, detailFragment, Constants.DETAIL_FRAGMENT_TAG);
                ft.commit();
                detailFragment.setArguments(b);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Handler mFragmentMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ADD_TO_SHOPPING_LIST) {
                showSnackBar();
            }

        }
    };

    public void onAddToShoppingList(String ingredient) {
        Message msg = mFragmentMessageHandler.obtainMessage(MSG_ADD_TO_SHOPPING_LIST);
        mFragmentMessageHandler.sendMessage(msg);
    }

    public void showSnackBar() {
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.fragment_container);
        Snackbar shopSnackBar = Snackbar
                .make(frameLayout, getString(R.string.added_to_shop_list), Snackbar.LENGTH_SHORT);
        View sbView = shopSnackBar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        shopSnackBar.show();
    }

    private Transition enterTransition() {
        ChangeBounds bounds = new ChangeBounds();
        bounds.setInterpolator(new DecelerateInterpolator());
        bounds.setDuration(500);
        return bounds;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    public void onRecipeClick(Recipe recipe, Bundle animationBundle) {
    }

    public void onMyRecipeClick(MyRecipe myrecipe, Bundle b) {
    }

    public void onListLoaded(Bundle bundle) {
    }

    public void onRecipeDeleted() {
        finish();
    }
}
