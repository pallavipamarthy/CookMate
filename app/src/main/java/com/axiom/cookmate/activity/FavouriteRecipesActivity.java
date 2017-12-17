package com.axiom.cookmate.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.axiom.cookmate.ActivityCallbackListener;
import com.axiom.cookmate.ProfileImage;
import com.axiom.cookmate.R;
import com.axiom.cookmate.data.MyRecipe;
import com.axiom.cookmate.data.Recipe;
import com.axiom.cookmate.fragment.DetailFragment;
import com.axiom.cookmate.fragment.FavouriteListFragment;
import com.axiom.cookmate.utilities.AccountUtils;
import com.axiom.cookmate.utilities.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavouriteRecipesActivity extends NavigationalActivity implements ActivityCallbackListener {

    private FragmentManager mFragmentManager;
    private Bundle mFirstRecipeBundle;
    @BindView(R.id.empty_list_view)
    TextView mEmptyListView;
    FavouriteListFragment mFavouriteListFragment;

    private static final int MSG_RECIPE_CLICKED = 1;
    private static final int MSG_ADD_TO_SHOPPING_LIST = 2;

    private View navHeader;
    private ImageView mProfileImageView;
    private TextView mProfileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourite_recipe_list);
        ButterKnife.bind(this);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navHeader = navigationView.getHeaderView(0);

        mProfileImageView = (ImageView) navHeader.findViewById(R.id.profile_image);
        mProfileName = (TextView) navHeader.findViewById(R.id.user_name);
        TextView profileEmail = (TextView) navHeader.findViewById(R.id.user_email_id);

        if (!AccountUtils.getUserLogin(this)) {
            mProfileName.setVisibility(View.INVISIBLE);
            profileEmail.setVisibility(View.INVISIBLE);
            Button signInButton = (Button) navHeader.findViewById(R.id.sign_in_button);
            signInButton.setVisibility(View.VISIBLE);
            signInButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startActivity(new Intent(FavouriteRecipesActivity.this, MainAuthenticationActivity.class));
                }
            });
        } else {
            Glide.with(this).load(AccountUtils.getAccountPhoto(this))
                    .crossFade()
                    .thumbnail(0.5f)
                    .bitmapTransform(new ProfileImage(this))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mProfileImageView);


            mProfileName = (TextView) navHeader.findViewById(R.id.user_name);
            mProfileName.setText(AccountUtils.getAccountName(this));
            profileEmail.setText(AccountUtils.getAccountEmail(this));
        }

        mEmptyListView.setText(getResources().getString(R.string.empty_fav_list_string));

        mFavouriteListFragment = (FavouriteListFragment)
                getFragmentManager().findFragmentByTag(Constants.FAVOURITE_LIST_FRAGMENT_TAG);
        if (mFavouriteListFragment == null) {
            mFragmentManager = getFragmentManager();
            mFavouriteListFragment = new FavouriteListFragment();
            mFavouriteListFragment.setRetainInstance(true);
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.add(R.id.fragment_container, mFavouriteListFragment, Constants.FAVOURITE_LIST_FRAGMENT_TAG);
            ft.commit();
        }
    }

    public void showSnackBar() {
        FrameLayout detailFrameLayout = (FrameLayout) findViewById(R.id.fragment_container1);
        Snackbar shopSnackBar = Snackbar
                .make(detailFrameLayout, getString(R.string.added_to_shop_list), Snackbar.LENGTH_SHORT);
        View sbView = shopSnackBar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        shopSnackBar.show();
    }

    public void onRecipeClick(Recipe recipe, Bundle animationBundle) {
        if (getResources().getBoolean(R.bool.isTablet) &&
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            DetailFragment detailFragment = new DetailFragment();
            Bundle recievedBundle = new Bundle();
            recievedBundle.putParcelable(Constants.RECIPE_OBJ, recipe);
            detailFragment.setArguments(recievedBundle);
            mFragmentManager = getFragmentManager();
            FragmentTransaction ft2 = mFragmentManager.beginTransaction();
            ft2.replace(R.id.fragment_container1, detailFragment, Constants.DETAIL_FRAGMENT_TAG);
            ft2.commit();
        } else {
            Intent intent = new Intent(FavouriteRecipesActivity.this, RecipeDetailActivity.class);
            Bundle recievedBundle = new Bundle();
            recievedBundle.putParcelable(Constants.RECIPE_OBJ, recipe);
            recievedBundle.putString(Constants.ACTIVITY, getResources().getString(R.string.recipes_string));
            intent.putExtras(recievedBundle);
            startActivity(intent, animationBundle);
        }
    }

    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void onListLoaded(Bundle bundle) {
        if (bundle == null) {
            mEmptyListView.setVisibility(View.VISIBLE);
        } else {
            mEmptyListView.setVisibility(View.INVISIBLE);
            mFirstRecipeBundle = bundle;
            Message msg = mFragmentMessageHandler.obtainMessage(MSG_RECIPE_CLICKED);
            mFragmentMessageHandler.sendMessage(msg);
        }
    }

    private void loadDetailFragment() {
        if (getResources().getBoolean(R.bool.isTablet) &&
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(mFirstRecipeBundle);
            mFragmentManager = getFragmentManager();
            FragmentTransaction ft2 = mFragmentManager.beginTransaction();
            ft2.add(R.id.fragment_container1, detailFragment, Constants.DETAIL_FRAGMENT_TAG);
            ft2.commit();

        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

    }

    private Handler mFragmentMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_RECIPE_CLICKED) {
                loadDetailFragment();
            } else if (msg.what == MSG_ADD_TO_SHOPPING_LIST) {
                showSnackBar();
            }

        }
    };

    public void onAddToShoppingList(String ingredient) {
        Message msg = mFragmentMessageHandler.obtainMessage(MSG_ADD_TO_SHOPPING_LIST);
        mFragmentMessageHandler.sendMessage(msg);
    }

    public void onMyRecipeClick(MyRecipe myrecipe, Bundle b) {
    }

    public void onRecipeDeleted() {

    }
}
