package com.foodle.foodle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class FoodleLoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    String email = "";

    String password = "";

    String type = "Email";

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    String url = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/Login/";

    ProgressDialog pDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;

    private ProgressDialog getProgressDialog(){
        if (pDialog == null){

            pDialog = new ProgressDialog(this);

            pDialog.setMessage("Please wait...Loading Data");

            pDialog.setCancelable(false);

            pDialog.setCanceledOnTouchOutside(false);

        }
        return pDialog;
    }

    // UI references.
    private AutoCompleteTextView mEmailView;
    private TextView register_text_btn, forgot_password_btn;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    /*Advice :-
    * We ask the contacts permission to get the email id of
    * the user if it exists inside the device memory.*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foodle_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getUserDetails(loginResult);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);

        Button skip_button = (Button) findViewById(R.id.skip_button);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        skip_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("Foodle", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putBoolean("isSkipped", true);

                editor.putBoolean("isVerified", true);

                editor.apply();

                Intent intent = new Intent(FoodleLoginActivity.this, MainActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);

                finish();

            }
        });

        forgot_password_btn = findViewById(R.id.forgot_password_btn);

        register_text_btn = findViewById(R.id.register_text_btn);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        forgot_password_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openRatingDialog().show();
            }
        });

        register_text_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodleLoginActivity.this, RegistrationActivity.class);

                startActivity(intent);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

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

        rating_bar.setVisibility(View.GONE);

        add_review_button.setText("Send");

        final EditText review_edit_text = dialog.findViewById(R.id.review_edit_text);

        review_edit_text.setHint("Email id");

        add_review_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*Here we restrict user from creating the rating of 0*/

                if (review_edit_text.getText().toString().trim().equals("")){

                    Toast.makeText(FoodleLoginActivity.this, "please enter email id", Toast.LENGTH_LONG).show();

                }
                else if (!isEmailValid(review_edit_text.getText().toString().trim())){
                    Toast.makeText(FoodleLoginActivity.this, "please enter email id", Toast.LENGTH_LONG).show();
                }
                else {

                    getProgressDialog().show();

                    String fpUrl = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/setAutoPassword/" + review_edit_text.getText().toString().trim() + "/";

                    /*Here we generate a POST Request where in we pass our url and data */

                    RequestQueue queue = Volley.newRequestQueue(FoodleLoginActivity.this);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, fpUrl, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {

                            Log.e("Response is ", s);

                            getProgressDialog().dismiss();

                            try {
                                JSONObject jsonObject = new JSONObject(s);

                                dialog.dismiss();

                                if (jsonObject.getString("output").equals("User Not Found.")){

                                    Toast.makeText(FoodleLoginActivity.this, "User Not Found.", Toast.LENGTH_LONG).show();

                                }
                                else {

                                    Toast.makeText(FoodleLoginActivity.this, "Password has been sent to registered email id", Toast.LENGTH_LONG).show();

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }



                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            getProgressDialog().dismiss();
                            dialog.dismiss();
                            Toast.makeText(FoodleLoginActivity.this, "An Error Occurred", Toast.LENGTH_LONG).show();
                            Log.e("Rating added ", "Error");
                        }
                    });
                    queue.add(stringRequest);

                }

            }
        });

        return dialog;
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void getUserDetails(LoginResult loginResult) {
        GraphRequest data_request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject json_object,
                            GraphResponse response) {
                        Log.e("json_object.toString()", json_object.toString());
                        try {
                            email = json_object.getString("email");

                            password = "1234";

                            checkIfUserExistsFacebook(json_object.getString("name"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                });
        Bundle permission_param = new Bundle();
        permission_param.putString("fields", "id,name,email, picture.width(120).height(120)");
                data_request.setParameters(permission_param);
        data_request.executeAsync();

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.

            email = account.getEmail();

            password = "1234";

            type = "Google";

            checkIfUserExists(account);

            //callRegistration(account);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("DEMO", "signInResult:failed code=" + e.getStatusCode() + "\n" + e.getMessage());

        }
    }

    private void checkIfUserExistsFacebook(final String name){

        getProgressDialog().show();

        type = "Facebook";

        RequestQueue queue = Volley.newRequestQueue(FoodleLoginActivity.this);

        String checkUserUrl = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/checkexist/" + email + "/";

        Log.e("URL Is ", checkUserUrl);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, checkUserUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

                getProgressDialog().dismiss();

                Log.e("User " , s);

                try {
                    JSONObject object = new JSONObject(s);

                    String message = object.getString("output");

                    if (message.equals("User Not Found.")){

                        callRegistrationFaceBook(name);

                    }
                    else {

                        loginUser();

                    }

                } catch (JSONException e) {
                    getProgressDialog().dismiss();
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                getProgressDialog().dismiss();
                Log.e("User " , "Error");
            }
        });

        queue.add(stringRequest);



    }

    private void checkIfUserExists(final GoogleSignInAccount account){

        getProgressDialog().show();

        RequestQueue queue = Volley.newRequestQueue(FoodleLoginActivity.this);

        String checkUserUrl = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/checkexist/" + email + "/";

        Log.e("URL Is ", checkUserUrl);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, checkUserUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

                getProgressDialog().dismiss();

                Log.e("User " , s);

                try {
                    JSONObject object = new JSONObject(s);

                    String message = object.getString("output");

                    if (message.equals("User Not Found.")){

                        callRegistration(account);

                    }
                    else {

                        loginUser();

                    }

                } catch (JSONException e) {
                    getProgressDialog().dismiss();
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                getProgressDialog().dismiss();
                Log.e("User " , "Error");
            }
        });

        queue.add(stringRequest);



    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);

            callLogin();

        }
    }

    private void callRegistration(final GoogleSignInAccount account){

        getProgressDialog().show();

        RequestQueue queue = Volley.newRequestQueue(FoodleLoginActivity.this);

        String reg_url = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/adduser/";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, reg_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

                loginUser();

                Log.e("Res is ", s);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                getProgressDialog().dismiss();
                Toast.makeText(FoodleLoginActivity.this, "An Error Occurred.", Toast.LENGTH_SHORT).show();
                Log.e("REs is ", "Error");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                            /*Here we map our parameters to create the post data.*/
                Map<String, String> params = new HashMap<>();
                params.put("Name", account.getDisplayName());
                params.put("EmailId", account.getEmail());
                params.put("MobileNo", "");
                params.put("Password", password);
                return params;
            }
        };
        queue.add(stringRequest);

    }

    private void loginUser(){

        getProgressDialog().show();

        String loginUrl = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/Login/";

        RequestQueue queue = Volley.newRequestQueue(FoodleLoginActivity.this);

        String finalUrl = loginUrl + email.trim() + "/"
                + password.trim();

        StringRequest request = new StringRequest(Request.Method.GET, finalUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

                getProgressDialog().dismiss();

                try {

                                        /*After successfully registering the user, we call the login api to get
                                        * the user details and store them in our device using SharedPreferences which
                                        * creates an xml file called Foodle which is saved in the internal memory of the app
                                        * and save all the details of the user*/

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
                    editor.putString("type", type);
                    editor.putBoolean("isVerified", true);
                    editor.putBoolean("isSkipped", false);


                    Log.e("res is ", s);

                    editor.apply();

                                        /*Here we create an intent to move the app from registration screen to the Main Activity*/

                    Intent intent = new Intent(FoodleLoginActivity.this, MainActivity.class);

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
                Toast.makeText(FoodleLoginActivity.this, "An Error Occurred.", Toast.LENGTH_SHORT).show();
                Log.e("res is error", "occurred");
            }
        });

        queue.add(request);

    }

    private void callRegistrationFaceBook(final String name){

        getProgressDialog().show();

        RequestQueue queue = Volley.newRequestQueue(FoodleLoginActivity.this);

        String reg_url = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/adduser/";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, reg_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

                String loginUrl = "http://foodle-env.ap-south-1.elasticbeanstalk.com/api/Login/";

                Log.e("Res is ", s);

                RequestQueue queue = Volley.newRequestQueue(FoodleLoginActivity.this);

                String finalUrl = loginUrl + email.trim() + "/"
                        + password.trim();


                Log.e("res is ", finalUrl);

                StringRequest request = new StringRequest(Request.Method.GET, finalUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {

                        getProgressDialog().dismiss();

                        try {

                                        /*After successfully registering the user, we call the login api to get
                                        * the user details and store them in our device using SharedPreferences which
                                        * creates an xml file called Foodle which is saved in the internal memory of the app
                                        * and save all the details of the user*/

                            Log.e("res is ", s);

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
                            editor.putString("type", type);
                            editor.putBoolean("isVerified", true);

                            editor.apply();

                                        /*Here we create an intent to move the app from registration screen to the Main Activity*/

                            Intent intent = new Intent(FoodleLoginActivity.this, MainActivity.class);

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
                        Toast.makeText(FoodleLoginActivity.this, "An Error Occurred.", Toast.LENGTH_SHORT).show();
                        Log.e("res is error", "occurred");
                    }
                });

                queue.add(request);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                getProgressDialog().dismiss();
                Toast.makeText(FoodleLoginActivity.this, "An Error Occurred.", Toast.LENGTH_SHORT).show();
                Log.e("REs is ", "Error");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                            /*Here we map our parameters to create the post data.*/
                Map<String, String> params = new HashMap<>();
                params.put("Name", name);
                params.put("EmailId", email);
                params.put("MobileNo", "");
                params.put("Password", password);
                return params;
            }
        };
        queue.add(stringRequest);

    }

    private void callLogin(){

        getProgressDialog().show();

        RequestQueue queue = Volley.newRequestQueue(this);

        String finalUrl = url + email + "/" + password;

        StringRequest request = new StringRequest(Request.Method.GET, finalUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

                getProgressDialog().dismiss();

                Log.e("res is ", s);

                try {

                    JSONObject jsonObject = new JSONObject(s);

                    SharedPreferences sharedPreferences = getSharedPreferences("Foodle", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    JSONObject output = jsonObject.getJSONObject("output");

                    editor.putString("Id", output.getString("Id"));
                    editor.putString("Name", output.getString("Name"));
                    editor.putString("EmailId", output.getString("EmailId"));
                    editor.putString("MobileNo", output.getString("MobileNo"));
                    editor.putString("Password", output.getString("Password"));
                    editor.putBoolean("isVerified", true);

                    editor.apply();

                    Log.e("res is ", s);

                    Intent intent = new Intent(FoodleLoginActivity.this, MainActivity.class);

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
                Toast.makeText(FoodleLoginActivity.this, "An Error Occurred.", Toast.LENGTH_SHORT).show();
                Log.e("res is error", "occurred");
            }
        });

        queue.add(request);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 2;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(FoodleLoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
}

