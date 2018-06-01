package com.example.android.mepo;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
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


    private Button mBtn_prev_lectures,mBtn_start;
    private ProgressBar mProgressBar;
    private TextView tv_courseName;
    private String COURSE_NAME_ID, BACKCOURSE_NAME_ID;
    private String t_idOfCourse, c_id;
    WifiManager wifiManager;

    public static String IsStudentCourseActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_course);


        initComponnents();



        //check if user logged in or not.
        if(!SharedPrefManager.getInstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }


    }









    public void initComponnents(){

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        IsStudentCourseActivity = null;
        mBtn_start = findViewById(R.id.btn_start);
        mBtn_prev_lectures = findViewById(R.id.btn_previous_lectures);
        tv_courseName = findViewById(R.id.tv_courseName);
        mBtn_prev_lectures.setOnClickListener(this);
        mBtn_start.setOnClickListener(this);

        mProgressBar = findViewById(R.id.pb_loading_indicator);

        COURSE_NAME_ID =  getIntent().getStringExtra("EXTRA_STUDENT_COURSE_NAME_ID");
        System.out.println("COURSE_NAME_ID " + COURSE_NAME_ID);
        tv_courseName.setText(COURSE_NAME_ID.substring(2,COURSE_NAME_ID.length()-16));
        c_id = COURSE_NAME_ID;
        t_idOfCourse = COURSE_NAME_ID.substring(COURSE_NAME_ID.length()-7,COURSE_NAME_ID.length()-2);
    }






    public void courseDetails(){

        mProgressBar.setVisibility(View.VISIBLE);
        c_id = getIntent().getStringExtra("EXTRA_STUDENT_COURSE_NAME_ID");
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
                                            for (int i = 0; i < lectures_arr.length(); i++) {
                                                list_of_lectures.add(lectures_arr.getString(i));
                                            }
                                        }else{
                                            System.out.println("lectures_arr = null!!!!!!!!!!!!");
                                        }

                                        System.out.println(list_of_lectures);

                                        //Get us to TeacherActivity screen


                                        Intent intent = new Intent(getApplicationContext(), StudentCoursePrevLecActivity.class);

                                        intent.putExtra("EXTRA_COURSE_LECTURES_SIZE", list_of_lectures.size());
                                        intent.putExtra("EXTRA_COURSE_LECTURES", list_of_lectures);


                                        //for the Recyclerview adapter
                                        IsStudentCourseActivity = "yes";
                                        startActivity(intent);

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
                                        "Connection failed, Please try again",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }){

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                c_id = c_id.substring(1,c_id.length()-7);
                c_id = c_id.replaceAll("[A-z]","");
                c_id = c_id.replaceAll("[\\[\"\\],-]", "");
                params.put("s_id",SharedPrefManager.getInstance(getApplicationContext()).getUserId());
                params.put("c_id",c_id);
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

        if(v == mBtn_prev_lectures){
            courseDetails();
        }

        if(v == mBtn_start){

            Intent intent = new Intent(getApplication(), MyWiFiActivity.class);
            intent.putExtra("EXTRA_TEACHER_ID", t_idOfCourse);
            intent.putExtra("EXTRA_STUDENT_COURSE_NAME_ID",COURSE_NAME_ID);
            startActivity(intent);

        }

    }
}
