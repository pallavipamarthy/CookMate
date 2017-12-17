package com.axiom.cookmate.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Recipe implements Parcelable {
    private String mRecipeName;
    private String mImageUrl;
    private String mInstructionUrl;
    private String mHealthLabel;
    private List<String> mIngredientList;
    private String mDietLabel;
    private String mAllergyLabel;

    public Recipe(){

    }

    public Recipe(String recipeName, String imageUrl, String instructionUrl, List<String> ingredientList,
                  String dietLabel, String healthLabel, String allergyLabel) {
        mIngredientList = new ArrayList<>();
        mRecipeName = recipeName;
        mImageUrl = imageUrl;
        mInstructionUrl = instructionUrl;
        mIngredientList = ingredientList;
        mDietLabel = dietLabel;
        mHealthLabel = healthLabel;
        mAllergyLabel = allergyLabel;
    }

    private Recipe(Parcel in) {
        mRecipeName = in.readString();
        mImageUrl = in.readString();
        mInstructionUrl = in.readString();
        mIngredientList = in.readArrayList(String.class.getClassLoader());
        mDietLabel = in.readString();
        mHealthLabel = in.readString();
        mAllergyLabel = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getRecipeName() {
        return mRecipeName;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getInstructionUrl() {
        return mInstructionUrl;
    }

    public List<String> getIngredientList() {
        return mIngredientList;
    }

    public String getHealthLabel() {
        return mHealthLabel;
    }

    public String getDietLabel() {
        return mDietLabel;
    }

    public String getAllergyLabel() {
        return mAllergyLabel;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mRecipeName);
        parcel.writeString(mImageUrl);
        parcel.writeString(mInstructionUrl);
        parcel.writeList(mIngredientList);
        parcel.writeString(mDietLabel);
        parcel.writeString(mHealthLabel);
        parcel.writeString(mAllergyLabel);
    }

    public static final Parcelable.Creator<Recipe> CREATOR = new Parcelable.Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel parcel) {
            return new Recipe(parcel);
        }

        @Override
        public Recipe[] newArray(int i) {
            return new Recipe[i];
        }

    };
}
