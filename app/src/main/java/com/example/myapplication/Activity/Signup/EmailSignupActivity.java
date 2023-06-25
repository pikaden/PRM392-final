package com.example.myapplication.Activity.Signup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.Entity.Users;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EmailSignupActivity extends AppCompatActivity {
    private Button createAccount;
    private EditText name, status;
    private CircleImageView userProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String currentUserID;
    private StorageReference userProfileImageRef;
    private ProgressBar progressBar;
    private RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_profile_setting);

        initializeFields();

        createAccount.setOnClickListener(view -> createAccount());

        ActivityResultLauncher<Intent> startActivityForResult = getImageFromGallery();

        // change avatar
        userProfileImage.setOnClickListener(view -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");

            startActivityForResult.launch(galleryIntent);
        });
    }

    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("ProfileImages");
        createAccount = findViewById(R.id.create_profile_button);
        name = findViewById(R.id.name);
        status = findViewById(R.id.status);
        userProfileImage = findViewById(R.id.set_profile_image);
        layout = findViewById(R.id.rootView);

        // loading bar
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setVisibility(View.GONE);
        layout.addView(progressBar, params);
    }

    private ActivityResultLauncher<Intent> getImageFromGallery() {
        return registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        Intent data = result.getData();

                        // get image uri
                        Uri imageUri = data.getData();

                        // set image
                        userProfileImage.setImageURI(imageUri);

                        // upload image to firebase storage
                        final StorageReference filepath = userProfileImageRef.child(currentUserID + ".jpg");

                        filepath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl().addOnSuccessListener(uri -> {

                            HashMap<String, String> downloadUrl = new HashMap<>();
                            String d = downloadUrl.toString().valueOf(uri);
                            progressBar.setVisibility(View.VISIBLE);

                            rootRef.child("Users").child(currentUserID).child("image").setValue(d).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(EmailSignupActivity.this, "Image Save Successfully  ", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                } else {
                                    String message = task.getException().toString();

                                    Toast.makeText(EmailSignupActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }));
                    }
                });
    }

    private void createAccount() {
        String userName = name.getText().toString();
        String userStatus = status.getText().toString();

        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, "Please Write Your Name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(userStatus)) {
            Toast.makeText(this, "Please Write Your Status", Toast.LENGTH_SHORT).show();
        } else {
            // update user's name and status
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("name", userName);
            profileMap.put("status", userStatus);

            rootRef.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    sendUserToMainActivity();
                    Toast.makeText(EmailSignupActivity.this, "Create account successfully", Toast.LENGTH_SHORT).show();
                } else {
                    String message = task.getException().toString();
                    Toast.makeText(EmailSignupActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(EmailSignupActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }

}

