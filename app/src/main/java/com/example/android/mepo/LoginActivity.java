package com.example.android.mepo;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEt_userId,mEt_userPassword;
    private Button mBt_login;
    private Switch mSw_teacher;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //if user already logged in then pass him to TeacherActivity
        if(SharedPrefManager.getInstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(this, TeacherActivity.class));
            return;
        }

        mEt_userId = findViewById(R.id.et_userId);
        mEt_userPassword = findViewById(R.id.et_userPassword);
        mBt_login = findViewById(R.id.btn_login);
        mSw_teacher = findViewById(R.id.sw_teacher);

        mProgressBar = findViewById(R.id.pb_loading_indicator);

        mBt_login.setOnClickListener(this);
    }

    private void userLogin(){
        final String user_id = mEt_userId.getText().toString().trim();
        final String user_password = mEt_userPassword.getText().toString().trim();

        mProgressBar.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_LOGIN,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                mProgressBar.setVisibility(View.INVISIBLE);
                                try{
                                    JSONObject jsonObject = new JSONObject(response);
                                    //If there is no error message in the JSON string
                                    if(!jsonObject.getBoolean("error")){
                                        //A LOGIN function is called so that the parameters passed
                                        // are the values of the JSON string
                                        SharedPrefManager.getInstance(getApplicationContext())
                                                .userLogin(
                                                        jsonObject.getString("s_id"),
                                                        jsonObject.getString("s_firstName"),
                                                        jsonObject.getString("s_lastName"),
                                                        jsonObject.getString("s_email"),
                                                        jsonObject.getString("s_department")
                                                );

                                        //Get us to TeacherActivity screen
                                        startActivity(new Intent(getApplicationContext(), TeacherActivity.class));
                                        finish();
                                    }else{
                                        Toast.makeText(
                                                getApplicationContext(),
                                                jsonObject.getString("message"),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }

                                } catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                mProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(
                                        getApplicationContext(),
                                        error.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }){

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("s_id",user_id);
                params.put("s_password",user_password);
                return params;
            }
        };

        //Making a connection by singleton class to the database with stringRequest
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }





    @Override
    public void onClick(View view) {
        if (view == mBt_login) {
            if(mSw_teacher.isChecked()) {

            }
            else {
                userLogin();

            }
        }
    }
}
