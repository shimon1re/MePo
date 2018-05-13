package com.example.android.mepo;

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
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
    TextView read_msg_box, connectionStatus;
    //EditText writeMsg;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    List<WifiP2pDevice> clients = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    String[] groupDevicesNamesArray;
    WifiP2pDevice[] deviceArray;
    WifiP2pDevice[] groupDeviceArray;

    //static final int MESSAGE_READ = 1;
    static boolean haveGroup = false;

    //ServerClass serverClass;
    //ClientClass clientClass;
    //SendReceive sendReceive;
    int oneTimeServer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wi_fi);

        initWork();
        exqListener();



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


        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mManager != null) {
                    /**To discover peers that are available to connect to, call discoverPeers() to detect
                     *  available peers that are in range. The call to this function is asynchronous and a
                     *  success or failure is communicated to your application with onSuccess() and onFailure()*/
                    mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        /**This method only notifies that the discovery process succeeded and does not
                         *  provide any information about the actual peers that it discovered, if any*/
                        public void onSuccess() {
                            connectionStatus.setText("Discovery Started");
                            if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null && haveGroup == false) {
                                mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        // Device is ready to accept incoming connections from peers.
                                        Toast.makeText(getApplicationContext(), "Group created by: " + SharedPrefManager.getInstance(getApplicationContext()).getUserFName(), Toast.LENGTH_SHORT).show();
                                        haveGroup = true;
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        Toast.makeText(getApplicationContext(), "P2P group creation failed. Retry.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(int reason) {
                            connectionStatus.setText("Discovery Starting Failed");
                        }
                    });
                    /**If the discovery process succeeds and detects peers, the system broadcasts the
                     *  WIFI_P2P_PEERS_CHANGED_ACTION intent, which you can listen for in a
                     *  broadcast receiver to obtain a list of peers.*/
                }
            }
        });

        //obtain a peer from the WifiP2pDeviceList
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final WifiP2pDevice device = deviceArray[position];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null) {
                    mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                            //cancel the connect from teacher to check again after 10 minuets.
                            /*mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {

                                @Override
                                public void onSuccess() {
                                    Toast.makeText(getApplicationContext(), "Connect canceled from " + device.deviceName, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(int reason) {

                                }
                            });*/

                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(getApplicationContext(), "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        });

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(), "Group removed by: " +
                                    SharedPrefManager.getInstance(getApplicationContext()).getUserFName(), Toast.LENGTH_SHORT).show();
                            haveGroup = false;
                            clients.clear();
                            grouplistView.removeAllViewsInLayout();
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
        });
    }




    public void discoverAndCreateGroup(){
        if(mManager != null) {
            /**To discover peers that are available to connect to, call discoverPeers() to detect
             *  available peers that are in range. The call to this function is asynchronous and a
             *  success or failure is communicated to your application with onSuccess() and onFailure()*/
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                /**This method only notifies that the discovery process succeeded and does not
                 *  provide any information about the actual peers that it discovered, if any*/
                public void onSuccess() {
                    connectionStatus.setText("Discovery Started");
                    if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null) {
                        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                // Device is ready to accept incoming connections from peers.
                                Toast.makeText(getApplicationContext(), "Group created by: " + SharedPrefManager.getInstance(getApplicationContext()).getUserFName(), Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onFailure(int reason) {
                                Toast.makeText(getApplicationContext(), "P2P group creation failed. Retry.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(int reason) {
                    connectionStatus.setText("Discovery Starting Failed");
                }
            });
            /**If the discovery process succeeds and detects peers, the system broadcasts the
             *  WIFI_P2P_PEERS_CHANGED_ACTION intent, which you can listen for in a
             *  broadcast receiver to obtain a list of peers.*/
        }
    }



    public void initWork(){
        btnOnOff = findViewById(R.id.onOff);
        btnDiscover = findViewById(R.id.discover);
        btnEnd = findViewById(R.id.endButton);
        if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null)
            btnEnd.setVisibility(View.INVISIBLE);
        listView = findViewById(R.id.peerListView);
        grouplistView = findViewById(R.id.peerListView);
        read_msg_box = findViewById(R.id.readMsg);
        connectionStatus = findViewById(R.id.connectionStatus);
        //writeMsg = findViewById(R.id.writeMsg);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;

                /**Initialize a set of found devices*/
                for (WifiP2pDevice device : peerList.getDeviceList())
                {
                    deviceNameArray[index] = device.deviceName;
                    //deviceNameArray[index] = SharedPrefManager.getInstance(getApplicationContext()).getUserFName();
                    deviceArray[index] = device;
                    index++;
                }

                /**Displays the devices names in the ListView*/
                if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                            android.R.layout.simple_list_item_1, deviceNameArray);
                    listView.setAdapter(adapter);
                }
            }

            if(peers.size() == 0){
                Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_SHORT).show();
            }
        }
    };

    WifiP2pManager.GroupInfoListener groupInfoListener = new WifiP2pManager.GroupInfoListener() {
        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group) {
            if(!group.getClientList().equals(clients)){
                clients.clear();
                clients.addAll(group.getClientList());

                groupDeviceArray = new WifiP2pDevice[group.getClientList().size()];
                groupDevicesNamesArray = new String[group.getClientList().size()];
                int index = 0;

                /**Initialize a set of found devices*/
                for (WifiP2pDevice device : group.getClientList())
                {
                    groupDevicesNamesArray[index] = device.deviceName;
                    //deviceNameArray[index] = SharedPrefManager.getInstance(getApplicationContext()).getUserFName();
                    groupDeviceArray[index] = device;
                    index++;
                }
                /**Displays the devices names in the ListView*/
                if(SharedPrefManager.getInstance(getApplicationContext()).getUserIsStudent() == null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                            android.R.layout.simple_list_item_1, groupDevicesNamesArray);
                    grouplistView.setAdapter(adapter);
                }
            }
            if(clients.size() == 0){
                Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_SHORT).show();
            }
        }
    };



    //Device connection information
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            //System.out.println("oneTimeServer: " + oneTimeServer);

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




    /**
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
