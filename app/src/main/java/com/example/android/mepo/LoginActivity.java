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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEt_userId,mEt_userPassword;
    private Button mBt_login;
    private Switch mSw_teacher;
    private ProgressBar mProgressBar;
    private String mUser_id;
    private String mUser_password;
    public static String IsStudent;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponents();
        checkIfUserisLoggedIn();


    }




    public void initComponents(){

        mProgressBar = findViewById(R.id.pb_loading_indicator);
        mEt_userId = findViewById(R.id.et_userId);
        mEt_userPassword = findViewById(R.id.et_userPassword);
        mBt_login = findViewById(R.id.btn_login);
        mSw_teacher = findViewById(R.id.sw_teacher);
        mBt_login.setOnClickListener(this);

    }




    public void checkIfUserisLoggedIn(){
        //if user already logged in then pass him to his activity
        if(SharedPrefManager.getInstance(this).isLoggedIn()){

            IsStudent = SharedPrefManager.getInstance(this).getUserIsStudent();
            mUser_id = null;
            mUser_password = null;

            if(SharedPrefManager.getInstance(this).getUserIsStudent() == null) {
                IsStudent = null;
                finish();
                teacherLogin();
                return;
            }
            else {
                finish();
                studentLogin();
                return;
            }

        }
    }






    private void studentLogin(){

        if(!SharedPrefManager.getInstance(this).isLoggedIn()) {
            mUser_id = mEt_userId.getText().toString().trim();
            mUser_password = mEt_userPassword.getText().toString().trim();

            mEt_userId.setVisibility(View.INVISIBLE);
            mEt_userPassword.setVisibility(View.INVISIBLE);
            mBt_login.setVisibility(View.INVISIBLE);
            mSw_teacher.setVisibility(View.INVISIBLE);
        }

        final String user_id = mUser_id;
        final String user_password = mUser_password;


        mProgressBar.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_S_LOGIN,
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
                                        if(!SharedPrefManager.getInstance(getApplicationContext()).isLoggedIn()){

                                            mEt_userId.setVisibility(View.INVISIBLE);
                                            mEt_userPassword.setVisibility(View.INVISIBLE);
                                            mBt_login.setVisibility(View.INVISIBLE);
                                            mSw_teacher.setVisibility(View.INVISIBLE);

                                        SharedPrefManager.getInstance(getApplicationContext())
                                                .studentLogin(
                                                        jsonObject.getString("s_id"),
                                                        jsonObject.getString("s_password"),
                                                        jsonObject.getString("s_firstName"),
                                                        jsonObject.getString("s_lastName"),
                                                        jsonObject.getString("s_email"),
                                                        jsonObject.getString("s_department")
                                                );
                                        IsStudent = SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent();
                                        }
                                        JSONArray courses_arr;
                                        courses_arr = jsonObject.getJSONArray("c_names");

                                        ArrayList<String> list_of_courses = new ArrayList<String>();
                                        if (courses_arr != null) {

                                            for (int i = 0; i < courses_arr.length(); i++) {
                                                list_of_courses.add(courses_arr.getString(i));
                                            }
                                        }

                                        //Get us to StudentActivity screen
                                        Intent intent = new Intent(getApplicationContext(), StudentActivity.class);
                                        intent.putExtra("EXTRA_STUDENT_COURSES_SIZE", list_of_courses.size());
                                        intent.putExtra("EXTRA_STUDENT_COURSES_NAME", list_of_courses);



                                        startActivity(intent);
                                        finish();


                                    }else{
                                        Toast.makeText(
                                                getApplicationContext(),
                                                jsonObject.getString("message"),
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                        finish();

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
                                        "Connection failed, Please try again",
                                        Toast.LENGTH_LONG
                                ).show();
                                //startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();
                            }
                        }){

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                if(SharedPrefManager.getInstance(getApplicationContext()).isLoggedIn()){
                    params.put("s_id",SharedPrefManager.getInstance(getApplicationContext()).getUserId());
                }
                else{
                    params.put("s_id",user_id);
                    params.put("s_password",user_password);
                }
                return params;
            }
        };

        //Making a connection by singleton class to the database with stringRequest
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }











    private void teacherLogin(){

        if(!SharedPrefManager.getInstance(this).isLoggedIn()) {
            mUser_id = mEt_userId.getText().toString().trim();
            mUser_password = mEt_userPassword.getText().toString().trim();

            mEt_userId.setVisibility(View.INVISIBLE);
            mEt_userPassword.setVisibility(View.INVISIBLE);
            mBt_login.setVisibility(View.INVISIBLE);
            mSw_teacher.setVisibility(View.INVISIBLE);
        }
        final String user_id = mUser_id;
        final String user_password = mUser_password;


        mProgressBar.setVisibility(View.VISIBLE);


        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_T_LOGIN,
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
                                        if(!SharedPrefManager.getInstance(getApplicationContext()).isLoggedIn()) {
                                            SharedPrefManager.getInstance(getApplicationContext())
                                                    .teacherLogin(
                                                            jsonObject.getString("t_id"),
                                                            jsonObject.getString("t_password"),
                                                            jsonObject.getString("t_firstName"),
                                                            jsonObject.getString("t_lastName"),
                                                            jsonObject.getString("t_email")
                                                    );
                                        }

                                        JSONArray courses_arr;
                                        courses_arr = jsonObject.getJSONArray("c_names");

                                        ArrayList<String> list_of_courses = new ArrayList<String>();
                                        if (courses_arr != null) {
                                            for (int i = 0; i < courses_arr.length(); i++) {
                                                list_of_courses.add(courses_arr.getString(i));
                                            }
                                        }

                                        //Get us to TeacherActivity screen
                                        Intent intent = new Intent(getApplicationContext(), TeacherActivity.class);
                                        intent.putExtra("EXTRA_TEACHER_COURSES_SIZE", list_of_courses.size());
                                        intent.putExtra("EXTRA_TEACHER_COURSES_NAME_ID", list_of_courses);

                                        startActivity(intent);
                                        finish();
                                    }else{
                                        Toast.makeText(
                                                getApplicationContext(),
                                                jsonObject.getString("message"),
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                        finish();
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
                                        "Connection failed, Please try again",
                                        Toast.LENGTH_LONG
                                ).show();
                                //startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();
                            }
                        }){

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                if(SharedPrefManager.getInstance(getApplicationContext()).isLoggedIn()){
                    params.put("t_id",SharedPrefManager.getInstance(getApplicationContext()).getUserId());
                }
                else{
                    params.put("t_id",user_id);
                    params.put("t_password",user_password);
                }
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
                IsStudent = null;
                teacherLogin();
            }
            else {
                studentLogin();
            }
        }
    }
}
