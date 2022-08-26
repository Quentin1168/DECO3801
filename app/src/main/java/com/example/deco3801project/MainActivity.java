package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;
import java.lang.String;


public class MainActivity extends AppCompatActivity {

    SharedPreferences pref;

    private EditText ageInput;
    private EditText intakeInput;
    private Button genderButton;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

        ageInput = findViewById(R.id.ageText);
        genderButton = findViewById(R.id.gender_button);
        continueButton = findViewById(R.id.continue_button);
        intakeInput = findViewById(R.id.intakeInput);
        ageInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                intakeInput.setBackground(AppCompatResources.getDrawable(getApplicationContext(),
                        R.drawable.rounded_grey_button));
                intakeInput.setText("");
                ageInput.setBackground(AppCompatResources.getDrawable(getApplicationContext(),
                        R.drawable.rounded_white_button));
            }
        });
        intakeInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                ageInput.setBackground(AppCompatResources.getDrawable(getApplicationContext(),
                        R.drawable.rounded_grey_button));
                ageInput.setText("");
                intakeInput.setBackground(AppCompatResources.getDrawable(getApplicationContext(),
                        R.drawable.rounded_white_button));
            }
        });
        ageInput.addTextChangedListener(continueTextWatcher);
    }

    private TextWatcher continueTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // Whenever the Age input is empty, the Continue button will be disabled
            String ageInputText = ageInput.getText().toString().trim();
            continueButton.setEnabled(!ageInputText.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public void handleGender(View v) {
        if (genderButton.getText().equals("Male")) {
            genderButton.setText(R.string.gender_female);
        } else {
            genderButton.setText(R.string.gender_male);
        }
    }

    public void handleText(View v) { // This method run when button is clicked
        String age = ageInput.getText().toString(); // Get age
        Log.d("info", age);

        // Saving information to UserInfo preference
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("age", age);
        boolean gender = genderButton.getText().equals("Male");
        edit.putBoolean("gender", gender);
        edit.apply();

        Intent intent = new Intent(MainActivity.this, MainActivity2.class);
        startActivity(intent);
    }


}