package com.example.android.mepo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class CheckPresenceRunnable extends TimerTask implements Runnable {


    Timer timer;
    String strDate;
    MyWiFiActivity mMyWifiTaskActivity;

    CheckPresenceRunnable(MyWiFiActivity sMyWiFiActivity){
        mMyWifiTaskActivity = sMyWiFiActivity;

        if(timer != null)
            timer.cancel();

        timer = new Timer();
        //timer.schedule(this, 1000); //1 second
        // לשנות בין סטודנט למרצה, סטודנט 5 שניות, מרצה כמה דקות
        if (mMyWifiTaskActivity.isGroupOwner == true)
            timer.schedule(this, 1000, 5000); //5 second
        else
            timer.schedule(this, 1000, 10000); //10 second
    }

    @Override
    public void run() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy  HH:mm:ss a");
        strDate = simpleDateFormat.format(calendar.getTime());




        //After the task is done
        mMyWifiTaskActivity.handleState(this, 1);
    }

    public void cancelTimer(){
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }
}
