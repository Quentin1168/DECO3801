package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity {

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_main2);

        // Retrieves the user's input age and gender
        String age = pref.getString("age", null);
        String gender = pref.getString("gender", null);

        // Displays the user's age and gender for debugging purposes
        TextView ageInput = findViewById(R.id.ageText);
        ageInput.setText(age);
        TextView genderInput = findViewById(R.id.genderText);
        genderInput.setText(gender);
        TextView intakeInput = findViewById(R.id.intakeText);

        int intake = calculateIntake(age, gender);

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