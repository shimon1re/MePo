package com.example.android.mepo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class StudentCourseActivity extends AppCompatActivity implements View.OnClickListener{


    private Button mBtn_courseDetails,mBtn_start;
    private ProgressBar mProgressBar;
    private String COURSE_NAME_ID;

    public static String IsCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_activity_student);

        IsCourse = null;

        //check if user logged in or not.
        if(!SharedPrefManager.getInstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        mBtn_start = findViewById(R.id.btn_start);
        mBtn_courseDetails = findViewById(R.id.btn_courseDetails);

        mBtn_courseDetails.setOnClickListener(this);

        mProgressBar = findViewById(R.id.pb_loading_indicator);

        COURSE_NAME_ID =  getIntent().getStringExtra("EXTRA_STUDENT_COURSE_NAME_ID");


    }








    public void courseDetails(){

        mProgressBar.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_S_ACTIVITY,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                mProgressBar.setVisibility(View.INVISIBLE);
                                try{
                                    JSONObject jsonObject = new JSONObject(response);
                                    //If there is no error message in the JSON string
                                    if(!jsonObject.getBoolean("error")){

                                        JSONArray lectures_arr;
                                        lectures_arr = jsonObject.getJSONArray("c_lectures");


                                        ArrayList<String> list_of_lectures = new ArrayList<String>();
                                        if (lectures_arr != null) {
                                            System.out.println("lectures_arr not null!!!!!!!!!!!!");
                                            System.out.println(lectures_arr.length());
                                            for (int i = 0; i < lectures_arr.length(); i++) {
                                                list_of_lectures.add(lectures_arr.getString(i));
                                                //System.out.println(list_of_courses.get(i));
                                            }
                                        }else{
                                            System.out.println("lectures_arr = null!!!!!!!!!!!!");
                                        }

                                        System.out.println(list_of_lectures);

                                        //Get us to TeacherActivity screen

                                        Intent intent = new Intent(getApplicationContext(), StudentCourseDetailsActivity.class);
                                        //intent.putExtra("EXTRA_STUDENT_COURSE_NAME", COURSE_NAME);

                                        intent.putExtra("EXTRA_COURSE_LECTURES_SIZE", list_of_lectures.size());
                                        intent.putExtra("EXTRA_COURSE_LECTURES", list_of_lectures);

                                        //for the recyclerview adapter
                                        IsCourse = "yes";
                                        startActivity(intent);
                                        //startActivity(new Intent(getApplicationContext(), TeacherActivity.class));
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
                COURSE_NAME_ID = COURSE_NAME_ID.replaceAll("[A-z]","");
                COURSE_NAME_ID = COURSE_NAME_ID.replaceAll("[\\[\"\\],-]", "");
                params.put("s_id",SharedPrefManager.getInstance(getApplicationContext()).getUserId());
                params.put("c_id",COURSE_NAME_ID);
                //params.put("t_password",SharedPrefManager.getInstance(getApplicationContext()).getUserPassword());
                return params;
            }
        };

        //Making a connection by singleton class to the database with stringRequest
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);


    }










    //Responsible for the logout button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }






    //Responsible for the menu buttons, what each button does
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuLogout:
                SharedPrefManager.getInstance(this).logout();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
        return true;
    }






    @Override
    public void onClick(View v) {

        if(v == mBtn_courseDetails){
            courseDetails();
        }

    }
}
