package com.example.deco3801project;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;

public class EditNotification extends AppCompatActivity {
    private SharedPreferences pref;
    private EditText notificationFrequencyInput;
    private EditText notificationWindowStartInput;
    private EditText notificationWindowEndInput;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setContentView(R.layout.activity_edit_notification);

        notificationFrequencyInput = findViewById(R.id.notificationFrequencyInput);
        notificationWindowStartInput = findViewById(R.id.notificationWindowStartInput);
        notificationWindowEndInput = findViewById(R.id.notificationWindowEndInput);

        notificationFrequencyInput.addTextChangedListener(continueTextWatcher);
        notificationWindowStartInput.addTextChangedListener(continueTextWatcher);
        notificationWindowEndInput.addTextChangedListener(continueTextWatcher);

        saveButton = findViewById(R.id.saveButton);
    }

    private final TextWatcher continueTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // The Save button is enabled only if the frequency is filled or both window inputs
            String notificationFrequencyText = notificationFrequencyInput.getText().toString().trim();
            String notificationWindowStartText = notificationWindowStartInput.getText().toString().trim();
            String notificationWindowEndText = notificationWindowEndInput.getText().toString().trim();

            saveButton.setEnabled(!notificationFrequencyText.isEmpty() ||
                    (!notificationWindowStartText.isEmpty() && !notificationWindowEndText.isEmpty()));
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    /**
     * This function is overridden to ensure that the back button will always return the user to the
     * activity_water_intake.xml.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(EditNotification.this, WaterIntake.class));
        finish();
    }

    /**
     * Returns the user to the previous screen when the backButton is pressed in
     * activity_edit_notification.xml.
     * @param v The backButton in activity_edit_notification.xml.
     */
    public void handleBackButton(View v) {
        onBackPressed();
    }

    /**
     * Handles the saveButton's functionality in activity_edit_notification.xml by applying the
     * user's adjustments on the Notifications.
     * @param v The saveButton in activity_edit_notification.xml.
     */
    public void handleSaveButton(View v) {
        String notificationFrequencyText = notificationFrequencyInput.getText().toString().trim();
        String notificationWindowStartText = notificationWindowStartInput.getText().toString().trim();
        String notificationWindowEndText = notificationWindowEndInput.getText().toString().trim();

        SharedPreferences.Editor edit = pref.edit();

        if (!notificationFrequencyText.isEmpty()) {
            edit.putFloat("interval", Float.parseFloat(notificationFrequencyText));
        }

        if (!notificationWindowStartText.isEmpty() && !notificationWindowEndText.isEmpty()) {
            edit.putInt("windowStart", Integer.parseInt(notificationWindowStartText));
            edit.putInt("windowEnd", Integer.parseInt(notificationWindowEndText));
        }

        edit.apply();

        float interval = pref.getFloat("interval", 1);
        int windowStart = pref.getInt("windowStart", 8);
        int windowEnd = pref.getInt("windowEnd", 20);

        try {
            setNotificationToIntervals(interval, windowStart, windowEnd);
        } catch (IllegalArgumentException e) {
            //
        }

        onBackPressed();
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
}
