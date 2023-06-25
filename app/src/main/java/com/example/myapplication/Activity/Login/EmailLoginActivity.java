package com.example.myapplication.Activity.Login;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.myapplication.Activity.Signup.EmailSignupActivity;
import com.example.myapplication.Activity.Signup.SignupActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;

public class EmailLoginActivity extends AppCompatActivity {
    private Button signInByEmailButton;
    private EditText inputEmail, inputPassword;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private RelativeLayout layout;
    private Toolbar signupToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        initializeFields();

        signInByEmailButton.setOnClickListener(v -> {
            isDisable(true);
            signInByEmail(inputEmail.getText().toString(), inputPassword.getText().toString());
        });
    }

    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();

        signInByEmailButton = findViewById(R.id.sign_in_by_email_button);
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
        signupToolbar = findViewById(R.id.email_sign_in_toolbar);
        setSupportActionBar(signupToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    private void signInByEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        isDisable(false);
                        // Sign in success
                        Toast.makeText(EmailLoginActivity.this, "Sign in user success!.",
                                Toast.LENGTH_SHORT).show();

                        // send user to main activity
                        sendUserToMainActivity();
                    } else {
                        isDisable(false);
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInUserWithEmailAndPassword:failure", task.getException());
                        Toast.makeText(EmailLoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(EmailLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
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