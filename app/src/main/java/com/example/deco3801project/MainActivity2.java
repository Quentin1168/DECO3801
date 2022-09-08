package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity2 extends AppCompatActivity {

    SharedPreferences pref;

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

        // Retrieves the user's input age and gender
        String age = pref.getString("age", null);
        String gender = getString(R.string.gender_female);
        String input = pref.getString("intake", null);
        int intake;
        TextView ageInput = findViewById(R.id.ageText);
        TextView genderInput = findViewById(R.id.genderText);
        // Displays the user's age and gender for debugging purposes
        TextView intakeInput = findViewById(R.id.intakeText);
        boolean genderBool = pref.getBoolean("gender", true);
        if (age.equals("")) {
            ageInput.setText("N/A");
            genderInput.setText("N/A");
            intake = Integer.parseInt(input);
        }
        else {
            if (genderBool) {
                gender = getString(R.string.gender_male);
            }
            ageInput.setText(age);
            genderInput.setText(gender);
            intake = calculateIntake(age, gender);
        }
        intakeInput.setText(String.valueOf(intake));

    }

    public int calculateIntake(String strAge, String gender) {
        int intake;
        int age = Integer.parseInt(strAge);
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
        else if (gender.equalsIgnoreCase("Male")) {
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