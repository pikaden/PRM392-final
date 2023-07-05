package com.example.myapplication.Activity.MenuOption;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangePassword extends AppCompatActivity {

    EditText currentPassword, newPassword;
    Button changePassword;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;

    private ProgressBar progressBar;
    private Toolbar SettingsToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        initializeFields();

        changePassword.setOnClickListener(v -> {
            String currentP = currentPassword.getText().toString().trim();
            String newP = newPassword.getText().toString().trim();
            if (TextUtils.isEmpty(currentP)) {
                Toast.makeText(this, "Enter your current Password", Toast.LENGTH_SHORT).show();
            }
            if (newP.length() < 6) {
                Toast.makeText(this, "Your new Password must > 6", Toast.LENGTH_SHORT).show();
            }
            changePassword(currentP, newP);
        });
    }

    private void initializeFields() {
        changePassword = findViewById(R.id.password_change_button);
        currentPassword = findViewById(R.id.current_password);
        newPassword = findViewById(R.id.new_password);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);

        SettingsToolBar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");
    }

    private void changePassword(String currentPassword, String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(authCredential).addOnSuccessListener(v -> {
            user.updatePassword(newPassword).addOnSuccessListener(v1 -> {
                SendUserToMainActivity();
                Toast.makeText(this, "Change Password Success", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ChangePassword.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}