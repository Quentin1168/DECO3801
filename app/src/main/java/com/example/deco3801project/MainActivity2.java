package com.example.deco3801project;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.UUID;

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
     * interval: The hourly interval/frequency at which the app's notifications will fire up,
     *           1 by default.
     * windowStart: The start of the window in which the app's notifications may fire up in a 24
     *              hour clock, 8 by default.
     * windowEnd: The end of the window in which the app's notifications may fire up in a 24
     *            hour clock, 20 by default.
     * running: If the user decides to use the NFC tag timer feature, this will be used to measure
     *          their water intake based on how long the user is taking to drink, where 0 sets the
     *          timer and 1 ends it.
     */
    protected SharedPreferences pref;

    private EditText drinkInput;
    private Button continueButton;
    private int recommendedIntake;
    SharedPreferences checkAppStart = null;    // Check app start times
    private float drinkingRate;
    Context context = this;
    StopWatch timer = new StopWatch();

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
        drinkingRate = pref.getFloat("drinkingRate", 0);
        // Create a new sharedPref called currentAmountLeftToDrink that will be edited later on
        SharedPreferences.Editor edit = pref.edit();

        if (!pref.contains("currentAmountLeftToDrink")) {
            if (recommendedIntake > 0) {
                edit.putInt("currentAmountLeftToDrink", recommendedIntake);
            }
        }

        // Store the current day
        Calendar calendar = Calendar.getInstance();
        edit.putInt("today", calendar.get(Calendar.DAY_OF_YEAR));

        // Store the notification hourly interval, by default 1, if none exist yet
        if (!pref.contains("interval")) {
            edit.putFloat("interval", (float) 1.0);
        }

        // Store the notification window, by default 8 and 20, if they don't exist yet
        if (!pref.contains("windowStart")) {
            edit.putInt("windowStart", 8);
        }
        if (!pref.contains("windowEnd")) {
            edit.putInt("windowEnd", 20);
        }

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
        seekBar.setProgress((int)calculateRemainingPercentage());
        waveLoadingView.setProgressValue(seekBar.getProgress());
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
        float interval = pref.getFloat("interval", (float) 1.0);
        int windowStart = pref.getInt("windowStart", 8);
        int windowEnd = pref.getInt("windowEnd", 20);
        try {
            setNotificationToIntervals(interval, windowStart, windowEnd);
        } catch (IllegalArgumentException e) {
            //
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences.Editor edit = pref.edit();

        // Check if the current day is different from the current day stored, and reset the water
        // intake if so
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_YEAR) != pref.getInt("today", 0)) {
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
        logIntakeHelper(drink);

    }

    public void logIntakeHelper(int drink) {
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
     * This function will create the app's Notifications and set them to pop up in the interval
     * given and between windowStart and windowEnd.
     * @param interval The interval (in hours) in which the Notification will fire up.
     * @param windowStart The start of the window in which Notifications may ring in a 24 hour clock.
     * @param windowEnd The end of the window in which Notifications may ring in a 24 hour clock.
     * @exception IllegalArgumentException If windowEnd < windowStart
     */
    private void setNotificationToIntervals(float interval, int windowStart, int windowEnd)
            throws IllegalArgumentException {
        if (windowEnd < windowStart) {
            throw new IllegalArgumentException();
        }

        // Set the Notification created to fire up at regular intervals
        Intent intent = new Intent(this, HourlyReceiver.class);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Apply the arguments
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // If it is past windowEnd, set the alarm for tomorrow instead
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > windowEnd) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        calendar.set(Calendar.HOUR_OF_DAY, windowStart); // Set the time to windowStart
        int windowHours = windowEnd - windowStart;

        // Set the window to be from windowStart to windowEnd
        alarmManager.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP, calendar.getTimeInMillis(),
                (long) windowHours * 60 * 60 * 1000, pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                (long) (interval * 60 * 60 * 1000), pendingIntent);
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
        amountToDrink.setText(String.valueOf(recommendedIntake));
        amountToDrinkPercent.setText(maxPercentage);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            if (pref.getBoolean("Bluetooth", false)) {
                Parcelable tagMessages = intent.getParcelableArrayExtra(
                        NfcAdapter.EXTRA_NDEF_MESSAGES)[0];
                NdefMessage message = (NdefMessage) tagMessages;
                if (message.getRecords().length <= 1) {
                    finish();
                }
                byte[] payload = message.getRecords()[1].getPayload();
                int languageLength = payload[0] & 0x03F;
                int textLength = payload.length - languageLength - 1;
                String mac = new String(payload,
                        languageLength + 1, textLength, StandardCharsets.UTF_8);
                BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice bDevice = bAdapter.getRemoteDevice(mac);
                try {
                    new ConnectThread(bDevice);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                measureTime();
            }
        }
    }

    public class ConnectThread extends Thread {
        BluetoothSocket socket;
        BluetoothDevice bDevice;
        BluetoothAdapter bAdapter;
        private ConnectThread(BluetoothDevice device) throws IOException {
            bAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothSocket tmp = null;
            bDevice = device;
            UUID uuid = UUID.fromString("f04c83b4-0f9a-4d5c-869d-700833daeec1");
            try {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                tmp = bDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Toast.makeText(context, "Permission needed.", Toast.LENGTH_LONG).show();
            }
            socket = tmp;
            try {
                assert socket != null;
                socket.connect();
            } catch (IOException connectException) {
                Toast.makeText(context, "Connect Failure", Toast.LENGTH_LONG).show();
                try {
                    socket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
            }
            receive();
        }

        public void receive() throws IOException {
            InputStream mmInputStream = socket.getInputStream();
            byte[] buffer = new byte[256];
            int bytes;
            SharedPreferences.Editor edit = pref.edit();
            try {
                bytes = mmInputStream.read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                Toast.makeText(context, readMessage, Toast.LENGTH_LONG).show();
                if (!pref.contains("firstReading")) {
                    edit.putInt("firstReading", Integer.parseInt(readMessage));
                } else {
                    int firstReading = pref.getInt("firstReading", 0);
                    int result = firstReading - Integer.parseInt(readMessage);
                    logIntakeHelper(result);
                    edit.remove("firstReading");
                }
                edit.apply();
                socket.close();
            } catch (IOException e) {
                finish();
            }
        }

    }

    private void measureTime() {
        int running = pref.getInt("running", 0);
        if (pref.getFloat("drinkingRate", 0) == 0.0) {
            Toast.makeText(context, "Initialisation needed.", Toast.LENGTH_LONG).show();
            return;
        }
        SharedPreferences.Editor edit = pref.edit();
        if (running == 0) {
            Toast.makeText(context, "Timer started.", Toast.LENGTH_LONG).show();
            timer.setCurrentTime();
            edit.putInt("running", 1);
            edit.apply();
        } else if (running == 1) {
            double diff = timer.getTimeDifference()-1.5;
            edit.putInt("running", 0);
            edit.apply();
            drinkingRate = pref.getFloat("drinkingRate", 0);
            int amount = (int) Math.round(diff * drinkingRate);
            Toast.makeText(context, "You drank: " + amount +"."
                    , Toast.LENGTH_LONG).show();
            logIntakeHelper(amount);
        }
    }

    public void enterTimer(View v) {
        Intent intent = new Intent(MainActivity2.this, TimerActivity.class);
        startActivity(intent);
    }

    /**
     * This function sends the user to the NFC screen when the btnNFC at the top right of
     * activity_main2.xml is clicked.
     * @param v The btnNFC in activity_main2.xml.
     */
    public void handleNFCButton (View v) { // This function runs when NFC button is pressed
        Intent intent = new Intent(MainActivity2.this, WriteNFC.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * This function sends the user to the Notification Editing screen when the btnEditNotification
     * at the bottom right of activity_main2.xml is clicked.
     * @param v The btnEditNotification in activity_main2.xml.
     */
    public void handleEditNotification(View v) {
        Intent intent = new Intent(MainActivity2.this, EditNotification.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * This function is overridden to ensure that the back button will always return the user to the
     * activity_main.xml.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(MainActivity2.this, MainActivity.class));
        finish();
    }

    /**
     * This function sends the user to the information input screen when the btnSettings at the
     * top right of activity_main2.xml is clicked.
     * @param v The btnSettings in activity_main2.xml.
     */
    public void handleSettingsButton (View v) { // This function runs when NFC button is pressed
        onBackPressed();
    }
}