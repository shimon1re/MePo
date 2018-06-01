package com.example.android.mepo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.android.mepo.TeacherCoursePrevLecActivity.IsTeacherLecturesActivity;


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
    public String c_id;
    public Button delLecButt;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_course_prev_lec_details);

        mProgressBar = findViewById(R.id.pb_loading_indicator);

        Bundle extras = getIntent().getExtras();

        STUDENTS_NUM_LIST_ITEMS =  getIntent().getIntExtra("EXTRA_LECTURES_DETAILS_SIZE",0);
        l_id = getIntent().getStringExtra("l_id");
        c_id = getIntent().getStringExtra("c_id");
        delLecButt = (Button) findViewById(R.id.delLecButt);
        delLecButt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteLec();
            }
        });
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
        
        Intent intent = new Intent(getApplicationContext(), ChangeUserStatus.class);
        //Get AND Send the list of course to the next Activity
 
 
        intent.putExtra("COURSE_LIST", course_list);
        intent.putExtra("S_ID", S_ID);
        intent.putExtra("L_ID", l_id);
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


    public void deleteLec()
    {
        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.DELETE_LECTURE,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                // mProgressBar.setVisibility(View.INVISIBLE);
                                try{

                                    JSONObject jsonObject = new JSONObject(response);
                                    //If there is no error message in the JSON string
                                    if(!jsonObject.getBoolean("error")){
                                        Toast.makeText(
                                                getApplicationContext(),
                                                jsonObject.getString("message"),
                                                Toast.LENGTH_LONG
                                        ).show();
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
                                //mProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Connection failed, Please try again",
                                        Toast.LENGTH_LONG
                                ).show();
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();
                            }
                        }){

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("c_id",c_id);
                params.put("l_id",l_id);
                return params;
            }
        };

        //Making a connection by singleton class to the database with stringRequest
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

    }

    @Override
    public void onBackPressed() {
        System.out.println("Back");
        IsTeacherLecturesActivity = null;
        finish();
    }


}
