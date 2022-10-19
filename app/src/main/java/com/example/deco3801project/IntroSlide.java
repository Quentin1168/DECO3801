package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class IntroSlide extends AppCompatActivity {

    ViewPager viewPager;
    SliderAdapter sliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_slide);

        // Show slides
        viewPager = findViewById(R.id.viewpager);
        sliderAdapter = new SliderAdapter(this);
        viewPager.setAdapter(sliderAdapter);

        TextView txtButton = findViewById(R.id.txtButtonClickable);
        txtButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), InputInformation.class);
            startActivity(intent);
        });

    }


}