package com.example.myapplication.Activity.Login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Activity.Signup.SignupActivity;
import com.example.myapplication.R;

public class LoginActivity extends AppCompatActivity {
    private Button phoneLoginButton, emailLoginButton, signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        InitializeFields();

        phoneLoginButton.setOnClickListener(v -> {
            Intent phoneLoginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
            startActivity(phoneLoginIntent);
        });

        emailLoginButton.setOnClickListener(v -> {
            Intent emailLoginIntent = new Intent(LoginActivity.this, EmailLoginActivity.class);
            startActivity(emailLoginIntent);
        });

        signUpButton.setOnClickListener(v -> {
            Intent signupIntent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(signupIntent);
        });
    }

    private void InitializeFields() {
        phoneLoginButton = findViewById(R.id.phone_login_button);
        emailLoginButton = findViewById(R.id.email_login_button);
        signUpButton = findViewById(R.id.signup_button);
    }
}

