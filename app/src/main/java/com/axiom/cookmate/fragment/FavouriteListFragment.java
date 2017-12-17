package com.axiom.cookmate.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.axiom.cookmate.ActivityCallbackListener;
import com.axiom.cookmate.R;
import com.axiom.cookmate.adapter.RecipeListAdapter;
import com.axiom.cookmate.data.MyRecipe;
import com.axiom.cookmate.data.Recipe;
import com.axiom.cookmate.loader.FavouriteRecipeLoader;
import com.axiom.cookmate.utilities.Constants;
import com.axiom.cookmate.utilities.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavouriteListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Recipe>> {

    private Context mContext;
    private RecipeListAdapter mRecipeListAdapter;
    @BindView(R.id.recipe_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    private static final int FAV_RECIPE_LOADER_ID = 1;
    private ActivityCallbackListener mRecipeListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecipeListener = (ActivityCallbackListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, rootView);
        mContext = getActivity();
        getActivity().setTitle(getString(R.string.favourites_string));
        List<Recipe> recipeList = new ArrayList<>();
        if (getActivity() != null) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            if (getResources().getBoolean(R.bool.isTablet)) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
                } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                }
            }
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(FAV_RECIPE_LOADER_ID, null, this);
            mRecipeListAdapter = new RecipeListAdapter(mContext, recipeList, mRecipeListener);
            mRecyclerView.setAdapter(mRecipeListAdapter);
            mRecipeListAdapter.setData(recipeList);
        }
        return rootView;
    }

    @Override
    public Loader<List<Recipe>> onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        return new FavouriteRecipeLoader(mContext);
    }

    @Override
    public void onLoadFinished(Loader<List<Recipe>> loader, List<Recipe> recipeList) {

        if (recipeList != null && !recipeList.isEmpty() && NetworkUtils.isNetworkConnected(mContext)) {
            mRecipeListAdapter.setData(recipeList);
            mProgressBar.setVisibility(View.GONE);
            Bundle b = new Bundle();
            b.putParcelable(Constants.RECIPE_OBJ, recipeList.get(0));
            mRecipeListener.onListLoaded(b);

            LoaderManager loaderManager = getLoaderManager();
            loaderManager.destroyLoader(FAV_RECIPE_LOADER_ID);
        }

        if (recipeList == null || recipeList.isEmpty()) {
            mProgressBar.setVisibility(View.GONE);
            mRecipeListener.onListLoaded(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Recipe>> loader) {
    }

    public void onMyRecipeClick(MyRecipe myRecipe) {
    }
}
