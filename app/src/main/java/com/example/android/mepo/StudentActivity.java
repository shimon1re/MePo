package com.example.android.mepo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.android.mepo.StudentCourseActivity.IsStudentCourseActivity;


public class StudentActivity extends AppCompatActivity
        implements RecyclerViewAdapter.ListItemClickListener{


    public int STUDENT_NUM_LIST_ITEMS ;

    private RecyclerViewAdapter mAdapter;
    private RecyclerView mNumbersListRecycler;
    private TextView mTvUserWelcome;
    public static ArrayList<String> list_of_courses_names = new ArrayList<String>();;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);


        /*Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);*/


        mTvUserWelcome = findViewById(R.id.tv_hello_user);
        mTvUserWelcome.setText("Hi " + SharedPrefManager.getInstance(this).getUserFName() + " select a course:");


        STUDENT_NUM_LIST_ITEMS =  getIntent().getIntExtra("EXTRA_STUDENT_COURSES_SIZE",0);

        list_of_courses_names = getIntent().getStringArrayListExtra("EXTRA_STUDENT_COURSES_NAME");
        System.out.println(list_of_courses_names);


        mNumbersListRecycler = findViewById(R.id.rv_studentCourses);

        IsStudentCourseActivity = null;

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

        Intent intent = new Intent(getApplicationContext(), StudentCourseActivity.class);
        intent.putExtra("EXTRA_STUDENT_COURSE_NAME_ID", list_of_courses_names.get(clickedItemIndex).toString());
        startActivity(intent);
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
