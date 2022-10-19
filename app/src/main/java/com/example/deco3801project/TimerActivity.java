package com.example.deco3801project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TimerActivity extends AppCompatActivity {

    SharedPreferences pref;
    Context context = this;
    StopWatch timer = new StopWatch();
    int time = 0;
    private EditText drinkInput;
    private Button continueButton;
    private TextView drinkingRate;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setContentView(R.layout.timer_activity);
        drinkInput = findViewById(R.id.drinkInput);
        continueButton = findViewById(R.id.inputButton);
        drinkInput.addTextChangedListener(continueTextWatcher);

        drinkingRate = findViewById(R.id.drinkingRate);

        if (pref.contains("drinkingRate")) {
            drinkingRate.setText(String.valueOf(pref.getFloat("drinkingRate", (float) 0.0)));
        }
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

        }
    };

    public void measureTime(View view) {
            int running = pref.getInt("running", 0);

            SharedPreferences.Editor edit = pref.edit();
            if (running == 0) {
                Toast.makeText(context, "Timer Started", Toast.LENGTH_LONG).show();
                timer.setCurrentTime();
                edit.putInt("running", 1);
                edit.apply();
            } else if (running == 1) {
                Toast.makeText(context, "Timer Ended", Toast.LENGTH_LONG).show();
                int diff = timer.getTimeDifference();
                edit.putInt("running", 0);
                edit.apply();
                time = diff;
                TextView time = findViewById(R.id.timer);
                time.setText(String.valueOf(diff));
            }
    }

    public void setDrinkingRate(View view) {
        SharedPreferences.Editor edit = pref.edit();
        String drinkStr = drinkInput.getText().toString();
        int drink = 0;
        if (!drinkStr.equals("")) {
            drink = Integer.parseInt(drinkStr); // Get amount to be drunk
        }
        if (time != 0) {
            Toast.makeText(context, String.valueOf(drink/time), Toast.LENGTH_LONG).show();
            edit.putFloat("drinkingRate", (float) drink/time);
        }
        else {
            edit.putFloat("drinkingRate", 0);
        }
        edit.apply();

        drinkingRate.setText(String.valueOf(pref.getFloat("drinkingRate", (float) 0.0)));
    }

    /**
     * This function is overridden to ensure that the back button will always return the user to the
     * activity_water_intake.xml.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(TimerActivity.this, WaterIntake.class));
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


}
