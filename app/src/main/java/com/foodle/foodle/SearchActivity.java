package com.foodle.foodle;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    EditText search_recepie_edit_text;

    Spinner type_spinner_filter, age_group_spinner_filter;

    Button find;

    private GoogleSignInClient mGoogleSignInClient;

    RadioGroup recepie_name_ingredients_group;

    ProgressDialog pDialog;

    String foodType = "";

    String ageType = "";

    ImageView breakfast_icon, meal_icon, desserts_icon, kids_icon, young_icon, old_icon;

    String selectedType = "Recepie";

    private ProgressDialog getProgressDialog(){
        if (pDialog == null){

            pDialog = new ProgressDialog(this);

            pDialog.setMessage("Please wait...Loading Data");

            pDialog.setCancelable(false);

            pDialog.setCanceledOnTouchOutside(false);

        }
        return pDialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        recepie_name_ingredients_group = findViewById(R.id.recepie_name_ingredients_group);

        search_recepie_edit_text = findViewById(R.id.search_recepie_edit_text);

        breakfast_icon = findViewById(R.id.breakfast_icon);

        meal_icon = findViewById(R.id.meal_icon);

        desserts_icon = findViewById(R.id.desserts_icon);

        kids_icon = findViewById(R.id.kids_icon);

        young_icon = findViewById(R.id.young_icon);

        old_icon = findViewById(R.id.old_icon);

        type_spinner_filter = findViewById(R.id.type_spinner_filter);

        breakfast_icon.setOnClickListener(this);

        meal_icon.setOnClickListener(this);

        desserts_icon.setOnClickListener(this);

        old_icon.setOnClickListener(this);

        young_icon.setOnClickListener(this);

        kids_icon.setOnClickListener(this);

        age_group_spinner_filter = findViewById(R.id.age_group_spinner_filter);

        find = findViewById(R.id.find);

        recepie_name_ingredients_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                group.check(checkedId);

                switch (checkedId){

                    case R.id.by_recipie_name:

                        selectedType = "Recepie";

                        search_recepie_edit_text.setFocusable(true);

                        search_recepie_edit_text.setFocusableInTouchMode(true);

                        break;
                    case R.id.by_ingredients:

                        selectedType = "Ingredients";

                        search_recepie_edit_text.setFocusable(true);

                        search_recepie_edit_text.setFocusableInTouchMode(true);

                        break;
                    case R.id.by_type:

                        selectedType = "FoodType";

                        search_recepie_edit_text.setFocusable(false);

                        search_recepie_edit_text.setFocusableInTouchMode(false);

                        break;
                    default:

                        selectedType = "Recepie";

                        search_recepie_edit_text.setFocusable(true);

                        search_recepie_edit_text.setFocusableInTouchMode(true);

                        break;

                }

                Log.e("Daya os", selectedType);

            }
        });

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FilterModel filterModel = new FilterModel();

                filterModel.ingredients = search_recepie_edit_text.getText().toString().trim();

                filterModel.ageGroup = ageType;

                filterModel.typeOfFood = foodType;

                Intent intent = new Intent(SearchActivity.this, RecepieListActivity.class);

                intent.putExtra("selectedType", selectedType);

                intent.putExtra("filter", filterModel);

                startActivity(intent);

                finish();

            }
        });

    }

    private void selectType(int id){

        switch (id){
            case R.id.breakfast_icon :

                foodType = "3";

                breakfast_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.breakfast_selected));

                meal_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.meal_unselected));

                desserts_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.desserts_unselected));

                break;
            case R.id.meal_icon :
                foodType = "1";
                breakfast_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.breakfast_unselected));

                meal_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.meal_selected));

                desserts_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.desserts_unselected));

                break;
            case R.id.desserts_icon :
                foodType = "2";
                breakfast_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.breakfast_unselected));

                meal_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.meal_unselected));

                desserts_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.desserts_selected));

                break;
        }

    }

    private void selectAgeType(int id){

        switch (id){

            case R.id.kids_icon:

                ageType = "Kids";

                kids_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.child_selected));

                young_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.young_selected));

                old_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.old_selected));

                break;

            case R.id.young_icon:

                ageType = "Adults";

                kids_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.child_unselected));

                young_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.young_selected_this));

                old_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.old_selected));

                break;

            case R.id.old_icon:

                ageType = "Old People";

                kids_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.child_unselected));

                young_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.young_selected));

                old_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.old_selected_this));

                break;

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:


                SharedPreferences sharedPreferences = getSharedPreferences("Foodle", Context.MODE_PRIVATE);

                boolean isSkipped = sharedPreferences.getBoolean("isSkipped", false);

                if (sharedPreferences.getBoolean("isSkipped", false)){

                    Toast.makeText(SearchActivity.this, "Please login", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(SearchActivity.this, FoodleLoginActivity.class);

                    startActivity(intent);

                }
                else {

                    String type = sharedPreferences.getString("type", "Email");

                    Log.e("type is ", type);

                    switch (type){
                        case "Google":
                            signOut(sharedPreferences);
                            break;
                        case "Facebook":
                            LoginManager.getInstance().logOut();

                            setEmpty(sharedPreferences);

                            break;
                        default:
                            setEmpty(sharedPreferences);
                    }

                }

                return true;
            case R.id.favourites:

                Intent intent1 = new Intent(SearchActivity.this, FavouritesActivity.class);

                startActivity(intent1);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signOut(final SharedPreferences sharedPreferences) {
        getProgressDialog().show();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        getProgressDialog().dismiss();

                        setEmpty(sharedPreferences);

                    }
                });
    }

    private void setEmpty(SharedPreferences sharedPreferences){
        AppConstant.getFoodleDBInstance(this).deleteAllDataOnLogout();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("Id", "");
        editor.putString("Name", "");
        editor.putString("EmailId", "");
        editor.putString("MobileNo", "");
        editor.putString("Password", "");
        editor.putString("type", "");
        editor.putBoolean("isVerified", false);

        editor.apply();

        Toast.makeText(SearchActivity.this, "Logged out successfully", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(SearchActivity.this, FoodleLoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);

        finish();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.breakfast_icon:
                selectType(v.getId());
                break;

            case R.id.meal_icon:
                selectType(v.getId());
                break;

            case R.id.desserts_icon:
                selectType(v.getId());
                break;

            case R.id.kids_icon:
                selectAgeType(v.getId());
                break;

            case R.id.young_icon:
                selectAgeType(v.getId());
                break;

            case R.id.old_icon:
                selectAgeType(v.getId());
                break;

        }

    }
}
