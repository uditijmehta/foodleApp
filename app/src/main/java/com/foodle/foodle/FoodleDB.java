package com.foodle.foodle;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by obtainitpc12 on 1/24/18.
 */

public class FoodleDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "foodle.db";

    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_RECEPIE = "RECEPIE";

    private static final String TABLE_FAVOURITES = "FAVOURITES";

    private static final String TABLE_INGREDIENTS = "INGREDIENTS";

    private static final String TABLE_DIRECTIONS = "DIRECTIONS";

    private static final String RECEPIE_ID = "ID";

    private static final String RECEPIE_NAME = "RECEPIE_NAME";

    private static final String RECEPIE_IMAGE = "RECEPIE_IMAGE";

    private static final String SEARCH_TYPE = "SEARCH_TYPE";

    private static final String TYPE = "TYPE";

    private static final String CALORIES = "CALORIES";

    private static final String AGE = "AGE";

    private static final String INGREDIENTS = "INGREDIENTS";

    private static final String DIRECTIONS = "DIRECTIONS";

    private static final String CREATE_TABLE_RECEPIE = "CREATE TABLE "
            + TABLE_RECEPIE + "( "
            + RECEPIE_ID + " TEXT,"
            + RECEPIE_NAME + " TEXT,"
            + TYPE + " TEXT,"
            + CALORIES + " TEXT,"
            + AGE + " TEXT,"
            + RECEPIE_IMAGE + " TEXT,"
            + SEARCH_TYPE + " TEXT)";

    private static final String CREATE_TABLE_FAVOURITES = "CREATE TABLE "
            + TABLE_FAVOURITES + "( "
            + RECEPIE_ID + " TEXT,"
            + RECEPIE_NAME + " TEXT,"
            + TYPE + " TEXT,"
            + CALORIES + " TEXT,"
            + AGE + " TEXT,"
            + RECEPIE_IMAGE + " TEXT,"
            + SEARCH_TYPE + " TEXT)";

    private static final String CREATE_TABLE_INGREDIENTS = "CREATE TABLE "
            + TABLE_INGREDIENTS + "( "
            + RECEPIE_ID + " TEXT,"
            + INGREDIENTS + " TEXT)";

    private static final String CREATE_TABLE_DIRECTIONS = "CREATE TABLE "
            + TABLE_DIRECTIONS + "( "
            + RECEPIE_ID + " TEXT,"
            + DIRECTIONS + " TEXT)";

    public FoodleDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(CREATE_TABLE_RECEPIE);

        sqLiteDatabase.execSQL(CREATE_TABLE_INGREDIENTS);

        sqLiteDatabase.execSQL(CREATE_TABLE_DIRECTIONS);

        sqLiteDatabase.execSQL(CREATE_TABLE_FAVOURITES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean insertIntoFavourites(SearchListModel searchListModel){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(RECEPIE_ID, searchListModel.id);

        contentValues.put(RECEPIE_NAME, searchListModel.name);

        contentValues.put(RECEPIE_IMAGE, searchListModel.recipieimage);

        contentValues.put(TYPE, searchListModel.typefood);

        contentValues.put(CALORIES, searchListModel.calories);

        contentValues.put(AGE, searchListModel.agegroup);

        contentValues.put(SEARCH_TYPE, "");

        for (String ingredient : searchListModel.ingredients){

            insertIntoTableIngredients(searchListModel.id, ingredient);

        }

        for (String directions : searchListModel.directions){

            insertIntoTableDirections(searchListModel.id, directions);

        }

        long id = db.insert(TABLE_FAVOURITES, null, contentValues);

        return id > 0;

    }

    public boolean deleteFavourites(String id){

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TABLE_FAVOURITES, RECEPIE_ID + "=?", new String[]{id}) > 0;

    }

    public boolean isAddedFavourites(String id){


        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_FAVOURITES,
                new String[]{RECEPIE_ID, CALORIES ,RECEPIE_NAME, RECEPIE_IMAGE, SEARCH_TYPE, TYPE, AGE},
                RECEPIE_ID + "=?", new String[]{id}, null, null, null);

        boolean isExists = false;

        if (cursor.moveToFirst()){

            while (!cursor.isAfterLast()){

                isExists = true;

                cursor.moveToNext();

            }

        }

        cursor.close();

        return isExists;


    }

    public ArrayList<SearchListModel> getAllFavourites(){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_FAVOURITES,
                new String[]{RECEPIE_ID, RECEPIE_NAME,
                        CALORIES,
                        RECEPIE_IMAGE, SEARCH_TYPE,
                        TYPE, AGE}, null,
                null,
                null, null,
                null);

        ArrayList<SearchListModel> searchListModels = new ArrayList<>();

        if (cursor.moveToFirst()){

            while (!cursor.isAfterLast()){

                SearchListModel searchListModel = new SearchListModel();

                searchListModel.id = cursor.getString(cursor.getColumnIndex(RECEPIE_ID));

                searchListModel.name = cursor.getString(cursor.getColumnIndex(RECEPIE_NAME));

                searchListModel.agegroup = cursor.getString(cursor.getColumnIndex(AGE));

                searchListModel.typefood = cursor.getString(cursor.getColumnIndex(TYPE));

                searchListModel.searchtype = cursor.getString(cursor.getColumnIndex(SEARCH_TYPE));

                searchListModel.recipieimage = cursor.getString(cursor.getColumnIndex(RECEPIE_IMAGE));

                searchListModel.calories = cursor.getString(cursor.getColumnIndex(CALORIES));

                searchListModel.directions = getDirections(searchListModel.id);

                searchListModel.ingredients = getIngredients(searchListModel.id);

                searchListModels.add(searchListModel);

                cursor.moveToNext();

            }

        }

        cursor.close();

        return searchListModels;

    }

    public boolean insertIntoTableRecepie(SearchListModel searchListModel){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(RECEPIE_ID, searchListModel.id);

        contentValues.put(RECEPIE_NAME, searchListModel.name);

        contentValues.put(RECEPIE_IMAGE, searchListModel.recipieimage);

        contentValues.put(TYPE, searchListModel.typefood);

        contentValues.put(CALORIES, searchListModel.calories);

        contentValues.put(AGE, searchListModel.agegroup);

        contentValues.put(SEARCH_TYPE, "");

        for (String ingredient : searchListModel.ingredients){

            insertIntoTableIngredients(searchListModel.id, ingredient);

        }

        for (String directions : searchListModel.directions){

            insertIntoTableDirections(searchListModel.id, directions);

        }

        long id = db.insert(TABLE_RECEPIE, null, contentValues);

        return id > 0;

    }

    public void insertIntoTableIngredients(String id, String ingredients){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(RECEPIE_ID, id);

        contentValues.put(INGREDIENTS, ingredients);

        db.insert(TABLE_INGREDIENTS, null, contentValues);

    }

    public void insertIntoTableDirections(String id, String ingredients){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(RECEPIE_ID, id);

        contentValues.put(DIRECTIONS, ingredients);

        db.insert(TABLE_DIRECTIONS, null, contentValues);

    }

    private String[] getDirections(String recepieId){


        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_DIRECTIONS,
                new String[]{RECEPIE_ID, DIRECTIONS}, RECEPIE_ID + "=?", new String[]{recepieId}, null, null, null);

        ArrayList<String> ingredients = new ArrayList<>();

        if (cursor.moveToFirst()){

            while (!cursor.isAfterLast()){

                ingredients.add(cursor.getString(cursor.getColumnIndex(DIRECTIONS)));

                cursor.moveToNext();

            }

        }

        cursor.close();

        return ingredients.toArray(new String[0]);

    }

    private String[] getIngredients(String recepieId){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_INGREDIENTS,
                new String[]{RECEPIE_ID, INGREDIENTS}, RECEPIE_ID + "=?", new String[]{recepieId}, null, null, null);

        ArrayList<String> ingredients = new ArrayList<>();

        if (cursor.moveToFirst()){

            while (!cursor.isAfterLast()){

                ingredients.add(cursor.getString(cursor.getColumnIndex(INGREDIENTS)));

                cursor.moveToNext();

            }

        }

        cursor.close();

        return ingredients.toArray(new String[0]);

    }

    public boolean isAdded(String id){


        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RECEPIE,
                new String[]{RECEPIE_ID, RECEPIE_NAME, RECEPIE_IMAGE, SEARCH_TYPE, TYPE, AGE},
                RECEPIE_ID + "=?", new String[]{id}, null, null, null);

       boolean isExists = false;

        if (cursor.moveToFirst()){

            while (!cursor.isAfterLast()){

                isExists = true;

                cursor.moveToNext();

            }

        }

        cursor.close();

        return isExists;


    }

    public ArrayList<SearchListModel> getAllRecentSearches(){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RECEPIE,
                new String[]{RECEPIE_ID, RECEPIE_NAME, CALORIES, RECEPIE_IMAGE, SEARCH_TYPE, TYPE, AGE}, null, null, null, null, null);

        ArrayList<SearchListModel> searchListModels = new ArrayList<>();

        if (cursor.moveToFirst()){

            while (!cursor.isAfterLast()){

                SearchListModel searchListModel = new SearchListModel();

                searchListModel.id = cursor.getString(cursor.getColumnIndex(RECEPIE_ID));

                searchListModel.name = cursor.getString(cursor.getColumnIndex(RECEPIE_NAME));

                searchListModel.agegroup = cursor.getString(cursor.getColumnIndex(AGE));

                searchListModel.typefood = cursor.getString(cursor.getColumnIndex(TYPE));

                searchListModel.searchtype = cursor.getString(cursor.getColumnIndex(SEARCH_TYPE));

                searchListModel.recipieimage = cursor.getString(cursor.getColumnIndex(RECEPIE_IMAGE));

                searchListModel.calories = cursor.getString(cursor.getColumnIndex(CALORIES));

                searchListModel.directions = getDirections(searchListModel.id);

                searchListModel.ingredients = getIngredients(searchListModel.id);

                searchListModels.add(searchListModel);

                cursor.moveToNext();

            }

        }

        cursor.close();

        return searchListModels;

    }

    public boolean deleteRecepie(String id){

        SQLiteDatabase db = this.getWritableDatabase();

        deleteIngredients(id);

        deleteDirections(id);

        return db.delete(TABLE_RECEPIE, RECEPIE_ID + "=?", new String[]{id}) > 0;

    }

    public boolean deleteIngredients(String id){

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TABLE_INGREDIENTS, RECEPIE_ID + "=?", new String[]{id}) > 0;

    }

    public boolean deleteDirections(String id){

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TABLE_DIRECTIONS, RECEPIE_ID + "=?", new String[]{id}) > 0;

    }

    public void deleteAllDataOnLogout(){

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_FAVOURITES, null, null);

        db.delete(TABLE_DIRECTIONS, null, null);

        db.delete(TABLE_INGREDIENTS, null, null);

        db.delete(TABLE_RECEPIE, null, null);

    }

}
