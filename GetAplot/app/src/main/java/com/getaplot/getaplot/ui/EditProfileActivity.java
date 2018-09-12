package com.getaplot.getaplot.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.ui.settings.AllSettingsActivity;
import com.getaplot.getaplot.utils.Handy;
import com.getaplot.getaplot.utils.Permissions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class EditProfileActivity extends AppCompatActivity{

    private static final String TAG = "EditProfileActivity";
    private static final int GALLERY_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    FirebaseUser user;
    ProgressBar progressBar;
    private CircleImageView changeProfilePhoto;
    private ProgressDialog progressDialog;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference, userSearchNode, userPhotoes, userPoints;
    private EditText display_name, description, mail, phoneNumber;
    private ImageView backArrow, saveChanges;
    private String current_uid;
    private CircleImageView openCam;
    private Context mContext;
    //vars
    private Bitmap mSelectedBitmap;
    private Uri mSelectedUri;
    private byte[] mUploadBytes;
    private double mProgress = 0;
    private double points;
    private EditText userName;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Log.d(TAG, "onCreate: started");
        //---------------get photo from the dialog

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");

        if (checkPermissionsArray(Permissions.PERMISSIONS)) {

        } else {
            verifyPermissions(Permissions.PERMISSIONS);
        }


        progressDialog = new ProgressDialog(EditProfileActivity.this);
        mStorage = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        userName = findViewById(R.id.user_name);

        current_uid = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        userSearchNode = FirebaseDatabase.getInstance().getReference().child("userSearchNode").child(current_uid);
        userPhotoes = FirebaseDatabase.getInstance().getReference().child("userPhotos").child(current_uid);
        userPoints = FirebaseDatabase.getInstance().getReference().child("userPoints").child(current_uid);

        userPoints.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    points = Double.parseDouble(dataSnapshot.child("points").getValue().toString());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        changeProfilePhoto = findViewById(R.id.changeProfilePhoto);
        changeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePhoto();
            }
        });
        initWidgets();
        loadUserData();
        updateImage();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;

            case R.id.saveChanges:
               validateUserProfileData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateImage() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid())
                .child("image").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.VISIBLE);
                try {
                    RequestOptions requestOptions = new RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.test);
                    Glide.with(getBaseContext()).load(mAuth.getCurrentUser().getPhotoUrl()).apply(requestOptions).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                            .into(changeProfilePhoto);
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "onDataChange: IllegalArgumentException" + e.getMessage());
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    //INITIALISE THE WIDGETS--------------------------------------------------
    public void initWidgets() {
        Log.d(TAG, "initWidgets: Widgets Initialised");
        backArrow = findViewById(R.id.backArrow);
        progressBar = findViewById(R.id.progressBar);
        display_name = findViewById(R.id.display_name);
        mAuth = FirebaseAuth.getInstance();
        display_name.setText(mAuth.getCurrentUser().getDisplayName());
        description = findViewById(R.id.description);
        openCam = findViewById(R.id.opencam);
        mContext = EditProfileActivity.this;
        openCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: opening dialog to choose new photo");
                //todo make the dialog fragment robust and use it instead

                openCamera();
            }
        });
        mail = findViewById(R.id.email);
        mail.setText(mAuth.getCurrentUser().getEmail());



    }
//----------------CAMERA INTENT-----------------------------------------//


    private void openCamera() {

        if ((EditProfileActivity.this.checkPermissions(Permissions.CAMERA_PERMISSION[0]))) {
            Log.d(TAG, "onClick: starting camera");
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } else {
            verifyPermissions(Permissions.CAMERA_PERMISSION);
        }


    }


    //--------------------------------VALIDATION----------------------------------------
    private void validateUserProfileData() {
        final String username = display_name.getText().toString().trim();
        String status = description.getText().toString().trim();
        String em = mail.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            return;
        }


        if (Handy.isValidEmail(em)) {
            updateUserInfo(username, em, status);
        } else {
            Toast.makeText(this, "The email incorrect,please check it again", Toast.LENGTH_SHORT).show();
        }


        //TODO MANAGE VALIDATION


    }

    //-----------------------------------------DO THE UPDATE NOW-------------------------------//
    private void updateUserInfo(final String username, String em, String status) {
        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        progressDialog.setMessage("Please wait while your your profile gets updated");
        progressDialog.show();
        final Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", Handy.getTrimmedName(username));
        userMap.put("email", em);
        userMap.put("status", status);
        Map map = new HashMap();
        Double v = points + 0.25;
        map.put("points", v);
        map.put("fitnessPoint", Handy.fitnessPoint(v));
        userPoints.setValue(map);
        databaseReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    userSearchNode.updateChildren(userMap);
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(Handy.getTrimmedName(username))
                            .build();
                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            Log.d(TAG, "onComplete: Updated success");
                            Toast.makeText(EditProfileActivity.this, "Information has been updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } else {
                    progressDialog.hide();
                    Toast.makeText(EditProfileActivity.this, "Sorry could not update your profile Info", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void loadUserData() {
        Log.d(TAG, "loadUserData: Loading users data");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    String name = dataSnapshot.child("name").getValue().toString();

                    String email = dataSnapshot.child("email").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String imageUrl = dataSnapshot.child("image").getValue().toString();
                    description.setText(status);
                    mail.setText(email);
                    if (!imageUrl.equals("default")) {
                        try {
                            Glide.with(getBaseContext()).load(imageUrl).listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);

                                    e.printStackTrace();
                                    Log.d(TAG, "onException: " + e.getMessage());
                                    Toast.makeText(mContext, "could not load your photo", Toast.LENGTH_SHORT).show();

                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            }).into(changeProfilePhoto);

                            progressBar.setVisibility(View.GONE);
                        } catch (IllegalArgumentException e) {

                        }
                    }
                } catch (NullPointerException e) {

                }
                try {
                    String username = dataSnapshot.child("userName").getValue().toString();
                    Log.d(TAG, "onDataChange: username" + username);
                    userName.setText(username);
                } catch (NullPointerException e) {

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void changePhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri mUri = data.getData();
            //pass result to crop activity
            CropImage.activity(mUri).setAspectRatio(1, 1)
                    .start(EditProfileActivity.this);

        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: done taking a photo.");
            Log.d(TAG, "onActivityResult: attempting to navigate to final share screen.");
            progressDialog.setTitle("Uploading image...");
            progressDialog.setMessage("Please wait while image gets uploaded");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            Bitmap bitmap;
            bitmap = (Bitmap) data.getExtras().get("data");


//prepea fpr bitmap uploading

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            final byte[] bytes = byteArrayOutputStream.toByteArray();


            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference filePath = mStorage.child("profile_images").child(current_uid + ".jpg");
            filePath.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                    final Map<String, Object> hmap = new HashMap<String, Object>();
                    hmap.put("image", downloadUrl);
                    hmap.put("thumb_image", downloadUrl);
                    hmap.put("lastUpdated", Handy.fitnessNumber());
                    Glide.with(EditProfileActivity.this).load(downloadUrl).into(changeProfilePhoto);
                    databaseReference.updateChildren(hmap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                userSearchNode.updateChildren(hmap);
                                userPhotoes.push().setValue(hmap);
                                Map map = new HashMap();
                                Double v = points + 0.25;
                                map.put("points", v);
                                map.put("fitnessPoint", Handy.fitnessPoint(v));
                                userPoints.setValue(map);
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setPhotoUri(Uri.parse(downloadUrl))
                                        .build();
                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProfileActivity.this, "Photo updated successfully", Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(mContext, AllSettingsActivity.class));
                                                finish();
//todo figurre out how to remove the null
                                            }
                                        });


                            }
                        }

                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();


                    Toast.makeText(EditProfileActivity.this, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();


                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });


        }
        //----------------PREPARE IMAGE FOR UPLOADING------------------------/
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);
            progressDialog.setMessage("Please wait while image gets uploaded");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            Uri resultUri = activityResult.getUri();
            final File thumb_filePath = new File(resultUri.getPath());

            Bitmap thumb_bitmap = null;
            try {
                thumb_bitmap = new Compressor(EditProfileActivity.this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(100)
                        .compressToBitmap(thumb_filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

//prepea fpr bitmap uploading

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            final byte[] thumb_byte = byteArrayOutputStream.toByteArray();
            StorageReference filePath = mStorage.child("profile_images").child(current_uid + ".jpg");
            final StorageReference thumb_fPath = mStorage.child("profile_images").child("thumbs").child(current_uid + ".jpg");
            filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        final String downloadUrl = task.getResult().getDownloadUrl().toString();

                        //CREATE AN UPLOAD TASK------------------------//

                        UploadTask uploadTask = thumb_fPath.putBytes(thumb_byte);
                        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();


                                Toast.makeText(EditProfileActivity.this, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();


                                Log.d(TAG, "onProgress: upload progress: " + progress + "% done");

                            }
                        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> ttask) {
                                if (ttask.isSuccessful()) {
                                    final String thumb_downloadUrl = ttask.getResult().getDownloadUrl().toString();
                                    final Map<String, Object> hmap = new HashMap<String, Object>();
                                    hmap.put("image", thumb_downloadUrl);
                                    hmap.put("thumb_image", downloadUrl);
                                    hmap.put("lastUpdated", Handy.fitnessNumber());
                                    Map map = new HashMap();
                                    Double v = points + 0.25;
                                    map.put("points", v);
                                    map.put("fitnessPoint", Handy.fitnessPoint(v));
                                    userPoints.setValue(map);

                                    databaseReference.updateChildren(hmap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError == null) {
                                                userSearchNode.updateChildren(hmap);
                                                userPhotoes.push().setValue(hmap);
                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setPhotoUri(Uri.parse(thumb_downloadUrl))
                                                        .build();
                                                user.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                try {
                                                                    progressDialog.dismiss();
                                                                } catch (IllegalArgumentException e) {
                                                                    Log.d(TAG, "onComplete: IllegalArgumentException:View not attached to window" + e.getMessage());
                                                                }
                                                                Toast.makeText(EditProfileActivity.this, "Photo updated successfully", Toast.LENGTH_LONG).show();
                                                                startActivity(new Intent(mContext, AllSettingsActivity.class));
                                                                finish();

                                                            }
                                                        });


                                            }
                                        }

                                    });
                                } else {
                                    Log.d(TAG, "onComplete: Error uploading thumb");
                                    Toast.makeText(EditProfileActivity.this, "Error uploading photo", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });


                    } else {
                        Toast.makeText(EditProfileActivity.this, "Error uploading photo", Toast.LENGTH_SHORT).show();
                    }

                }
            });


        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.child("online").setValue("true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.child("online").setValue(ServerValue.TIMESTAMP);
    }

    /**
     * verifiy all the permissions passed to the array
     *
     * @param permissions
     */
    public void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(
                EditProfileActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    /**
     * Check an array of permissions
     *
     * @param permissions
     * @return
     */
    public boolean checkPermissionsArray(String[] permissions) {
        Log.d(TAG, "checkPermissionsArray: checking permissions array.");

        for (int i = 0; i < permissions.length; i++) {
            String check = permissions[i];
            if (!checkPermissions(check)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check a single permission is it has been verified
     *
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission) {
        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(EditProfileActivity.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: \n Permission was not granted for: " + permission);
            Toast.makeText(this, "Permissions not granted to access camera,\n" +
                    "please give permissions to GetAplot", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }


}

