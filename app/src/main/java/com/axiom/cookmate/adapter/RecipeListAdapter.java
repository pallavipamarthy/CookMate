package com.axiom.cookmate.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.axiom.cookmate.ActivityCallbackListener;
import com.axiom.cookmate.R;
import com.axiom.cookmate.data.Recipe;
import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecipeListAdapter extends RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder> {

    private List<Recipe> mRecipeList;
    private Context mContext;
    private ActivityCallbackListener mRecipeClickListener;

    public RecipeListAdapter(Context context, List<Recipe> recipeList,
                             ActivityCallbackListener recipeClickListener) {
        mContext = context;
        mRecipeList = recipeList;
        mRecipeClickListener = recipeClickListener;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        mContext = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item, viewGroup, false);
        return new RecipeViewHolder(view);
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.recipe_image_view)
        ImageView mRecipeImageView;
        @BindView(R.id.list_recipe_name_view)
        TextView mRecipeNameView;


        public RecipeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.recipe_image_view)
        public void onClick(View v) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation((Activity) mContext, v, "recipeImage");
            mRecipeClickListener.onRecipeClick(mRecipeList.get(getAdapterPosition()), options.toBundle());
        }
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        String imageUrl = mRecipeList.get(position).getImageUrl();
        String recipeName = mRecipeList.get(position).getRecipeName();
        holder.mRecipeImageView.setContentDescription(recipeName);
        holder.mRecipeNameView.setText(recipeName);
        Glide.with(mContext)
                .load(imageUrl)
                .into(holder.mRecipeImageView);
    }

    @Override
    public int getItemCount() {
        return mRecipeList.size();
    }

    public void setData(List<Recipe> recipeList) {
        mRecipeList.clear();
        mRecipeList = recipeList;
        notifyDataSetChanged();
    }
}
