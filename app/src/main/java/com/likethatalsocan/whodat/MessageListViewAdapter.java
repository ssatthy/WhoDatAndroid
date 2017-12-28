package com.likethatalsocan.whodat;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by sathy on 27/12/17.
 */

public class MessageListViewAdapter extends ArrayAdapter<Message> {

    private List<Message> messages = new ArrayList<>();
    private Activity context;
    private ArrayAdapter<Message> adapter;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public MessageListViewAdapter(@NonNull Activity context) {
        super(context, R.layout.activity_message);
        this.context = context;
        this.adapter = this;

        mDatabase = FirebaseDatabase.getInstance().getReference().child("development");
        mAuth = FirebaseAuth.getInstance();

        getLastMessages();
    }

    private void getLastMessages() {

        final FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        ChildEventListener listener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final String userId = dataSnapshot.getKey();
                Map<String, String> messageMap = (Map<String, String>) dataSnapshot.getValue();
                final String messageId = messageMap.keySet().toArray()[0].toString();
                final String toUserId = messageMap.get(messageId);

                mDatabase.child("messages").child(messageId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final Message message = dataSnapshot.getValue(Message.class);
                        message.setId(messageId);

                        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue() != null) {
                                    Account account = dataSnapshot.getValue(Account.class);
                                    account.setId(userId);
                                    account.setToId(toUserId);
                                    message.setAccount(account);
                                    messages.add(message);
                                    reloadData();
                                } else {

                                    mDatabase.child("anonymous-users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.getValue() != null) {
                                                Map<String, String> userMap = (Map<String, String>) dataSnapshot.getValue();
                                                String anonymousName = userMap.values().toArray()[0].toString();

                                                Account account = new Account();
                                                account.setId(userId);
                                                account.setToId(toUserId);
                                                account.setName(anonymousName);
                                                account.setAnonymous(true);
                                                message.setAccount(account);
                                                messages.add(message);
                                                reloadData();
                                            } else {

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                System.out.print("child changed + " + dataSnapshot.getKey());


                final String userId = dataSnapshot.getKey();
                Map<String, String> messageMap = (Map<String, String>) dataSnapshot.getValue();
                final String messageId = messageMap.keySet().toArray()[0].toString();
                final String toUserId = messageMap.get(messageId);

                mDatabase.child("messages").child(messageId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final Message message = dataSnapshot.getValue(Message.class);
                        message.setId(messageId);

                        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue() != null) {
                                    Account account = dataSnapshot.getValue(Account.class);
                                    account.setId(userId);
                                    account.setToId(toUserId);
                                    message.setAccount(account);
                                    messages.remove(message);
                                    messages.add(message);
                                    reloadData();
                                } else {

                                    mDatabase.child("anonymous-users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.getValue() != null) {
                                                Map<String, String> userMap = (Map<String, String>) dataSnapshot.getValue();
                                                String anonymousName = userMap.values().toArray()[0].toString();

                                                Account account = new Account();
                                                account.setId(userId);
                                                account.setToId(toUserId);
                                                account.setName(anonymousName);
                                                account.setAnonymous(true);
                                                message.setAccount(account);
                                                messages.remove(message);
                                                messages.add(message);
                                                reloadData();
                                            } else {

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });





            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.child("last-user-message").child(user.getUid()).addChildEventListener(listener);

    }

    private void reloadData() {

        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message message, Message t1) {
                return message.getTimestamp().intValue() > t1.getTimestamp().intValue() ? -1 : 1;
            }
        });

        adapter.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        LastMessageRow lastMessageRow;
        Message message = messages.get(position);

        if(null == v) {
            LayoutInflater inflater = context.getLayoutInflater();
            v = inflater.inflate(R.layout.view_last_message, null, true);
            lastMessageRow = new LastMessageRow(v);
            v.setTag(lastMessageRow);
        } else {
            lastMessageRow = (LastMessageRow) v.getTag();
        }


        lastMessageRow.getNameView().setText(message.getAccount().getName());
        lastMessageRow.getMessageView().setText(message.getMessage());
        lastMessageRow.setAccount(message.getAccount());

        long myLong =  (long) (message.getTimestamp() * 1000);
        Date itemDate = new Date(myLong);
        String time = new SimpleDateFormat("hh:mm a").format(itemDate);
        lastMessageRow.getTimeView().setText(time.toUpperCase());
        System.out.println("Starting async=================");
        DownloadImageTask task = new DownloadImageTask(lastMessageRow.getProfilePictureView());
        task.execute(message.getAccount().getProfileImageUrl());

        return v;
    }

    class LastMessageRow {

        Account account;

        TextView nameView;
        TextView messageView;
        ImageView profilePictureView;
        TextView timeView;

        LastMessageRow(View view) {
            nameView = view.findViewById(R.id.userNameView);
            messageView = view.findViewById(R.id.lastMessage);
            profilePictureView = view.findViewById(R.id.profileImageView);
            timeView = view.findViewById(R.id.timestamp);

        }

        public Account getAccount() {
            return account;
        }

        public void setAccount(Account account) {
            this.account = account;
        }

        public TextView getNameView() {
            return nameView;
        }

        public void setNameView(TextView nameView) {
            this.nameView = nameView;
        }

        public TextView getMessageView() {
            return messageView;
        }

        public void setMessageView(TextView messageView) {
            this.messageView = messageView;
        }

        public ImageView getProfilePictureView() {
            return profilePictureView;
        }

        public void setProfilePictureView(ImageView profilePictureView) {
            this.profilePictureView = profilePictureView;
        }

        public TextView getTimeView() {
            return timeView;
        }

        public void setTimeView(TextView timeView) {
            this.timeView = timeView;
        }
    }
}
