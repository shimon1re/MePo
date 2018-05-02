package com.example.android.mepo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
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


public class TeacherCoursePrevLecActivity extends AppCompatActivity
        implements RecyclerViewAdapter.ListItemClickListener{


    public int COURSE_NUM_LIST_ITEMS ;
    private String c_id;

    //References to RecyclerView and Adapter
    private RecyclerViewAdapter mAdapter;
    private RecyclerView mNumbersListRecycler;
    private ProgressBar mProgressBar;
    private Toast mToast;
    //private TextView mTvUserWelcome;
    public static String IsTeacherLecturesActivity;
    public static ArrayList<String> list_of_lectures = new ArrayList<String>();;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_course_prev_lec);

        mProgressBar = findViewById(R.id.pb_loading_indicator);

        /*Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);*/


        //mTvUserWelcome = findViewById(R.id.tv_hello_user);
        //mTvUserWelcome.setText("Hi " + SharedPrefManager.getInstance(this).getUserFName() + " select a course:");

        IsTeacherLecturesActivity = null;

        COURSE_NUM_LIST_ITEMS =  getIntent().getIntExtra("EXTRA_COURSE_LECTURES_SIZE",0);
        c_id = getIntent().getStringExtra("EXTRA_COURSE_NAME_ID");

        list_of_lectures = getIntent().getStringArrayListExtra("EXTRA_COURSE_LECTURES");
        System.out.println(list_of_lectures);


        mNumbersListRecycler = findViewById(R.id.rv_courseLectures);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mNumbersListRecycler.setLayoutManager(layoutManager);
        mNumbersListRecycler.setHasFixedSize(true);
        //Initializing the RecyclerViewAdapter class
        mAdapter = new RecyclerViewAdapter(COURSE_NUM_LIST_ITEMS, this);
        mNumbersListRecycler.setAdapter(mAdapter);


        //check if user logged in or not.
        if(!SharedPrefManager.getInstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }



    public void lectureDetails(int clickedItemIndex){

        final String l_num = String.valueOf(clickedItemIndex+1);
        mProgressBar.setVisibility(View.VISIBLE);


        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_T_ACTIVITY,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                mProgressBar.setVisibility(View.INVISIBLE);
                                try{
                                    JSONObject jsonObject = new JSONObject(response);
                                    //If there is no error message in the JSON string
                                    if(!jsonObject.getBoolean("error")){

                                        JSONArray students_in_lecture_arr;
                                        students_in_lecture_arr = jsonObject.getJSONArray("l_details");
                                        System.out.println(students_in_lecture_arr);

                                        ArrayList<String> list_of_students_in_lecture = new ArrayList<String>();
                                        if (students_in_lecture_arr != null) {
                                            //System.out.println("lectures_arr not null!!!!!!!!!!!!");
                                            //System.out.println(lectures_arr.length());
                                            for (int i = 0; i < students_in_lecture_arr.length(); i++) {
                                                list_of_students_in_lecture.add(students_in_lecture_arr.getString(i));
                                            }
                                            System.out.println(list_of_students_in_lecture);
                                        }else{
                                            System.out.println("lectures_arr = null!!!!!!!!!!!!");
                                        }

                                        //System.out.println(list_of_lectures);

                                        //for the Recyclerview adapter
                                        IsTeacherLecturesActivity = "yes";
                                        //Get us to TeacherActivity screen
                                        Intent intent = new Intent(getApplicationContext(), TeacherCoursePrevLecDetailsActivity.class);
                                        //intent.putExtra("EXTRA_STUDENT_COURSE_NAME", COURSE_NAME);

                                        intent.putExtra("EXTRA_LECTURES_DETAILS_SIZE", list_of_students_in_lecture.size());
                                        intent.putExtra("EXTRA_LECTURES_DETAILS", list_of_students_in_lecture);

                                        //for the Recyclerview adapter
                                        //IsCourse = "yes";
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
                                startActivity(new Intent(getApplicationContext(), TeacherActivity.class));
                                finish();
                            }
                        }){

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                c_id = c_id.replaceAll("[A-z]","");
                c_id = c_id.replaceAll("[\\[\"\\],-]", "");
                params.put("c_id",c_id);
                params.put("l_number",l_num);
                //params.put("t_password",SharedPrefManager.getInstance(getApplicationContext()).getUserPassword());
                return params;
            }
        };

        //Making a connection by singleton class to the database with stringRequest
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);


    }







    public static ArrayList<String> getListOfTeacherCourseLectures(){
        return list_of_lectures;
    }






    @Override
    public void onListItemClick(int clickedItemIndex) {

        if (mToast != null) {
            mToast.cancel();
        }
        //Here we have to load the list of lectures that belong to the specific course,
        //and open them in a new screen.
        //String toastMessage = list_of_courses_names.get(clickedItemIndex).toString().replaceAll("[\\[\"\\],-]","") + " clicked.";
        //mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);

        //mToast.show();

        lectureDetails(clickedItemIndex);
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






}
