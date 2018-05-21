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
//import static com.example.android.mepo.StudentCourseActivity.IsCourse;
import static com.example.android.mepo.TeacherCourseActivity.IsTeacherCourseActivity;
import static com.example.android.mepo.TeacherCoursePrevLecActivity.IsTeacherLecturesActivity;


public class TeacherActivity extends AppCompatActivity
        implements RecyclerViewAdapter.ListItemClickListener{


    public int TEACHER_NUM_LIST_ITEMS ;

    //References to RecyclerView and Adapter
    private RecyclerViewAdapter mAdapter;
    private RecyclerView mNumbersListRecycler;
    //private ProgressBar mProgressBar;
    private Toast mToast;
    public static ArrayList<String> list_of_courses_names_id = new ArrayList<String>();
    private int courseIndex;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        //mProgressBar = findViewById(R.id.pb_loading_indicator);

        initComponnents();




        //check if user logged in or not.
        if(!SharedPrefManager.getInstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }







    public void initComponnents(){

        TEACHER_NUM_LIST_ITEMS =  getIntent().getIntExtra("EXTRA_TEACHER_COURSES_SIZE",0);

        list_of_courses_names_id = getIntent().getStringArrayListExtra("EXTRA_TEACHER_COURSES_NAME_ID");

        mNumbersListRecycler = findViewById(R.id.rv_teacherCourses);

        //mProgressBar.findViewById(R.id.pb_loading_indicator);

        IsTeacherCourseActivity = null;
        IsTeacherLecturesActivity = null;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mNumbersListRecycler.setLayoutManager(layoutManager);
        mNumbersListRecycler.setHasFixedSize(true);
        //Initializing the RecyclerViewAdapter class
        mAdapter = new RecyclerViewAdapter(TEACHER_NUM_LIST_ITEMS, this);
        mNumbersListRecycler.setAdapter(mAdapter);

    }




    public static ArrayList<String> getList_of_teacher_courses_names(){
        return list_of_courses_names_id;
    }



    public void studentIdsPerCourse(int clickedItemIndex){
        courseIndex = clickedItemIndex;
        //mProgressBar.setVisibility(View.VISIBLE);


        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_T_ACTIVITY,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                //mProgressBar.setVisibility(View.INVISIBLE);
                                try{
                                    JSONObject jsonObject = new JSONObject(response);
                                    //If there is no error message in the JSON string
                                    if(!jsonObject.getBoolean("error")){

                                        JSONArray students_in_course_arr;
                                        students_in_course_arr = jsonObject.getJSONArray("s_ids");
                                        System.out.println(students_in_course_arr);
                                        ArrayList<String> list_of_students_in_course = new ArrayList<String>();
                                        if (students_in_course_arr != null) {
                                            for (int i = 0; i < students_in_course_arr.length(); i++) {
                                                list_of_students_in_course.add(students_in_course_arr.getString(i));
                                            }

                                        }else{
                                            System.out.println("lectures_arr = null!!!!!!!!!!!!");
                                        }

                                        //for the Recyclerview adapter
                                        IsTeacherLecturesActivity = "yes";
                                        Intent intent = new Intent(getApplicationContext(), TeacherCourseActivity.class);
                                        intent.putExtra("EXTRA_TEACHER_COURSE_NAME_ID", list_of_courses_names_id.get(courseIndex).toString());

                                        intent.putExtra("EXTRA_STUDENTS_IN_COURSE_SIZE", list_of_students_in_course.size());
                                        System.out.println("TeacherActivity list_of_students_in_course " + list_of_students_in_course);
                                        intent.putExtra("EXTRA_STUDENTS_IN_COURSE", list_of_students_in_course);
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
                                //mProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Connection failed, Please try again",
                                        Toast.LENGTH_LONG
                                ).show();
                                startActivity(new Intent(getApplicationContext(), TeacherCourseActivity.class));
                                finish();
                            }
                        }){

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                String c_id = list_of_courses_names_id.get(courseIndex).toString();
                c_id = c_id.replaceAll("[A-z]","");
                c_id = c_id.replaceAll("[\\[\"\\],-]", "");
                params.put("c_id",c_id);
                return params;
            }
        };

        //Making a connection by singleton class to the database with stringRequest
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);


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
        studentIdsPerCourse(clickedItemIndex);

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
