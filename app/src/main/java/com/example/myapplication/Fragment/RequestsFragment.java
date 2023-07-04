package com.example.myapplication.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Entity.Contacts;
import com.example.myapplication.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private View requestsFragmentView;
    private RecyclerView myRequestsList;
    private DatabaseReference chatRequestsRef, userRef, contactRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        myRequestsList = (RecyclerView) requestsFragmentView.findViewById(R.id.chat_requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return requestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(chatRequestsRef.child(currentUserID), Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(final RequestViewHolder holder, int position, Contacts model) {
                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);

                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                        final String list_user_id = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String type = dataSnapshot.getValue().toString();
                                    if (type.equals("received")) {
                                        userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {

                                                    final String requestProfileImage = dataSnapshot.child("image").getValue().toString();


                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);
                                                }

                                                final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("Wants To Connect With You");

                                                // accept friend request
                                                holder.acceptButton.setOnClickListener(v -> acceptFriendRequest(list_user_id));

                                                // cancel friend request
                                                holder.cancelButton.setOnClickListener(v -> deleteFriendRequest(list_user_id));
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    } else if (type.equals("sent")) {
                                        Button request_sent_btn = holder.acceptButton;
                                        request_sent_btn.setText("Cancel request");
                                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);

                                        userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {
                                                    final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);
                                                }

                                                final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("You have sent Friend Request to " + requestUserName);

                                                // cancel friend request
                                                holder.acceptButton.setOnClickListener(v -> cancelFriendRequest(list_user_id));
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        RequestViewHolder holder = new RequestViewHolder(view);
                        return holder;
                    }
                };
        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    /**
     * user can cancel friend request
     * @param list_user_id
     */
    private void cancelFriendRequest(String list_user_id) {
        chatRequestsRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRequestsRef.child(list_user_id).child(currentUserID).removeValue().addOnCompleteListener(task13 -> {
                    if (task13.isSuccessful()) {
                        Toast.makeText(getContext(), "Friend Request Deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * receiver can cancel friend request
     * @param list_user_id
     */
    private void deleteFriendRequest(String list_user_id) {
        chatRequestsRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRequestsRef.child(list_user_id).child(currentUserID).removeValue().addOnCompleteListener(task12 -> {
                    if (task12.isSuccessful()) {
                        Toast.makeText(getContext(), "Friend Request Deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * receiver can accept friend request
     * @param list_user_id
     */
    private void acceptFriendRequest(String list_user_id) {
        contactRef.child(currentUserID).child(list_user_id).child("Contact").setValue("Saved").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                contactRef.child(list_user_id).child(currentUserID).child("Contact").setValue("Saved").addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {

                        chatRequestsRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(task11 -> {
                            if (task11.isSuccessful()) {

                                chatRequestsRef.child(list_user_id).child(currentUserID).removeValue().addOnCompleteListener(task111 -> {
                                    if (task111.isSuccessful()) {
                                        Toast.makeText(getContext(), "Now,You Are A Friend", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptButton, cancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);

            userStatus = itemView.findViewById(R.id.user_status);

            profileImage = itemView.findViewById(R.id.users_profile_image);

            acceptButton = itemView.findViewById(R.id.request_accept_btn);

            cancelButton = itemView.findViewById(R.id.request_cancel_btn);
        }
    }

}
