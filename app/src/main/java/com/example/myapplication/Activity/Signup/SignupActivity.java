package com.example.myapplication.Activity.Signup;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private Button signUpByEmailButton;
    private EditText inputEmail, inputPassword;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private RelativeLayout layout;
    private Toolbar signupToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeFields();

        signUpByEmailButton.setOnClickListener(v -> {
            isDisable(true);
            signUpByEmail(inputEmail.getText().toString(), inputPassword.getText().toString());
        });
    }

    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();

        signUpByEmailButton = findViewById(R.id.signup_by_email_button);
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        layout = findViewById(R.id.rootView);

        // loading bar
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setVisibility(View.GONE);
        layout.addView(progressBar, params);

        // back button
        signupToolbar = findViewById(R.id.signup_toolbar);
        setSupportActionBar(signupToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    private void signUpByEmail(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        isDisable(false);
                        // Sign up success
                        Toast.makeText(SignupActivity.this, "Sign up user success!.",
                                Toast.LENGTH_SHORT).show();

                        // login user
                        mAuth.signInWithEmailAndPassword(email, password);

                        // update user profile
                        sendUserToEmailSignupActivity();
                    } else {
                        isDisable(false);
                        // If sign up fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignupActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendUserToEmailSignupActivity() {
        Intent settingsIntent = new Intent(SignupActivity.this, EmailSignupActivity.class);
        startActivity(settingsIntent);
    }

    /**
     * @param isDisable turn on progress bar
     */
    private void isDisable(boolean isDisable) {
        if (isDisable == true) {
            progressBar.setVisibility(View.VISIBLE);
            // disable user interaction
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            progressBar.setVisibility(View.GONE);
            // enable user interaction
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
}
