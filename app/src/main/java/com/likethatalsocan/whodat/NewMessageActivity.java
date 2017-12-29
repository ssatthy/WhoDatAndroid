package com.likethatalsocan.whodat;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NewMessageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("development");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        ListView contactList = findViewById(R.id.contactList);

        ContactListViewAdapter adapter = new ContactListViewAdapter(this);
        contactList.setAdapter(adapter);

        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ContactListViewAdapter.AccountRow accountRow = (ContactListViewAdapter.AccountRow) view.getTag();
                Account account = accountRow.getAccount();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("accountId",account.getId());
                returnIntent.putExtra("toId", account.getToId());
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Select a friend");

        autoScanContacts();

    }

    private void autoScanContacts() {
        ContentResolver resolver = getContentResolver();
        Cursor contactCursor;

        try {
                contactCursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                if (contactCursor.getCount() == 0) return;

                while (contactCursor.moveToNext()) {
                    if (Integer.parseInt(contactCursor.getString(
                            contactCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) continue;

                    String contactId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor phones = resolver.query(Phone.CONTENT_URI, null,
                            Phone.CONTACT_ID + " = " + contactId, null, null);
                    while (phones.moveToNext()) {
                        String number = phones.getString(phones.getColumnIndex(Phone.NUMBER));

                        if(number.startsWith("+"))
                            addConnections(number);
                    }
                    phones.close();
                }
                contactCursor.close();
        } catch (Exception e) {
            System.out.println("Error accessing contacts" + e.getMessage());
        }
    }

    private void addConnections(String phoneNumber) {

        final String myUid = mAuth.getCurrentUser().getUid();
        mDatabase.child("phones").child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    String userId = (String) dataSnapshot.getValue();
                    mDatabase.child("connections").child(myUid).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() == null) {
                                dataSnapshot.getRef().setValue("none");
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}});

                    mDatabase.child("connections").child(userId).child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() == null) {
                                dataSnapshot.getRef().setValue("none");
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}});
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}});

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_message_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.newMessageCancelMenuItem :
                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
