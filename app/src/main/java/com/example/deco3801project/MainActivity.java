package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void handleText(View v) {
        TextView ageInput = findViewById(R.id.ageText);
        String age = ageInput.getText().toString(); // Get age
        Log.d("info", age);
    }
}