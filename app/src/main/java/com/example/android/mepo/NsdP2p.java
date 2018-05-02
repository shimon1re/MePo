package com.example.android.mepo;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class NsdP2p {
    //private Object mNsdManager;

    //final private int SERVER_PORT = 0;

    //@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void registerService(int port) {
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName("NsdMepo");
        serviceInfo.setServiceType("_NsdMepo._tcp");
        serviceInfo.setPort(port);


        NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.
                System.out.println("Registration failed!");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
                System.out.println("Unregistration failed!");
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                String mServiceName = NsdServiceInfo.getServiceName();
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
            }




        };

        /*mNsdManager = Context.getSystemService(Context.NSD_SERVICE);

        mNsdManager.registerService(
                serviceInfo,NsdManager.PROTOCOL_DNS_SD,mRegistrationListener);*/

        initializeServerSocket();
    }
    //registerService(80);



    public void initializeServerSocket() {
        // Initialize a server socket on the next available port.
        try {
            ServerSocket mServerSocket = new ServerSocket(0);
            // Store the chosen port.
            Object mLocalPort = mServerSocket.getLocalPort();
        }catch (IOException e){
            e.printStackTrace();
        }


    }

















    /*@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void startRegistration() {
        //  Create a string map containing information about your service.
        Map record = new HashMap();
        record.put("listenport", String.valueOf(SERVER_PORT));
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mManager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
            }
        });
    }*/

}


