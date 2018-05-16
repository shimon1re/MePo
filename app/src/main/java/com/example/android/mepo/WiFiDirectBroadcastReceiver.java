package com.example.android.mepo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;
import android.net.wifi.p2p.WifiP2pManager.Channel;

import static com.example.android.mepo.MyWiFiActivity.isGroupOwner;

//import java.nio.channels.Channel;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MyWiFiActivity mActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       MyWiFiActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                Toast.makeText(context, "Wifi is ON", Toast.LENGTH_SHORT).show();
                mActivity.btnOnOff.setText("Wifi: ON");
            } else {
                // Wi-Fi P2P is not enabled
                Toast.makeText(context, "Wifi is OFF", Toast.LENGTH_SHORT).show();
                //Preform only on teacher
                if(mActivity.haveGroup == false && mActivity.listView == null) {
                    mActivity.wifiManager.setWifiEnabled(true);
                }
                else
                    mActivity.btnOnOff.setText("ON");
            }
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // When your application receives the WIFI_P2P_PEERS_CHANGED_ACTION intent,
            // you can request a list of the discovered peers with requestPeers()
            // Call WifiP2pManager.requestPeers() to get a list of current peers available.
            // The requestPeers() method is also asynchronous and can notify your activity when a
            // list of peers is available with onPeersAvailable(),
            // which is defined in the WifiP2pManager.PeerListListener interface.
            if(mManager != null){
                System.out.println("check peers");
                mManager.requestPeers(mChannel, mActivity.peerListListener);
            }
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Respond to new connection or disconnections
            if(mManager == null){
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            System.out.println("networkInfo.getDetailedState(): " + networkInfo.getDetailedState());
            System.out.println("networkInfo.isConnected(): " + networkInfo.isConnected());
            if(networkInfo.isConnected()){
                mActivity.connectionStatus.setText("Connected");
                mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener);
                if(isGroupOwner) {
                    //mActivity.connectionStatus.setText("Host");
                    mManager.requestGroupInfo(mChannel, mActivity.groupInfoListener);
                }

            }
            else {
                mActivity.connectionStatus.setText("Device Disconnected");
                mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener);

            }
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }


    }


}
