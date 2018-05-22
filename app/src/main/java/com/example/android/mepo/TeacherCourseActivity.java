package com.example.android.mepo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.android.mepo.TeacherCoursePrevLecActivity.IsTeacherLecturesActivity;



public class TeacherCourseActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtn_prev_lectures, mBtn_start, mBtn_send_report;
    private ProgressBar mProgressBar;
    private String COURSE_NAME_ID;
    public static String IsTeacherCourseActivity;

    ArrayList<String> list_of_students_in_course = new ArrayList<String>();


    public Date time;
    public String dateAndTime; // hold the current date and time in one format
    public String c_id; //Hold the course id.
    public String t_id; // Hold the tid
    public String maxLectureNumber; // the last lecture of particular course as String
    public int intMaxLectureNumber; //  the last lecture of particular converting to int.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_course);

        IsTeacherCourseActivity = null;
        IsTeacherLecturesActivity = null;

        //check if user logged in or not.
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        mBtn_start = findViewById(R.id.btn_start);
        mBtn_prev_lectures = findViewById(R.id.btn_previous_lectures);
        mBtn_send_report = findViewById(R.id.btn_send_report);

        mBtn_start.setOnClickListener(this);
        mBtn_send_report.setOnClickListener(this);
        mBtn_prev_lectures.setOnClickListener(this);

        mProgressBar = findViewById(R.id.pb_loading_indicator);

        COURSE_NAME_ID = getIntent().getStringExtra("EXTRA_TEACHER_COURSE_NAME_ID");

        list_of_students_in_course = getIntent().getStringArrayListExtra("EXTRA_STUDENTS_IN_COURSE");


    }


    public void prevLectures() {

        mProgressBar.setVisibility(View.VISIBLE);


        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_T_ACTIVITY,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                mProgressBar.setVisibility(View.INVISIBLE);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    //If there is no error message in the JSON string
                                    if (!jsonObject.getBoolean("error")) {

                                        JSONArray lectures_arr;
                                        lectures_arr = jsonObject.getJSONArray("c_lectures");
                                        //System.out.println(lectures_arr);

                                        ArrayList<String> list_of_lectures = new ArrayList<String>();
                                        if (lectures_arr != null) {
                                            //System.out.println("lectures_arr not null!!!!!!!!!!!!");
                                            //System.out.println(lectures_arr.length());
                                            for (int i = 0; i < lectures_arr.length(); i++) {
                                                list_of_lectures.add(lectures_arr.getString(i));

                                            }

                                            System.out.println(list_of_lectures);
                                        } else {
                                            System.out.println("lectures_arr = null!!!!!!!!!!!!");
                                        }

                                        //System.out.println(list_of_lectures);

                                        //for the Recyclerview adapter
                                        IsTeacherCourseActivity = "yes";
                                        //Get us to TeacherActivity screen
                                        Intent intent = new Intent(getApplicationContext(), TeacherCoursePrevLecActivity.class);
                                        //intent.putExtra("EXTRA_STUDENT_COURSE_NAME", COURSE_NAME);

                                        intent.putExtra("EXTRA_COURSE_LECTURES_SIZE", list_of_lectures.size());
                                        intent.putExtra("EXTRA_COURSE_LECTURES", list_of_lectures);
                                        intent.putExtra("EXTRA_COURSE_NAME_ID", COURSE_NAME_ID);

                                        startActivity(intent);


                                    } else {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                jsonObject.getString("message"),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }

                                } catch (JSONException e) {
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
                                //finish();
                            }
                        }) {

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                COURSE_NAME_ID = COURSE_NAME_ID.replaceAll("[A-z]", "");
                COURSE_NAME_ID = COURSE_NAME_ID.replaceAll("[\\[\"\\],-]", "");

                params.put("t_id", SharedPrefManager.getInstance(getApplicationContext()).getUserId());
                params.put("c_id", COURSE_NAME_ID);
                //params.put("t_password",SharedPrefManager.getInstance(getApplicationContext()).getUserPassword());
                return params;
            }
        };

        //Making a connection by singleton class to the database with stringRequest
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }


    public void sendCourseReport() {
        mProgressBar.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_T_EXPORTEXCELFILE,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                mProgressBar.setVisibility(View.INVISIBLE);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    //If there is no error message in the JSON string
                                    if (!jsonObject.getBoolean("error")) {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                jsonObject.getString("message"),
                                                Toast.LENGTH_LONG
                                        ).show();

                                    } else {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                jsonObject.getString("message"),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }

                                } catch (JSONException e) {
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
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();
                            }
                        }) {

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                COURSE_NAME_ID = COURSE_NAME_ID.replaceAll("[A-z]", "");
                COURSE_NAME_ID = COURSE_NAME_ID.replaceAll("[\\[\"\\],-]", "");


                params.put("c_id", COURSE_NAME_ID);
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
        switch (item.getItemId()) {
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
        if (v == mBtn_prev_lectures) {
            prevLectures();
        }
        if (v == mBtn_send_report) {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
            View mView = getLayoutInflater().inflate(R.layout.dialog_export_report, null);
            TextView mText = mView.findViewById(R.id.txt_exportreport);
            Button mBtnOk = mView.findViewById(R.id.btn_ok);
            Button mBtnCancel = mView.findViewById(R.id.btn_cancel);

            mBuilder.setView(mView);
            final AlertDialog dialog = mBuilder.create();
            dialog.show();

            mBtnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //sendCourseReport();
                    Toast.makeText(getApplicationContext(), "The report has been sent to the department",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            mBtnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            //sendCourseReport();
        }



        if (v == mBtn_start) {// =-====Add new lecture======
            getMaxLecture();
            time = (Calendar.getInstance().getTime());
            dateAndTime = time.toString();
            String temp_c_id = COURSE_NAME_ID.replaceAll("[A-z]", "");
            c_id = temp_c_id.replaceAll("[\\[\"\\],-]", "");
            t_id = SharedPrefManager.getInstance(getApplicationContext()).getUserId();
            System.out.println(dateAndTime + " " + c_id+ " " + t_id+ " " + intMaxLectureNumber);
            addLecture();// activate and write inset to DB function via post requests
            Intent intent = new Intent(getApplication(), MyWiFiActivity.class);
            intent.putStringArrayListExtra("EXTRA_STUDENTS_IN_COURSE",list_of_students_in_course);
            intent.putExtra("EXTRA_TEACHER_COURSE_NAME_ID",COURSE_NAME_ID);
            startActivity(intent);

        }
    }

    //============= update cid,l_number,t_id,dateAndtime in tbl_lecture_per_courses========


    // get the max luctere number from the DB

    public void getMaxLecture() {

        mProgressBar.setVisibility(View.VISIBLE);


        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_T_ACTIVITY,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                mProgressBar.setVisibility(View.INVISIBLE);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    //If there is no error message in the JSON string
                                    if (!jsonObject.getBoolean("error")) {
                                        maxLectureNumber= jsonObject.toString();
                                        maxLectureNumber =  maxLectureNumber.replaceAll("[^\\d.]", "");
                                        intMaxLectureNumber= Integer.parseInt(maxLectureNumber);
                                        intMaxLectureNumber = intMaxLectureNumber+1;
                                        System.out.println("===="+intMaxLectureNumber);


                                        IsTeacherCourseActivity = "yes";




                                    } else {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                jsonObject.getString("message"),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }

                                } catch (JSONException e) {
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
                        }) {

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("cc_id", c_id);



                return params;
            }
        };

        //Making a connection by singleton class to the database with stringRequest
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

    }


    public void addLecture() {

        mProgressBar.setVisibility(View.VISIBLE);


        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_T_ACTIVITY,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                mProgressBar.setVisibility(View.INVISIBLE);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    //If there is no error message in the JSON string
                                    if (!jsonObject.getBoolean("error")) {

                                    // NO NEED TO GET JASON ERROR


                                    } else {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                jsonObject.getString("message"),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }

                                } catch (JSONException e) {
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
                        }) {

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("dateAndTime", dateAndTime);
                params.put("c_id", c_id);
                params.put("t_id", t_id);
                params.put("l_id", String.valueOf(intMaxLectureNumber));

                return params;
            }
        };

        //Making a connection by singleton class to the database with stringRequest
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

    }

}
