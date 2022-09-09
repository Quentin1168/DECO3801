package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity3 extends AppCompatActivity {

    ViewPager viewPager;
    SliderAdapter sliderAdapter;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        button = (Button) findViewById(R.id.close_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        sliderAdapter = new SliderAdapter(this);
        viewPager.setAdapter(sliderAdapter);



    }


}