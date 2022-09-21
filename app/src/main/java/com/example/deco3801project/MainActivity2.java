package com.example.deco3801project;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

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

        // The total amount of water to drink initially
        TextView amountToDrink = findViewById(R.id.amountToDrink);
        amountToDrink.setText(String.valueOf(recommendedIntake));

        // The percentage of the amount left to drink
        TextView amountToDrinkPercent = findViewById(R.id.amountToDrinkPercentage);
        amountToDrinkPercent.setText("0%");

        drinkInput = findViewById(R.id.drinkText);
        continueButton = findViewById(R.id.add_button);
        drinkInput.addTextChangedListener(continueTextWatcher);

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(100); // Sets the water height to 100%

        WaveLoadingView waveLoadingView = findViewById(R.id.textView3);

        seekBar.setEnabled(false);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                waveLoadingView.setProgressValue(i);

                // Show % of the wave
                String title = String.valueOf(i);
                waveLoadingView.setBottomTitle("");
                waveLoadingView.setCenterTitle("");
                waveLoadingView.setTopTitle("");

                if (i < 50) {
                    waveLoadingView.setBottomTitle(title);  // % show in the bottom part of wave
                } else if (i == 50) {
                    waveLoadingView.setCenterTitle(title);
                } else {
                    waveLoadingView.setTopTitle(title);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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
    };

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

}