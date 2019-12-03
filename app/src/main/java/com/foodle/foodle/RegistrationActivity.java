package com.foodle.foodle;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

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

    Button register_button;

    String url = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/adduser/";

    EditText user_name_edit, email_edit, mobile_number_edit, password_edit, confirm_password_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        register_button = findViewById(R.id.register_button);

        user_name_edit = findViewById(R.id.user_name_edit);
        email_edit = findViewById(R.id.email_edit);
        mobile_number_edit = findViewById(R.id.mobile_number_edit);
        password_edit = findViewById(R.id.password_edit);
        confirm_password_edit = findViewById(R.id.confirm_password_edit);

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isValidRegistration()){

                    getProgressDialog().show();

                    /*Here we first create our post request for registering the data*/

                    RequestQueue queue = Volley.newRequestQueue(RegistrationActivity.this);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {

                            String loginUrl = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/Login/";

                            Log.e("Res is ", s);

                            RequestQueue queue = Volley.newRequestQueue(RegistrationActivity.this);

                            String finalUrl = loginUrl + email_edit.getText().toString().trim() + "/"
                                    + confirm_password_edit.getText().toString().trim();

                            StringRequest request = new StringRequest(Request.Method.GET, finalUrl, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String s) {

                                    getProgressDialog().dismiss();

                                    try {

                                        /*After successfully registering the user, we call the login api to get
                                        * the user details and store them in our device using SharedPreferences which
                                        * creates an xml file called Foodle which is saved in the internal memory of the app
                                        * and save all the details of the user*/

                                        Toast.makeText(RegistrationActivity.this, "Registered Successfully.", Toast.LENGTH_LONG).show();

                                        JSONObject jsonObject = new JSONObject(s);

                                        SharedPreferences sharedPreferences =
                                                getSharedPreferences("Foodle", Context.MODE_PRIVATE);

                                        SharedPreferences.Editor editor = sharedPreferences.edit();

                                        JSONObject output = jsonObject.getJSONObject("output");

                                        editor.putString("Id", output.getString("Id"));
                                        editor.putString("Name", output.getString("Name"));
                                        editor.putString("EmailId", output.getString("EmailId"));
                                        editor.putString("MobileNo", output.getString("MobileNo"));
                                        editor.putString("Password", output.getString("Password"));
                                        editor.putBoolean("isVerified", true);
                                        editor.putBoolean("isSkipped", false);

                                        Log.e("res is ", s);

                                        editor.apply();

                                        /*Here we create an intent to move the app from registration screen to the Main Activity*/

                                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);

                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                                        startActivity(intent);

                                        finish();

                                    } catch (JSONException e) {
                                        Log.e("res is error", "occurred");
                                        e.printStackTrace();
                                    }

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    getProgressDialog().dismiss();
                                    Toast.makeText(RegistrationActivity.this, "An Error Occurred.", Toast.LENGTH_SHORT).show();
                                    Log.e("res is error", "occurred");
                                }
                            });

                            queue.add(request);

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            getProgressDialog().dismiss();
                            Toast.makeText(RegistrationActivity.this, "An Error Occurred.", Toast.LENGTH_SHORT).show();
                            Log.e("REs is ", "Error");
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            /*Here we map our parameters to create the post data.*/
                            Map<String, String> params = new HashMap<>();
                            params.put("Name", user_name_edit.getText().toString().trim());
                            params.put("EmailId", email_edit.getText().toString().trim());
                            params.put("MobileNo", mobile_number_edit.getText().toString().trim());
                            params.put("Password", confirm_password_edit.getText().toString().trim());
                            return params;
                        }
                    };
                    queue.add(stringRequest);

                }
            }
        });

    }

    /*Here we first validate the data which has been entered by the user in the given fields*/

    private boolean isValidRegistration(){
        boolean isValid = true;

        if (user_name_edit.getText().toString().trim().equals("")){
            user_name_edit.setError("Please enter name");
            isValid = false;
        }
        else if (email_edit.getText().toString().trim().equals("")){
            email_edit.setError("Please enter email id");
            isValid = false;
        }
        /*Here we check if the email id entered by the user matches with
        the standard pattern, i.e., if it contains @ and all*/
        else if (!email_edit.getText().toString().trim().matches(Patterns.EMAIL_ADDRESS.pattern())){
            email_edit.setError("Please enter valid email id");
            isValid = false;
        }
        else if (password_edit.getText().toString().trim().equals("")){
            password_edit.setError("Please enter password");
            isValid = false;
        }
        else if (confirm_password_edit.getText().toString().trim().equals("")){
            confirm_password_edit.setError("Please confirm password");
            isValid = false;
        }
        else if (!confirm_password_edit.getText().toString().trim().equals(password_edit.getText().toString().trim())){
            confirm_password_edit.setError("Password does not match");
            isValid = false;
        }

        return isValid;
    }

}
