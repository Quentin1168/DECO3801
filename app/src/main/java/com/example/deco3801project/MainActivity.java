package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.util.Log;


public class MainActivity extends AppCompatActivity {

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
    }


    public void handleText(View v) { // This method run when button is clicked
        TextView ageInput = findViewById(R.id.ageText);
        String age = ageInput.getText().toString(); // Get age
        Log.d("info", age);
        TextView genderInput = findViewById(R.id.genderText);
        String gender = genderInput.getText().toString(); // Get gender
        Log.d("info", gender);

        // Saving information to UserInfo preference
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("age", age);
        edit.putString("gender", gender);
        edit.commit();

        Intent intent = new Intent(MainActivity.this, MainActivity2.class);
        startActivity(intent);
    }

    public int calculateIntake(int age, String gender) {
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