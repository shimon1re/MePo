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


public class StudentCoursePrevLecActivity extends AppCompatActivity
        implements RecyclerViewAdapter.ListItemClickListener{


    public int COURSE_NUM_LIST_ITEMS ;

    //References to RecyclerView and Adapter
    private RecyclerViewAdapter mAdapter;
    private RecyclerView mNumbersListRecycler;
    //private ProgressBar mProgressBar;
    private Toast mToast;
    //private TextView mTvUserWelcome;
    public static ArrayList<String> list_of_lectures = new ArrayList<String>();
    public static ArrayList<String> sorted_list_of_lec ;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_course_prevlec);

        //mProgressBar = findViewById(R.id.pb_loading_indicator);

        COURSE_NUM_LIST_ITEMS =  getIntent().getIntExtra("EXTRA_COURSE_LECTURES_SIZE",0);

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







    public static ArrayList<String> getListOfStudentCourseLectures(){
        //לסדר פה את התצוגה כמו אצל המרצה
        int maxLec=0, temp;
        String s_num;
        for(int i=0; i<list_of_lectures.size();i++){
            s_num = list_of_lectures.get(i).substring(2, 4).replaceAll("[\\[\"]","");
            temp = Integer.valueOf(s_num);
            if(maxLec <= temp)
                maxLec = temp;
        }
        String[] arr_list_of_lectures = new String[maxLec];

        int num;
        System.out.println("list_of_lectures: " + list_of_lectures);
        for(int i=0; i<list_of_lectures.size();i++){
            num = Integer.parseInt(list_of_lectures.get(i).substring(2, 4).replaceAll("[\\[\"]",""));
            arr_list_of_lectures[num-1] = list_of_lectures.get(i);
        }

        sorted_list_of_lec = new ArrayList<>();

        for(int i=0; i<arr_list_of_lectures.length;i++){
            if(arr_list_of_lectures[i] != null){
                sorted_list_of_lec.add(arr_list_of_lectures[i]);
            }
        }
        return sorted_list_of_lec;




        /*String[] sorted_list_of_lectures = new String[list_of_lectures.size()];
        int num;
        System.out.println("list_of_lectures: " + list_of_lectures);
        for(int i=0; i<list_of_lectures.size();i++){
            num = Integer.parseInt(list_of_lectures.get(i).substring(2, 4).replaceAll("[\\[\"]",""));
            sorted_list_of_lectures[num-1] = list_of_lectures.get(i);
        }
        return sorted_list_of_lectures;*/
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
