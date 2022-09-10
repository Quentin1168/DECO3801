package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.content.Context;
import android.content.SharedPreferences;
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

import me.itangqi.waveloadingview.WaveLoadingView;

public class MainActivity2 extends AppCompatActivity {

    SharedPreferences pref;
    private EditText drinkInput;
    private Button continueButton;
    private int intake;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        //pref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_main2);
        intake = pref.getInt("intake", 0);
        // Retrieves the user's input age and gender

        TextView intakeInput = findViewById(R.id.intakeText);

        intakeInput.setText(String.valueOf(intake));
        drinkInput = findViewById(R.id.drinkText);
        continueButton = findViewById(R.id.add_button);
        drinkInput.addTextChangedListener(continueTextWatcher);


        SeekBar seekBar = findViewById(R.id.seekBar);
        WaveLoadingView waveLoadingView = findViewById(R.id.textView3);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                waveLoadingView.setProgressValue(i);

                String title = String.valueOf(i);
                waveLoadingView.setBottomTitle("");
                waveLoadingView.setCenterTitle("");
                waveLoadingView.setTopTitle("");

                if (i < 50) {
                    waveLoadingView.setBottomTitle(title);
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
            // Whenever the Age input is empty, the Continue button will be disabled
            String drinkInputText = drinkInput.getText().toString().trim();
            continueButton.setEnabled(!drinkInputText.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public void handleText(View v) { // This method run when button is clicked
        String drinkStr = drinkInput.getText().toString();
        int drink = 0;
        if (!drinkStr.equals("")) {
            drink = Integer.parseInt(drinkStr); // Get age
        }

        try {
            subtract_intake(drink);
        } catch(IllegalArgumentException e) {

        }
        TextView intakeInput = findViewById(R.id.intakeText);

        intakeInput.setText(String.valueOf(intake));
        drinkInput.setText("");
    };

    protected void subtract_intake(int drink) throws IllegalArgumentException {
        intake = intake - drink;
        if (intake > 0) {
            throw new IllegalArgumentException("Drink cannot be larger than intake");
        }
    }



}