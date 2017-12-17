package com.axiom.cookmate.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.axiom.cookmate.ProfileImage;
import com.axiom.cookmate.R;
import com.axiom.cookmate.data.RecipeBookmark;
import com.axiom.cookmate.utilities.AccountUtils;
import com.axiom.cookmate.utilities.RecipeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookmarkListActivity extends NavigationalActivity {
    private ArrayList<RecipeBookmark> mRecipeBookmarks;
    private ArrayAdapter mAdapter;
    @BindView(R.id.bookmark_list_view)
    ListView mBookmarkRecipeListView;
    private static final String BOOKMARK_OBJ = "bookmarkObj";
    @BindView(R.id.empty_list_view)
    TextView mEmptyListView;

    private View navHeader;
    private ImageView mProfileImageView;
    private TextView mProfileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark_list_layout);
        ButterKnife.bind(this);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.bookmark_activity_title));

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
                    startActivity(new Intent(BookmarkListActivity.this, MainAuthenticationActivity.class));
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

    }

    protected void onResume() {
        super.onResume();
        mEmptyListView.setText(getResources().getString(R.string.empty_bookmark_list_view));

        mRecipeBookmarks = RecipeUtils.getRecipeBookmarks(this);
        ArrayList<String> recipeNames = new ArrayList<>();
        for (RecipeBookmark recipeBookmark : mRecipeBookmarks) {
            String recipeName = recipeBookmark.getRecipeName();
            recipeNames.add(recipeName);
        }

        if (mRecipeBookmarks != null && !mRecipeBookmarks.isEmpty()) {
            mEmptyListView.setVisibility(View.INVISIBLE);
            mAdapter = new ArrayAdapter(this, R.layout.shop_list_item, R.id.itemText, recipeNames);
            mBookmarkRecipeListView.setAdapter(mAdapter);
            mBookmarkRecipeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {
                    Intent intent = new Intent(BookmarkListActivity.this, BookmarkWebActivity.class);
                    RecipeBookmark bookmarkObj = mRecipeBookmarks.get(position);
                    intent.putExtra(BOOKMARK_OBJ, bookmarkObj);
                    startActivity(intent);
                }
            });
        } else {
            if (mAdapter != null) {
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
            }
            mEmptyListView.setVisibility(View.VISIBLE);

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
}