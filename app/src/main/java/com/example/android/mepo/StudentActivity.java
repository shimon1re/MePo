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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.android.mepo.StudentCourseActivity.IsCourse;


public class StudentActivity extends AppCompatActivity
        implements RecyclerViewAdapter.ListItemClickListener{


    public int STUDENT_NUM_LIST_ITEMS ;

    //References to RecyclerView and Adapter
    private RecyclerViewAdapter mAdapter;
    private RecyclerView mNumbersListRecycler;
    private ProgressBar mProgressBar;
    private Toast mToast;
    private TextView mTvUserWelcome;
    public static ArrayList<String> list_of_courses_names = new ArrayList<String>();;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        mProgressBar = findViewById(R.id.pb_loading_indicator);

        /*Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);*/


        mTvUserWelcome = findViewById(R.id.tv_hello_user);
        mTvUserWelcome.setText("Hi " + SharedPrefManager.getInstance(this).getUserFName() + " select a course:");


        STUDENT_NUM_LIST_ITEMS =  getIntent().getIntExtra("EXTRA_STUDENT_COURSES_SIZE",0);

        list_of_courses_names = getIntent().getStringArrayListExtra("EXTRA_STUDENT_COURSES_NAME");
        System.out.println(list_of_courses_names);


        mNumbersListRecycler = findViewById(R.id.rv_studentCourses);

        IsCourse = null;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mNumbersListRecycler.setLayoutManager(layoutManager);
        mNumbersListRecycler.setHasFixedSize(true);
        //Initializing the RecyclerViewAdapter class
        mAdapter = new RecyclerViewAdapter(STUDENT_NUM_LIST_ITEMS, this);
        mNumbersListRecycler.setAdapter(mAdapter);


        //check if user logged in or not.
        if(!SharedPrefManager.getInstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }







    public static ArrayList<String> getList_of_student_courses_names(){
        return list_of_courses_names;
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


        Intent intent = new Intent(getApplicationContext(), StudentCourseActivity.class);
        intent.putExtra("EXTRA_STUDENT_COURSE_NAME_ID", list_of_courses_names.get(clickedItemIndex).toString());

        startActivity(intent);
        //finish();


        //startActivity(new Intent(this, StudentCourseActivity.class));

        //mToast.show();
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
