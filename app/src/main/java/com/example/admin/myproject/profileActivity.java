package com.example.admin.myproject;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class profileActivity extends AppCompatActivity {

    private final int PICK_IMAGE_REQUEST = 1;
    private ImageView profilePhoto;
    private Button upload;
    private TextView name, email, location, time;
    FirebaseStorage storage;
    Geocoder geocoder;
    List<Address> addresses;
    String AddressOfUser;
    public AddressResultReceiver mResultReceiver;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 7;

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            final String mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    location.setText(mAddressOutput);
                }
            });
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(getApplicationContext(),getString(R.string.address_found),Toast.LENGTH_SHORT).show();
            }

        }
    }
    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        startService(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mResultReceiver = new AddressResultReceiver(null);
        startIntentService();
        storage = FirebaseStorage.getInstance();
        profilePhoto = (ImageView) findViewById(R.id.profilePhoto);
        upload = (Button) findViewById(R.id.upload);
        name = (TextView) findViewById(R.id.profileName);
        email = (TextView) findViewById(R.id.profileEmail);
        location = (TextView) findViewById(R.id.profileLocation);
        time = (TextView)findViewById(R.id.time);
        if (!userList.getUser().getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            Log.d("user", userList.getUser().getUid());
            upload.setVisibility(View.INVISIBLE);
        } else {
            Log.d("current", FirebaseAuth.getInstance().getCurrentUser().getUid());
            upload.setVisibility(View.VISIBLE);
        }

        upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                upload();
            }
        });

        time.setText(GetDate.getDate(userList.getUser().getTimestamp()));
        name.setText(userList.getUser().getName());
        email.setText(userList.getUser().getEmail());
        location.setText(userList.getUser().getlatlng());
        if(userList.getUser().getUrl()==null || userList.getUser().getUrl().equals("")) {

        }
        else{
            Picasso
                    .with(profileActivity.this)
                    .load(userList.getUser().getUrl())
                    .placeholder(R.drawable.placeholder)
                    .fit()
                    .centerCrop()
                    .into(profilePhoto);
        }
    }

    private void upload() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                       MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return;
            }
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            StorageReference storageReference = storage.getReference().child("images" + "/" + userList.getUser().getUid());
            UploadTask uploadTask = storageReference.putFile(uri);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(profileActivity.this, "failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(profileActivity.this, "success", Toast.LENGTH_SHORT).show();
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Log.d("url", downloadUrl.toString());
                    Picasso
                            .with(profileActivity.this)
                            .load(downloadUrl.toString())
                            .placeholder(R.drawable.placeholder)
                            .fit()
                            .centerCrop()
                            .into(profilePhoto);

                    FirebaseDatabase.getInstance().getReference()
                            .child("users").child(userList.getUser().getUid()).child("url")
                            .setValue(downloadUrl.toString()).addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // successfully added user
                                    } else {
                                        // failed to add user
                                    }
                                }
                            });
                    }

                }

                );
                // Observe state change events such as progress, pause, and resume
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>(){
                    @Override
                        public void onProgress (UploadTask.TaskSnapshot taskSnapshot){
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d("progress", String.valueOf(progress));
                }
                }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                    @Override
                        public void onPaused (UploadTask.TaskSnapshot taskSnapshot){
                            Toast.makeText(profileActivity.this, "paused", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        }
    }
