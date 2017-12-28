package com.likethatalsocan.whodat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId = "dummy";
    private Account account;

    CircleImageView imageView;
    Uri selectedImageData;
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("development");

        imageView = findViewById(R.id.imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });

        final Button login = findViewById(R.id.loginButton);
        login.setEnabled(false);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText phoneField = findViewById(R.id.phoneField);
                EditText nameField = findViewById(R.id.nameField);

                if (phoneField.getText() == null || phoneField.getText().toString().isEmpty()
                        || nameField.getText() == null || nameField.getText().toString().isEmpty()
                        || selectedImageData == null ) {

                    Toast.makeText(LoginActivity.this, "Name, phone & profile picture are required.", Toast.LENGTH_LONG).show();
                    return;
                }
                account = new Account();
                account.setName(nameField.getText().toString());
                account.setPhone(phoneField.getText().toString());

                String phoneNumber = phoneField.getText().toString();
                System.out.println("login button clicked" + phoneNumber);

                verifyPhoneNumber(phoneNumber);

            }
        });

        final CheckBox agreeTerms = findViewById(R.id.agreeTerms);
        agreeTerms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                login.setEnabled(agreeTerms.isChecked());
            }
        });


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                System.out.println("=============verification completed" + credential.toString());
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                System.out.println(" ========== verification failed " + e.getMessage());
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                System.out.println("========= code sent" + verificationId);
            }
        };
    }


    private void verifyPhoneNumber(String phoneNumber) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks);

        showVerifyAlert();

    }

    public void showVerifyAlert() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alert_verify_code, null);
        dialogBuilder.setView(dialogView);


        dialogBuilder.setTitle("Verify Phone");
        dialogBuilder.setMessage("Enter the 6-digit number you have received.");
        dialogBuilder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                EditText codeField = dialogView.findViewById(R.id.verificationCodeField);

                if(codeField.getText() == null || codeField.getText().toString().isEmpty()
                        || codeField.getText().toString().length() != 6 ) {
                    Toast.makeText(LoginActivity.this, "Invalid Code.", Toast.LENGTH_LONG).show();
                    return;
                }

                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage("Login in...");
                progressDialog.show();

                String code = codeField.getText().toString();
                System.out.println("Entered code: " + code);
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

                signIn(credential);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }


    private void signIn(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser user = task.getResult().getUser();
                            System.out.println("=============Sign in successful..");

                            File selectedImage = new File(selectedImageData.getPath());
                            byte[] data = null;
                            try {
                                Bitmap compressedImageBitmap = new Compressor(LoginActivity.this).compressToBitmap(selectedImage);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
                                data = baos.toByteArray();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            StorageReference  imageStorageRef = storage.getReference().child("development").child("profile_pictures").child(user.getUid()+".jpg");
                            StorageMetadata metadata = new StorageMetadata.Builder().setContentType("application/octet-stream").build();

                            UploadTask uploadTask = imageStorageRef.putBytes(data, metadata);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {

                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    System.out.println("========Upload success ==========");
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                                    System.out.print("url :" + downloadUrl);

                                    account.setProfileImageUrl(downloadUrl.toString());
                                    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                                    account.setToken(refreshedToken);

                                    mDatabase.child("users").child(user.getUid()).setValue(account);

                                    progressDialog.dismiss();
                                    finish();
                                }
                            });



                        } else {
                            progressDialog.dismiss();
                            System.out.print("verification failed!");
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null)
            System.out.println("No authencation");
        else
            System.out.println("Authencation");
    }


    private void pickImage() {
        Crop.pickImage(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(result.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, result);
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            selectedImageData = Crop.getOutput(result);

            imageView.setImageURI(selectedImageData);
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
