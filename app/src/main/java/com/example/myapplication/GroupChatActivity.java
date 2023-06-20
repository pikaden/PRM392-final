package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class GroupChatActivity extends AppCompatActivity {
    private Toolbar GroupToolBar;
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private FirebaseAuth mAuth;
    private String currentGroupName,currentUserID,currentUserName,currentDate,currentTime;

    private DatabaseReference UsersRef,GroupNameRef,GroupMessageKeyRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        currentGroupName=getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this,currentGroupName,Toast.LENGTH_SHORT).show();

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child( currentGroupName);




        InitializeFields();

        GetUserInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
            saveMessageinfoToDatabase();
            userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

@Override
protected void onStart()
{
    super.onStart();
    GroupNameRef.addChildEventListener(new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
        {
            if(dataSnapshot.exists())
            {
                DisplayMessages(dataSnapshot);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
        {

            if(dataSnapshot.exists())
            {
                DisplayMessages(dataSnapshot);
            }

        }


        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
        {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });
}



    private void InitializeFields()
    {
        mToolbar=(Toolbar)findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        SendMessageButton=(ImageButton)findViewById(R.id.send_message_button);
        userMessageInput=(EditText)findViewById(R.id.input_group_message);
        displayTextMessages=(TextView)findViewById(R.id.group_chat_text_display);
        mScrollView=(ScrollView)findViewById(R.id.my_scroll_view);


        GroupToolBar =(Toolbar)findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(GroupToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        



    }

    private void GetUserInfo()
    {
    UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
        {
            if(dataSnapshot.exists())
            {
                currentUserName=dataSnapshot.child("name").getValue().toString();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError)
        {

        }
    });

    }

  private void  saveMessageinfoToDatabase()
  {
      String  message = userMessageInput.getText().toString();
      String messagekEY =GroupNameRef.push().getKey();

      if(TextUtils.isEmpty(message))
      {
          Toast.makeText(GroupChatActivity.this,"Write Message",Toast.LENGTH_SHORT).show();
      }
      else
      {
          Calendar calForDate =Calendar.getInstance();
          SimpleDateFormat currentDateFormat =new SimpleDateFormat("MMM dd , yyyy");
          currentDate= currentDateFormat.format(calForDate.getTime());


          Calendar calForTime =Calendar.getInstance();
          SimpleDateFormat currentTimeFormat =new SimpleDateFormat("hh:mm a");
          currentTime= currentTimeFormat.format(calForTime.getTime());


          HashMap<String,Object>groupMessageKey=new HashMap<>();
          GroupNameRef.updateChildren(groupMessageKey);

          GroupMessageKeyRef=GroupNameRef.child(messagekEY);
          HashMap<String,Object>messageInfoMap=new HashMap<>();
          messageInfoMap.put("name",currentUserName);
          messageInfoMap.put("message",message);
          messageInfoMap.put("date",currentDate);
          messageInfoMap.put("time",currentTime);
          GroupMessageKeyRef.updateChildren(messageInfoMap);
      }
  }

    private void DisplayMessages(DataSnapshot dataSnapshot)
    {
        Iterator iterator =dataSnapshot.getChildren().iterator();

        while (iterator.hasNext())
        {
           String chatDate=(String)((DataSnapshot)iterator.next()).getValue();
            String chatMessage=(String)((DataSnapshot)iterator.next()).getValue();
            String chatName=(String)((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String)((DataSnapshot)iterator.next()).getValue();
            displayTextMessages.append(chatName+": "+chatMessage + "\n" + chatTime +"\n"+ chatDate+"\n\n");
           

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }




}
