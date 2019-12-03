package com.foodle.foodle;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class RecepieListActivity extends AppCompatActivity {

    RecyclerView searchElementList;

    ArrayList<SearchListModel> searchListModels = new ArrayList<>();

    RequestQueue queue;

    boolean isFilterApplied = false;

    LinearLayout no_data_layout;

    String data = "";

    String selectedType = "";

    String ingredientsUrl = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/getDishesBySearchType/";

    String foodTypeUrl = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/getDishesByFoodType/";

    String recepieUrl = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/searchDishByName/";

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
        setContentView(R.layout.activity_recepie_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchElementList = findViewById(R.id.searchElementList);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        selectedType = getIntent().getStringExtra("selectedType") == null ? "" :
                getIntent().getStringExtra("selectedType");

        no_data_layout = findViewById(R.id.no_data_layout);

        final FilterModel filterModel = getIntent().getParcelableExtra("filter");

        String url = "";

        int requestId = Request.Method.GET;

        switch (selectedType){

            case "Recepie":

                url = recepieUrl + filterModel.ingredients.replace(" ","%20") + "/";

                requestId = Request.Method.POST;

                break;
            case "Ingredients":

                url = ingredientsUrl + filterModel.ingredients.replace(" ","%20") + "/";

                requestId = Request.Method.GET;

                break;
            case "FoodType":

                url = foodTypeUrl + filterModel.typeOfFood + "/";

                requestId = Request.Method.GET;

                break;
            default:

                requestId = Request.Method.GET;

                url = ingredientsUrl + filterModel.ingredients.replace(" ","%20") + "/";

                break;

        }

        //url += filterModel.ingredients.replace(" ","%20") + "/" + filterModel.typeOfFood + "/";

        Log.e("URL is ", url);

        queue = Volley.newRequestQueue(this);

        getProgressDialog().show();

        StringRequest request = new StringRequest(requestId, url ,new Response.Listener<String>() {

            @Override
            public void onResponse(String s) {

                getProgressDialog().dismiss();

                Log.e("Res is ", s);

                ArrayList<SearchListModel> dataSearchListModels = new ArrayList<>();

                ArrayList<SearchListModel> tFSearchListModels = new ArrayList<>();

                ArrayList<SearchListModel> aGSearchListModels = new ArrayList<>();

                try {
                    JSONObject jsonObject = new JSONObject(s);

                    JSONArray output = jsonObject.getJSONArray("output");

                    for (int i = 0; i < output.length(); i++){

                        /*Here we get the search data and parse it individually*/

                        JSONObject innerObject = output.getJSONObject(i);

                        SearchListModel searchListModel = new SearchListModel();

                        searchListModel.ingredients = innerObject.getString("Ingredients").split(",");

                        searchListModel.directions = innerObject.getString("Procedure").split("~");

                        searchListModel.recipieimage = innerObject.getString("ImageUrl");

                        searchListModel.typefood = innerObject.getString("Foodtype");

                        searchListModel.calories = innerObject.getString("Calories");

                        switch (innerObject.getString("Agegroup")){

                            case "child":
                                searchListModel.agegroup = "Kids";
                            break;

                            case "young":
                                searchListModel.agegroup = "Adults";
                                break;

                            case "old":
                                searchListModel.agegroup = "Old People";
                                break;

                            default:
                                searchListModel.agegroup = "Adults";
                                break;
                        }

                        searchListModel.id = innerObject.getString("Id");

                        searchListModel.name = innerObject.getString("Dishname");

                        dataSearchListModels.add(searchListModel);
                    }

                } catch (JSONException e) {

                    no_data_layout.setVisibility(View.VISIBLE);

                    searchElementList.setVisibility(View.GONE);

                    e.printStackTrace();
                }

                /*Here we check if user has selected any filter.
                If selected, then the following will be performed.*/

                if (dataSearchListModels.size() > 0){

                    if (!filterModel.typeOfFood.equals("")){

                        Log.e("This is ", filterModel.typeOfFood);

                        data = "Food";

                        isFilterApplied = true;

                        for (SearchListModel searchListModel : dataSearchListModels){

                            if (searchListModel.typefood.toLowerCase().equals(filterModel.typeOfFood.toLowerCase())){

                                tFSearchListModels.add(searchListModel);

                            }

                        }

                    }

                    if (!filterModel.ageGroup.equals("")){

                        data = "Age";

                        isFilterApplied = true;

                        if (tFSearchListModels.size() > 0){

                            for (SearchListModel searchListModel : tFSearchListModels){

                                if (searchListModel.agegroup.toLowerCase().equals(filterModel.ageGroup.toLowerCase())){

                                    aGSearchListModels.add(searchListModel);

                                }

                            }

                        }
                        else {

                            for (SearchListModel searchListModel : dataSearchListModels){

                                if (searchListModel.agegroup.toLowerCase().equals(filterModel.ageGroup)){

                                    aGSearchListModels.add(searchListModel);

                                }

                            }
                        }

                    }

                    switch (data){

                        case "Food":
                            searchListModels = tFSearchListModels;
                            break;
                        case "Age":
                            searchListModels = aGSearchListModels;
                            break;
                        default:
                            searchListModels = dataSearchListModels;
                            break;

                    }

                    if (searchListModels.size() > 0) {

                        no_data_layout.setVisibility(View.GONE);

                        searchElementList.setVisibility(View.VISIBLE);

                        searchElementList.setHasFixedSize(true);

                        searchElementList.setLayoutManager(new LinearLayoutManager(RecepieListActivity.this, LinearLayoutManager.VERTICAL, false));

                        searchElementList.setAdapter(new SearchRecyclerAdapter(RecepieListActivity.this));

                    }
                    else {

                        no_data_layout.setVisibility(View.VISIBLE);

                        searchElementList.setVisibility(View.GONE);

                    }
                }
                else {

                    no_data_layout.setVisibility(View.VISIBLE);

                    searchElementList.setVisibility(View.GONE);

                }


            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                no_data_layout.setVisibility(View.VISIBLE);

                searchElementList.setVisibility(View.GONE);

                getProgressDialog().dismiss();
            }
        });

        queue.add(request);


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

                    Toast.makeText(RecepieListActivity.this, "Please login", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(RecepieListActivity.this, FoodleLoginActivity.class);

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

                Intent intent1 = new Intent(RecepieListActivity.this, FavouritesActivity.class);

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

        Toast.makeText(RecepieListActivity.this, "Logged out successfully", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(RecepieListActivity.this, FoodleLoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);

        finish();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchRecyclerAdapter.Holder>{

        Context context;

        public SearchRecyclerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.search_list_cel, parent, false);

            Holder holder = new Holder(view);

            return holder;
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {

            holder.recepie_name.setText(searchListModels.get(position).name);

            switch (searchListModels.get(position).typefood){

                case "3":

                    holder.search_type.setText("Breakfast");

                    break;

                case "1":

                    holder.search_type.setText("Meal");

                    break;

                case "2":
                    holder.search_type.setText("Desserts");
                    break;
            }

            holder.age_group.setText(searchListModels.get(position).agegroup);

            holder.calories_text.setText(searchListModels.get(position).calories + " cals.");

            holder.recepie_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    /*Here we check if user has already viewed the recepie. If not, then it will be added in the database*/

                    if (!AppConstant.getFoodleDBInstance(context).isAdded(searchListModels.get(position).id)){

                        if (AppConstant.getFoodleDBInstance(context).insertIntoTableRecepie(searchListModels.get(position))){

                        }
                    }

                    Intent intent = new Intent(context, RecepieDetailsActivity.class);

                    intent.putExtra("searchModel", searchListModels.get(position));

                    startActivity(intent);

                }
            });

            holder.add_to_favourites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    /*Here we check if user has already viewed the recepie. If not, then it will be added in the database*/

                    if (AppConstant.getFoodleDBInstance(context).isAddedFavourites(searchListModels.get(position).id)){

                        if (AppConstant.getFoodleDBInstance(context).deleteFavourites(searchListModels.get(position).id)){
                            Toast.makeText(context, "Removed from favourites", Toast.LENGTH_LONG).show();
                            holder.add_to_favourites.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.star_unselected));
                        }
                    }
                    else {
                        if (AppConstant.getFoodleDBInstance(context).insertIntoFavourites(searchListModels.get(position))){
                            Toast.makeText(context, "Added in favourites", Toast.LENGTH_LONG).show();
                            holder.add_to_favourites.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.star_selected));
                        }
                    }

                }
            });

            Picasso.with(context)
                    .load(searchListModels.get(position).recipieimage)
                    .resize(holder.itemView.getWidth(), 360)
                    .into(holder.recepie_image, new Callback() {
                @Override
                public void onSuccess() {
                    Log.e("Image Loaded "," Successfully");
                }

                @Override
                public void onError() {

                    holder.recepie_image.setImageResource(R.mipmap.ic_launcher);

                }
            });

        }

        @Override
        public int getItemCount() {
            return searchListModels.size();
        }

        public class Holder extends RecyclerView.ViewHolder{

            ImageView recepie_image, add_to_favourites;
            TextView recepie_name, search_type, age_group, calories_text;

            public Holder(View itemView) {
                super(itemView);

                add_to_favourites = itemView.findViewById(R.id.add_to_favourites);

                recepie_image = itemView.findViewById(R.id.recepie_image);

                recepie_name = itemView.findViewById(R.id.recepie_name);

                calories_text = itemView.findViewById(R.id.calories_text);

                search_type = itemView.findViewById(R.id.search_type);

                age_group = itemView.findViewById(R.id.age_group);

            }
        }
    }

    private class SearchListAdapter extends BaseAdapter {

        ArrayList<SearchListModel> searchListModels;

        LayoutInflater layoutInflater;

        Context context;

        public SearchListAdapter(Context context, ArrayList<SearchListModel> searchListModels){

            this.searchListModels = searchListModels;

            this.context = context;

            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return searchListModels.size();
        }

        @Override
        public Object getItem(int i) {
            return searchListModels.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            Holder holder;

            if (view == null){

                view = layoutInflater.inflate(R.layout.search_list_cel, viewGroup, false);

                holder = new Holder();

                holder.recepie_image = view.findViewById(R.id.recepie_image);

                holder.recepie_name = view.findViewById(R.id.recepie_name);

                view.setTag(holder);

            }

            holder = (Holder) view.getTag();

            SearchListModel searchListModel = (SearchListModel) getItem(position);

            holder.recepie_name.setText(searchListModel.name);

            final Holder finalHolder = holder;
            Picasso.with(context).load(searchListModel.recipieimage).into(holder.recepie_image, new Callback() {
                @Override
                public void onSuccess() {
                    Log.e("Image Loaded "," Successfully");
                }

                @Override
                public void onError() {

                    finalHolder.recepie_image.setImageResource(R.mipmap.ic_launcher);

                }
            });

            return view;
        }

        private class Holder{

            ImageView recepie_image;
            TextView recepie_name, search_type, age_group;

        }

    }

}
