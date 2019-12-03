package com.foodle.foodle;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RecepieDetailsActivity extends AppCompatActivity {

    TextView recepie_name_text, ingredients_list_text, directions_text, rate_text, calories_text;

    ImageView recepie_image, share_icon, start_icon;

    Toolbar toolbar;

    RequestQueue queue;

    SharedPreferences sharedPreferences;

    String getRating = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/getReatingByRecipeIdUserId";

    String addRating = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/addRating";

    String getReview = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/getReviewByRecipeId";

    SearchListModel searchListModel;

    private GoogleSignInClient mGoogleSignInClient;

    ProgressDialog pDialog;

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
        setContentView(R.layout.activity_recepie_details);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        sharedPreferences = getSharedPreferences("Foodle", Context.MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        start_icon = toolbar.findViewById(R.id.start_icon);

        share_icon = toolbar.findViewById(R.id.share_icon);

        calories_text = findViewById(R.id.calories_text);

        recepie_name_text = findViewById(R.id.recepie_name_text);

        ingredients_list_text = findViewById(R.id.ingredients_list_text);

        directions_text = findViewById(R.id.directions_text);

        rate_text = findViewById(R.id.rate_text);

        recepie_image = findViewById(R.id.recepie_image);

        start_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("Foodle", Context.MODE_PRIVATE);

                boolean isSkipped = sharedPreferences.getBoolean("isSkipped", false);

                if (isSkipped){

                    Intent intent = new Intent(RecepieDetailsActivity.this, FoodleLoginActivity.class);

                    startActivity(intent);

                }
                else {

                    openRatingDialog().show();

                }

            }
        });

        queue = Volley.newRequestQueue(this);

        searchListModel = getIntent().getParcelableExtra("searchModel");

        if (searchListModel != null){

            getRatingReview();

            recepie_name_text.setText(searchListModel.name);

            calories_text.setText(searchListModel.calories + " cals.");

            share_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = searchListModel.name + "\n\n"
                            + Arrays.toString(searchListModel.directions);
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Sub");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
            });

            /*Here we used a Library Called Picasso which helps us to load the images efficiently
             and cache it in the background so that it would take less time when the user opens the app next time*/

            Picasso.with(this).load(searchListModel.recipieimage).into(recepie_image, new Callback() {
                @Override
                public void onSuccess() {
                    Log.e("Image Loaded "," Successfully");
                }

                @Override
                public void onError() {

                    recepie_image.setImageResource(R.mipmap.ic_launcher);

                }
            });

            for (String ingredients : searchListModel.ingredients){

                ingredients_list_text.append(ingredients + "\n\n");

            }

            for (String directions : searchListModel.directions){

                directions_text.append(directions + "\n\n");

            }

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

                    Toast.makeText(RecepieDetailsActivity.this, "Please login", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(RecepieDetailsActivity.this, FoodleLoginActivity.class);

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

                Intent intent1 = new Intent(RecepieDetailsActivity.this, FavouritesActivity.class);

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

        Toast.makeText(RecepieDetailsActivity.this, "Logged out successfully", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(RecepieDetailsActivity.this, FoodleLoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);

        finish();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void getRatingReview(){

        /*Here we get all our ratings and reviews entered by the user. For now only the rating text is showned. At*/

        getProgressDialog().show();

        String getRate = getRating + "/" + searchListModel.id + "/" + sharedPreferences.getString("Id", "");

        StringRequest request = new StringRequest(Request.Method.GET, getRate ,new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {

                getProgressDialog().dismiss();

                Log.e("res is ", s);

                try {

                    /*Here we parse the json which we receive on success of the api call.
                    * and set the rating text*/

                    JSONObject jsonObject = new JSONObject(s);

                    JSONObject out = jsonObject.getJSONObject("output");

                    rate_text.setText(out.getInt("Value") + "/5 Ratings");



                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String getRev= getReview + "/" + searchListModel.id;

                StringRequest request1 = new StringRequest(Request.Method.GET, getRev ,new Response.Listener<String>() {

                    @Override
                    public void onResponse(String s) {

                        getProgressDialog().dismiss();

                        Log.e("res 1 is ", s);

                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        getProgressDialog().dismiss();
                    }
                });

                queue.add(request1);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                getProgressDialog().dismiss();
            }
        });

        queue.add(request);

    }

    /*This is the rating dialog which opens when user clicks on the star button on the toolbar of the screen*/

    private Dialog openRatingDialog(){
        final Dialog dialog = new Dialog(this);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;

        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        layoutParams.gravity = Gravity.CENTER;

        dialog.getWindow().setAttributes(layoutParams);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.dialog_rating);

        Button add_review_button = dialog.findViewById(R.id.add_review_button);

        final RatingBar rating_bar = dialog.findViewById(R.id.rating_bar);

        final EditText review_edit_text = dialog.findViewById(R.id.review_edit_text);

        add_review_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*Here we restrict user from creating the rating of 0*/

                if (rating_bar.getRating() == 0f){
                    Toast.makeText(RecepieDetailsActivity.this, "Zero Rating Not Allowed", Toast.LENGTH_SHORT).show();
                }
                else {

                    getProgressDialog().show();

                    final int rating = (int) rating_bar.getRating();

                    /*Here we generate a POST Request where in we pass our url and data */

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, addRating, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {

                            getRatingReview();

                            /*After successfully entering the rating, we add our review for the recepie using the following function*/

                            addReview(review_edit_text.getText().toString().trim());

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            getProgressDialog().dismiss();
                            Log.e("Rating added ", "Error");
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            /*Here we map our parameters to create the post data.*/
                            Map<String, String> params = new HashMap<>();
                            params.put("UserId", sharedPreferences.getString("Id", ""));
                            params.put("RecipeId", searchListModel.id);
                            params.put("Value", String.valueOf(rating));
                            return params;
                        }
                    };
                    queue.add(stringRequest);

                }

                dialog.dismiss();
            }
        });

        return dialog;
    }

    private void addReview(final String reviewText){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, addRating, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

                getProgressDialog().dismiss();

                Log.e("review is ", s);

                getRatingReview();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                getProgressDialog().dismiss();
                Log.e("Review added ", "Error");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("UserId", sharedPreferences.getString("Id", ""));
                params.put("RecipeId", searchListModel.id);
                params.put("ReviewText", reviewText.trim());
                return params;
            }
        };
        queue.add(stringRequest);
    }

}
