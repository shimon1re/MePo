package com.example.android.mepo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class CheckPresenceRunnable extends TimerTask implements Runnable {


    Timer timer;
    String strDate;
    MyWiFiActivity mMyWifiTaskActivity;
    int afterFiveHouers = 0, state = 1;

    CheckPresenceRunnable(MyWiFiActivity sMyWiFiActivity){
        mMyWifiTaskActivity = sMyWiFiActivity;

        if(timer != null)
            timer.cancel();

        timer = new Timer();

        // לשנות למרצה, מרצה כמה דקות
        if (mMyWifiTaskActivity.isGroupOwner == true)
            timer.schedule(this, 1000, 5000); //5 second

        //Used for attempts to connect a student
        //Every 10 seconds he will try to connect again to the lecturer
        else
            timer.schedule(this, 1000, 10000); //10 second


    }

    @Override
    public void run() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy  HH:mm:ss a");
        strDate = simpleDateFormat.format(calendar.getTime());

        afterFiveHouers++;
        //Checks if it has been 5 hours since the start of the lecture so that the group is removed automatically
        if(afterFiveHouers >=60)
            state = 2;


        //After the task is done
        mMyWifiTaskActivity.handleState(this, state);
    }

    public void cancelTimer(){
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }
}
