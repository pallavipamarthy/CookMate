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
import com.axiom.cookmate.adapter.MyRecipeAdapter;
import com.axiom.cookmate.data.MyRecipe;
import com.axiom.cookmate.loader.MyRecipeLoader;
import com.axiom.cookmate.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyRecipeListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<MyRecipe>> {
    private Context mContext;
    private MyRecipeAdapter mMyRecipeAdapter;
    @BindView(R.id.recipe_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    private static final int MY_RECIPE_LOADER_ID = 1;
    private ActivityCallbackListener mMyRecipeListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMyRecipeListener = (ActivityCallbackListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, rootView);
        mContext = getActivity();

        getActivity().setTitle(getString(R.string.my_recipe_string));
        List<MyRecipe> myRecipeList = new ArrayList<>();

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
            loaderManager.initLoader(MY_RECIPE_LOADER_ID, null, this);

            mMyRecipeAdapter = new MyRecipeAdapter(mContext, myRecipeList, mMyRecipeListener);
            mRecyclerView.setAdapter(mMyRecipeAdapter);
            mMyRecipeAdapter.setData(myRecipeList);
        }
        return rootView;
    }

    @Override
    public Loader<List<MyRecipe>> onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        return new MyRecipeLoader(mContext);
    }

    @Override
    public void onLoadFinished(Loader<List<MyRecipe>> loader, List<MyRecipe> myRecipeList) {

        if (myRecipeList != null && !(myRecipeList.isEmpty())) {
            mMyRecipeAdapter.setData(myRecipeList);
            mProgressBar.setVisibility(View.GONE);
            Bundle b = new Bundle();
            b.putParcelable(Constants.MY_RECIPE_OBJ, myRecipeList.get(0));
            mMyRecipeListener.onListLoaded(b);

            LoaderManager loaderManager = getLoaderManager();
            loaderManager.destroyLoader(MY_RECIPE_LOADER_ID);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mMyRecipeListener.onListLoaded(null);
            mMyRecipeAdapter.setData(new ArrayList<MyRecipe>());
        }
    }

    @Override
    public void onLoaderReset(Loader<List<MyRecipe>> loader) {
    }

    public void refreshList() {
        LoaderManager loaderManager = getLoaderManager();
        Loader<List<MyRecipe>> loader = loaderManager.getLoader(MY_RECIPE_LOADER_ID);
        if (loader == null ||
                !loaderManager.getLoader(MY_RECIPE_LOADER_ID).isStarted()) {
            loaderManager.initLoader(MY_RECIPE_LOADER_ID, null, this);
        }
    }
}