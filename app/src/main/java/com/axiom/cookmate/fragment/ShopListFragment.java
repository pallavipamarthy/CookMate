package com.axiom.cookmate.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.axiom.cookmate.R;
import com.axiom.cookmate.utilities.RecipeUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShopListFragment extends Fragment {

    @BindView(R.id.recipe_shop_list_view)
    ListView mShopRecipeListView;
    private Context mContext;
    private OnShopRecipeListener mListener;
    private ArrayList<String> mRecipeNames;
    private ArrayAdapter mAdapter;

    public interface OnShopRecipeListener {
        void onShopRecipeClick(Bundle b);

        void onShopListLoaded(Bundle b);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListener = (OnShopRecipeListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.shop_list_fragment, container, false);
        ButterKnife.bind(this, rootView);
        mContext = getActivity();
        refreshList();
        return rootView;
    }

    public void refreshList() {

        mRecipeNames = RecipeUtils.getShoppingRecipeList(mContext);
        if (mRecipeNames != null && !mRecipeNames.isEmpty()) {
            mAdapter = new ArrayAdapter(mContext, R.layout.shop_list_item, R.id.itemText, mRecipeNames);
            mShopRecipeListView.setAdapter(mAdapter);
            mShopRecipeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {
                    Bundle b = new Bundle();
                    b.putString(getString(R.string.recipe_name_string), mRecipeNames.get(position));
                    mListener.onShopRecipeClick(b);
                }
            });

            Bundle b = new Bundle();
            b.putString(getString(R.string.recipe_name_string), mRecipeNames.get(0));
            mListener.onShopListLoaded(b);
        } else {
            if (mAdapter != null) {
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
            }
            mListener.onShopListLoaded(null);
        }
    }
}
