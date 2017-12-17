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
import com.axiom.cookmate.fragment.MyRecipeDetailFragment;
import com.axiom.cookmate.fragment.MyRecipeListFragment;
import com.axiom.cookmate.utilities.AccountUtils;
import com.axiom.cookmate.utilities.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyRecipesActivity extends NavigationalActivity implements ActivityCallbackListener {

    private FragmentManager mFragmentManager;
    private static final int MSG_RECIPE_CLICKED = 1;
    private static final int MSG_ADD_TO_SHOPPING_LIST = 2;
    private Bundle mFirstRecipeBundle;
    @BindView(R.id.empty_list_view)
    TextView mEmptyListView;
    private MyRecipeListFragment mMyRecipeListFragment;
    private View navHeader;
    private ImageView mProfileImageView;
    private TextView mProfileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_recipes_layout);
        ButterKnife.bind(this);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mEmptyListView.setText(getResources().getString(R.string.empty_my_recipes_string));
        Toolbar toolbar = (Toolbar) findViewById(R.id.myrecipes_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.my_recipe_string));

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
                    startActivity(new Intent(MyRecipesActivity.this, MainAuthenticationActivity.class));
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

        mMyRecipeListFragment = (MyRecipeListFragment)
                getFragmentManager().findFragmentByTag(Constants.MY_RECIPE_LIST_FRAGMENT_TAG);
        if (mMyRecipeListFragment == null) {
            mFragmentManager = getFragmentManager();
            mMyRecipeListFragment = new MyRecipeListFragment();
            mMyRecipeListFragment.setRetainInstance(true);
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.add(R.id.fragment_container, mMyRecipeListFragment, Constants.MY_RECIPE_LIST_FRAGMENT_TAG);
            ft.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMyRecipeListFragment.refreshList();

    }

    public void onMyRecipeClick(MyRecipe myRecipe, Bundle animationBundle) {
        if (getResources().getBoolean(R.bool.isTablet) &&
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            MyRecipeDetailFragment myRecipeDetailFragment = new MyRecipeDetailFragment();
            Bundle recievedBundle = new Bundle();
            recievedBundle.putParcelable(Constants.MY_RECIPE_OBJ, myRecipe);
            myRecipeDetailFragment.setArguments(recievedBundle);
            mFragmentManager = getFragmentManager();
            FragmentTransaction ft2 = mFragmentManager.beginTransaction();
            ft2.replace(R.id.fragment_container1, myRecipeDetailFragment, Constants.MY_RECIPE_DETAIL_FRAGMENT_TAG);
            ft2.commit();
        } else {

            Intent intent = new Intent(MyRecipesActivity.this, RecipeDetailActivity.class);
            Bundle recievedBundle = new Bundle();
            recievedBundle.putParcelable(Constants.MY_RECIPE_OBJ, myRecipe);
            recievedBundle.putString(Constants.ACTIVITY, getResources().getString(R.string.my_recipe_string));
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
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

    private void loadMyDetailFragment() {
        if (getResources().getBoolean(R.bool.isTablet) &&
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            MyRecipeDetailFragment myRecipeDetailFragment = new MyRecipeDetailFragment();
            myRecipeDetailFragment.setArguments(mFirstRecipeBundle);
            mFragmentManager = getFragmentManager();
            FragmentTransaction ft2 = mFragmentManager.beginTransaction();
            ft2.add(R.id.fragment_container1, myRecipeDetailFragment, Constants.MY_RECIPE_DETAIL_FRAGMENT_TAG);
            ft2.commit();
        }
    }

    private void removeDetailFragment() {
        if (getResources().getBoolean(R.bool.isTablet) &&
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mFragmentManager = getFragmentManager();
            FragmentTransaction ft2 = mFragmentManager.beginTransaction();
            ft2.remove(mFragmentManager.findFragmentByTag(Constants.MY_RECIPE_DETAIL_FRAGMENT_TAG));
            ft2.commitAllowingStateLoss();
        }
    }

    private Handler mFragmentMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_RECIPE_CLICKED) {
                loadMyDetailFragment();
            } else if (msg.what == MSG_ADD_TO_SHOPPING_LIST) {
                showSnackBar();
            }
        }
    };

    public void onAddToShoppingList(String ingredient) {
        Message msg = mFragmentMessageHandler.obtainMessage(MSG_ADD_TO_SHOPPING_LIST);
        mFragmentMessageHandler.sendMessage(msg);
    }

    public void onRecipeDeleted() {
        removeDetailFragment();
        mMyRecipeListFragment.refreshList();
    }

    public void onRecipeClick(Recipe recipe, Bundle animationBundle) {
    }
}
