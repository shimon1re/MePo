package com.example.android.mepo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class TeacherActivity extends AppCompatActivity
        implements RecyclerViewAdapter.ListItemClickListener{

    //Need to be replaced with the number of courses of each student
    private static final int NUM_LIST_ITEMS = 100;

    //References to RecyclerView and Adapter
    private RecyclerViewAdapter mAdapter;
    private RecyclerView mNumbersListRecycler;

    private Toast mToast;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

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

        //check if user logged in or not.
        if(!SharedPrefManager.getInstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
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
