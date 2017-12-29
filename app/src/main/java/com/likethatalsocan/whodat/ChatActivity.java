package com.likethatalsocan.whodat;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(Configuration.environment);

    String accountId;
    String toId;
    boolean anonymous;

    EditText messageEditText;

    private static int GUESS_WHO = 1;

    ChatListRecycleAdapter recycleAdapter;
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        accountId = getIntent().getStringExtra("accountId");
        toId = getIntent().getStringExtra("toId");
        anonymous = getIntent().getBooleanExtra("anonymous", false);


        recyclerView = findViewById(R.id.recyclerChat);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        recycleAdapter = new ChatListRecycleAdapter(this, accountId, toId, anonymous);

        recyclerView.setAdapter(recycleAdapter);

        setNameAndProfilePicture();

        if("none".equals(toId)) {
            setupAnonymousId();
        }


        messageEditText = findViewById(R.id.messageEditView);
        setupSendButton();

    }

    private void setupSendButton() {

        Button sendButton = findViewById(R.id.btnSend);
        final String myUid = mAuth.getCurrentUser().getUid();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String message = messageEditText.getText().toString();
                if(!message.isEmpty()) {
                    messageEditText.setText("");

                    double timestamp = Calendar.getInstance().getTimeInMillis() / 1000.0;
                    final String messageId = mDatabase.child("messages").push().getKey();

                    if(anonymous) {
                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("message", message);
                        messageMap.put("toId", toId);
                        messageMap.put("fromId", myUid);
                        messageMap.put("timestamp", timestamp);

                        mDatabase.child("last-user-message-read").child(toId).child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Long Num =(Long) dataSnapshot.getValue();
                                Long NumAfter = Num+1;
                                dataSnapshot.getRef().setValue(NumAfter);}
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}});

                        mDatabase.child("messages").child(messageId).updateChildren(messageMap, new DatabaseReference.CompletionListener() {

                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                mDatabase.child("user-messages").child(myUid).child(accountId).child(messageId).setValue(0);

                                mDatabase.child("last-user-message").child(myUid).child(accountId).child(messageId).setValue(toId);

                                mDatabase.child("user-messages").child(toId).child(myUid).child(messageId).setValue(0);

                                mDatabase.child("last-user-message").child(toId).child(myUid).child(messageId).setValue(accountId);

                            }
                        });

                    } else {
                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("message", message);
                        messageMap.put("toId", accountId);
                        messageMap.put("fromId", toId);
                        messageMap.put("timestamp", timestamp);

                        mDatabase.child("last-user-message-read").child(accountId).child(toId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue() != null) {
                                    Long Num =(Long) dataSnapshot.getValue();
                                    Long NumAfter = Num+1;
                                    dataSnapshot.getRef().setValue(NumAfter);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}});

                        mDatabase.child("messages").child(messageId).updateChildren(messageMap, new DatabaseReference.CompletionListener() {

                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                mDatabase.child("user-messages").child(myUid).child(accountId).child(messageId).setValue(0);

                                Map<String, Object> lastMsg1 = new HashMap<>();
                                lastMsg1.put(messageId, toId);
                                mDatabase.child("last-user-message").child(myUid).child(accountId).setValue(lastMsg1);

                                mDatabase.child("user-messages").child(accountId).child(toId).child(messageId).setValue(0);

                                Map<String, Object> lastMsg2 = new HashMap<>();
                                lastMsg2.put(messageId, myUid);
                                mDatabase.child("last-user-message").child(accountId).child(toId).setValue(lastMsg2);

                            }
                        });

                    }
                }
            }
        });

    }

    private void setupAnonymousId() {
        System.out.println("Setting up anonymous id ...");

        final String myUid = mAuth.getCurrentUser().getUid();

        String milli = Long.toString(Calendar.getInstance().getTimeInMillis());
        String last3Digit = milli.substring(milli.length()-3);

        final String anonymousId = mDatabase.child("anonymous-users").push().getKey();
        DatabaseReference reference = mDatabase.child("anonymous-users").child(anonymousId);

        Map<String, Object> anonymousMap = new HashMap<>();
        anonymousMap.put(accountId, "Anonymous "+ last3Digit);
        reference.updateChildren(anonymousMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                toId = anonymousId;
                recycleAdapter.setToId(toId);
                observeEndChat();
                mDatabase.child("connections").child(myUid).child(accountId).setValue(toId);

                mDatabase.child("last-user-message-read").child(myUid).child(accountId).setValue(0);
                mDatabase.child("last-user-message-read").child(accountId).child(toId).setValue(0);

            }
        });


    }

    private void setNameAndProfilePicture() {

        LayoutInflater inflater = getLayoutInflater();
        View titleBar = inflater.inflate(R.layout.view_profile, null);
        final ProfileBarView profileBarView = new ProfileBarView(titleBar);

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(titleBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDatabase.child("users").child(accountId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    Account account = dataSnapshot.getValue(Account.class);
                    profileBarView.getNameView().setText(account.getName());
                    DownloadImageTask task = new DownloadImageTask(profileBarView.getProfileImageView());
                    task.execute(account.getProfileImageUrl());
                } else {
                    mDatabase.child("anonymous-users").child(accountId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() != null) {
                                Map<String, String> userMap = (Map<String, String>) dataSnapshot.getValue();
                                String anonymousName = userMap.values().toArray()[0].toString();
                                profileBarView.getNameView().setText(anonymousName);
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
    protected void onStart() {
        super.onStart();

        resetReadCount();

        if(!"none".equals(toId)) observeEndChat();

        observeFailedAttempt();

    }

    private void observeFailedAttempt() {
        String myUid = mAuth.getCurrentUser().getUid();
        mDatabase.child("failed-attempt").child(myUid).child(accountId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    menu.findItem(R.id.chatMenuWho).setVisible(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void observeEndChat() {
        if (anonymous) {
            mDatabase.child("anonymous-users").child(accountId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                    finish();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            mDatabase.child("anonymous-users").child(toId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                    finish();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void resetReadCount() {

        String myUid = mAuth.getCurrentUser().getUid();
        mDatabase.child("last-user-message-read").child(myUid).child(accountId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    dataSnapshot.getRef().setValue(0);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        this.menu = menu;
        MenuItem menuItemWho = menu.findItem(R.id.chatMenuWho);

        if (!anonymous) menuItemWho.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.chatMenuWho :
                System.out.println("Who pressed!.");
                Intent intent = new Intent(this, NewMessageActivity.class);
                startActivityForResult(intent, GUESS_WHO);

                return true;

            case R.id.chatMenuEnd :

                if(anonymous) endAnonymousChat("end");
                else endChat();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GUESS_WHO && resultCode == RESULT_OK) {
            String accountId = data.getStringExtra("accountId");
            if(this.toId.equals(accountId)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("You caught this person.")
                        .setTitle("Well Done!");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        endAnonymousChat("caught");
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();


            } else {
                updateFailedAttempt();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Your guess is wrong.")
                        .setTitle("Missed It!");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }

        }
    }

    private void updateFailedAttempt() {
        String myUid = mAuth.getCurrentUser().getUid();
        mDatabase.child("failed-attempt").child(myUid).child(accountId).setValue(0);
    }

    private void endAnonymousChat(final String stage) {

        String myUid = mAuth.getCurrentUser().getUid();
        mDatabase.child("last-user-message").child(myUid).child(accountId).removeValue();
        mDatabase.child("last-user-message").child(toId).child(myUid).removeValue();

        mDatabase.child("last-user-message-read").child(myUid).child(accountId).removeValue();
        mDatabase.child("last-user-message-read").child(toId).child(myUid).removeValue();

        mDatabase.child("connections").child(toId).child(myUid).setValue("none");

        mDatabase.child("user-messages").child(toId).child(myUid).removeValue();
        mDatabase.child("user-messages").child(myUid).child(accountId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Map<String, Object> messageList = (Map<String, Object>) dataSnapshot.getValue();

                for(String messageId : messageList.keySet()) {
                    mDatabase.child("messages").child(messageId).removeValue();
                }

                dataSnapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("anonymous-users").child(accountId).removeValue();

        mDatabase.child("users").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Account account = dataSnapshot.getValue(Account.class);
                mDatabase.child("chat-ended").child(toId).child(stage).setValue(account.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("failed-attempt").child(myUid).child(accountId).removeValue();

    }

    private void endChat() {

        String myUid = mAuth.getCurrentUser().getUid();
        mDatabase.child("last-user-message").child(myUid).child(accountId).removeValue();
        mDatabase.child("last-user-message").child(accountId).child(toId).removeValue();

        mDatabase.child("last-user-message-read").child(myUid).child(accountId).removeValue();
        mDatabase.child("last-user-message-read").child(accountId).child(toId).removeValue();

        mDatabase.child("connections").child(myUid).child(accountId).setValue("none");

        mDatabase.child("user-messages").child(accountId).child(toId).removeValue();
        mDatabase.child("user-messages").child(myUid).child(accountId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Map<String, Object> messageList = (Map<String, Object>) dataSnapshot.getValue();

                for(String messageId : messageList.keySet()) {
                    mDatabase.child("messages").child(messageId).removeValue();
                }

                dataSnapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("anonymous-users").child(toId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> anonymousMap = (Map<String, String>) dataSnapshot.getValue();
                String anonymousName = anonymousMap.values().toArray()[0].toString();
                mDatabase.child("chat-ended").child(accountId).child("ended").setValue(anonymousName);

                dataSnapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("failed-attempt").child(accountId).child(toId).removeValue();

    }

    public LinearLayoutManager getLinearLayoutManager() {
        return linearLayoutManager;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }
}
