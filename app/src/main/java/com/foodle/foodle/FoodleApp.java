package com.foodle.foodle;

import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * Created by saikiranbaitwork on 19/2/18.
 */

public class FoodleApp extends MultiDexApplication{

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);

    }
}
