package com.example.android.mepo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class TeacherActivity extends AppCompatActivity {

    private TextView mtv_id, mtv_fName, mtv_lName, mtv_email, mtv_dep;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        //check if user logged in or not
        if(!SharedPrefManager.getInstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        mtv_id = findViewById(R.id.tv_id);
        mtv_fName = findViewById(R.id.tv_firstName);
        mtv_lName = findViewById(R.id.tv_lastName);
        mtv_email = findViewById(R.id.tv_email);
        mtv_dep = findViewById(R.id.tv_department);


        mtv_id.setText(SharedPrefManager.getInstance(this).getUserId());
        mtv_fName.setText(SharedPrefManager.getInstance(this).getUserFName());
        mtv_lName.setText(SharedPrefManager.getInstance(this).getUserLName());
        mtv_email.setText(SharedPrefManager.getInstance(this).getUserEmail());
        mtv_dep.setText(SharedPrefManager.getInstance(this).getUserDepartment());

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
