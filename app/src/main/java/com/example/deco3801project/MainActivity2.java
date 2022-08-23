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
    }
}