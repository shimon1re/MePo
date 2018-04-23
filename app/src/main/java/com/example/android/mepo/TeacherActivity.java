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


public class TeacherActivity extends AppCompatActivity
        implements RecyclerViewAdapter.ListItemClickListener{

    //Need to be replaced with the number of courses of each student
    public int NUM_LIST_ITEMS ;

    //References to RecyclerView and Adapter
    private RecyclerViewAdapter mAdapter;
    private RecyclerView mNumbersListRecycler;
    private ProgressBar mProgressBar;
    private Toast mToast;
    public static ArrayList<String> list_of_courses_names = new ArrayList<String>();;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        mProgressBar = findViewById(R.id.pb_loading_indicator);

        NUM_LIST_ITEMS =  getIntent().getIntExtra("EXTRA_TEACHER_COURSES_SIZE",0);

        list_of_courses_names = getIntent().getStringArrayListExtra("EXTRA_TEACHER_COURSES_NAME");
        System.out.println(list_of_courses_names);


        mNumbersListRecycler = findViewById(R.id.rv_studentCourses);
        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list depending on which parameter you pass in to the LinearLayoutManager
         * constructor. By default, if you don't specify an orientation, you get a vertical list.
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mNumbersListRecycler.setLayoutManager(layoutManager);
        mNumbersListRecycler.setHasFixedSize(true);
        //Initializing the RecyclerViewAdapter class
        mAdapter = new RecyclerViewAdapter(NUM_LIST_ITEMS, this);
        mNumbersListRecycler.setAdapter(mAdapter);

        //teacherCoursesList();


        //check if user logged in or not.
        if(!SharedPrefManager.getInstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }


    private void teacherCoursesList(){

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

                                        JSONArray courses_arr;
                                        courses_arr = jsonObject.getJSONArray("c_names");
                                        NUM_LIST_ITEMS = courses_arr.length();

                                        ArrayList<String> list_of_courses = new ArrayList<String>();
                                        if (courses_arr != null) {
                                            System.out.println("courses_arr not null!!!!!!!!!!!!");
                                            System.out.println(courses_arr.length());
                                            for (int i = 0; i < courses_arr.length(); i++) {
                                                list_of_courses.add(courses_arr.getString(i));
                                                //System.out.println(list_of_courses.get(i));
                                            }
                                        }else{
                                            System.out.println("courses_arr = null!!!!!!!!!!!!");
                                        }

                                        System.out.println(list_of_courses);

                                        //Get us to TeacherActivity screen
                                        /*Intent intent = new Intent(getBaseContext(), TeacherActivity.class);
                                        intent.putExtra("EXTRA_TEACHER_COURSES_SIZE", list_of_courses.size());
                                        intent.putExtra("EXTRA_TEACHER_COURSES_NAME", list_of_courses);
                                        for (int i = 0; i < list_of_courses.size(); i++) {
                                            intent.putExtra("EXTRA_TEACHER_COURSES_NAME:"+i, list_of_courses.get(i));
                                        }
                                        //intent.putExtra("EXTRA_TEACHER_COURSES_NAMES", "");
                                        startActivity(intent);
                                        //startActivity(new Intent(getApplicationContext(), TeacherActivity.class));
                                        finish();*/

                                        /*LinearLayoutManager layoutManager = new LinearLayoutManager(TeacherActivity.this);
                                        mNumbersListRecycler.setLayoutManager(layoutManager);
                                        mNumbersListRecycler.setHasFixedSize(true);
                                        //Initializing the RecyclerViewAdapter class
                                        mAdapter = new RecyclerViewAdapter(NUM_LIST_ITEMS, TeacherActivity.this);
                                        mNumbersListRecycler.setAdapter(mAdapter);*/


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
                params.put("t_id",SharedPrefManager.getInstance(getApplicationContext()).getUserId());
                return params;
            }
        };



        //Making a connection by singleton class to the database with stringRequest
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);


    }





    public static ArrayList<String> getList_of_courses_names(){
        return list_of_courses_names;
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
    public void onListItemClick(int clickedItemIndex) {

        if (mToast != null) {
            mToast.cancel();
        }
        /*
         * Create a Toast and store it in our Toast field.
         * The Toast that shows up will have a message.
         */
        String toastMessage = "Item #" + clickedItemIndex + " clicked.";
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);

        mToast.show();
    }




    /*@Override
    public void onClick(View view) {
        if (view == startBttn) {
            startActivity(new Intent(getApplicationContext(), StudentAction.class));
            finish();
            }
            else {

            }
        }
    }*/



}
