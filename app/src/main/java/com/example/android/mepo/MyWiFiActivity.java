package com.example.android.mepo;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.nio.channels.Channel;



public class MyWiFiActivity extends AppCompatActivity {

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    Button btnOnOff, btnDiscover, btnEnd;
    ListView listView;
    ListView grouplistView;
    //TextView read_msg_box,
    TextView connectionStatus;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    List<WifiP2pDevice> clients = new ArrayList<WifiP2pDevice>();
    ArrayAdapter<String> adapter;
    ArrayAdapter<String> groupAdapter;
    String[] deviceNameArray;
    String[] groupDevicesNamesArray;
    String[] groupDeviceIsCorrectStudent;
    WifiP2pDevice[] deviceArray;
    WifiP2pDevice[] groupDeviceArray;


    static boolean haveGroup = false;
    static boolean endButtonPressed = false;
    static boolean studentConnected = false;
    String courseT_ID;
    ArrayList<String> list_of_students_in_course = new ArrayList<String>();


    //static final int MESSAGE_READ = 1;
    //EditText writeMsg;
    //ServerClass serverClass;
    //ClientClass clientClass;
    //SendReceive sendReceive;
    //int oneTimeServer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wi_fi);

        initWork();
        exqListener();



    }



    public void initWork(){
        courseT_ID =  getIntent().getStringExtra("EXTRA_TEACHER_ID");
        list_of_students_in_course = getIntent().getStringArrayListExtra("EXTRA_STUDENTS_IN_COURSE");
        btnOnOff = findViewById(R.id.onOff);
        btnDiscover = findViewById(R.id.discover);
        btnEnd = findViewById(R.id.endButton);
        /*if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null)
            btnEnd.setVisibility(View.INVISIBLE);*/
        if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null)
            listView = findViewById(R.id.peerListView);
        if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null)
            grouplistView = findViewById(R.id.peerListView);
        //read_msg_box = findViewById(R.id.readMsg);
        //read_msg_box.setText("Status:...");
        connectionStatus = findViewById(R.id.connectionStatus);
        //writeMsg = findViewById(R.id.writeMsg);
        //mServiceInfo

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (mManager != null) {
            /** register your application with the Wi-Fi P2P framework by calling initialize().
             *  This method returns a WifiP2pManager.Channel, which is used to connect your application
             *  to the Wi-Fi P2P framework.*/
            mChannel =  mManager.initialize(this, getMainLooper(), null);
            if (mChannel == null) {
                //Failure to set up connection
                Toast.makeText(this, "Failed to set up connection with wifi p2p service", Toast.LENGTH_LONG).show();
                mManager = null;
            }
        } else {
            Toast.makeText(this, "This device does not support Wi-Fi Direct", Toast.LENGTH_LONG).show();
        }

        /** You should also create an instance of your broadcast receiver with the WifiP2pManager and
         *  WifiP2pManager.Channel objects along with a reference to your activity.
         *  This allows your broadcast receiver to notify your activity of interesting events and
         *  update it accordingly. It also lets you manipulate the device's Wi-Fi state if necessary: */
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        changeDeviceName();
    }




    public void exqListener(){

        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    btnOnOff.setText("ON");
                }else {
                    wifiManager.setWifiEnabled(true);
                    btnOnOff.setText("OFF");
                }
            }
        });
        //btnDiscover.performClick();
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endButtonPressed = false;
                if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null ) {
                    //listView.removeAllViewsInLayout();
                    //Toast.makeText(getApplicationContext(), "listView.removeAllViewsInLayout 1", Toast.LENGTH_SHORT).show();
                }
                else if(!connectionStatus.getText().equals("Host") && !connectionStatus.getText().equals("Connected") ) {
                    //grouplistView.removeAllViewsInLayout();
                }
                discoverAndCreateGroup();
            }
        });

        //obtain a peer from the WifiP2pDeviceList
        /*if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null){
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    if (deviceNameArray[0] != null ) {
                        connectToTeacherGroup(position);
                    }

                }
            });
        }*/


        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endButtonPressed = true;
                if(!connectionStatus.getText().equals("Device Disconnected")) {
                    removeGroup();
                    changeDeviceName();
                }

            }
        });
    }





    public void connectToTeacherGroup(int position){
        final WifiP2pDevice device = deviceArray[position];
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null && connectionStatus.getText().equals("Device Disconnected")
                 && device.isGroupOwner() == true && deviceNameArray[0] != null) {
            changeDeviceName();
            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    studentConnected = true;
                    Toast.makeText(getApplicationContext(), "Connected to: " + deviceNameArray[0], Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(getApplicationContext(), "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }





    public void discoverAndCreateGroup(){

        if(mManager != null && wifiManager.isWifiEnabled()) {
            if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null &&
                    (!connectionStatus.getText().equals("Host") && !connectionStatus.getText().equals("Connected")) /*&& haveGroup == false*/) {

                mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Device is ready to accept incoming connections from peers.
                        Toast.makeText(getApplicationContext(), "Group created by: " + SharedPrefManager.getInstance(getApplicationContext()).getUserFName(), Toast.LENGTH_SHORT).show();
                        haveGroup = true;

                    }

                    @Override
                    public void onFailure(int reason) {

                        Toast.makeText(getApplicationContext(), "Group creation failed. Retry.",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            }
            else if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null /*&& !read_msg_box.getText().equals("Discovery Started")*/){
                /**To discover peers that are available to connect to, call discoverPeers() to detect
                 *  available peers that are in range. The call to this function is asynchronous and a
                 *  success or failure is communicated to your application with onSuccess() and onFailure()*/
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    /**This method only notifies that the discovery process succeeded and does not
                     *  provide any information about the actual peers that it discovered, if any*/
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Discovery Started...", Toast.LENGTH_LONG).show();
                        //if(read_msg_box !=null)
                          //  read_msg_box.setText("Discovery Started");

                    }

                    @Override
                    public void onFailure(int reason) {
                        //read_msg_box.setText("Discovery Starting Failed ");
                        Toast.makeText(getApplicationContext(),
                                "Discovery Failed : " + getDiscoveryFailureReasonCode(reason),
                                Toast.LENGTH_SHORT).show();
                        System.out.println("Discovery Failed - reason: " + reason);

                    }
                });
                /**If the discovery process succeeds and detects peers, the system broadcasts the
                 *  WIFI_P2P_PEERS_CHANGED_ACTION intent, which you can listen for in a
                 *  broadcast receiver to obtain a list of peers.*/
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
        if(clients!=null)
            clients.clear();
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

                Toast.makeText(getApplicationContext(), "Group removed by: " +
                        SharedPrefManager.getInstance(getApplicationContext()).getUserFName(), Toast.LENGTH_SHORT).show();
                haveGroup = false;
                //clients.clear();
                if (SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null ){
                    //grouplistView.removeAllViewsInLayout();
                }
                else {
                    studentConnected = false;
                    //listView.removeAllViewsInLayout();
                    //Toast.makeText(getApplicationContext(), "listView.removeAllViewsInLayout 2", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int reason) {
                    Toast.makeText(getApplicationContext(), "Group remove failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });


        //String msg = writeMsg.getText().toString().trim();
        //sendReceive.write(msg.getBytes());
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
            arglist[1] = SharedPrefManager.getInstance(getApplicationContext()).getUserId();
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




    /**when a list of peers is available:
     * The onPeersAvailable() method provides you with an WifiP2pDeviceList,
     * which you can iterate through to find the peer that you want to connect to.*/
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                //groupDeviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                //groupDevicesNamesArray = new String[peerList.getDeviceList().size()];

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;

                /**Initialize a set of found devices for student*/
                if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null) {

                    for (WifiP2pDevice device : peerList.getDeviceList()) {
                        if(device.deviceName.equals(courseT_ID)) {
                            System.out.println("equal");
                            System.out.println("device.isGroupOwner(): " + device.isGroupOwner());
                            deviceNameArray[index] = device.deviceName;
                            deviceArray[index] = device;
                            index++;
                            if(studentConnected == false && SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null) {
                                Toast.makeText(getApplicationContext(), "peerList Found " + device.deviceName, Toast.LENGTH_LONG).show();
                                connectToTeacherGroup(0);
                            }
                        }
                    }
                }
                /**Initialize a set of found devices for teacher*/
                /*else{
                    for (WifiP2pDevice device : peerList.getDeviceList()) {
                        System.out.println("list_of_students_in_course.size()" + list_of_students_in_course.size());
                        System.out.println("group.getClientList().size(): " + peerList.getDeviceList().size());
                        for (int i = 0; i < list_of_students_in_course.size(); i++) {
                            System.out.println("device.deviceName: " + device.deviceName);
                            System.out.println("list_of_students_in_course.get(i): " + list_of_students_in_course.get(i));
                            String clean = list_of_students_in_course.get(i);
                            clean = clean.replaceAll("[\\[\"\\],-]", "");
                            if (device.deviceName.equals(clean)) {
                                System.out.println("equal");
                                groupDevicesNamesArray[index] = device.deviceName;
                                groupDeviceArray[index] = device;
                                index++;
                                Toast.makeText(getApplicationContext(), "group in peerList Found " + device.deviceName, Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                }*/

                /**Displays the devices names in the ListView*/
                if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null && index > 0 ) {
                    adapter = new ArrayAdapter<String>(getApplicationContext(),
                            android.R.layout.simple_list_item_1, deviceNameArray);
                    listView.setAdapter(adapter);

                }
            }

            if(peers.size() == 0 && SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null){
                if(adapter != null) {
                    //listView.removeAllViewsInLayout();
                }
                //Toast.makeText(getApplicationContext(), "PearList: No Device Found", Toast.LENGTH_SHORT).show();

            }
        }
    };




    WifiP2pManager.GroupInfoListener groupInfoListener = new WifiP2pManager.GroupInfoListener() {
        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group) {
            if(group != null) {
                if (!group.getClientList().equals(clients)) {
                    if(clients!=null) {
                        System.out.println("1Clients: " + clients.size());
                        for (WifiP2pDevice device : clients)
                            System.out.println("1device.deviceName " + device.deviceName);
                    }
                    clients.clear();
                    clients.addAll(group.getClientList());

                    groupDeviceArray = new WifiP2pDevice[group.getClientList().size()];
                    groupDevicesNamesArray = new String[group.getClientList().size()];

                    int index = 0;

                    /**Initialize a set of found devices*/
                    for (WifiP2pDevice device : clients) {
                        System.out.println("list_of_students_in_course.size()" + list_of_students_in_course.size());
                        System.out.println("group.getClientList().size(): " + group.getClientList().size());
                        for (int i = 0; i < list_of_students_in_course.size(); i++) {
                            System.out.println("device.deviceName: " + device.deviceName);
                            System.out.println("list_of_students_in_course.get(i): " + list_of_students_in_course.get(i));
                            String clean = list_of_students_in_course.get(i);
                            clean = clean.replaceAll("[\\[\"\\],-]", "");
                            if (device.deviceName.equals(clean)) {
                                System.out.println("equal");
                                groupDevicesNamesArray[index] = device.deviceName;
                                groupDeviceArray[index] = device;
                                index++;
                                System.out.println("index " + index);
                                Toast.makeText(getApplicationContext(), "group index " + index, Toast.LENGTH_SHORT).show();
                                Toast.makeText(getApplicationContext(), "group Found " + device.deviceName, Toast.LENGTH_SHORT).show();
                            }
                        }

                    }

                    groupDeviceIsCorrectStudent = new String[index];
                    for (int i = 0; i < index; i++) {
                        if (groupDevicesNamesArray[i] != null) {
                            groupDeviceIsCorrectStudent[i] = groupDevicesNamesArray[i];
                            Toast.makeText(getApplicationContext(), groupDeviceIsCorrectStudent[i] + " connected", Toast.LENGTH_SHORT).show();
                            System.out.println("insert to the new arrey: " + groupDeviceIsCorrectStudent[i]);
                        }
                    }
                    //deviceListNameArray = new ArrayList<>(Arrays.asList(groupDeviceIsCorrectStudent));


                    /**Displays the devices names in the ListView*/
                    if (SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null && index > 0) {
                        //System.out.println(groupDeviceIsCorrectStudent);
                        //grouplistView.removeAllViewsInLayout();
                        groupAdapter = new ArrayAdapter<String>(getApplicationContext(),
                                android.R.layout.simple_list_item_1, groupDeviceIsCorrectStudent);
                        grouplistView.setAdapter(groupAdapter);

                    }
                }
            }
            if(clients.size() == 0 && SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null){
                if(groupAdapter != null) {
                    //grouplistView.removeAllViewsInLayout();
                }
                //Toast.makeText(getApplicationContext(), "GroupList: No Device Found", Toast.LENGTH_SHORT).show();
            }
        }
    };





    //Device connection information
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            //if(read_msg_box != null)
              //  read_msg_box.setText(" ");

            if(connectionStatus.getText().equals("Device Disconnected") && SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null ){
                //Toast.makeText(getApplicationContext(), deviceNameArray[0] + " 1", Toast.LENGTH_SHORT).show();
                studentConnected = false;
                /*mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "cancel the connect", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {

                    }
                });*/
                removeGroup();
            }
            else if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null &&
                    connectionStatus.getText().equals("Device Disconnected") ){
                //grouplistView.removeAllViewsInLayout();
            }else if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null && clients !=null){
                System.out.println("Clients: " + clients.size());
                for(WifiP2pDevice device : clients)
                    System.out.println("device.deviceName " + device.deviceName);
            }

            Toast.makeText(getApplicationContext(),connectionStatus.getText() , Toast.LENGTH_SHORT).show();


            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner && haveGroup == true){
                //oneTimeServer++;
                //System.out.println("serverClass");
                connectionStatus.setText("Host");
                //serverClass = new ServerClass();
                //serverClass.start();
            }else if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner == false) {
                //Toast.makeText(getApplicationContext(), "clientClass", Toast.LENGTH_SHORT).show();
                //System.out.println("clientClass");
                connectionStatus.setText("Client");
                //clientClass = new ClientClass(groupOwnerAddress);
                //clientClass.start();
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
























//////////////////////////////////////////////////////// Thread zone /////////////////////////////////////////////////////////////////



    /*Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff,0,msg.arg1);
                    read_msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });*/




    /*
     * Create a server socket and wait for client connections. This
     * call blocks until a connection is accepted from a client
     */
    /*public class ServerClass extends Thread{
        Socket socket = null;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(); // <-- create an unbound socket first
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(8888)); // <-- now bind it
                //serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    serverSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt){
            socket = skt;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while(socket != null){
                try {
                    bytes = inputStream.read(buffer);
                    if(bytes > 0){
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/



    /*public class ClientClass extends Thread{
        Socket socket = null;
        String hostAdd;

        public ClientClass(InetAddress hosAddress){
            hostAdd = hosAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                /**
                 * Create a client socket with the host,
                 * port, and timeout information.
                 */
                /*socket.connect(new InetSocketAddress(hostAdd,8888),500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
}