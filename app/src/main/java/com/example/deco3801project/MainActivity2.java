package com.example.deco3801project;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * See https://github.com/tangqi92/WaveLoadingView for the code of wave function
 * external library
 */
import me.itangqi.waveloadingview.WaveLoadingView;



public class MainActivity2 extends AppCompatActivity {

    SharedPreferences pref;
    private EditText drinkInput;
    private Button continueButton;
    private int recommendedIntake;

    public static final String Error_Detected = "No NFC Tag Detected";
    public static final String Write_Sucess = "Text Successfully Written";
    public static final String Write_Error = "Error During Writing, Try Again";

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    Context context;
    int running = 0;
    StopWatch timer = new StopWatch();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get current date in format dd
        DateFormat df = new SimpleDateFormat("dd");
        String date = df.format(Calendar.getInstance().getTime());
        Log.d("date", date);

        // Encryption
        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            pref = EncryptedSharedPreferences.create(
                    "secret_shared_prefs",
                    masterKeyAlias,
                    getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main2);
        recommendedIntake = pref.getInt("recommendedIntake", 0);

        // Create a new sharedPref called amountLeftToDrink that will be edited later on
        SharedPreferences.Editor edit = pref.edit();
        edit.putInt("currentAmountLeftToDrink", recommendedIntake);

        edit.apply();

        // writingTagFilters = new IntentFilter[] { tagDetected };
        // The total amount of water to drink initially
        TextView amountToDrink = findViewById(R.id.amountToDrink);
        amountToDrink.setText(String.valueOf(recommendedIntake));

        // The percentage of the amount left to drink
        TextView amountToDrinkPercent = findViewById(R.id.amountToDrinkPercentage);
        amountToDrinkPercent.setText("0%");

        drinkInput = findViewById(R.id.drinkText);
        continueButton = findViewById(R.id.add_button);
        drinkInput.addTextChangedListener(continueTextWatcher);



        // Build and call the Notification for testing
        Notification notification = buildNotification();
        callNotification(notification);
        setNotificationToIntervals(notification);
        measureTime(getIntent());
    }





    private TextWatcher continueTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // Whenever the Drink input is empty, the Continue button will be disabled
            String drinkInputText = drinkInput.getText().toString().trim();
            continueButton.setEnabled(!drinkInputText.isEmpty());
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void afterTextChanged(Editable editable) {
            SeekBar seekBar = findViewById(R.id.seekBar);
            int currentAmountLeftToDrink = pref.getInt("currentAmountLeftToDrink", 0);
            double percentageRemaining = calculate_remaining_percentage(currentAmountLeftToDrink);
            seekBar.setProgress((int) percentageRemaining, true);
        }
    };

    public void handleText(View v) { // This method run when button is clicked
        String drinkStr = drinkInput.getText().toString();
        int drink = 0;
        if (!drinkStr.equals("")) {
            drink = Integer.parseInt(drinkStr); // Get amount to be drunk
        }

        int currentAmountLeftToDrink = pref.getInt("currentAmountLeftToDrink", 0);

        try {
            currentAmountLeftToDrink = subtract_intake(currentAmountLeftToDrink, drink);
        } catch (IllegalArgumentException e) {

        }
        // Update the current amount left to drink
        SharedPreferences.Editor edit = pref.edit();
        edit.putInt("currentAmountLeftToDrink", currentAmountLeftToDrink);
        edit.apply();

        // Get the TextViews' texts
        TextView amountToDrink = findViewById(R.id.amountToDrink);
        TextView amountToDrinkPercent = findViewById(R.id.amountToDrinkPercentage);

        // Update the TextViews' texts appropriately
        amountToDrink.setText(String.valueOf(currentAmountLeftToDrink));
        double percentageRemaining = calculate_remaining_percentage(currentAmountLeftToDrink);
        amountToDrinkPercent.setText(String.valueOf(percentageRemaining).concat("%"));
        drinkInput.setText("");
    }

    protected double calculate_remaining_percentage(int currentAmountLeftToDrink) {
        recommendedIntake = pref.getInt("recommendedIntake", 0);
        double percentage = 100 * ((double) currentAmountLeftToDrink / (double) recommendedIntake);
        return percentage;
    }

    protected int subtract_intake(int currentAmountLeftToDrink, int drink)
            throws IllegalArgumentException {
        if (drink > currentAmountLeftToDrink) {
            throw new IllegalArgumentException("Drink cannot be larger than intake");
        }

        return (currentAmountLeftToDrink - drink);
    }

    private Notification buildNotification() {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.drawable.ic_baseline_local_drink_24)
                .setContentTitle("Drink Water!")
                .setContentText("You should drink some water soon if you haven't yet!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setVibrate( new long []{ 1000 , 1000 , 1000 , 1000})
                .setAutoCancel(true);

        return notificationBuilder.build();
    }

    private void callNotification(Notification notification) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that must be defined
        notificationManager.notify(1, notification);
    }

    private void setNotificationToIntervals(Notification notification) {
        // Set the Notification created to fire up at regular intervals
        Intent intent = new Intent(this, HourlyReceiver.class);

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm to start at 8 AM and to repeat every hour afterwards until 8 PM
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // If it is past 8 PM, set the alarm for tomorrow instead
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 20) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        calendar.set(Calendar.HOUR_OF_DAY, 8); // set the time to 8 AM

        // set the window to be from 8 AM to 8 PM
        alarmManager.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP, calendar.getTimeInMillis(),
                12 * 60 * 60 * 1000, pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                60 * 60 * 1000, pendingIntent); // make the alarm repeat every hour (every second for testing)

        // TODO: Make sure the Notification Alarm can handle device reboots
    }

    // Handles NFC read
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        measureTime(intent);

    }

    private void measureTime(Intent intent) {
        Toast.makeText(context, "This is my Toast message!",
                Toast.LENGTH_LONG).show();
        if (running == 0) {
            timer.runTimer();
            running = 1;
        }
        else if (running == 1) {
            timer.reset();
            running = 0;
            TextView time = findViewById(R.id.textView2);
            time.setText(String.valueOf(timer.getPrevSeconds()));
        }
    }

}

