package com.likethatalsocan.whodat;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by sathy on 28/12/17.
 */

public class ProfileBarView {

    TextView nameView;
    ImageView profileImageView;

    public ProfileBarView(View view) {
        nameView = view.findViewById(R.id.barUsernameView);
        profileImageView = view.findViewById(R.id.barProfileImageView);
    }

    public TextView getNameView() {
        return nameView;
    }

    public void setNameView(TextView nameView) {
        this.nameView = nameView;
    }

    public ImageView getProfileImageView() {
        return profileImageView;
    }

    public void setProfileImageView(ImageView profileImageView) {
        this.profileImageView = profileImageView;
    }
}
