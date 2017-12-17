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
import com.axiom.cookmate.loader.RecipeLoader;
import com.axiom.cookmate.utilities.Constants;
import com.axiom.cookmate.utilities.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SearchListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Recipe>> {

    private Context mContext;
    private static final int RECIPE_LOADER_ID = 1;
    @BindView(R.id.recipe_recycler_view)
    RecyclerView mRecyclerView;
    private RecipeListAdapter mRecipeListAdapter;
    private ActivityCallbackListener mRecipeListener;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    private List<Recipe> mRecipeList;
    String mRecipeUrl = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRecipeUrl = getArguments().getString(getString(R.string.final_recipe_url_string));

            mRecipeListener = (ActivityCallbackListener) getActivity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, rootView);
        mContext = getActivity();

        getActivity().setTitle(getString(R.string.recipes_string));

        mRecipeList = new ArrayList<>();

        if (getActivity() != null) {
            LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(mContext);
            mRecyclerView.setLayoutManager(mLinearLayoutManager);
            if (getResources().getBoolean(R.bool.isTablet)) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
                } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                }
            }

            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(RECIPE_LOADER_ID, null, this);

            mRecipeListAdapter = new RecipeListAdapter(mContext, mRecipeList, mRecipeListener);
            mRecyclerView.setAdapter(mRecipeListAdapter);
            mRecipeListAdapter.setData(mRecipeList);
        }
        return rootView;
    }

    @Override
    public Loader<List<Recipe>> onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        return new RecipeLoader(mContext, mRecipeUrl);
    }

    @Override
    public void onLoadFinished(Loader<List<Recipe>> loader, List<Recipe> recipeList) {

        if (recipeList != null && !recipeList.isEmpty() && NetworkUtils.isNetworkConnected(mContext)) {
            mRecipeList = recipeList;
            mRecipeListAdapter.setData(mRecipeList);
            mProgressBar.setVisibility(View.GONE);

            Bundle b = new Bundle();
            b.putParcelable(Constants.RECIPE_OBJ, mRecipeList.get(0));
            mRecipeListener.onListLoaded(b);

            LoaderManager loaderManager = getLoaderManager();
            loaderManager.destroyLoader(RECIPE_LOADER_ID);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mRecipeListener.onListLoaded(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Recipe>> loader) {
    }

    public void onMyRecipeClick(MyRecipe myRecipe) {
    }

    public void refreshAdapterData(ArrayList<Recipe> recipeList) {
        if (mRecipeList != null) {
            mRecipeList.addAll(0, recipeList);
            mRecipeListAdapter.notifyDataSetChanged();
        }
    }
}
