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


}