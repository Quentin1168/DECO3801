package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import java.io.IOException;
import java.lang.String;
import java.security.GeneralSecurityException;


public class MainActivity extends AppCompatActivity {


    SharedPreferences pref;

    private EditText ageInput;
    private EditText intakeInput;
    private Button genderButton;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Slider Intro
        Intent i = new Intent(getApplicationContext(),MainActivity3.class);
        startActivity(i);

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

        ageInput = findViewById(R.id.ageText);
        genderButton = findViewById(R.id.gender_button);

        continueButton = findViewById(R.id.continue_button);
        intakeInput = findViewById(R.id.intakeInput);
        ageInput.setOnFocusChangeListener((view, b) -> {
            intakeInput.setBackground(AppCompatResources.getDrawable(getApplicationContext(),
                    R.drawable.rounded_grey_button));
            intakeInput.setText("");
            ageInput.setBackground(AppCompatResources.getDrawable(getApplicationContext(),
                    R.drawable.rounded_white_button));
        });
        intakeInput.setOnFocusChangeListener((view, b) -> {
            ageInput.setBackground(AppCompatResources.getDrawable(getApplicationContext(),
                    R.drawable.rounded_grey_button));
            ageInput.setText("");
            intakeInput.setBackground(AppCompatResources.getDrawable(getApplicationContext(),
                    R.drawable.rounded_white_button));
        });
        ageInput.addTextChangedListener(continueTextWatcher);
        intakeInput.addTextChangedListener(continueTextWatcher);
    }

    private TextWatcher continueTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // Whenever the Age input is empty, the Continue button will be disabled
            String ageInputText = ageInput.getText().toString().trim();
            String intakeInputText = intakeInput.getText().toString().trim();
            continueButton.setEnabled(!ageInputText.isEmpty() || !intakeInputText.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public void handleGender(View v) {
        if (genderButton.getText().equals("Male")) {
            genderButton.setText(R.string.gender_female);
        } else {
            genderButton.setText(R.string.gender_male);
        }
    }

    public void handleText(View v) { // This method run when button is clicked
        String ageStr = ageInput.getText().toString();
        int age = 0;
        if (!ageStr.equals("")) {
            age = Integer.parseInt(ageStr); // Get age
        }
        String intake = intakeInput.getText().toString();
        // Saving information to UserInfo preference
        SharedPreferences.Editor edit = pref.edit();
        boolean gender = genderButton.getText().toString().equalsIgnoreCase("Male");
        if (!intake.equals("")) {
            edit.putInt("intake", Integer.parseInt(intake));
        } else {
            edit.putInt("intake", calculateIntake(age, gender));
        }
        edit.apply();

        Intent intent = new Intent(MainActivity.this, MainActivity2.class);
        startActivity(intent);
    }

    public int calculateIntake(int age, boolean gender) {
        int intake;
        if (age <=8) { // Specifying for ages under 8
            if (age == 1) {
                intake = 1150;
            } else if (age == 2 || age == 3) {
                intake = 1300;
            } else {
                intake = 1600;
            }
            return intake;
        }
        // Since water intake is different for different genders at later ages
        else if (gender) {
            if (age <= 13) {
                intake = 2100;
            }
            else {
                intake = 2500;
            }
        }
        else {
            if (age <= 13) {
                intake = 1900;
            }
            else {
                intake = 2000;
            }
        }
        return intake;
    }

}