package com.axiom.cookmate.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.axiom.cookmate.ActivityCallbackListener;
import com.axiom.cookmate.R;
import com.axiom.cookmate.data.MyRecipe;
import com.axiom.cookmate.data.RecipeContract;
import com.axiom.cookmate.utilities.AccountUtils;
import com.axiom.cookmate.utilities.Constants;
import com.axiom.cookmate.utilities.RecipeUtils;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyRecipeDetailFragment extends Fragment {

    private MyRecipe mMyCurrentRecipe;
    @BindView(R.id.detail_image_view)
    ImageView mDetailImageView;
    @BindView(R.id.my_ingredient_list_layout)
    LinearLayout mMyIngredientListLayout;
    @BindView(R.id.my_instruction_list_layout)
    LinearLayout mMyInstructionListLayout;
    TextView[] mIngredientTextList;
    TextView[] mInstructionTextList;
    LinearLayout[] mIngredientItemLayout;
    LinearLayout.LayoutParams mIngredientLayoutParams;
    ActivityCallbackListener mListener;
    @BindView(R.id.delete)
    FloatingActionButton mDeleteButton;
    @BindView(R.id.share)
    FloatingActionButton mShareButton;
    DatabaseReference mMyRecipeNode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mListener = (ActivityCallbackListener) getActivity();
        if (getArguments() != null) {
            mMyCurrentRecipe = getArguments().getParcelable(Constants.MY_RECIPE_OBJ);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.my_recipe_detail_layout, container, false);
        Context context = getActivity();
        ButterKnife.bind(this, rootView);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)
                rootView.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(mMyCurrentRecipe.getRecipeName());

        mShareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, mMyCurrentRecipe.getRecipeName());
                intent.putExtra(Intent.EXTRA_TEXT, mMyCurrentRecipe.getIngredientList());
                intent.putExtra(Intent.EXTRA_TEXT, mMyCurrentRecipe.getInstructionList());
                startActivity(Intent.createChooser(intent, getString(R.string.share_via_text)));
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createAndShowAlertDialog();
            }
        });
        Glide.with(this)
                .load(mMyCurrentRecipe.getImage())
                .placeholder(R.drawable.placeholder_image)
                .into(mDetailImageView);
        mDetailImageView.setContentDescription(getString(R.string.a11y_dish_image, mMyCurrentRecipe.getRecipeName()));
        ArrayList<String> ingredientList = mMyCurrentRecipe.getIngredientList();
        ArrayList<String> instructionList = mMyCurrentRecipe.getInstructionList();
        int ingredientCount = ingredientList.size();
        int instructionCount = instructionList.size();

        mMyIngredientListLayout = (LinearLayout) rootView.findViewById(R.id.my_ingredient_list_layout);
        mMyInstructionListLayout = (LinearLayout) rootView.findViewById(R.id.my_instruction_list_layout);

        mIngredientTextList = new TextView[ingredientCount];
        mInstructionTextList = new TextView[instructionCount];
        mIngredientItemLayout = new LinearLayout[ingredientCount];
        ImageView mShopImageButton;

        mIngredientLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams tvlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageParams.setMargins(0, 0, 30, 0);

        for (int i = 0; i < ingredientCount; i++) {
            mIngredientItemLayout[i] = new LinearLayout(context);
            mIngredientItemLayout[i].setLayoutParams(mIngredientLayoutParams);
            mIngredientItemLayout[i].setOrientation(LinearLayout.HORIZONTAL);
            mIngredientItemLayout[i].setPadding(30, 30, 30, 30);

            mShopImageButton = new ImageView(context);
            mShopImageButton.setLayoutParams(imageParams);
            mShopImageButton.setImageResource(R.drawable.ic_add_shopping_cart);
            mShopImageButton.setTag(i);
            mShopImageButton.setContentDescription(getString(R.string.a11y_add_to_shopping_list));
            mShopImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (Integer) v.getTag();
                    String ingredient = mIngredientTextList[position].getText().toString();
                    RecipeUtils.addIngredientToRecipeShoppingList(getActivity(),
                            mMyCurrentRecipe.getRecipeName(), ingredient);
                    mListener.onAddToShoppingList(ingredient);
                }
            });

            mIngredientItemLayout[i].addView(mShopImageButton);

            mIngredientTextList[i] = new TextView(context);
            mIngredientTextList[i].setTextColor(Color.BLACK);
            mIngredientTextList[i].setLayoutParams(tvlp);
            mIngredientTextList[i].setText(ingredientList.get(i));
            mIngredientItemLayout[i].addView(mIngredientTextList[i]);
            mMyIngredientListLayout.addView(mIngredientItemLayout[i]);
        }

        LinearLayout.LayoutParams instructionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < instructionCount; i++) {
            mInstructionTextList[i] = new TextView(context);
            mInstructionTextList[i].setTextColor(Color.BLACK);
            mInstructionTextList[i].setPadding(50, 30, 50, 30);
            mInstructionTextList[i].setLayoutParams(instructionParams);
            mInstructionTextList[i].setCompoundDrawablesWithIntrinsicBounds(R.drawable.bullet_list, 0, 0, 0);
            mInstructionTextList[i].setCompoundDrawablePadding(50);
            mInstructionTextList[i].setText(instructionList.get(i));
            mMyInstructionListLayout.addView(mInstructionTextList[i]);
        }

        return rootView;
    }

    private void deleteRecipe() {
        String selection = RecipeContract.MyRecipeEntry._ID + "=?";
        String[] selectionArgs = {mMyCurrentRecipe.getRecipeId()};
        getActivity().getContentResolver().delete(RecipeContract.MyRecipeEntry.CONTENT_URI,
                selection, selectionArgs);

        //Sync: delete from realtime db
        if (AccountUtils.getUserLogin(getActivity())){
            FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
            DatabaseReference mFirebaseDBRef = mFirebaseInstance.getReference("users");
            String userId = AccountUtils.getFirebaseUserId(getActivity());
            DatabaseReference firebaseUserNode = mFirebaseDBRef.child(userId);
            mMyRecipeNode = firebaseUserNode.child("my_recipe_list");
            mMyRecipeNode.child(mMyCurrentRecipe.getRecipeId()).removeValue();
        }
    }

    private void createAndShowAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.delete_my_recipe));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteRecipe();
                dialog.dismiss();
                mListener.onRecipeDeleted();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

