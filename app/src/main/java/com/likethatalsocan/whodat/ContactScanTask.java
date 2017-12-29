package com.likethatalsocan.whodat;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Created by sathy on 29/12/17.
 */

public class ContactScanTask extends AsyncTask<Activity, Void, Void> {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(Configuration.environment);

    @Override
    protected Void doInBackground(Activity... activities) {
        Activity activity = activities[0];

        ContentResolver resolver = activity.getContentResolver();
        Cursor contactCursor;

        try {
            contactCursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (contactCursor.getCount() == 0)  return  null;

            while (contactCursor.moveToNext()) {
                if (Integer.parseInt(contactCursor.getString(
                        contactCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    String contactId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor phones = resolver.query(Phone.CONTENT_URI, null,
                            Phone.CONTACT_ID + " = " + contactId, null, null);
                    while (phones.moveToNext()) {
                        String number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
                        number = number.replaceAll("\\s","");
                        System.out.println("==============Phone no: " + number);
                        if(number.startsWith("+"))
                            addConnections(number);
                    }
                    phones.close();
                }
            }
            contactCursor.close();
        } catch (Exception e) {
            System.out.println("Error accessing contacts" + e.getMessage());
        }

        return null;
    }


    private void addConnections(String phoneNumber) {

        final String myUid = mAuth.getCurrentUser().getUid();
        mDatabase.child("phones").child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    String userId = (String) dataSnapshot.getValue();
                    if(userId.equals(myUid)) return;

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
}
