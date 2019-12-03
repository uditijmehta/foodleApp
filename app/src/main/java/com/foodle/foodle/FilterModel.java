package com.foodle.foodle;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by obtainitpc12 on 1/25/18.
 *
 */

public class FilterModel implements Parcelable{

    String ingredients;

    String ageGroup;

    String typeOfFood;

    protected FilterModel(Parcel in) {
        ingredients = in.readString();
        ageGroup = in.readString();
        typeOfFood = in.readString();
    }

    public FilterModel(){

    }

    public static final Creator<FilterModel> CREATOR = new Creator<FilterModel>() {
        @Override
        public FilterModel createFromParcel(Parcel in) {
            return new FilterModel(in);
        }

        @Override
        public FilterModel[] newArray(int size) {
            return new FilterModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(ingredients);
        parcel.writeString(ageGroup);
        parcel.writeString(typeOfFood);
    }
}
