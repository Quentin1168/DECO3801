package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity3 extends AppCompatActivity {

    ViewPager viewPager;
    SliderAdapter sliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);



        // Show slides
        viewPager = findViewById(R.id.viewpager);
        sliderAdapter = new SliderAdapter(this);
        viewPager.setAdapter(sliderAdapter);

        // Finish this activity and back to where it was intended
        //button = (Button) findViewById(R.id.close_button);
        TextView txtButton = findViewById(R.id.txtButtonClickable);
        txtButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

    }


}