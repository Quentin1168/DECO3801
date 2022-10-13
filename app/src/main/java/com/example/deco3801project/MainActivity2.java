package com.example.deco3801project;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;

// https://github.com/tangqi92/WaveLoadingView for the code of wave function
import me.itangqi.waveloadingview.WaveLoadingView;

public class MainActivity2 extends AppCompatActivity {
    /**
     * The application's SharedPreferences, containing:
     * recommendedIntake: The user's recommended water intake based on their age and gender
     *                    (or set manually).
     * currentAmountLeftToDrink: The amount of water left to drink out of the recommendedIntake.
     * today: The current day of the year, which will be used to reset the WaveLoadingView and its
     *        TextViews every day.
     * running: If the user decides to use the NFC tag timer feature, this will be used to measure
     *          their water intake based on how long the user is taking to drink, where 0 sets the
     *          timer and 1 ends it.
     */
    protected SharedPreferences pref;

    private EditText drinkInput;
    private Button continueButton;
    private int recommendedIntake;
    SharedPreferences checkAppStart = null;    // Check app start times

    Context context = this;
    StopWatch timer =  new StopWatch();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAppStart = getSharedPreferences("com.example.deco3801project", MODE_PRIVATE);
        // Slider Intro
        if (checkAppStart.getBoolean("first-run", true)) {   // if it is first run
            Intent i = new Intent(getApplicationContext(), MainActivity3.class);
            checkAppStart.edit().putBoolean("first-run", false).apply();    // make param to be false
            startActivity(i);
            finish();
        }

        // Encryption
        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.notify();
        }

        try {
            assert masterKeyAlias != null;
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
            e.notify();
        }

        setContentView(R.layout.activity_main2);
        recommendedIntake = pref.getInt("recommendedIntake", 0);

        // Create a new sharedPref called currentAmountLeftToDrink that will be edited later on
        SharedPreferences.Editor edit = pref.edit();

        // Check if the current day is greater than the current day stored, if one exists
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_YEAR) > pref.getInt("today", 0)) {
            edit.putInt("currentAmountLeftToDrink", recommendedIntake);
            resetWaterIntake();
        }

        if (!pref.contains("currentAmountLeftToDrink")) {
            if (recommendedIntake > 0) {
                edit.putInt("currentAmountLeftToDrink", recommendedIntake);
            }
        }

        // Store the current day
        edit.putInt("today", calendar.get(Calendar.DAY_OF_YEAR));
        edit.apply();

        // The total amount of water to drink initially
        TextView amountToDrink = findViewById(R.id.amountToDrink);
        amountToDrink.setText(String.valueOf(pref.getInt("currentAmountLeftToDrink", 0)));

        // The percentage of the amount left to drink
        TextView amountToDrinkPercent = findViewById(R.id.amountToDrinkPercentage);
        amountToDrinkPercent.setText(String.valueOf((int) calculateRemainingPercentage()).concat("%"));

        drinkInput = findViewById(R.id.drinkInput);
        continueButton = findViewById(R.id.logButton);
        drinkInput.addTextChangedListener(continueTextWatcher);

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(100); // Sets the water height to 100%

        WaveLoadingView waveLoadingView = findViewById(R.id.WaveLoadingView);

        // Disable the seekBar
        seekBar.setEnabled(false);

        // Make sure the WaveLoadingView's water level changes accordingly
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                waveLoadingView.setProgressValue(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Set the Notification to pop up at regular intervals
        setNotificationToIntervals();
    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences.Editor edit = pref.edit();

        // Check if the current day is greater than the current day stored, if one exists
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_YEAR) > pref.getInt("today", 0)) {
            edit.putInt("currentAmountLeftToDrink", recommendedIntake);
            resetWaterIntake();
        }

        // Store the current day also
        edit.putInt("today", calendar.get(Calendar.DAY_OF_YEAR));
        edit.apply();
    }

    private final TextWatcher continueTextWatcher = new TextWatcher() {
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
            double percentageRemaining = calculateRemainingPercentage();
            seekBar.setProgress((int) percentageRemaining, true);
            System.out.println(seekBar.getProgress());
        }
    };

    /**
     * This function logs the manually-input water intake in drinkInput in activity_main2.xml.
     * This changes the WaveLoadingView's water leve and its TextViews' texts accordingly,
     * and updates the currentAmountLeftToDrink sharedPreference.
     * @param v The logButton in activity_main2.xml.
     */
    public void logIntake(View v) {
        String drinkStr = drinkInput.getText().toString();
        int drink = 0;
        if (!drinkStr.equals("")) {
            drink = Integer.parseInt(drinkStr); // Get amount to be drunk
        }

        int currentAmountLeftToDrink = pref.getInt("currentAmountLeftToDrink", 0);

        try {
            currentAmountLeftToDrink = subtractIntake(drink);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
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
        double percentageRemaining = calculateRemainingPercentage();
        amountToDrinkPercent.setText(String.valueOf((int) percentageRemaining).concat("%"));
        drinkInput.setText("");
    }

    /**
     * This helper function calculates the percentage of the water left to drink out of the user's
     * recommended water intake.
     * @return The percentage of the water left to drink out of the user's recommended water intake.
     */
    protected double calculateRemainingPercentage() {
        int currentAmountLeftToDrink = pref.getInt("currentAmountLeftToDrink", 0);
        int recommendedIntake = pref.getInt("recommendedIntake", 0);
        return 100 * ((double) currentAmountLeftToDrink / (double) recommendedIntake);
    }

    /**
     * This helper function subtracts the amount of water drunk by the user from the user's current
     * amount left to drink. If amountDrunk > currentAmountLeftToDrink, then 0 will be returned.
     * @param amountDrunk The amount the user has just drunk.
     * @return If amountDrunk > currentAmountLeftToDrink, return 0.
     *         Otherwise, return currentAmountLeftToDrink - amountDrunk.
     */
    protected int subtractIntake(int amountDrunk) {
        int currentAmountLeftToDrink = pref.getInt("currentAmountLeftToDrink", 0);
        if (amountDrunk > currentAmountLeftToDrink) {
            return 0;
        }
        return (currentAmountLeftToDrink - amountDrunk);
    }

    /**
     * This function will create the app's Notifications and set them to pop up in 1 hour intervals
     * between 8 AM and 8 PM.
     */
    private void setNotificationToIntervals() {
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

        // Set the window to be from 8 AM to 8 PM
        alarmManager.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP, calendar.getTimeInMillis(),
                12 * 60 * 60 * 1000, pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                60 * 60 * 1000, pendingIntent); // make the alarm repeat every hour
    }

    /**
     * This function resets the water intake TextViews in the middle of the WaveLoadingView in
     * activity_main2.xml.
     */
    private void resetWaterIntake() {
        int recommendedIntake = pref.getInt("recommendedIntake", 0);
        String maxPercentage = "100%";

        // Get the TextViews' texts
        TextView amountToDrink = findViewById(R.id.amountToDrink);
        TextView amountToDrinkPercent = findViewById(R.id.amountToDrinkPercentage);

        // Update the TextViews' texts appropriately
        amountToDrink.setText(recommendedIntake);
        amountToDrinkPercent.setText(maxPercentage);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        measureTime(intent);
    }

    private void measureTime(Intent intent) {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            int running = pref.getInt("running", 0);
            Toast.makeText(context, String.valueOf(running), Toast.LENGTH_LONG).show();
            SharedPreferences.Editor edit = pref.edit();
            if (running == 0) {
                timer.runTimer();

                edit.putInt("running", 1);
                edit.apply();
            } else if (running == 1) {
                timer.reset();
                edit.putInt("running", 0);
                edit.apply();
                TextView time = findViewById(R.id.Or);
                time.setText(String.valueOf(timer.getPrevSeconds()));
            }

        }
    }

    public void handleNFCButton (View v) { // This function runs when NFC button is pressed
        Intent intent = new Intent(MainActivity2.this, WriteNFC.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}

