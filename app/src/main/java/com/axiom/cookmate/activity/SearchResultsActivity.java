package com.axiom.cookmate.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.axiom.cookmate.RecipeIntentService;
import com.axiom.cookmate.data.MyRecipe;
import com.axiom.cookmate.data.Recipe;
import com.axiom.cookmate.fragment.DetailFragment;
import com.axiom.cookmate.fragment.SearchListFragment;
import com.axiom.cookmate.utilities.AccountUtils;
import com.axiom.cookmate.utilities.Constants;
import com.axiom.cookmate.utilities.NetworkUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchResultsActivity extends NavigationalActivity implements ActivityCallbackListener {

    FragmentManager fm;
    Bundle mFirstRecipeBundle;
    @BindView(R.id.empty_list_view)
    TextView mEmptyListView;
    @BindView(R.id.fragment_container)
    FrameLayout mFrameLayout;
    private Snackbar mSnackbar;
    SearchListFragment mListFragment;
    private NetworkInfoReceiver mNetworkInfoReceiver;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Bundle mFinalRecipeUrlBundle;
    private int mResultsFrom = 11;
    private int mResultsTo = 31;

    private static final int MSG_RECIPE_CLICKED = 1;
    private static final int MSG_ADD_TO_SHOPPING_LIST = 2;
    private static final String FINAL_RECIPE_URL = "com.axiom.cookmate.extra.FINAL_RECIPE_URL";

    private View navHeader;
    private ImageView mProfileImageView;
    private TextView mProfileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        ButterKnife.bind(this);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFrameLayout = (FrameLayout) findViewById(R.id.fragment_container);
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
                    startActivity(new Intent(SearchResultsActivity.this, MainAuthenticationActivity.class));
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
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));

        mEmptyListView.setText(getResources().getString(R.string.empty_search_results_string));

        mFinalRecipeUrlBundle = getIntent().getBundleExtra("bundle");
        mListFragment = (SearchListFragment)
                getFragmentManager().findFragmentByTag(Constants.LIST_FRAGMENT_TAG);
        if (mListFragment == null) {
            fm = getFragmentManager();
            mListFragment = new SearchListFragment();
            mListFragment.setArguments(mFinalRecipeUrlBundle);
            mListFragment.setRetainInstance(true);
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_container, mListFragment, Constants.LIST_FRAGMENT_TAG);
            ft.commit();
        }

        mSnackbar = Snackbar
                .make(mFrameLayout, R.string.no_internet_connection_snackbar, Snackbar.LENGTH_INDEFINITE);

        View sbView = mSnackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        textView.setGravity(Gravity.CENTER);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mNetworkInfoReceiver = new NetworkInfoReceiver();
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        IntentFilter refreshFilter = new IntentFilter(Constants.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(mNetworkInfoReceiver, networkFilter);
        registerReceiver(mNetworkInfoReceiver, refreshFilter);
        if (!NetworkUtils.isNetworkConnected(this)) {
            mSnackbar.show();
        }
    }

    private void refresh() {
        Intent intent = new Intent(this, RecipeIntentService.class);
        intent.setAction(RecipeIntentService.ACTION_NEXT_PAGE_DOWNLOAD);
        String finalRecipeUrl = mFinalRecipeUrlBundle.getString(getString(R.string.final_recipe_url_string));
        StringBuilder sb = new StringBuilder();
        sb.append(finalRecipeUrl);
        sb.append(Constants.PAGE_FROM);
        sb.append(mResultsFrom);
        sb.append(Constants.PAGE_TO);
        sb.append(mResultsTo);
        intent.putExtra(FINAL_RECIPE_URL, sb.toString());
        startService(intent);
    }

    public void onRecipeClick(Recipe recipe, Bundle animationBundle) {
        if (getResources().getBoolean(R.bool.isTablet) &&
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            DetailFragment detailFragment = new DetailFragment();
            Bundle recipeBundle = new Bundle();
            recipeBundle.putParcelable(Constants.RECIPE_OBJ, recipe);
            detailFragment.setArguments(recipeBundle);
            fm = getFragmentManager();
            FragmentTransaction ft2 = fm.beginTransaction();
            ft2.replace(R.id.fragment_container1, detailFragment, Constants.DETAIL_FRAGMENT_TAG);
            ft2.commitAllowingStateLoss();

        } else {
            Intent intent = new Intent(SearchResultsActivity.this, RecipeDetailActivity.class);
            Bundle recipeBundle = new Bundle();
            recipeBundle.putParcelable(Constants.RECIPE_OBJ, recipe);
            recipeBundle.putString(Constants.ACTIVITY, getResources().getString(R.string.recipes_string));
            intent.putExtras(recipeBundle);
            startActivity(intent, animationBundle);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkInfoReceiver != null) {
            unregisterReceiver(mNetworkInfoReceiver);
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
            fm = getFragmentManager();
            FragmentTransaction ft2 = fm.beginTransaction();
            ft2.add(R.id.fragment_container1, detailFragment, Constants.DETAIL_FRAGMENT_TAG);
            ft2.commitAllowingStateLoss();
        }
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

    public void showSnackBar() {
        Snackbar shopSnackBar = Snackbar
                .make(mFrameLayout, getString(R.string.added_to_shop_list), Snackbar.LENGTH_SHORT);
        View sbView = shopSnackBar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        textView.setGravity(Gravity.CENTER);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        shopSnackBar.show();
    }

    public void onMyRecipeClick(MyRecipe myrecipe, Bundle b) {
    }

    private class NetworkInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (NetworkUtils.isNetworkConnected(context)) {
                    mSnackbar.dismiss();
                } else {
                    mSnackbar.show();
                }
            } else if (action.equals(Constants.ACTION_DOWNLOAD_COMPLETE)) {
                ArrayList<Recipe> recipeList = intent.getParcelableArrayListExtra("recipeList");
                SearchListFragment searchListFragment = (SearchListFragment) getFragmentManager().findFragmentByTag(Constants.LIST_FRAGMENT_TAG);
                searchListFragment.refreshAdapterData(recipeList);
                mSwipeRefreshLayout.setRefreshing(false);
                mResultsFrom = mResultsFrom + 20;
                mResultsTo = mResultsTo + 20;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

    }

    public void onRecipeDeleted() {
    }
}
