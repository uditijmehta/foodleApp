package com.foodle.foodle;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by obtainitpc12 on 1/8/18.
 */

public class SearchListModel implements Parcelable{

    String id;
    String name;
    String[] ingredients;
    String[] directions;
    String recipieimage;
    String searchtype;
    String agegroup;
    String typefood;
    String imageUrl;
    String calories;

    public SearchListModel() {

    }

    protected SearchListModel(Parcel in) {
        id = in.readString();
        name = in.readString();
        ingredients = in.createStringArray();
        directions = in.createStringArray();
        recipieimage = in.readString();
        searchtype = in.readString();
        agegroup = in.readString();
        typefood = in.readString();
        imageUrl = in.readString();
        calories = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeStringArray(ingredients);
        dest.writeStringArray(directions);
        dest.writeString(recipieimage);
        dest.writeString(searchtype);
        dest.writeString(agegroup);
        dest.writeString(typefood);
        dest.writeString(imageUrl);
        dest.writeString(calories);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SearchListModel> CREATOR = new Creator<SearchListModel>() {
        @Override
        public SearchListModel createFromParcel(Parcel in) {
            return new SearchListModel(in);
        }

        @Override
        public SearchListModel[] newArray(int size) {
            return new SearchListModel[size];
        }
    };
}
