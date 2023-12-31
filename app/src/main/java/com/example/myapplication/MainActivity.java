package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.myapplication.Activity.Login.LoginActivity;
import com.example.myapplication.Activity.MenuOption.FindFriendsActivity;
import com.example.myapplication.Activity.MenuOption.SettingsActivity;
import com.example.myapplication.Activity.MenuOption.about;
import com.example.myapplication.Fragment.TabsAccessorAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;
    // private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFields();
    }

    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();

        rootRef = FirebaseDatabase.getInstance().getReference();
        mToolbar = findViewById(R.id.main_page_toolbar);
        myViewPager = findViewById(R.id.main_tab_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myTabLayout = findViewById(R.id.main_tabs);

        // setup toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ChatApp");

        // setup option menu
        myViewPager.setAdapter(myTabsAccessorAdapter);

        // setup layout
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            SendUserToLoginActivity();
        } else {
            updateUserStatus("online");
            VerifyUserExistence();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }


    private void VerifyUserExistence() {

        String currentUserID = mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if ((dataSnapshot.child("name").exists())) {
//                     Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();
                } else {
//                     Toast.makeText(MainActivity.this,"",Toast.LENGTH_SHORT).show();
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        // menu item
        if (item.getItemId() == R.id.main_logout_option) {
            updateUserStatus("offline");

            //  LogOutUser();
            mAuth.signOut();
            SendUserToLoginActivity();
        }

        if (item.getItemId() == R.id.main_settings_option) {
            SendUserToSettingsActivity();
        }

        if (item.getItemId() == R.id.main_create_group_option) {
            RequestNewGroup();
        }

        if (item.getItemId() == R.id.main_find_friends_option) {
            SendUserToFriendsActivity();
        }

        if (item.getItemId() == R.id.about) {
            about();
        }

        return true;
    }

    private void about() {
        Intent aboutIntent = new Intent(MainActivity.this, about.class);
        startActivity(aboutIntent);
    }

    private void RequestNewGroup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name:");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g MTech 2017");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", (dialogInterface, i) -> {
            String groupName = groupNameField.getText().toString();
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(MainActivity.this, "Enter Group Name", Toast.LENGTH_SHORT).show();
            } else {
                CreateNewGroup(groupName);

            }
        });


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.cancel();
            }
        });


        builder.show();


    }

    private void CreateNewGroup(final String groupName) {
        rootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, groupName + "Group is Created Successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd , yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserID).child("userState").updateChildren(onlineStateMap);
    }
}
