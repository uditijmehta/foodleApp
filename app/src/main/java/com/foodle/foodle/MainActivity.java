package com.foodle.foodle;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView search_recepie_edit_text;
    RecyclerView searchElementList;
    LinearLayout no_data_layout;
    private GoogleSignInClient mGoogleSignInClient;
    ArrayList<SearchListModel> searchListModels = new ArrayList<>();

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
        setContentView(R.layout.activity_main);

        search_recepie_edit_text = findViewById(R.id.search_recepie_edit_text);

        no_data_layout = findViewById(R.id.no_data_layout);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        searchElementList = findViewById(R.id.searchElementList);

        search_recepie_edit_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);

                startActivity(intent);

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        loadData();

    }

    private void loadData(){

        /*Here we get the data saved in the database to show them offline.*/

        searchListModels = new ArrayList<>();

        searchListModels = AppConstant.getFoodleDBInstance(this).getAllRecentSearches();

        if (searchListModels.size() > 0){

            no_data_layout.setVisibility(View.GONE);

            searchElementList.setVisibility(View.VISIBLE);

            searchElementList.setHasFixedSize(true);

            searchElementList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

            searchElementList.setAdapter(new SearchRecyclerAdapter(this));
        }
        else {

            no_data_layout.setVisibility(View.VISIBLE);

            searchElementList.setVisibility(View.GONE);

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

                    Toast.makeText(MainActivity.this, "Please login", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(MainActivity.this, FoodleLoginActivity.class);

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

                Intent intent1 = new Intent(MainActivity.this, FavouritesActivity.class);

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
        editor.putBoolean("isSkipped", false);

        editor.apply();

        Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(MainActivity.this, FoodleLoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);

        finish();

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

            holder.search_type.setText(searchListModels.get(position).typefood);

            holder.age_group.setText(searchListModels.get(position).agegroup);

            holder.calories_text.setText(searchListModels.get(position).calories + " cals.");

            switch (searchListModels.get(position).typefood){

                case "1":

                    holder.search_type.setText("Breakfast");

                    break;

                case "2":

                    holder.search_type.setText("Meal");

                    break;

                case "3":
                    holder.search_type.setText("Desserts");
                    break;
            }

            holder.recepie_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                        Intent intent = new Intent(context, RecepieDetailsActivity.class);

                        SearchListModel searchListModel = searchListModels.get(position);

                        intent.putExtra("searchModel", searchListModel);

                        startActivity(intent);
                }
            });

            if (AppConstant.getFoodleDBInstance(context).isAddedFavourites(searchListModels.get(position).id)){

                holder.add_to_favourites.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.star_selected));

            }
            else {
                holder.add_to_favourites.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.star_unselected));
            }

            holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.e("Long ", "Pressed");

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setTitle("Delete");

                    builder.setMessage("Are you sure you want to delete the recepie?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (AppConstant.getFoodleDBInstance(context).deleteRecepie(searchListModels.get(position).id)){

                                Toast.makeText(context, "Recepie deleted successfully.", Toast.LENGTH_LONG).show();

                                loadData();

                            }

                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    builder.show();

                    return true;
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

            Picasso.with(context).load(searchListModels.get(position).recipieimage).into(holder.recepie_image, new Callback() {
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
            View view;
            TextView recepie_name, search_type, age_group, calories_text;

            public Holder(View itemView) {
                super(itemView);

                view = itemView;

                add_to_favourites = itemView.findViewById(R.id.add_to_favourites);

                recepie_image = itemView.findViewById(R.id.recepie_image);

                calories_text = itemView.findViewById(R.id.calories_text);

                recepie_name = itemView.findViewById(R.id.recepie_name);

                search_type = itemView.findViewById(R.id.search_type);

                age_group = itemView.findViewById(R.id.age_group);

            }
        }
    }

}
