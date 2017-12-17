package com.axiom.cookmate.fragment;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.axiom.cookmate.ActivityCallbackListener;
import com.axiom.cookmate.ErrorDialogFragment;
import com.axiom.cookmate.FirebaseAnalyticsUtils;
import com.axiom.cookmate.R;
import com.axiom.cookmate.activity.WebViewActivity;
import com.axiom.cookmate.data.Recipe;
import com.axiom.cookmate.data.RecipeContract.FavouriteRecipeEntry;
import com.axiom.cookmate.data.RecipeDbHelper;
import com.axiom.cookmate.utilities.AccountUtils;
import com.axiom.cookmate.utilities.Constants;
import com.axiom.cookmate.utilities.NetworkUtils;
import com.axiom.cookmate.utilities.RecipeUtils;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DetailFragment extends Fragment {

    private Recipe mCurrentRecipe;
    private Context mContext;
    private TextView[] mIngredientTextList;

    @BindView(R.id.detail_image_view)
    ImageView mDetailImageView;
    @BindView(R.id.ingredient_list_layout)
    LinearLayout mIngredientListLayout;
    @BindView(R.id.instruction_link)
    Button mInstructionLinkButton;
    @BindView(R.id.fab)
    FloatingActionButton mFavouriteImageView;
    RecipeDbHelper mDbHelper;
    String favRecipeUrl;
    @BindView(R.id.share)
    FloatingActionButton mShareButton;
    private ActivityCallbackListener mListener;
    private CoordinatorLayout mCoordinatorLayout;

    private static final String EVENT_RECIPE_NAME = "recipe_name";
    private static final String EVENT_ADD_TO_SHOP_LIST = "add_to_shop_list";

    DatabaseReference mFavoriteRecipeNode;
    DatabaseReference mShopListNode;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mListener = (ActivityCallbackListener) getActivity();
        if (getArguments() != null) {
            mCurrentRecipe = getArguments().getParcelable(Constants.RECIPE_OBJ);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mContext = getActivity();
        ButterKnife.bind(this, rootView);

        if (AccountUtils.getUserLogin(getActivity())) {
            FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
            DatabaseReference mFirebaseDBRef = mFirebaseInstance.getReference("users");
            String userId = AccountUtils.getFirebaseUserId(mContext);
            DatabaseReference firebaseUserNode = mFirebaseDBRef.child(userId);
            mFavoriteRecipeNode = firebaseUserNode.child("favorite_recipe_list");
            mShopListNode = firebaseUserNode.child("shopping_list");
        }
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_layout);
        ImageView attributionImageView = (ImageView) rootView.findViewById(R.id.image_attribution_view);
        attributionImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String edamamUrl = getString(R.string.edamam_url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(edamamUrl));
                startActivity(intent);
            }
        });

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mCurrentRecipe.getRecipeName());

        mShareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, mCurrentRecipe.getInstructionUrl());
                startActivity(Intent.createChooser(intent, getString(R.string.share_via_text)));
            }
        });

        Glide.with(this).load(mCurrentRecipe.getImageUrl()).into(mDetailImageView);
        mDetailImageView.setContentDescription(getString(R.string.a11y_dish_image, mCurrentRecipe.getRecipeName()));
        List<String> ingredientList = mCurrentRecipe.getIngredientList();
        int count = ingredientList.size();
        String instructionUrl = mCurrentRecipe.getInstructionUrl();

        mDbHelper = new RecipeDbHelper(mContext);
        String[] projection = {FavouriteRecipeEntry.COLUMN_INSTRUCTION_URL};
        String selection = FavouriteRecipeEntry.COLUMN_INSTRUCTION_URL + "=?";
        String[] selectionArgs = {instructionUrl};

        //Query the database of favourite movies to get a cursor object
        Cursor cursor = getActivity().getContentResolver().query(FavouriteRecipeEntry.CONTENT_URI, projection, selection, selectionArgs, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                favRecipeUrl = cursor.getString(cursor.getColumnIndexOrThrow(FavouriteRecipeEntry.COLUMN_INSTRUCTION_URL));

                if (favRecipeUrl.equals(instructionUrl)) {
                    mFavouriteImageView.setImageResource(R.drawable.favourite_icon_selected);
                    mFavouriteImageView.setTag("image2");
                }
            }
            cursor.close();
        }

        mFavouriteImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ((mFavouriteImageView.getTag() != null && mFavouriteImageView.getTag().toString().equals("image2"))) {
                    mFavouriteImageView.setImageResource(R.drawable.add_as_fav);
                    mFavouriteImageView.setTag("image1");
                    String selection = FavouriteRecipeEntry.COLUMN_INSTRUCTION_URL + "=?";
                    String[] selectionArgs = new String[]{mCurrentRecipe.getInstructionUrl()};
                    //Sync: delete from realtime db
                    if (AccountUtils.getUserLogin(getActivity())) {
                        String recipeId = null;
                        String[] projection = {FavouriteRecipeEntry._ID};
                        Cursor cursor1 = getActivity().getContentResolver().query(FavouriteRecipeEntry.CONTENT_URI, projection, selection, selectionArgs, null);
                        if (cursor1 != null) {
                            while (cursor1.moveToNext()) {
                                recipeId = cursor1.getString(cursor1.getColumnIndexOrThrow(FavouriteRecipeEntry._ID));
                            }
                        }
                        mFavoriteRecipeNode.child(recipeId).removeValue();
                    }

                    int recipeDeleted = getActivity().getContentResolver().delete(FavouriteRecipeEntry.CONTENT_URI, selection, selectionArgs);
                    if (recipeDeleted > 0) {
                        showSnackBar(getString(R.string.remove_from_favourites_snackbar));
                    }
                } else {
                    //If favourite view is not already selected,add the movie to favourites list
                    mFavouriteImageView.setImageResource(R.drawable.favourite_icon_selected);
                    mFavouriteImageView.setTag("image2");
                    addRecipe();

                    mFavouriteImageView.setActivated(true);
                }
            }
        });

        mIngredientListLayout = (LinearLayout) rootView.findViewById(R.id.ingredient_list_layout);

        LinearLayout.LayoutParams ingredientLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout.LayoutParams tvlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        mIngredientTextList = new TextView[count];
        LinearLayout[] mIngredientItemLayout = new LinearLayout[count];
        ImageView mShopImageButton;

        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageParams.setMargins(0, 0, 50, 0);

        for (int i = 0; i < count; i++) {
            mIngredientItemLayout[i] = new LinearLayout(mContext);
            mIngredientItemLayout[i].setLayoutParams(ingredientLayoutParams);
            mIngredientItemLayout[i].setOrientation(LinearLayout.HORIZONTAL);
            mIngredientItemLayout[i].setPadding(50, 50, 50, 50);

            mShopImageButton = new ImageView(mContext);
            mShopImageButton.setLayoutParams(imageParams);
            mShopImageButton.setImageResource(R.drawable.ic_add_shopping_cart);
            mShopImageButton.setContentDescription(getString(R.string.a11y_add_to_shopping_list));
            mShopImageButton.setTag(i);
            mShopImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (Integer) v.getTag();
                    String ingredient = mIngredientTextList[position].getText().toString();
                    RecipeUtils.addIngredientToRecipeShoppingList(getActivity(),
                            mCurrentRecipe.getRecipeName(), ingredient);
                    mListener.onAddToShoppingList(ingredient);
                    Bundle b = new Bundle();
                    b.putString(EVENT_ADD_TO_SHOP_LIST, getString(R.string.event_add_to_shoplist));
                    FirebaseAnalyticsUtils.reportAddToShoppingList(mContext, b);
                    if (AccountUtils.getUserLogin(getActivity())) {
                        DatabaseReference shoplistRef = mShopListNode.child(mCurrentRecipe.getRecipeName());
                        String ingredientKey = shoplistRef.push().getKey();
                        shoplistRef.child(ingredientKey).setValue(ingredient);
                    }
                }
            });
            mIngredientItemLayout[i].addView(mShopImageButton);

            mIngredientTextList[i] = new TextView(mContext);
            mIngredientTextList[i].setTextColor(Color.BLACK);
            mIngredientTextList[i].setLayoutParams(tvlp);
            mIngredientTextList[i].setText(ingredientList.get(i));
            mIngredientItemLayout[i].addView(mIngredientTextList[i]);
            mIngredientListLayout.addView(mIngredientItemLayout[i]);
        }

        mInstructionLinkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!NetworkUtils.isNetworkConnected(mContext)) {
                    ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
                    Bundle b = new Bundle();
                    b.putString(Constants.ERROR_DIALOG_TITLE, getString(R.string.no_network_dialog_title));
                    b.putString(Constants.ERROR_DIALOG_BODY, getString(R.string.no_network_dialog_body));
                    errorDialogFragment.setArguments(b);
                    errorDialogFragment.show(getFragmentManager(), Constants.ALERT_DIALOG_TAG);
                } else {
                    Intent webViewIntent = new Intent(mContext, WebViewActivity.class);
                    webViewIntent.putExtra(getString(R.string.recipe_url_string), mCurrentRecipe.getInstructionUrl());
                    webViewIntent.putExtra(getString(R.string.recipe_name_string), mCurrentRecipe.getRecipeName());
                    startActivity(webViewIntent);
                }
            }
        });
        return rootView;
    }

    private void showSnackBar(String message) {
        Snackbar snackBar = Snackbar
                .make(mCoordinatorLayout, message, Snackbar.LENGTH_SHORT);
        View sbView = snackBar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackBar.show();
    }

    private void addRecipe() {
        String recipeImageUrl = mCurrentRecipe.getImageUrl();
        String recipeName = mCurrentRecipe.getRecipeName();
        String instructionUrl = mCurrentRecipe.getInstructionUrl();
        List<String> ingredients = mCurrentRecipe.getIngredientList();
        if (recipeName.isEmpty()) {
            recipeName = getString(R.string.unnamed_fav_recipe);
        }
        ContentValues values = new ContentValues();
        values.put(FavouriteRecipeEntry.COLUMN_IMAGE_URL, recipeImageUrl);
        values.put(FavouriteRecipeEntry.COLUMN_RECIPE_NAME, recipeName);
        values.put(FavouriteRecipeEntry.COLUMN_INSTRUCTION_URL, instructionUrl);
        values.put(FavouriteRecipeEntry.COLUMN_INGREDIENTS, ingredients.toString());

        Uri newUri = getActivity().getContentResolver().insert(FavouriteRecipeEntry.CONTENT_URI, values);

        // Sync: Add to realtime DB
        if (AccountUtils.getUserLogin(getActivity())) {
            mFavoriteRecipeNode.child(newUri.getLastPathSegment()).setValue(mCurrentRecipe);
        }

        if (newUri != null) {
            showSnackBar(getString(R.string.added_to_favourites_snackbar));
        }
        Bundle addToFavBundle = new Bundle();
        addToFavBundle.putString(EVENT_RECIPE_NAME, recipeName);
        FirebaseAnalyticsUtils.reportAddToFavorites(mContext, addToFavBundle);
    }
}
