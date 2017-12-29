package com.likethatalsocan.whodat;

import android.app.ActionBar;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("development");
    MessageListViewAdapter adapter;
    ListView messageList;

    private static int SELECT_CONTACT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        messageList = findViewById(R.id.messageList);


        messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Account account = ((MessageListViewAdapter.LastMessageRow) view.getTag()).getAccount();
                Intent intent = new Intent(MessageActivity.this, ChatActivity.class);
                intent.putExtra("accountId", account.getId());
                intent.putExtra("toId", account.getToId());
                intent.putExtra("anonymous", account.isAnonymous());
                startActivity(intent);

            }
        });

        FloatingActionButton newMessageButton = findViewById(R.id.newMessageButton);

        newMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageActivity.this, NewMessageActivity.class);
                startActivityForResult(intent, SELECT_CONTACT);
            }
        });

    }

    private void setNameAndProfilePicture() {

        LayoutInflater inflater = getLayoutInflater();
        View titleBar = inflater.inflate(R.layout.view_profile, null);
        final ProfileBarView profileBarView = new ProfileBarView(titleBar);

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(titleBar);

        final String myUid = mAuth.getCurrentUser().getUid();

        mDatabase.child("users").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    Account account = dataSnapshot.getValue(Account.class);
                    account.setId(myUid);
                    profileBarView.getNameView().setText(account.getName());
                    DownloadImageTask task = new DownloadImageTask(profileBarView.getProfileImageView());
                    task.execute(account.getProfileImageUrl());
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

        if (mAuth.getCurrentUser() == null){
            signOut();

        }

        if(adapter == null) {
            setNameAndProfilePicture();
            adapter = new MessageListViewAdapter(this);
            messageList.setAdapter(adapter);
        }


    }

    private void signOut() {
        String uid = null;

        if(mAuth.getCurrentUser() != null) uid = mAuth.getCurrentUser().getUid();

        mAuth.signOut();
        adapter = null;

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        if (uid != null) {
            mDatabase.child("users").child(uid).child("token").setValue("none");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.messageMenu :
                signOut();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SELECT_CONTACT && resultCode == RESULT_OK) {

            Intent intent = new Intent(MessageActivity.this, ChatActivity.class);

            String accountId = data.getStringExtra("accountId");
            String toId = data.getStringExtra("toId");
            if(accountId != null && !accountId.isEmpty()) intent.putExtra("accountId", accountId);
            if(toId != null && !toId.isEmpty()) intent.putExtra("toId", toId);

            startActivity(intent);

        }
    }
}
