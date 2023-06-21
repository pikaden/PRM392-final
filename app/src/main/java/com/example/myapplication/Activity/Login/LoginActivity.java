package com.example.myapplication.Activity.Login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button PhoneLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        PhoneLoginButton = findViewById(R.id.phone_login_button);

        PhoneLoginButton.setOnClickListener(v -> {
            Intent phoneLoginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
            startActivity(phoneLoginIntent);
        });
    }
}

