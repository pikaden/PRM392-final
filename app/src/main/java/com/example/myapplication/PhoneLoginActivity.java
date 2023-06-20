package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

    private Button SendVerificationCodeButton, VerifyButton;
    private EditText InputPhoneNumber, InputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private RelativeLayout layout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        mAuth = FirebaseAuth.getInstance();


        SendVerificationCodeButton = (Button) findViewById(R.id.send_ver_code_button);

        VerifyButton = (Button) findViewById(R.id.verify_button);

        InputPhoneNumber = (EditText) findViewById(R.id.phone_number_input);

        InputVerificationCode = (EditText) findViewById(R.id.verification_code_input);

        layout = findViewById(R.id.rootView);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(progressBar, params);

        SendVerificationCodeButton.setOnClickListener(view -> {

            // phone number in VN only!
            String phoneNumber = "+84" + InputPhoneNumber.getText().toString();

            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(PhoneLoginActivity.this, "Please Enter Phone Number", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                // disable user interaction
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)    // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS)  // Timeout and unit
                        .setActivity(PhoneLoginActivity.this)   // Activity (for callback binding)
                        .setCallbacks(callbacks)    // OnVerificationStateChangedCallbacks
                        .build();

                PhoneAuthProvider.verifyPhoneNumber(options);
            }

        });


        VerifyButton.setOnClickListener(view -> {
            SendVerificationCodeButton.setVisibility(View.INVISIBLE);

            InputPhoneNumber.setVisibility(View.INVISIBLE);

            String verificationCode = InputVerificationCode.getText().toString();
            if (TextUtils.isEmpty(verificationCode)) {
                Toast.makeText(PhoneLoginActivity.this, "Please Write Verification Code", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                // disable user interaction
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                signInWithPhoneAuthCredential(credential);
            }

        });


        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                progressBar.setVisibility(View.GONE);
                // enable user interaction
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number ,Please Enter Correct Phone Number", Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.VISIBLE);

                InputPhoneNumber.setVisibility(View.VISIBLE);

                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);


            }

            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;

                progressBar.setVisibility(View.GONE);

                Toast.makeText(PhoneLoginActivity.this, "Code Has Been Sent", Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);

                InputPhoneNumber.setVisibility(View.INVISIBLE);

                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);


            }


        };


    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            // enable user interaction
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                            Toast.makeText(PhoneLoginActivity.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        } else {
                            String message = task.getException().toString();

                            Toast.makeText(PhoneLoginActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }


}
