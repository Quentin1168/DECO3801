package com.example.deco3801project;

import android.app.Activity;
import android.os.Handler;
import java.util.Calendar;
import java.util.Date;

public class StopWatch extends Activity {

    private Date currentTime;

    private Date previousTime;

    public StopWatch() {
        currentTime = null;
        previousTime = null;
    }

    public void setCurrentTime() {
        currentTime =  Calendar.getInstance().getTime();
    }

    public int getTimeDifference() {
        previousTime = currentTime;
        setCurrentTime();
        long diff = currentTime.getTime() - previousTime.getTime();
        int seconds = (int) (diff/ 1000);
        if (seconds > 15) {
            return 0;
        }
        return seconds;
    }

}

