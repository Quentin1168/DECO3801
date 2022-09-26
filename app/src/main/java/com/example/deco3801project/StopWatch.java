package com.example.deco3801project;

import android.app.Activity;
import android.os.Handler;

public class StopWatch extends Activity{
    private int seconds = 0;
    private int prevSeconds;
    private boolean running;


    protected void onCreate() {
        runTimer();
    }

    public void reset() {
        running = false;
        prevSeconds = seconds;
        seconds = 0;

    }

    public void start() {
        running = true;
    }

    public void stop()
    {
        running = false;
    }

    public int getPrevSeconds() {
        return prevSeconds;
    }

    public void runTimer() {
        start();
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (running) {
                    seconds++;
                }

                handler.postDelayed(this, 1000);
            }
        });


    }
}
