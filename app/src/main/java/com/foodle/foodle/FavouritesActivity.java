package com.foodle.foodle;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FavouritesActivity extends AppCompatActivity {

    RecyclerView favouritesList;

    LinearLayout no_data_layout;

    ArrayList<SearchListModel> searchListModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        favouritesList = findViewById(R.id.favouritesList);

        no_data_layout = findViewById(R.id.no_data_layout);

        loadData();

    }

    private void loadData(){

        searchListModels = AppConstant.getFoodleDBInstance(this).getAllFavourites();

        if (searchListModels.size() > 0){

            no_data_layout.setVisibility(View.GONE);

            favouritesList.setVisibility(View.VISIBLE);

            favouritesList.setHasFixedSize(true);

            favouritesList.setLayoutManager(new LinearLayoutManager(FavouritesActivity.this, LinearLayoutManager.VERTICAL, false));

            favouritesList.setAdapter(new SearchRecyclerAdapter(FavouritesActivity.this));

        }
        else {
            no_data_layout.setVisibility(View.VISIBLE);

            favouritesList.setVisibility(View.GONE);
        }
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

            holder.add_to_favourites.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.star_selected));

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

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setTitle("Remove Favourties");

                    builder.setMessage("Do you want to remove the recipie from favourites?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (AppConstant.getFoodleDBInstance(context).isAddedFavourites(searchListModels.get(position).id)){

                                if (AppConstant.getFoodleDBInstance(context).deleteFavourites(searchListModels.get(position).id)){
                                    Toast.makeText(context, "Removed from favourites", Toast.LENGTH_LONG).show();
                                    loadData();
                                }
                            }
                            else {
                                Toast.makeText(context, "No data found", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.show();

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
            TextView recepie_name, search_type, age_group, calories_text;

            public Holder(View itemView) {
                super(itemView);

                add_to_favourites = itemView.findViewById(R.id.add_to_favourites);

                calories_text = itemView.findViewById(R.id.calories_text);

                recepie_image = itemView.findViewById(R.id.recepie_image);

                recepie_name = itemView.findViewById(R.id.recepie_name);

                search_type = itemView.findViewById(R.id.search_type);

                age_group = itemView.findViewById(R.id.age_group);

            }
        }
    }

}
