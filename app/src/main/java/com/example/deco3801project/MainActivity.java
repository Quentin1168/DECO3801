package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

    public void handleGender(View v) {
        Button genderButton = findViewById(R.id.gender_button);
        if (genderButton.getText().equals("Male")) {
            genderButton.setText(R.string.gender_female);
        } else {
            genderButton.setText(R.string.gender_male);
        }
    }

    public void handleText(View v) { // This method run when button is clicked
        TextView ageInput = findViewById(R.id.ageText);
        String age = ageInput.getText().toString(); // Get age
        Log.d("info", age);

        // Saving information to UserInfo preference
        Button genderButton = findViewById(R.id.gender_button);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("age", age);
        boolean gender = genderButton.getText().equals("Male");
        edit.putBoolean("gender", gender);
        edit.apply();

        Intent intent = new Intent(MainActivity.this, MainActivity2.class);
        startActivity(intent);
    }


}