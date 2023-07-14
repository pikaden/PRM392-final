package com.example.myapplication.Activity.Screen.SingleChat;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Entity.Messages;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverID, messageReceiverName, MessageReceiverImage, messageSenderID;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar ChatToolBar;
    private ImageButton SendMessageButton, SendFilesButton;
    private EditText MessageInputText;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private ProgressDialog loadingBar;
    private String saveCurrentTime, saveCurrentDate;
    private String checker = "", myUrl = "";
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());
    private ChildEventListener messageEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        MessageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        initializeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(MessageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        SendMessageButton.setOnClickListener(view -> SendMessage());

        DisplayLastSeen();

        ActivityResultLauncher<Intent> startActivityForResult = getImageFromGallery();
        ActivityResultLauncher<Intent> startActivityForPDFResult = getPDFFromGallery();

        SendFilesButton.setOnClickListener(view -> {
            CharSequence options[] = new CharSequence[]
                    {
                            "Images",
                            "Document"
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            builder.setTitle("Select the File");

            builder.setItems(options, (dialogInterface, i) -> {
                if (i == 0) {
                    checker = "image";
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult.launch(galleryIntent);
                }
                if (i == 1) {
                    // Code for selecting and sending documents (PDF in this case)
                    checker = "pdf";
                    Intent pdfIntent = new Intent();
                    pdfIntent.setAction(Intent.ACTION_GET_CONTENT);
                    pdfIntent.setType("application/pdf");

                    startActivityForPDFResult.launch(pdfIntent);
                }
                if (i == 2) {
                    checker = "docx";
                    loadingBar.dismiss();
                    Log.i(TAG, "onCreate: sida vcl");

                    Toast.makeText(this, "Please Select", Toast.LENGTH_SHORT).show();
                }
            });

            builder.show();
        });


    }

    private ActivityResultLauncher<Intent> getImageFromGallery() {
        return registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        loadingBar.setTitle("Send File");
                        loadingBar.setMessage("Please Wait...");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        Intent data = result.getData();
                        // get image uri
                        Uri imageUri = data.getData();

                        final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;

                        final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                        // push message
                        DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();

                        final String messagePushID = userMessageKeyRef.getKey();

                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                        // save image to storage
                        final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");

                        // upload image
                        filePath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                            HashMap<String, String> downloadUrl = new HashMap<>();
                            myUrl = downloadUrl.toString().valueOf(uri);

                            Map<String, String> messagepicBody = new HashMap<>();
                            messagepicBody.put("message", myUrl);
                            messagepicBody.put("name", uri.getLastPathSegment());

                            messagepicBody.put("type", checker);
                            messagepicBody.put("from", messageSenderID);
                            messagepicBody.put("to", messageReceiverID);
                            messagepicBody.put("messageID", messagePushID);
                            messagepicBody.put("time", saveCurrentTime);
                            messagepicBody.put("date", saveCurrentDate);

                            // send message
                            Map<String, Object> messageBodyDetails = new HashMap<>();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messagepicBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messagepicBody);

                            RootRef.updateChildren(messageBodyDetails)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            loadingBar.dismiss();
                            MessageInputText.setText("");
                        }));
                    }
                });
    }

    private ActivityResultLauncher<Intent> getPDFFromGallery() {
        return registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        loadingBar.setTitle("Send File");
                        loadingBar.setMessage("Please Wait...");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        Intent data = result.getData();
                        // get image uri
                        Uri pdfUri = data.getData();

                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                        final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                        final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                        DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();
                        final String messagePushID = userMessageKeyRef.getKey();

                        final StorageReference filePath = storageReference.child(messagePushID + ".pdf");

                        // upload pdf file
                        filePath.putFile(pdfUri).addOnCompleteListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                            HashMap<String, String> downloadUrl = new HashMap<>();
                            myUrl = downloadUrl.toString().valueOf(uri);

                            Map<String, Object> messageDocBody = new HashMap<>();
                            messageDocBody.put("message", myUrl);
                            messageDocBody.put("name", uri.getLastPathSegment());
                            messageDocBody.put("type", checker);
                            messageDocBody.put("from", messageSenderID);
                            messageDocBody.put("to", messageReceiverID);
                            messageDocBody.put("messageID", messagePushID);
                            messageDocBody.put("time", saveCurrentTime);
                            messageDocBody.put("date", saveCurrentDate);

                            Map<String, Object> messageBodyDetails = new HashMap<>();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageDocBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageDocBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    loadingBar.dismiss();
                                    MessageInputText.setText("");
                                } else {
                                    loadingBar.dismiss();
                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }));
                    }
                });
    }

    private void initializeControllers() {

        // back button
        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);

        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);

        MessageInputText = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


        loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd , yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }

    private void DisplayLastSeen() {
        RootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("userState").hasChild("state")) {
                    // async task to get image from cloud server
                    executor.execute(() -> {
                        //Background work here
                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                        String time = dataSnapshot.child("userState").child("time").getValue().toString();
                        handler.post(() -> {
                            //UI Thread work here
                            if (state.equals("online")) {
                                userLastSeen.setText("online");
                            } else if (state.equals("offline")) {
                                userLastSeen.setText(date + " " + time);
                            }
                        });
                    });
                } else {
                    userLastSeen.setText("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        // Remove post value event listener
        if (messageEventListener != null && RootRef != null) {
            RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).removeEventListener(messageEventListener);
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Hey, we are onStart!");

        // must clear message list first to prevent duplicate message
        messagesList.clear();

        messageEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // this function iterate all data in database and receive new data
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();

                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(messageEventListener);
    }

    private void SendMessage() {
        String messageText = MessageInputText.getText().toString();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "write message", Toast.LENGTH_SHORT).show();
        } else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;

            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;


            DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map<String, String> messageTextBody = new HashMap<String, String>();

            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map<String, Object> messageBodyDetails = new HashMap<String, Object>();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            //Toast.makeText(ChatActivity.this,"Message Sent Successfully",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
            MessageInputText.setText("");
        }
    }

}
