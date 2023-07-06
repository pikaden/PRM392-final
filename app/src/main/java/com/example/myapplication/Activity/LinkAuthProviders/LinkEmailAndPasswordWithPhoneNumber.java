package com.example.myapplication.Activity.LinkAuthProviders;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LinkEmailAndPasswordWithPhoneNumber extends AppCompatActivity {

    private Button sendVerificationCodeButton, verifyButton, skipButton;
    private EditText inputPhoneNumber, inputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView phoneNumberCode;
    private RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_email_and_password_with_phone_number);

        initializeFields();

        sendVerificationCodeButton.setOnClickListener(view -> {
            isDisable(true);

            // use fiction number only
            String phoneNumber = "+1" + inputPhoneNumber.getText().toString();

            if (TextUtils.isEmpty(phoneNumber)) {
                isDisable(false);
                Toast.makeText(LinkEmailAndPasswordWithPhoneNumber.this, "Please Enter Phone Number", Toast.LENGTH_SHORT).show();
            } else {
                isDisable(false);

                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)    // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS)  // Timeout and unit
                        .setActivity(LinkEmailAndPasswordWithPhoneNumber.this)   // Activity (for callback binding)
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

            String verificationCode = inputVerificationCode.getText().toString();
            if (TextUtils.isEmpty(verificationCode)) {
                isDisable(false);
                Toast.makeText(LinkEmailAndPasswordWithPhoneNumber.this, "Please Write Verification Code", Toast.LENGTH_SHORT).show();
            } else {
                isDisable(false);

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);

                // link account with phone number
                linkAccountWithPhoneNumber(credential);
            }

        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                isDisable(false);
                linkAccountWithPhoneNumber(phoneAuthCredential);
                Log.i(TAG, "onVerificationCompleted:" + phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                isDisable(false);

                Toast.makeText(LinkEmailAndPasswordWithPhoneNumber.this, "Invalid Phone Number ,Please Enter Correct Phone Number", Toast.LENGTH_SHORT).show();

                sendVerificationCodeButton.setVisibility(View.VISIBLE);

                inputPhoneNumber.setVisibility(View.VISIBLE);
                verifyButton.setVisibility(View.INVISIBLE);
                inputVerificationCode.setVisibility(View.INVISIBLE);
            }

            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;

                isDisable(false);

                Toast.makeText(LinkEmailAndPasswordWithPhoneNumber.this, "Code Has Been Sent", Toast.LENGTH_SHORT).show();

                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                phoneNumberCode.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);
            }
        };

        skipButton.setOnClickListener(v -> sendUserToMainActivity());
    }

    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();

        sendVerificationCodeButton = findViewById(R.id.send_ver_code_button);
        verifyButton = findViewById(R.id.verify_button);
        skipButton = findViewById(R.id.skip_link_phone_number);

        phoneNumberCode = findViewById(R.id.phoneNumberCode);
        inputPhoneNumber = findViewById(R.id.phone_number_input);
        inputVerificationCode = findViewById(R.id.verification_code_input);
        layout = findViewById(R.id.rootView);

        // loading bar
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setVisibility(View.GONE);
        layout.addView(progressBar, params);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LinkEmailAndPasswordWithPhoneNumber.this, MainActivity.class);
        startActivity(mainIntent);
    }

    private void linkAccountWithPhoneNumber(PhoneAuthCredential credential) {
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Link account with phone number: success");
                        Toast.makeText(LinkEmailAndPasswordWithPhoneNumber.this, "Link account with phone number: success",
                                Toast.LENGTH_LONG).show();
                        sendUserToMainActivity();
                    } else {
                        // This credential is already associated with a different user account
                        Log.w(TAG, "This credential: " + credential +" is already associated with a different user account", task.getException());
                        Toast.makeText(LinkEmailAndPasswordWithPhoneNumber.this, "This phone number is already associated with a different user account",
                                Toast.LENGTH_LONG).show();

                        sendVerificationCodeButton.setVisibility(View.VISIBLE);
                        phoneNumberCode.setVisibility(View.VISIBLE);
                        inputPhoneNumber.setVisibility(View.VISIBLE);

                        verifyButton.setVisibility(View.INVISIBLE);
                        inputVerificationCode.setVisibility(View.INVISIBLE);
                    }
                });
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