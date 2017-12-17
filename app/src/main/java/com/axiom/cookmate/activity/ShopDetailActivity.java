package com.axiom.cookmate.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.axiom.cookmate.R;
import com.axiom.cookmate.fragment.ShopDetailFragment;
import com.axiom.cookmate.utilities.Constants;

public class ShopDetailActivity extends AppCompatActivity implements ShopDetailFragment.OnShopDetailsListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        String recipeName = getIntent().getStringExtra(getResources().getString(R.string.recipe_name_string));
        setTitle(getString(R.string.app_name));
        setTitleColor(Color.WHITE);
        Bundle b = new Bundle();
        b.putString(getResources().getString(R.string.recipe_name_string), recipeName);

        ShopDetailFragment shopDetailFragment;
        shopDetailFragment = (ShopDetailFragment)
                getFragmentManager().findFragmentByTag(Constants.SHOP_DETAIL_FRAGMENT_TAG);
        if (shopDetailFragment == null) {
            shopDetailFragment = new ShopDetailFragment();
            shopDetailFragment.setRetainInstance(true);
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_container, shopDetailFragment, Constants.SHOP_DETAIL_FRAGMENT_TAG);
            ft.commit();
            shopDetailFragment.setArguments(b);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

    }

    public void onListDeleted() {
        finish();
    }
}
