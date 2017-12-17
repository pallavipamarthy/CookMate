package com.axiom.cookmate.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.axiom.cookmate.ErrorDialogFragment;
import com.axiom.cookmate.FirebaseAnalyticsUtils;
import com.axiom.cookmate.ProfileImage;
import com.axiom.cookmate.R;
import com.axiom.cookmate.utilities.AccountUtils;
import com.axiom.cookmate.utilities.Constants;
import com.axiom.cookmate.utilities.NetworkUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchMainActivity extends NavigationalActivity {

    @BindView(R.id.cal_from_text)
    EditText mCaloriesFromEditText;
    @BindView(R.id.cal_to_text)
    EditText mCaloriesToEditText;
    @BindView(R.id.search_edit_text)
    EditText mRecipeSearchEditText;
    @BindView(R.id.search_button_image)
    ImageView mSearchButtonImageView;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.diet_radio_group)
    RadioGroup mDietGrp;
    @BindView(R.id.allergy_radio_group)
    RadioGroup mAllergyRadioGrp;
    private Snackbar mSnackbar;

    private String mCalGreaterThan = null;
    private String mCalLessThan = null;
    private String mRecipeQuery = "";
    private String mDietQuery = null;
    private String mHealthQuery = null;

    private NetworkInfoReceiver mNetworkInfoReceiver;

    private static final String EVENT_QUERY = "query";
    private static final String EVENT_CAL_FROM = "cal_from";
    private static final String EVENT_CAL_TO = "cal_to";
    private static final String EVENT_DIET = "diet";
    private static final String EVENT_HEALTH = "health";

    private View navHeader;
    private ImageView mProfileImageView;
    private TextView mProfileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_recipe);
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
                    startActivity(new Intent(SearchMainActivity.this, MainAuthenticationActivity.class));
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
        mSearchButtonImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onSearchButtonClick();
            }
        });
        mDietGrp.setOnCheckedChangeListener
                (new RadioGroup.OnCheckedChangeListener() {
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton selectedDietButton = (RadioButton) findViewById(checkedId);
                        boolean isChecked = selectedDietButton.isChecked();
                        if (isChecked) {
                            mDietQuery = selectedDietButton.getText().toString().toLowerCase();
                        }
                    }
                });

        mAllergyRadioGrp.setOnCheckedChangeListener
                (new RadioGroup.OnCheckedChangeListener() {
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton selectedAllergyButton = (RadioButton) findViewById(checkedId);
                        boolean isChecked = selectedAllergyButton.isChecked();
                        if (isChecked) {
                            mHealthQuery = selectedAllergyButton.getText().toString().toLowerCase();
                        }
                    }
                });
        mSnackbar = Snackbar
                .make(mCoordinatorLayout, R.string.no_internet_connection_snackbar, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(Color.RED);
        View sbView = mSnackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        mNetworkInfoReceiver = new NetworkInfoReceiver();
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkInfoReceiver, networkFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkInfoReceiver != null) {
            unregisterReceiver(mNetworkInfoReceiver);
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

    protected void onResume() {
        super.onResume();
        if (!NetworkUtils.isNetworkConnected(this)) {
            mSnackbar.show();
        }
    }

    private void onSearchButtonClick() {
        if (!NetworkUtils.isNetworkConnected(this)) {
            ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
            Bundle b = new Bundle();
            b.putString(Constants.ERROR_DIALOG_TITLE, getResources().getString(R.string.no_network_dialog_title));
            b.putString(Constants.ERROR_DIALOG_BODY, getResources().getString(R.string.no_network_dialog_body));
            errorDialogFragment.setArguments(b);
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            errorDialogFragment.show(getFragmentManager(), Constants.ALERT_DIALOG_TAG);
        } else {
            mCalGreaterThan = mCaloriesFromEditText.getText().toString().trim();
            mCalLessThan = mCaloriesToEditText.getText().toString().trim();
            mRecipeQuery = mRecipeSearchEditText.getText().toString().trim();

            if (mRecipeQuery.isEmpty()) {
                ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
                Bundle b = new Bundle();
                b.putString(Constants.ERROR_DIALOG_TITLE, getResources().getString(R.string.no_search_entered_title));
                b.putString(Constants.ERROR_DIALOG_BODY, getResources().getString(R.string.enter_search_query_string));
                errorDialogFragment.setArguments(b);
                errorDialogFragment.show(getFragmentManager(), Constants.ALERT_DIALOG_TAG);
            } else {
                String FINAL_RECIPE_URL = createRecipeQuery();
                Bundle bundle = new Bundle();
                bundle.putString(getResources().getString(R.string.final_recipe_url_string), FINAL_RECIPE_URL);
                bundle.putString(Constants.ACTIVITY, getResources().getString(R.string.recipes_string));
                Intent intent = new Intent(SearchMainActivity.this, SearchResultsActivity.class);
                intent.putExtra("bundle", bundle);
                Bundle searchEventBundle = getSearchEventBundle();
                FirebaseAnalyticsUtils.reportSearchEvent(this, searchEventBundle);
                startActivity(intent);
            }
        }
    }

    private String createRecipeQuery() {
        StringBuilder sb = new StringBuilder();
        if ((mRecipeQuery != null) && (!mRecipeQuery.isEmpty())) {
            sb.append(Constants.QUERY);
            sb.append(mRecipeQuery);
        }
        if (mCalGreaterThan != null && !mCalGreaterThan.equals("")) {
            sb.append(Constants.CALORIE_GREATER_THAN);
            sb.append(mCalGreaterThan);
        } else {
            sb.append("");
        }
        if (mCalLessThan != null && !mCalLessThan.equals("")) {
            sb.append(Constants.CALORIE_TO);
            sb.append(mCalLessThan);
        } else {
            sb.append("");
        }
        if (mHealthQuery != null) {
            sb.append(Constants.HEALTH);
            sb.append(mHealthQuery);
        }
        if (mDietQuery != null) {
            sb.append(Constants.DIET);
            sb.append(mDietQuery);
        }

        String query = sb.toString();
        return Constants.RECIPE_BASE_URL + query + Constants.APP_ID + Constants.APP_KEY;
    }

    private class NetworkInfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkUtils.isNetworkConnected(context)) {
                mSnackbar.dismiss();
            } else {
                mSnackbar.show();
            }
        }
    }

    private Bundle getSearchEventBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(EVENT_QUERY, mRecipeQuery);
        bundle.putString(EVENT_CAL_FROM, mCalGreaterThan);
        bundle.putString(EVENT_CAL_TO, mCalLessThan);
        bundle.putString(EVENT_DIET, mDietQuery);
        bundle.putString(EVENT_HEALTH, mHealthQuery);
        return bundle;
    }
}