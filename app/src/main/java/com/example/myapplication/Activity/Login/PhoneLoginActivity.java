package com.example.myapplication.Activity.Login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private Button sendVerificationCodeButton, verifyButton;
    private EditText inputPhoneNumber, InputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView phoneNumberCode;
    private RelativeLayout layout;
    private Toolbar loginToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        initializeFields();

        // set
        inputPhoneNumber.setText("1234567890");
        InputVerificationCode.setText("123456");

        sendVerificationCodeButton.setOnClickListener(view -> {
            isDisable(true);

            // use fiction number only
            String phoneNumber = "+1" + inputPhoneNumber.getText().toString();

            if (TextUtils.isEmpty(phoneNumber)) {
                isDisable(false);
                Toast.makeText(PhoneLoginActivity.this, "Please Enter Phone Number", Toast.LENGTH_SHORT).show();
            } else {
                isDisable(false);

                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)    // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS)  // Timeout and unit
                        .setActivity(PhoneLoginActivity.this)   // Activity (for callback binding)
                        .setCallbacks(callbacks)    // OnVerificationStateChangedCallbacks
                        .build();

                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        });


        verifyButton.setOnClickListener(view -> {
            sendVerificationCodeButton.setVisibility(View.INVISIBLE);
            phoneNumberCode.setVisibility(View.INVISIBLE);
            inputPhoneNumber.setVisibility(View.INVISIBLE);

            isDisable(true);

            String verificationCode = InputVerificationCode.getText().toString();
            if (TextUtils.isEmpty(verificationCode)) {
                isDisable(false);
                Toast.makeText(PhoneLoginActivity.this, "Please Write Verification Code", Toast.LENGTH_SHORT).show();
            } else {
                isDisable(false);

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                signInWithPhoneAuthCredential(credential);
            }

        });


        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                isDisable(false);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                isDisable(false);

                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number ,Please Enter Correct Phone Number", Toast.LENGTH_SHORT).show();

                sendVerificationCodeButton.setVisibility(View.VISIBLE);

                inputPhoneNumber.setVisibility(View.VISIBLE);
                verifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);
            }

            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;

                isDisable(false);

                Toast.makeText(PhoneLoginActivity.this, "Code Has Been Sent", Toast.LENGTH_SHORT).show();

                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                phoneNumberCode.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);
            }
        };
    }

    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();

        sendVerificationCodeButton = findViewById(R.id.send_ver_code_button);
        verifyButton = findViewById(R.id.verify_button);
        phoneNumberCode = findViewById(R.id.phoneNumberCode);
        inputPhoneNumber = findViewById(R.id.phone_number_input);
        InputVerificationCode = findViewById(R.id.verification_code_input);
        layout = findViewById(R.id.rootView);

        // loading bar
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setVisibility(View.GONE);
        layout.addView(progressBar, params);

        // back button
        loginToolbar = findViewById(R.id.phone_number_sign_in_toolbar);
        setSupportActionBar(loginToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PhoneLoginActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();
                        sendUserToMainActivity();
                    } else {
                        String message = "Cannot login, please check again your verification code";

                        Toast.makeText(PhoneLoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
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
