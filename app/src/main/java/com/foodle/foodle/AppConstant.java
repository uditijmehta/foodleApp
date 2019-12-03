package com.foodle.foodle;

import android.content.Context;

import java.util.UUID;

/**
 * Created by obtainitpc12 on 1/24/18.
 */

public class AppConstant {

    private static FoodleDB foodleDB;

    public static FoodleDB getFoodleDBInstance(Context context){

        if (foodleDB == null){

            foodleDB = new FoodleDB(context);

        }
        return foodleDB;

    }

    public String getId(){
        return UUID.randomUUID().toString();
    }

}
