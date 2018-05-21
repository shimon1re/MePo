package com.example.android.mepo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;


public class TeacherCoursePrevLecDetailsActivity extends AppCompatActivity
        implements RecyclerViewAdapter.ListItemClickListener{


    public int STUDENTS_NUM_LIST_ITEMS ;

    //References to RecyclerView and Adapter
    private RecyclerViewAdapter mAdapter;
    private RecyclerView mNumbersListRecycler;
    private ProgressBar mProgressBar;
    private Toast mToast;
    //private TextView mTvUserWelcome;
    public static ArrayList<String> list_of_students = new ArrayList<String>();
    public static ArrayList<String> course_list = new ArrayList<String>();
    public String l_id;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_course_prev_lec_details);

        mProgressBar = findViewById(R.id.pb_loading_indicator);

        /*Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);*/


        //mTvUserWelcome = findViewById(R.id.tv_hello_user);
        //mTvUserWelcome.setText("Hi " + SharedPrefManager.getInstance(this).getUserFName() + " select a course:");
        Bundle extras = getIntent().getExtras();

        STUDENTS_NUM_LIST_ITEMS =  getIntent().getIntExtra("EXTRA_LECTURES_DETAILS_SIZE",0);
        l_id = getIntent().getStringExtra("l_id");
        list_of_students = getIntent().getStringArrayListExtra("EXTRA_LECTURES_DETAILS");
        System.out.println("check2" + list_of_students);
        course_list  = getIntent().getStringArrayListExtra("COURSE_LIST");

        mNumbersListRecycler = findViewById(R.id.rv_studentsInLecture);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mNumbersListRecycler.setLayoutManager(layoutManager);
        mNumbersListRecycler.setHasFixedSize(true);
        //Initializing the RecyclerViewAdapter class
        mAdapter = new RecyclerViewAdapter(STUDENTS_NUM_LIST_ITEMS, this);
        mNumbersListRecycler.setAdapter(mAdapter);


        //check if user logged in or not.
        if(!SharedPrefManager.getInstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }







    public static ArrayList<String> getListOfStudentInLectures(){
        return list_of_students;
    }






    @Override
    public void onListItemClick(int clickedItemIndex) {

        if (mToast != null) {
            mToast.cancel();
        }
        String S_ID = list_of_students.get(clickedItemIndex).substring(2,7);
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
