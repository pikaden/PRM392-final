package com.example.myapplication.Activity.MenuOption;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings, changePasswordSetting;
    private EditText name, status;
    private CircleImageView userProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;
    private StorageReference UserProfileImageRef;
    private ProgressBar progressBar;
    private Toolbar SettingsToolBar;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        initializeFields();

        updateAccountSettings.setOnClickListener(view -> updateSettings());
        changePasswordSetting.setOnClickListener(view -> openChangePassword());

        RetrieveUserInfo();

        ActivityResultLauncher<Intent> startActivityForResult = getImageFromGallery();

        // change avatar
        userProfileImage.setOnClickListener(view -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");

            startActivityForResult.launch(galleryIntent);
        });
    }

    public void openChangePassword() {
        Intent intent = new Intent(this, ChangePassword.class);
        startActivity(intent);
    }

    private void initializeFields() {

        updateAccountSettings = findViewById(R.id.update_settings_button);
        changePasswordSetting = findViewById(R.id.password_change_button);
        name = findViewById(R.id.name);
        status = findViewById(R.id.status);
        userProfileImage = findViewById(R.id.set_profile_image);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);

        SettingsToolBar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");
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
                        final StorageReference filepath = UserProfileImageRef.child(currentUserID + ".jpg");

                        filepath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl().addOnSuccessListener(uri -> {

                            HashMap<String, String> downloadUrl = new HashMap<>();
                            String d = downloadUrl.toString().valueOf(uri);
                            progressBar.setVisibility(View.VISIBLE);

                            RootRef.child("Users").child(currentUserID).child("image").setValue(d).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SettingsActivity.this, "Image Save Successfully  ", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                } else {
                                    String message = task.getException().toString();

                                    Toast.makeText(SettingsActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }));
                    }
                });
    }

    private void updateSettings() {
        String userName = name.getText().toString();

        String userStatus = status.getText().toString();

        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, "Please Write Your Name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(userStatus)) {
            Toast.makeText(this, "Please Write Your Status", Toast.LENGTH_SHORT).show();
        } else {
            // update user name and status
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("name", userName);
            profileMap.put("status", userStatus);

            RootRef.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Profile Update Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    String message = task.getException().toString();
                    Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {
                    // async task to get information from cloud server
                    executor.execute(() -> {
                        //Background work here
                        String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                        String retrievesStatus = dataSnapshot.child("status").getValue().toString();
                        String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                        handler.post(() -> {
                            //UI Thread work here
                            name.setText(retrieveUserName);
                            status.setText(retrievesStatus);

                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                        });
                    });
                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                    // async task to get information from cloud server
                    executor.execute(() -> {
                        //Background work here
                        String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                        String retrievesStatus = dataSnapshot.child("status").getValue().toString();
                        handler.post(() -> {
                            //UI Thread work here
                            name.setText(retrieveUserName);
                            status.setText(retrievesStatus);
                        });
                    });
                } else {
                    Toast.makeText(SettingsActivity.this, "Update Your Profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SettingsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
