package com.likethatalsocan.whodat;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by sathy on 29/12/17.
 */

public class WhoDatFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("development");


    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        String myUid = mAuth.getCurrentUser().getUid();

        mDatabase.child("users").child(myUid).child("token").setValue(refreshedToken);

    }
}
