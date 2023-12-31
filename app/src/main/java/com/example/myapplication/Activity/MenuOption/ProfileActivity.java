package com.example.myapplication.Activity.MenuOption;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID, Current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button SendMessageRequestButton, cancelmessageButtonrequst;
    private DatabaseReference UserRef, ChatRequestRef, ContactRef, NotificationRef;
    private FirebaseAuth mAuth;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");

        ContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");


        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        //Toast.makeText( this,"User ID:"+receiverUserID, Toast.LENGTH_SHORT).show();
        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        SendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);

        cancelmessageButtonrequst = (Button) findViewById(R.id.decline_message_request_button);
        Current_State = "new";


        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && (dataSnapshot.hasChild("image"))) {
                    // async task to get image from cloud server
                    executor.execute(() -> {
                        //Background work here
                        String userImage = dataSnapshot.child("image").getValue().toString();
                        String userName = dataSnapshot.child("name").getValue().toString();
                        String userStatus = dataSnapshot.child("status").getValue().toString();
                        handler.post(() -> {
                            //UI Thread work here
                            Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                            userProfileName.setText(userName);
                            userProfileStatus.setText(userStatus);
                        });
                    });
                    ManageChatRequests();
                } else {
                    // async task to get image from cloud server
                    executor.execute(() -> {
                        //Background work here
                        String userName = dataSnapshot.child("name").getValue().toString();
                        String userStatus = dataSnapshot.child("status").getValue().toString();
                        handler.post(() -> {
                            //UI Thread work here
                            userProfileName.setText(userName);
                            userProfileStatus.setText(userStatus);
                        });
                    });
                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void ManageChatRequests() {

        ChatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserID)) {
                    // async task to get image from cloud server
                    executor.execute(() -> {
                        //Background work here
                        String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                        handler.post(() -> {
                            //UI Thread work here
                            if (request_type.equals("sent")) {
                                Current_State = "request_sent";
                                SendMessageRequestButton.setText("Cancel Request");
                            } else if (request_type.equals("received")) {
                                Current_State = "request_received";
                                SendMessageRequestButton.setText("Accept Request");

                                cancelmessageButtonrequst.setVisibility(View.VISIBLE);
                                cancelmessageButtonrequst.setEnabled(true);

                                cancelmessageButtonrequst.setOnClickListener(view -> CancelChatRequest());
                            }
                        });
                    });
                } else {
                    ContactRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserID)) {
                                Current_State = "friends";
                                SendMessageRequestButton.setText("Removed");
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if (!senderUserID.equals(receiverUserID)) {
            SendMessageRequestButton.setOnClickListener(view -> {
                SendMessageRequestButton.setEnabled(false);
                if (Current_State.equals("new")) {
                    SendChatRequest();
                }

                if (Current_State.equals("request_sent")) {
                    CancelChatRequest();

                }

                if (Current_State.equals("request_received")) {
                    AcceptChatRequest();

                }

                if (Current_State.equals("friends")) {
                    RemoveSpecificContact();

                }

            });
        } else {
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }


    }

    private void RemoveSpecificContact() {
        ContactRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ContactRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        SendMessageRequestButton.setEnabled(true);
                        Current_State = "new";
                        SendMessageRequestButton.setText("Send Request");

                        cancelmessageButtonrequst.setVisibility(View.INVISIBLE);
                        cancelmessageButtonrequst.setEnabled(false);
                    }
                });
            }
        });
    }


    private void AcceptChatRequest() {
        ContactRef.child(senderUserID).child(receiverUserID).child("Contacts").setValue("Saved").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                ContactRef.child(receiverUserID).child(senderUserID).child("Contacts").setValue("Saved").addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {

                        ChatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(task11 -> {
                            if (task11.isSuccessful()) {

                                ChatRequestRef.child(receiverUserID).child(receiverUserID).removeValue().addOnCompleteListener(task111 -> {

                                    SendMessageRequestButton.setEnabled(true);
                                    Current_State = "friends";
                                    SendMessageRequestButton.setText("Removed");

                                    cancelmessageButtonrequst.setVisibility(View.INVISIBLE);
                                    cancelmessageButtonrequst.setEnabled(false);
                                });
                            }
                        });
                    }
                });
            }
        });
    }


    private void CancelChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ChatRequestRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        SendMessageRequestButton.setEnabled(true);
                        Current_State = "new";
                        SendMessageRequestButton.setText("Send Request");

                        cancelmessageButtonrequst.setVisibility(View.INVISIBLE);
                        cancelmessageButtonrequst.setEnabled(false);
                    }
                });
            }
        });
    }

    private void SendChatRequest() {

        ChatRequestRef.child(senderUserID).child(receiverUserID).child("request_type").setValue("sent").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ChatRequestRef.child(receiverUserID).child(senderUserID).child("request_type").setValue("received").addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {

                        HashMap<String, String> chatNotificationMap = new HashMap<>();
                        chatNotificationMap.put("from", senderUserID);
                        chatNotificationMap.put("type", "request");

                        NotificationRef.child(receiverUserID).push().setValue(chatNotificationMap).addOnCompleteListener(task11 -> {
                            if (task11.isSuccessful()) {
                                SendMessageRequestButton.setEnabled(true);

                                Current_State = "request_sent";
                                SendMessageRequestButton.setText("Cancel Request");

                            }

                        });
                    }
                });
            }
        });
    }
}
