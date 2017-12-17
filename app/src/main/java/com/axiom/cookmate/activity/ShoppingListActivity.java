package com.axiom.cookmate.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.axiom.cookmate.FirebaseAnalyticsUtils;
import com.axiom.cookmate.R;
import com.axiom.cookmate.fragment.ShopDetailFragment;
import com.axiom.cookmate.fragment.ShopListFragment;
import com.axiom.cookmate.utilities.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShoppingListActivity extends AppCompatActivity implements
        ShopListFragment.OnShopRecipeListener, ShopDetailFragment.OnShopDetailsListener {

    FragmentManager fm;
    private static final int MSG_RECIPE_CLICKED = 1;
    Bundle mShopListBundle;
    @BindView(R.id.empty_list_view)
    TextView mEmptyListView;
    private static final String EVENT_SHOP_ACTIVITY_LAUNCH = "shop_activity_launch";

    private ShopListFragment shopListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        ButterKnife.bind(this);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Bundle b = new Bundle();
        b.putString(EVENT_SHOP_ACTIVITY_LAUNCH, getString(R.string.event_shop_activity_launched));
        FirebaseAnalyticsUtils.reportShoppingActivityLaunched(this, b);

        mEmptyListView.setText(getResources().getString(R.string.empty_shop_list_text));

        shopListFragment = (ShopListFragment)
                getFragmentManager().findFragmentByTag(Constants.SHOP_LIST_FRAGMENT_TAG);
        if (shopListFragment == null) {
            shopListFragment = new ShopListFragment();
            shopListFragment.setRetainInstance(true);
            fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.shop_fragment_container, shopListFragment, Constants.SHOP_LIST_FRAGMENT_TAG);
            ft.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        }
        shopListFragment.refreshList();
    }

    public void onShopRecipeClick(Bundle bundle) {
        if (getResources().getBoolean(R.bool.isTablet) &&
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mShopListBundle = bundle;
            loadDetailFragment();
        } else {
            Intent intent = new Intent(ShoppingListActivity.this, ShopDetailActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public void onShopListLoaded(Bundle bundle) {
        if (bundle == null) {
            mEmptyListView.setVisibility(View.VISIBLE);
        } else {
            mEmptyListView.setVisibility(View.INVISIBLE);
            mShopListBundle = bundle;
            Message msg = mFragmentMessageHandler.obtainMessage(MSG_RECIPE_CLICKED);
            mFragmentMessageHandler.sendMessage(msg);
        }

    }

    public void loadDetailFragment() {
        if (getResources().getBoolean(R.bool.isTablet) &&
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ShopDetailFragment shopDetailFragment = new ShopDetailFragment();
            shopDetailFragment.setArguments(mShopListBundle);
            fm = getFragmentManager();
            FragmentTransaction ft2 = fm.beginTransaction();
            ft2.replace(R.id.shop_fragment_container1, shopDetailFragment, Constants.SHOP_DETAIL_FRAGMENT_TAG);
            ft2.commit();
        }
    }

    private void removeDetailFragment() {
        if (getResources().getBoolean(R.bool.isTablet) &&
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fm = getFragmentManager();
            FragmentTransaction ft2 = fm.beginTransaction();
            ft2.remove(fm.findFragmentByTag(Constants.SHOP_DETAIL_FRAGMENT_TAG));
            ft2.commit();
        }
    }

    private Handler mFragmentMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_RECIPE_CLICKED) {
                loadDetailFragment();
            }
        }
    };

    public void onListDeleted() {
        removeDetailFragment();
        shopListFragment.refreshList();
    }
}
