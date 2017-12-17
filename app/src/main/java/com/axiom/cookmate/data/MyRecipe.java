package com.axiom.cookmate.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class MyRecipe implements Parcelable {
    private String mRecipeId;
    private String mRecipeName;
    private String mImage;
    private ArrayList<String> mInstructionList;
    private ArrayList<String> mIngredientList;

    public MyRecipe() {

    }

    public MyRecipe(String recipeId, String recipeName, String image, ArrayList<String> instructionList,
                    ArrayList<String> ingredientList) {
        mIngredientList = new ArrayList<>();
        mRecipeId = recipeId;
        mRecipeName = recipeName;
        mImage = image;
        mInstructionList = instructionList;
        mIngredientList = ingredientList;
    }

    private MyRecipe(Parcel in) {
        mRecipeId = in.readString();
        mRecipeName = in.readString();
        mImage = in.readString();
        mInstructionList = in.readArrayList(String.class.getClassLoader());
        mIngredientList = in.readArrayList(String.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getRecipeId() {
        return mRecipeId;
    }

    public String getRecipeName() {
        return mRecipeName;
    }

    public String getImage() {
        return mImage;
    }

    public ArrayList<String> getInstructionList() {
        return mInstructionList;
    }

    public ArrayList<String> getIngredientList() {
        return mIngredientList;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mRecipeId);
        parcel.writeString(mRecipeName);
        parcel.writeString(mImage);
        parcel.writeList(mInstructionList);
        parcel.writeList(mIngredientList);
    }

    public static final Parcelable.Creator<MyRecipe> CREATOR = new Parcelable.Creator<MyRecipe>() {
        @Override
        public MyRecipe createFromParcel(Parcel parcel) {
            return new MyRecipe(parcel);
        }

        @Override
        public MyRecipe[] newArray(int i) {
            return new MyRecipe[i];
        }

    };
}
