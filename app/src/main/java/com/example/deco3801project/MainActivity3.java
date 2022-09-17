package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.telecom.TelecomManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity3 extends AppCompatActivity {

    ViewPager viewPager;
    SliderAdapter sliderAdapter;
    private TextView txtButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);



        // Show slides
        viewPager = (ViewPager)findViewById(R.id.viewpager);
        sliderAdapter = new SliderAdapter(this);
        viewPager.setAdapter(sliderAdapter);

        // Finish this activity and back to where it was intended
        //button = (Button) findViewById(R.id.close_button);
        txtButton = (TextView) findViewById(R.id.txtButtonClickable);
        txtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


}