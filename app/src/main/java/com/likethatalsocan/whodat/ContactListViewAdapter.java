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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sathy on 27/12/17.
 */

public class ContactListViewAdapter extends ArrayAdapter<Account> {

    private List<Account> accounts = new ArrayList<>();
    private Activity context;
    private ArrayAdapter<Account> adapter;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public ContactListViewAdapter(@NonNull Activity context) {
        super(context, R.layout.activity_message);
        this.context = context;
        this.adapter = this;

        mDatabase = FirebaseDatabase.getInstance().getReference().child(Configuration.environment);
        mAuth = FirebaseAuth.getInstance();

        getContactList();
    }

    private void getContactList() {

        final FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        ChildEventListener listener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final String userId = dataSnapshot.getKey();
                final String toUserId = (String) dataSnapshot.getValue();

                mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() != null) {
                            Account account = dataSnapshot.getValue(Account.class);
                            account.setId(userId);
                            account.setToId(toUserId);
                            accounts.add(account);
                            reloadData();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                System.out.print("child changed + " + dataSnapshot.getKey());

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

        mDatabase.child("connections").child(user.getUid()).addChildEventListener(listener);

    }

    private void reloadData() {

        adapter.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return accounts.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        AccountRow accountRow;
        Account account = accounts.get(position);

        if(null == v) {
            LayoutInflater inflater = context.getLayoutInflater();
            v = inflater.inflate(R.layout.view_contact, null, true);
            accountRow = new AccountRow(v);
            v.setTag(accountRow);
        } else {
            accountRow = (AccountRow) v.getTag();
        }


        accountRow.getNameView().setText(account.getName());
        accountRow.getPhoneView().setText(account.getPhone());
        accountRow.setAccount(account);
        DownloadImageTask task = new DownloadImageTask(accountRow.getProfilePictureView());
        task.execute(account.getProfileImageUrl());

        return v;
    }

    class AccountRow {

        Account account;

        TextView nameView;
        TextView phoneView;
        ImageView profilePictureView;

        AccountRow(View view) {
            nameView = view.findViewById(R.id.contactUserNameView);
            phoneView = view.findViewById(R.id.contactPhoneView);
            profilePictureView = view.findViewById(R.id.contactProfileImageView);


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

        public TextView getPhoneView() {
            return phoneView;
        }

        public void setPhoneView(TextView phoneView) {
            this.phoneView = phoneView;
        }

        public ImageView getProfilePictureView() {
            return profilePictureView;
        }

        public void setProfilePictureView(ImageView profilePictureView) {
            this.profilePictureView = profilePictureView;
        }
    }
}
