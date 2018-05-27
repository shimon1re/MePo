package com.example.android.mepo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ChangeUserStatus extends AppCompatActivity {
    TextView textView1;
    private Button missButt;
    private Button approveButt;
    private Button arriveBut;
    private String S_ID;
    private String newStatus = null;
    private  String course_list=null;
    private String L_ID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_status);
        missButt = (Button) findViewById(R.id.missBut);
        approveButt = (Button) findViewById(R.id.approveBut);
        arriveBut = (Button) findViewById(R.id.arriveBut);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            S_ID = extras.getString("S_ID");
            L_ID = extras.getString("L_ID");
        }

        // ========================== MISS BUTTON =========================

        missButt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newStatus = "Missed";
                System.out.println("======"+L_ID);
                System.out.println("======"+S_ID);
                System.out.println("======"+newStatus);
                sendPost();
                Toast.makeText(getApplicationContext(),"DONE !",
                        Toast.LENGTH_SHORT).show();
                finish();


            }

        });
        // ========================== APPROVE BUTTON =========================

        approveButt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newStatus = "Approved";
                System.out.println("======"+L_ID);
                System.out.println("======"+S_ID);
                System.out.println("======"+newStatus);
                sendPost();
                Toast.makeText(getApplicationContext(),"DONE !",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        // ========================== ARRIVE BUTTON =========================

        arriveBut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newStatus = "Arrived";
                System.out.println("======"+L_ID);
                System.out.println("======"+S_ID);
                System.out.println("======"+newStatus);
                sendPost();
                Toast.makeText(getApplicationContext(),"DONE !",
                        Toast.LENGTH_SHORT).show();
                //Intent thirdActivity = new Intent(ChangeUserStatus.this,TeacherCoursePrevLecDetailsActivity.class);
                //startActivity(thirdActivity);
                finish();

            }
        });

    }









// ========= THIS FUNCTION SENDING THE POST REQUEST TO SERVER ==============
    public void sendPost()
    {
        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_CHANGE_STATUS,
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
                params.put("s_id",S_ID);
                params.put("status",newStatus);
                params.put("l_id",L_ID);
                return params;
            }
        };

        //Making a connection by singleton class to the database with stringRequest
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

    }


}