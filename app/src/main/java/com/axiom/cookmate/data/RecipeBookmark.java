package com.axiom.cookmate.data;

import android.os.Parcel;
import android.os.Parcelable;

public class RecipeBookmark implements Parcelable {
    private String mRecipeName;
    private String mRecipeLink;

    public RecipeBookmark(String recipeName, String recipeLink) {
        mRecipeName = recipeName;
        mRecipeLink = recipeLink;
    }

    private RecipeBookmark(Parcel in) {
        mRecipeName = in.readString();
        mRecipeLink = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getRecipeName() {
        return mRecipeName;
    }

    public String getRecipeLink() {
        return mRecipeLink;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mRecipeName);
        parcel.writeString(mRecipeLink);
    }

    public static final Parcelable.Creator<RecipeBookmark> CREATOR = new Parcelable.Creator<RecipeBookmark>() {
        @Override
        public RecipeBookmark createFromParcel(Parcel parcel) {
            return new RecipeBookmark(parcel);
        }

        @Override
        public RecipeBookmark[] newArray(int i) {
            return new RecipeBookmark[i];
        }

    };
}
