package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.lang.String;
import java.security.GeneralSecurityException;

public class InputInformation extends AppCompatActivity {
    SharedPreferences pref;

    private EditText ageInput;
    private EditText intakeInput;
    private Button genderButton;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_information);

        // Create a Notification Channel for the app's Notifications
        createNotificationChannel();

        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
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
            e.printStackTrace();
            finish();
        }

        ageInput = findViewById(R.id.ageInput);
        genderButton = findViewById(R.id.genderButton);

        continueButton = findViewById(R.id.inputButton);
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

    private final TextWatcher continueTextWatcher = new TextWatcher() {
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

    /**
     * This function handles the gender options present in the gender_button in activity_input_information.xml.
     * Pressing this button will switch it from one gender to the other.
     * @param v The genderButton in activity_input_information.xml.
     */
    public void handleGender(View v) {
        if (genderButton.getText().equals("Male")) {
            genderButton.setText(R.string.gender_female);
        } else {
            genderButton.setText(R.string.gender_male);
        }
    }

    /**
     * This function handles the click-ability of the continueButton in activity_input_information.xml.
     * If either the ageInput or intakeInput is filled, then the continueButton will be clickable.
     * If neither is filled, then it will not be clickable.
     * @param v The continueButton in activity_input_information.xml.
     */
    public void handleText(View v) {
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
            edit.putInt("recommendedIntake", Integer.parseInt(intake));
            edit.putInt("currentAmountLeftToDrink", Integer.parseInt(intake));
        } else {
            edit.putInt("recommendedIntake", calculateIntake(age, gender));
            edit.putInt("currentAmountLeftToDrink", calculateIntake(age, gender));
        }
        edit.apply();

        Intent intent = new Intent(getApplicationContext(), WaterIntake.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * Based on the user's given age and gender, determines the recommended water intake for
     * the user to follow.
     * @param age The user's age.
     * @param gender The user's gender, where 1 is Male and 0 is Female.
     * @return The user's recommended water intake based on their age and gender.
     */
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

    /**
     * This function creates a Notification Channel for this app with the ID 1, a high importance,
     * and vibrations enabled.
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.enableVibration(true);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}