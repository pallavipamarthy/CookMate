package com.axiom.cookmate.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.axiom.cookmate.FirebaseAnalyticsUtils;
import com.axiom.cookmate.R;
import com.axiom.cookmate.utilities.AccountUtils;
import com.axiom.cookmate.utilities.RecipeUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShopDetailFragment extends Fragment {

    private Context mContext;
    private String mRecipeName;
    private ArrayList<String> mIngredientShopList;
    @BindView(R.id.ingredient_shop_list_view)
    ListView mShopIngredientListView;
    @BindView(R.id.recipe_name_title)
    TextView mRecipeNameTextView;
    private static final String EVENT_SHOP_LIST_SHARE = "shop_list_share";
    private OnShopDetailsListener mListener;
    DatabaseReference mShopListNode;

    public interface OnShopDetailsListener {
        void onListDeleted();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListener = (OnShopDetailsListener) getActivity();
        if (getArguments() != null) {
            mRecipeName = getArguments().getString(getString(R.string.recipe_name_string));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().invalidateOptionsMenu();
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.shop_detail_fragment, container, false);
        mContext = getActivity();
        String title = getString(R.string.app_name);

        if (AccountUtils.getUserLogin(getActivity())) {
            FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
            DatabaseReference mFirebaseDBRef = mFirebaseInstance.getReference("users");
            String userId = AccountUtils.getFirebaseUserId(mContext);
            DatabaseReference firebaseUserNode = mFirebaseDBRef.child(userId);
            mShopListNode = firebaseUserNode.child("shopping_list");
        }
        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(s);
        ButterKnife.bind(this, rootView);
        mRecipeNameTextView.setText(mRecipeName);
        mIngredientShopList = RecipeUtils.getRecipeShoppingList(mContext, mRecipeName);
        ArrayAdapter adapter = new ArrayAdapter(mContext, R.layout.ingredient_shop_list_item, R.id.itemText, mIngredientShopList);
        mShopIngredientListView.setAdapter(adapter);

        mShopIngredientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                TextView tv = (TextView) v;
                if ((tv.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
                    tv.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
                    RecipeUtils.addIngredientToRecipeShoppingList(mContext, mRecipeName, tv.getText().toString());
                    if (AccountUtils.getUserLogin(getActivity())) {
                        mShopListNode.child(mRecipeName).setValue(tv.getText().toString().trim());
                    }
                } else {
                    tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    RecipeUtils.removeIngredientFromRecipeShoppingList(mContext, mRecipeName, tv.getText().toString());
                    if (AccountUtils.getUserLogin(getActivity())) {
                        mShopListNode.child(tv.getText().toString().trim()).removeValue();
                    }
                }
            }
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_myrecipe_detail, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Implementation for user clicks on different menu items.
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_item_share:
                //share intent to share trailer
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mIngredientShopList.size(); i++) {
                    sb.append(mIngredientShopList.get(i));
                    sb.append("\n");
                }
                intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                Bundle b = new Bundle();
                b.putString(EVENT_SHOP_LIST_SHARE, getString(R.string.event_shop_activity_launched));
                FirebaseAnalyticsUtils.reportShoppingListShared(mContext, b);
                startActivity(Intent.createChooser(intent, getString(R.string.share_via_text)));
                return true;
            case R.id.action_delete:
                deleteRecipe();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteRecipe() {
        createAndShowAlertDialog();
    }

    private void createAndShowAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.delete_shopping_list_dialog_title));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                RecipeUtils.removeRecipeFromList(mContext, mRecipeName);
                dialog.dismiss();
                mListener.onListDeleted();
                if (AccountUtils.getUserLogin(getActivity())) {
                    mShopListNode.child(mRecipeName).removeValue();
                }
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
