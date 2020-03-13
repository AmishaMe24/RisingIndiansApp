package com.example.risingindians;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewEventActivity extends AppCompatActivity{


    private Toolbar newEventtoolbar;
    private ImageView newEventImage;
    private EditText newEventDesc;
    private Button newEventbtn;
    private Uri eventImageUri;
    private ProgressDialog progressDialog;
    private StorageReference eventImagesReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String current_user_id;
    private static final int GalleryPick =1;
    private String savecurrentDate, savecurrentTime;
    private String eventrandonKey, downloadImageUrl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        eventImagesReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        newEventtoolbar = findViewById(R.id.add_new_event_toolbar);
        setSupportActionBar(newEventtoolbar);
        getSupportActionBar().setTitle("Add New Event");


        newEventImage = findViewById(R.id.add_new_image);
        newEventDesc = findViewById(R.id.event_desc);
        newEventbtn = findViewById(R.id.submit_event_btn);
        progressDialog = new ProgressDialog(this);


        newEventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        newEventbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String desc = newEventDesc.getText().toString();
                if (!TextUtils.isEmpty(desc) && eventImageUri != null) {

                    progressDialog.setTitle("Uploading");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    Calendar calendar =Calendar.getInstance();

                    SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                    savecurrentDate = currentDate.format(calendar.getTime());

                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
                    savecurrentTime = currentTime.format(calendar.getTime());

                    eventrandonKey =  UUID.randomUUID().toString();

                   final StorageReference filePath = eventImagesReference.child("Events").child(eventrandonKey +".jpg");


                    final UploadTask uploadTask = filePath.putFile(eventImageUri);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            String message = e.toString();
                            Toast.makeText(NewEventActivity.this,"Error: " + message,Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(NewEventActivity.this,"Event Uploaded Successfully",Toast.LENGTH_SHORT).show();

                            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                                    if(!task.isSuccessful()) {
                                        throw task.getException();
                                    }

                                    downloadImageUrl = filePath.getDownloadUrl().toString();
                                    return filePath.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        progressDialog.dismiss();

                                        downloadImageUrl = task.getResult().toString();
                                        Toast.makeText(NewEventActivity.this,"Event successfully added ",Toast.LENGTH_SHORT).show();

                                        HashMap<String, Object> eventMap = new HashMap<>();
                                        eventMap.put("Eventid", eventrandonKey);
                                        eventMap.put("Date",savecurrentDate);
                                        eventMap.put("Time",savecurrentTime);
                                        eventMap.put("Description",desc);
                                        eventMap.put("Image",downloadImageUrl);
                                        eventMap.put("user_id", current_user_id);

                                        Intent mainIntent = new Intent(NewEventActivity.this, HomeActivity.class);
                                        startActivity(mainIntent);
                                        finish();


                                    }
                                    else{
                                        progressDialog.dismiss();
                                        String message = task.getException().toString();
                                        Toast.makeText(NewEventActivity.this,"Error: " + message,Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                        }
                    });


                }
            }
        });




}

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null && data.getData() != null) {
            eventImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), eventImageUri);
                newEventImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
