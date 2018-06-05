package com.example.android.mepo;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.media.Image;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.android.mepo.TeacherCourseActivity.getmBtn_start_toResume;
import static com.example.android.mepo.TeacherCourseActivity.setmBtn_start_toResume;


public class MyWiFiActivity extends AppCompatActivity implements View.OnClickListener{

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiP2pGroup wifiP2pGroup;
    WifiP2pInfo wifiP2pInfo_forGroup;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    Button btnOnOff, btnDiscover, btnEnd;
    ImageView iv_connect, iv_noConnect;
    ListView listView, grouplistView;
    TextView read_msg_box, tv_connectedStudents, connectionStatus;
    ProgressBar progressBar;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    List<WifiP2pDevice> clients = new ArrayList<WifiP2pDevice>();
    ArrayAdapter<String> adapter, groupAdapter;
    String[] deviceNameArray, groupDevicesNamesArray, groupDeviceIsCorrectStudent;
    Map<String, int[]> stdStatusCountMap ;
    WifiP2pDevice[] deviceArray, groupDeviceArray;

    static boolean haveGroup = false;
    static boolean endButtonPressed = false;
    static boolean studentConnected = false;
    static boolean isGroupOwner;
    boolean backIsPressed = false;
    final int TIMES_TO_SEARCH = 10;
    String courseT_ID, c_id;
    int l_number;
    ArrayList<String> list_of_students_in_course = new ArrayList<String>();

    private CheckPresenceRunnable checkPresenceRunnable;
    private static int dateFlag = 0 ;
    private int numToRestartDiscovery = 0;
    boolean studentIsConnect = false;
    private  static String startStrDate, endStrDate;
    private String currentDate;
    private final int TASK_COMPLETE = 1;
    private final int TASK_TERMINATED = 2;






    // Defines a Handler object that's attached to the UI thread
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {

            checkPresenceRunnable = (CheckPresenceRunnable) inputMessage.obj;
            currentDate = checkPresenceRunnable.strDate;
            if(dateFlag == 0) {
                startStrDate = currentDate;
                dateFlag = 1;
            }

            switch (inputMessage.what) {

                // The status check is done
                case TASK_COMPLETE:

                    read_msg_box.setVisibility(View.VISIBLE);

                    if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null && endButtonPressed == false)
                        checkPresence();

                    else if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null && endButtonPressed == true){
                        endStrDate = currentDate;
                        checkPresenceRunnable.cancelTimer();
                        dateFlag = 0;
                    }
                    if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null &&
                            !connectionStatus.getText().equals("Connected") && numToRestartDiscovery < TIMES_TO_SEARCH && studentIsConnect == false) {

                        resetDiscover();
                        //endButtonPressed = false;
                        //checkPresenceRunnable = new CheckPresenceRunnable(MyWiFiActivity.this);
                        //discoverAndCreateGroup();
                        //btnDiscover.callOnClick();
                        System.out.println("1 numToRestartDiscovery: " + numToRestartDiscovery);
                        numToRestartDiscovery++;

                    }
                    else if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null &&
                            !connectionStatus.getText().equals("Connected") && numToRestartDiscovery >= TIMES_TO_SEARCH && studentIsConnect == false){
                        progressBar.setVisibility(View.INVISIBLE);
                        read_msg_box.setText("Teacher not found, try again");
                        checkPresenceRunnable.cancelTimer();
                        btnDiscover.setVisibility(View.VISIBLE);
                        btnEnd.setVisibility(View.INVISIBLE);
                        System.out.println("2 numToRestartDiscovery: " + numToRestartDiscovery);
                    }
                    break;

                case TASK_TERMINATED:

                    Toast.makeText(getApplicationContext(), "Due to inactivity of 5 hours the registration has been discontinued.", Toast.LENGTH_SHORT).show();
                    onBtnEndClick(2);
                    break;

                default:
                    /*
                     * Pass along other messages from the UI
                     */
                    super.handleMessage(inputMessage);
            }

        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wi_fi);

        initWork();
        exqListener();



    }





    public void initWork(){
        System.out.println("initWork");
        list_of_students_in_course = getIntent().getStringArrayListExtra("EXTRA_STUDENTS_IN_COURSE");
        numToRestartDiscovery = 0;
        endButtonPressed = false;
        btnOnOff = findViewById(R.id.onOff);
        btnOnOff.setVisibility(View.INVISIBLE);
        btnDiscover = findViewById(R.id.discover);
        btnEnd = findViewById(R.id.endButton);
        btnEnd.setOnClickListener(this);
        connectionStatus = findViewById(R.id.connectionStatus);
        read_msg_box = findViewById(R.id.readMsg);
        read_msg_box.setVisibility(View.INVISIBLE);

        if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null) {
            c_id = getIntent().getStringExtra("EXTRA_STUDENT_COURSE_NAME_ID").substring(7,10);
            listView = findViewById(R.id.peerListView);
            listView.setVisibility(View.INVISIBLE);
            iv_connect = findViewById(R.id.iv_connect);
            iv_noConnect = findViewById(R.id.iv_noConnect);
            iv_connect.setVisibility(View.INVISIBLE);
            iv_noConnect.setVisibility(View.INVISIBLE);
            if(tv_connectedStudents != null)
                tv_connectedStudents.setVisibility(View.INVISIBLE);
            isGroupOwner = false;
            progressBar = findViewById(R.id.pb_loading_indicator);
        }
        if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null) {
            tv_connectedStudents = findViewById(R.id.tv_connectedStudents);
            tv_connectedStudents.setText("List of connected students");
            c_id = getIntent().getStringExtra("EXTRA_TEACHER_COURSE_NAME_ID");
            c_id = c_id.substring(c_id.length()-5,c_id.length()-2);
            //l_number = getIntent().getIntExtra("EXTRA_LECTURE_NUMBER",l_number) ;
            grouplistView = findViewById(R.id.peerListView);
            isGroupOwner = true;
            btnDiscover.setVisibility(View.INVISIBLE);
        }

        courseT_ID =  getIntent().getStringExtra("EXTRA_TEACHER_ID");

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (mManager != null) {

            mChannel =  mManager.initialize(this, getMainLooper(), null);
            if (mChannel == null) {
                Toast.makeText(this, "Failed to set up connection with wifi p2p service", Toast.LENGTH_LONG).show();
                mManager = null;
            }
        } else {
            Toast.makeText(this, "This device does not support Wi-Fi Direct", Toast.LENGTH_LONG).show();

            finish();
        }

        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        changeDeviceName();

        //btnDiscover.performClick();
        //btnDiscover.callOnClick();
    }





    public void exqListener(){

        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                    //btnOnOff.setText("ON");
                }

            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("Discover");
                read_msg_box.setText("");
                numToRestartDiscovery = 0;

                checkPresenceRunnable = new CheckPresenceRunnable(MyWiFiActivity.this);

                endButtonPressed = false;
                discoverAndCreateGroup();
            }
        });

    }





    public void onBtnEndClick(int zeroForBtn){
        endButtonPressed = true;
        if(!connectionStatus.getText().equals("Device Disconnected") || zeroForBtn==0 || zeroForBtn==2) {
            //Toast.makeText(getApplicationContext(), "Timer canceled", Toast.LENGTH_SHORT).show();
            //progressBar.setVisibility(View.INVISIBLE);
            read_msg_box.setText("");
            if(checkPresenceRunnable!=null)
                checkPresenceRunnable.cancelTimer();
            if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null) {
                btnDiscover.setVisibility(View.VISIBLE);
                btnEnd.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int reason) {

                    }
                });
                studentIsConnect = false;
                numToRestartDiscovery = 0;
                removeGroup();
            }
        }
        if(checkPresenceRunnable != null && SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null) {
            endStrDate = currentDate;
            dateFlag = 0;
            if(zeroForBtn != 2) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_export_report, null);
                TextView mText = mView.findViewById(R.id.txt_exportreport);
                mText.setText("Do you want to save the lecture details?");
                Button mBtnOk = mView.findViewById(R.id.btn_ok);
                Button mBtnCancel = mView.findViewById(R.id.btn_cancel);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                mBtnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getMaxLecture();
                        //checkPresence();
                    /*Toast.makeText(getApplicationContext(), "The report has been sent to the department",
                            Toast.LENGTH_SHORT).show();*/
                        dialog.dismiss();
                    }
                });
                mBtnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeGroup();
                        dialog.dismiss();
                    }
                });
            }
            else
                getMaxLecture();

        }
        
    }






    public void connectToTeacherGroup(int position){
        final WifiP2pDevice device = deviceArray[position];
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null && connectionStatus.getText().equals("Device Disconnected")
                && device.isGroupOwner() == true && deviceNameArray[0] != null) {
            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    studentConnected = true;
                    Toast.makeText(getApplicationContext(), "Connected to " + deviceNameArray[0], Toast.LENGTH_SHORT).show();
                    iv_noConnect.setVisibility(View.INVISIBLE);
                    iv_connect.setVisibility(View.VISIBLE);
                    studentIsConnect = true;
                    numToRestartDiscovery=0;
                    read_msg_box.setText("Connected to " + deviceNameArray[0]);
                    progressBar.setVisibility(View.INVISIBLE);
                    btnDiscover.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onFailure(int reason) {
                    //Toast.makeText(getApplicationContext(), "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }





    public void discoverAndCreateGroup(){

        if(mManager != null && wifiManager.isWifiEnabled()) {
            if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null &&
                    (!connectionStatus.getText().equals("Connected") && haveGroup == false) /*&& !connectionStatus.getText().equals("Connected"))*/ /**/) {

                mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Device is ready to accept incoming connections from peers.
                        //Toast.makeText(getApplicationContext(), "Group Created" , Toast.LENGTH_SHORT).show();
                        haveGroup = true;
                        endStrDate = null;
                        btnDiscover.setVisibility(View.INVISIBLE);
                        //setmBtn_start_toResume(1);

                        /*stdStatusCountMap = new HashMap<>();
                        System.out.println("AAAAAAAAAAAAAAAAAAAAAAA");
                        int[] statusCount =new int[1];
                        statusCount[0] = 0;
                        String clean;
                        if(list_of_students_in_course != null) {
                            for (int i = 0; i < list_of_students_in_course.size(); i++) {
                                clean = list_of_students_in_course.get(i);
                                clean = clean.replaceAll("[\\[\"\\],-]", "");
                                stdStatusCountMap.put(clean, statusCount);
                            }
                        }*/
                    }

                    @Override
                    public void onFailure(int reason) {

                        Toast.makeText(getApplicationContext(), "Group creation failed. Retry.",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            }
            else if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null ){

                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override

                    public void onSuccess() {
                        //Toast.makeText(getApplicationContext(), "Searching ...", Toast.LENGTH_LONG).show();
                        read_msg_box.setText("Searching...");
                        iv_noConnect.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.INVISIBLE);
                        btnEnd.setVisibility(View.VISIBLE);
                        btnDiscover.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(),
                                "Discovery Failed : " + getDiscoveryFailureReasonCode(reason),
                                Toast.LENGTH_SHORT).show();
                        System.out.println("Discovery Failed - reason: " + reason);

                    }
                });

            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Check if wifi is on... ", Toast.LENGTH_SHORT).show();
        }
    }





    private static String getDiscoveryFailureReasonCode(int reasonCode) {
        switch (reasonCode) {
            case WifiP2pManager.ERROR:
                return "ERROR";
            case WifiP2pManager.P2P_UNSUPPORTED:
                return "P2P_UNSUPPORTED";
            case WifiP2pManager.BUSY:
                return "BUSY";
        }
        return "UNKNOWN";
    }





    public void removeGroup(){

        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //setmBtn_start_toResume(0);
                //Toast.makeText(getApplicationContext(), "Group Removed", Toast.LENGTH_SHORT).show();
                haveGroup = false;
                //clients.clear();
                if (SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null ){
                    clearStdStatusCountMap();
                    resetListView(1);
                    if(clients.size() != 0 && wifiManager.isWifiEnabled()) {
                        wifiManager.setWifiEnabled(false);
                    }
                }
                else {
                    resetListView(0);
                    studentConnected = false;
                }
                //if(backIsPressed == true)
                  //  MyWiFiActivity.super.onBackPressed();
            }

            @Override
            public void onFailure(int reason) {
                //Toast.makeText(getApplicationContext(), "Group remove failed. Retry.",
                //  Toast.LENGTH_SHORT).show();
            }
        });


        //String msg = writeMsg.getText().toString().trim();
        //sendReceive.write(msg.getBytes());
    }





    public void resetListView(int one_for_teacher){
        String[] note = new String[1];
        if(one_for_teacher == 1) {
            note[0] ="No Students Connected";
            groupAdapter = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_list_item_1, note);
            grouplistView.setAdapter(groupAdapter);
        }
        else if(one_for_teacher == 0){
            note[0] = "No Teacher Found";
            adapter = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_list_item_1, note);
            listView.setAdapter(adapter);
        }
        else if(one_for_teacher == 2){
            note[0] = courseT_ID;
            adapter = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_list_item_1, note);
            listView.setAdapter(adapter);
        }
    }





    public void changeDeviceName(){

        try {

            Class[] paramTypes = new Class[3];
            paramTypes[0] = WifiP2pManager.Channel.class;
            paramTypes[1] = String.class;
            paramTypes[2] = WifiP2pManager.ActionListener.class;
            Method setDeviceName = mManager.getClass().getMethod(
                    "setDeviceName", paramTypes);
            setDeviceName.setAccessible(true);

            Object arglist[] = new Object[3];
            arglist[0] = mChannel;
            arglist[1] = SharedPrefManager.getInstance(getApplicationContext()).getUserId().concat("_" + c_id);
            arglist[2] = new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Log.d("setDeviceName succeeded", "true");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d("setDeviceName failed", "true");
                }
            };
            setDeviceName.invoke(mManager, arglist);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }






    public void handleState(CheckPresenceRunnable checkPresenceRunnable, int state) {


        switch (state) {

            case TASK_COMPLETE:

                 /* Creates a message for the Handler
                    with the state and the task object*/
                Message completeMessage = mHandler.obtainMessage(state, checkPresenceRunnable);
                completeMessage.sendToTarget();
                //mHandler.sendMessageAtTime(mHandler.obtainMessage(state), System.currentTimeMillis()+interval);
                //handler.sendMessageDelayed(msg, interval);
                break;
            case TASK_TERMINATED:
                Message msg = mHandler.obtainMessage(state, checkPresenceRunnable);
                msg.sendToTarget();
                break;
        }

    }






    public void checkPresence() {

        String stdInCourse;
        int startHouer, startMin, endHouer, endMin, subHouers, subMinutes,
                sumOfMinutes = 0;
        float numOfCheckBeats = 0;

        if (startStrDate != null) {
            //זמני עד שאסיים לבדוק את החישוב...ואז אעביר לשעות ודקות
            //startHouer = Integer.parseInt(startStrDate.substring(10,12));
            //startMin = Integer.parseInt(startStrDate.substring(13,15));
            startHouer = Integer.parseInt(startStrDate.substring(13, 15));
            startMin = Integer.parseInt(startStrDate.substring(16, 18));
            if (endStrDate != null) {
                //endHouer = Integer.parseInt(endStrDate.substring(10, 12));
                //endMin = Integer.parseInt(endStrDate.substring(13, 15));
                endHouer = Integer.parseInt(endStrDate.substring(13, 15));
                endMin = Integer.parseInt(endStrDate.substring(16, 18));
                System.out.println("startHouer " + startHouer + "   startMin " + startMin);
                System.out.println("endHouer " + endHouer + "   endMin " + endMin);
                //זמני - רק בשביל בדיקות
                if(startHouer > 24 && endHouer > 24){
                    subHouers = endHouer - startHouer;
                    subHouers = subHouers % 24;
                }
                //זמני - רק בשביל בדיקות
                else if(startHouer > 24 && endHouer < 24){
                    startHouer = 60 - startHouer;
                    subHouers = startHouer + endHouer;
                }

                subHouers = endHouer - startHouer;
                if (subHouers < 0) {
                    subHouers = 24 + (subHouers);
                }
                if (subHouers > 0)
                    sumOfMinutes = (--subHouers) * 60;
                subMinutes = endMin - startMin;
                if (subMinutes <= 0) {
                    subMinutes = 60 + (subMinutes);
                }
                sumOfMinutes = sumOfMinutes + subMinutes;
                if (sumOfMinutes > 0)
                    numOfCheckBeats = (float) sumOfMinutes / 10; //10(minutes) equal to check interval time
                System.out.println("sumOfMinutes " + sumOfMinutes);

                //call the function that report to DB
                //here because endStrDate != null
                //getMaxLecture(numOfCheckBeats);

                reportStudentsPresence(numOfCheckBeats);

            }
        }



        if(stdStatusCountMap == null /*&& !getmBtn_start_toResume()*/){
            System.out.println("init stdStatusCountMap");
            stdStatusCountMap = new HashMap<>();
            int[] statusCount =new int[1];
            statusCount[0] = 0;
            String clean;
            if(list_of_students_in_course != null) {
                for (int i = 0; i < list_of_students_in_course.size(); i++) {
                    clean = list_of_students_in_course.get(i);
                    clean = clean.replaceAll("[\\[\"\\],-]", "");
                    stdStatusCountMap.put(clean, statusCount);
                }
            }
        }


        if(list_of_students_in_course != null) {
            for (int i = 0; i < list_of_students_in_course.size(); i++) {
                stdInCourse = list_of_students_in_course.get(i);
                stdInCourse = stdInCourse.replaceAll("[\\[\"\\],-]", "");
                if (groupDeviceIsCorrectStudent.length > 0) {
                    for (int j = 0; j < groupDeviceIsCorrectStudent.length; j++) {

                        if (stdInCourse.equals(groupDeviceIsCorrectStudent[j])) {
                            int[] appendStatusCount = new int[1];
                            appendStatusCount[0] = stdStatusCountMap.get(stdInCourse)[0];
                            appendStatusCount[0]++;
                            //stdStatusCountMap.remove(stdInCourse);
                            stdStatusCountMap.put(stdInCourse, appendStatusCount);
                            //System.out.println("stdStatusCountMap " + stdStatusCountMap.toString());
                            System.out.println("stdInCourse: " + stdInCourse + " StatusCount: " + appendStatusCount[0]);
                            //System.out.println("stdInCourse 2: " + stdInCourse + " StatusCount: " + stdStatusCountMap.get(stdInCourse)[0]);
                        }
                    }
                }
                //System.out.println(stdStatusCountMap.get(clean)[0]);
            }
        }

    }





    public void reportStudentsPresence(float numOfCheckBeats){

        String stdInCourse;
        ArrayList<String> reportThisStudentToDB = new ArrayList<>();
        float pass = 0;
        String l_num = String.valueOf(l_number);
        System.out.println("numOfCheckBeats " + numOfCheckBeats);
        if(list_of_students_in_course != null) {
            for (int i = 0; i < list_of_students_in_course.size(); i++) {
                stdInCourse = list_of_students_in_course.get(i);
                stdInCourse = stdInCourse.replaceAll("[\\[\"\\],-]", "");
                pass =  ((float) stdStatusCountMap.get(stdInCourse)[0] / numOfCheckBeats);
                System.out.println("stdInCourse: " + stdInCourse + "  count: " + stdStatusCountMap.get(stdInCourse)[0]);
                System.out.println("pass" + pass);
                if( pass >= 0.5 && stdStatusCountMap.get(stdInCourse)[0] != 0){
                    System.out.println("----------------------------------");
                    System.out.println("stdInCourse: " + stdInCourse + "  count: " + stdStatusCountMap.get(stdInCourse)[0]);
                    System.out.println("pass" + pass);
                    //reportThisStudentToDB.add(stdInCourse);
                    System.out.println("insert to DB: " + stdInCourse + " Arrived " + l_num);
                    getStudentDetailsFromDB(stdInCourse, l_num, "Arrived");
                }
                else{
                    System.out.println("insert to DB: " + stdInCourse + " Missed " + l_num);
                    getStudentDetailsFromDB(stdInCourse, l_num, "Missed");
                }

            }
            addLecture();
            Toast.makeText(this, "Those present successfully registered!", Toast.LENGTH_SHORT).show();
        }

        //System.out.println("reportThisStudentToDB: " + reportThisStudentToDB);
        /*if(reportThisStudentToDB.size() > 0) {
            //String l_num = String.valueOf(l_number);
            for (int i = 0; i < reportThisStudentToDB.size(); i++) {
                System.out.println("insert to DB: " + reportThisStudentToDB.get(i) + " " + l_num);
                getStudentDetailsFromDB(reportThisStudentToDB.get(i), l_num, "arrive");
                //reportThisStudentToDB.contains("22223");

            }
            for (int i = 0; i < list_of_students_in_course.size(); i++) {
                System.out.println("insert to DB: " + reportThisStudentToDB.get(i) + " " + l_num);
                getStudentDetailsFromDB(reportThisStudentToDB.get(i), l_num, "arrive");
                //reportThisStudentToDB.contains("22223");

            }
            addLecture();
            Toast.makeText(this, "Those present successfully registered!", Toast.LENGTH_SHORT).show();
        }*/

    }






    public void getStudentDetailsFromDB(String id, String l_num, String status){

        final String s_id = id;
        final String fc_id = c_id;
        final String fl_num = l_num;
        final String sStatus = status;

        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_S_ACTIVITY,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {


                                try {

                                    JSONObject jsonObject = new JSONObject(response);

                                    if (!jsonObject.getBoolean("error")) {

                                        System.out.println(jsonObject.getString("s_details"));
                                        /*Date time = (Calendar.getInstance().getTime());
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy  HH:mm:ss ");
                                        String dateAndTime = simpleDateFormat.format(time.getTime());*/
                                        addStudentTo_tbl_lectures(fl_num ,fc_id, jsonObject.getString("s_id"), jsonObject.getString("s_firstName"),
                                                jsonObject.getString("s_lastName"), jsonObject.getString("s_department"), sStatus, startStrDate);



                                    } else {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                jsonObject.getString("message"),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Toast.makeText(
                                        getApplicationContext(),
                                        "Connection failed, Please try again",
                                        Toast.LENGTH_LONG
                                ).show();

                            }
                        }) {

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("s_id", s_id);

                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }






    public void addStudentTo_tbl_lectures(final String l_num, final String c_id, final String s_id, final String s_firstName,
                                          final String s_lastName, final String dep_name, final String status , final String dateAndTime){


        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_INSERT_STUDENT,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {


                                try {

                                    JSONObject jsonObject = new JSONObject(response);

                                    if (!jsonObject.getBoolean("error")) {

                                        System.out.println(jsonObject.getString("message"));



                                    } else {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                jsonObject.getString("message"),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Toast.makeText(
                                        getApplicationContext(),
                                        "Connection failed, Please try again",
                                        Toast.LENGTH_LONG
                                ).show();

                            }
                        }) {

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("l_number", l_num);
                params.put("c_id", c_id);
                params.put("s_id", s_id);
                params.put("s_firstName", s_firstName);
                params.put("s_lastName", s_lastName);
                params.put("dep_name", dep_name);
                params.put("student_status", status);
                params.put("dateAndTime", dateAndTime);

                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);


    }






    public void addLecture() {

        //mProgressBar.setVisibility(View.VISIBLE);


        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_INSERT_LEC,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                //mProgressBar.setVisibility(View.INVISIBLE);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    //If there is no error message in the JSON string
                                    if (!jsonObject.getBoolean("error")) {
                                        // NO NEED TO GET JASON ERROR
                                        removeGroup();

                                    } else {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                jsonObject.getString("message"),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }

                                } catch (JSONException e) {
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

                            }
                        }) {

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("dateAndTime", endStrDate);
                params.put("c_id", c_id);
                params.put("t_id", SharedPrefManager.getInstance(getApplicationContext()).getUserId());
                params.put("l_id", String.valueOf(l_number));

                return params;
            }
        };

        //Making a connection by singleton class to the database with stringRequest
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

    }







    public void getMaxLecture(/*float numOfCheckBeats*/) {

        //mProgressBar.setVisibility(View.VISIBLE);
        //final float beats = numOfCheckBeats;

        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, Constants.URL_T_ACTIVITY,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                //mProgressBar.setVisibility(View.INVISIBLE);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);

                                    if (!jsonObject.getBoolean("error")) {
                                        JSONArray maxLec = jsonObject.getJSONArray("l_details");
                                        int max = 0;
                                        for(int i=0; i< maxLec.length(); i++){
                                            int temp = Integer.valueOf(maxLec.get(i).toString().replaceAll("[^\\d.]", ""));
                                            if(max <= temp)
                                                max = temp;
                                        }
                                        l_number = max;
                                        l_number = l_number+1;
                                        System.out.println("==== "+l_number);


                                        //reportStudentsPresence(beats);
                                        checkPresence();



                                    } else {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                jsonObject.getString("message"),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }

                                } catch (JSONException e) {
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

                                onBtnEndClick(1);

                            }
                        }) {

            //Push parameters to Request.Method.POST
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("cc_id", c_id);



                return params;
            }
        };

        //Making a connection by singleton class to the database with stringRequest
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

    }







    public void clearStdStatusCountMap(){
        if(stdStatusCountMap!=null && list_of_students_in_course!=null) {
            for (int i=0; i<stdStatusCountMap.size();i++){
                String stdInCourse = list_of_students_in_course.get(i);
                stdInCourse = stdInCourse.replaceAll("[\\[\"\\],-]", "");
                int[] clearStatusCount = new int[1];
                clearStatusCount[0] = 0;
                stdStatusCountMap.put(stdInCourse, clearStatusCount);
            }
        }
    }





    public void resetDiscover(){

        //checkPresenceRunnable = new CheckPresenceRunnable(MyWiFiActivity.this);

        endButtonPressed = false;
        discoverAndCreateGroup();
    }







    @Override
    public void onBackPressed() {
        if(connectionStatus.getText().equals("Connected") && SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit?      " +
                            "The presence test will end!")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            backIsPressed = true;
                            onBtnEndClick(0);

                        }
                    }).create().show();
        }
        else if(wifiManager.isWifiEnabled())
            MyWiFiActivity.super.onBackPressed();
    }







    /**when a list of peers is available:
     * The onPeersAvailable() method provides you with an WifiP2pDeviceList,
     * which you can iterate through to find the peer that you want to connect to.*/
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            if(!peerList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[1];
                deviceArray = new WifiP2pDevice[1];

                /**Initialize a set of found devices for student*/
                if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null) {

                    String deviceCID, deviceTID;

                    for (WifiP2pDevice device : peerList.getDeviceList()) {
                        System.out.println("device founded: " + device.deviceName);
                        if(device.deviceName.length() == 9){
                            deviceTID = device.deviceName.substring(0, 5);
                            deviceCID = device.deviceName.substring(6, 9);
                            System.out.println("deviceTID " + deviceTID + "  deviceCID" + deviceCID);

                            if (deviceCID.equals(c_id) && deviceTID.equals(courseT_ID)) {
                                System.out.println("equal");
                                System.out.println("device.isGroupOwner(): " + device.isGroupOwner());
                                deviceNameArray[0] = deviceTID;
                                deviceArray[0] = device;

                                if (studentConnected == false && SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null) {

                                    connectToTeacherGroup(0);
                                    read_msg_box.setText("Connected to: " + deviceNameArray[0]);
                                }
                            }
                        }
                    }
                }


                /**Displays the devices names in the ListView*/
                if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null && deviceNameArray[0] != null ) {
                    adapter = new ArrayAdapter<String>(getApplicationContext(),
                            android.R.layout.simple_list_item_1, deviceNameArray);
                    listView.setAdapter(adapter);

                }
            }

            if(peers.size() == 0 && SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null){

                //Toast.makeText(getApplicationContext(), "PearList: No Device Found", Toast.LENGTH_SHORT).show();
            }
        }
    };







    WifiP2pManager.GroupInfoListener groupInfoListener = new WifiP2pManager.GroupInfoListener() {
        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group) {
            int flag = 0;
            if(group != null) {
                wifiP2pGroup = group;
                if (!group.getClientList().equals(clients)) {
                    if(clients!=null && group.getClientList().size() > clients.size()) {
                        flag = 1;
                        //System.out.println("1Clients: " + clients.size());
                        //System.out.println("group.getClientList(): " + group.getClientList().size());
                    }
                    clients.clear();
                    clients.addAll(group.getClientList());

                    groupDeviceArray = new WifiP2pDevice[group.getClientList().size()];
                    groupDevicesNamesArray = new String[group.getClientList().size()];

                    int index = 0;
                    String deviceSID, deviceCID;

                    /**Initialize a set of found devices*/
                    for (WifiP2pDevice device : group.getClientList()) {
                        for (int i = 0; i < list_of_students_in_course.size(); i++) {
                            //System.out.println("device.deviceName: " + device.deviceName);
                            //System.out.println("list_of_students_in_course.get(i): " + list_of_students_in_course.get(i));
                            String clean = list_of_students_in_course.get(i);
                            clean = clean.replaceAll("[\\[\"\\],-]", "");
                            deviceSID =device.deviceName.substring(0,5);
                            deviceCID = device.deviceName.substring(6,9);
                            //System.out.println("deviceSID " + deviceSID + "  deviceCID" + deviceCID);
                            //if (device.deviceName.equals(clean)) {
                            if (deviceSID.equals(clean) && deviceCID.equals(c_id)) {
                                //System.out.println("equal");
                                //groupDevicesNamesArray[index] = device.deviceName;
                                groupDevicesNamesArray[index] = deviceSID;
                                groupDeviceArray[index] = device;
                                index++;
                                //System.out.println("index " + index);
                                btnEnd.setVisibility(View.VISIBLE);
                                //Toast.makeText(getApplicationContext(), "group index " + index, Toast.LENGTH_SHORT).show();
                                //Toast.makeText(getApplicationContext(), "group Found " + device.deviceName, Toast.LENGTH_SHORT).show();
                            }
                        }

                    }

                    groupDeviceIsCorrectStudent = new String[index];
                    for (int i = 0; i < index; i++) {
                        if (groupDevicesNamesArray[i] != null) {
                            groupDeviceIsCorrectStudent[i] = groupDevicesNamesArray[i];
                            if(flag == 1 )
                                Toast.makeText(getApplicationContext(), groupDeviceIsCorrectStudent[i] + " connected", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(),"index " + index, Toast.LENGTH_SHORT).show();
                            //System.out.println("insert to the new arrey: " + groupDeviceIsCorrectStudent[i]);
                        }
                    }



                    /**Displays the devices names in the ListView*/
                    if (SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null ) {
                        groupAdapter = new ArrayAdapter<String>(getApplicationContext(),
                                android.R.layout.simple_list_item_1, groupDeviceIsCorrectStudent);
                        grouplistView.setAdapter(groupAdapter);


                    }
                }
                if(group.getClientList().size() == 0)
                    resetListView(1);
            }
            if(clients.size() == 0 && SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null){

                //Toast.makeText(getApplicationContext(), "GroupList: No Device Found", Toast.LENGTH_SHORT).show();
            }
        }
    };








    //Device connection information
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            wifiP2pInfo_forGroup = wifiP2pInfo;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                haveGroup = true;
                isGroupOwner = true;
            }

            if(connectionStatus.getText().equals("Device Disconnected") &&
                    SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null){
                iv_connect.setVisibility(View.INVISIBLE);
                iv_noConnect.setVisibility(View.VISIBLE);
                read_msg_box.setText("");
                //resetListView(0);
                if(endButtonPressed == false && studentConnected==false){
                    studentConnected = false;
                    numToRestartDiscovery = 0;
                    btnDiscover.setVisibility(View.INVISIBLE);
                    checkPresenceRunnable = new CheckPresenceRunnable(MyWiFiActivity.this);
                    read_msg_box.setText("Searching...");
                    discoverAndCreateGroup();
                }
                else {
                    studentConnected = false;
                    if(!read_msg_box.getText().equals("Searching...")) {
                        btnEnd.setVisibility(View.INVISIBLE);
                        if(checkPresenceRunnable != null && endButtonPressed == false)
                            checkPresenceRunnable.cancelTimer();
                    }
                    btnDiscover.setVisibility(View.VISIBLE);
                }

            }
            else if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null &&
                    connectionStatus.getText().equals("Device Disconnected") && endButtonPressed == false ){
                resetListView(1);
                discoverAndCreateGroup();
                btnEnd.setVisibility(View.INVISIBLE);
                //btnDiscover.setVisibility(View.INVISIBLE);
            }
            else if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null &&
                    connectionStatus.getText().equals("Device Disconnected") ){
                btnEnd.setVisibility(View.INVISIBLE);
            }


            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner && haveGroup == true){
                btnDiscover.setVisibility(View.INVISIBLE);
                btnEnd.setVisibility(View.VISIBLE);
                //connectionStatus.setText("Host");
                if(checkPresenceRunnable == null)
                    checkPresenceRunnable = new CheckPresenceRunnable( MyWiFiActivity.this);

            }else if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner == false) {
                btnEnd.setVisibility(View.VISIBLE);
                if(connectionStatus.getText().equals("Connected")){
                    iv_noConnect.setVisibility(View.INVISIBLE);
                    iv_connect.setVisibility(View.VISIBLE);
                    studentIsConnect = true;
                    read_msg_box.setVisibility(View.VISIBLE);
                    read_msg_box.setText("Connected to " + courseT_ID);
                    btnDiscover.setVisibility(View.INVISIBLE);
                }
                if(deviceNameArray == null) {
                    // set the t_id for the student listView
                    resetListView(2);
                }

            }

        }
    };










    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick(View v) {
        if(v == btnEnd){
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
            View mView = getLayoutInflater().inflate(R.layout.dialog_export_report, null);
            TextView mText = mView.findViewById(R.id.txt_exportreport);
            mText.setText("Do you want to finish the lecture?");
            Button mBtnOk = mView.findViewById(R.id.btn_ok);
            Button mBtnCancel = mView.findViewById(R.id.btn_cancel);

            mBuilder.setView(mView);
            final AlertDialog dialog = mBuilder.create();
            dialog.show();

            mBtnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBtnEndClick(0);
                    /*Toast.makeText(getApplicationContext(), "The report has been sent to the department",
                            Toast.LENGTH_SHORT).show();*/
                    dialog.dismiss();
                }
            });
            mBtnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }
    }













}
