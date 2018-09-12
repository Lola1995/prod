package com.getaplot.getaplot.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.utils.Handy;
import com.getaplot.getaplot.utils.Location;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class FinishActivity extends AppCompatActivity {
    private static final String TAG = "FinishActivity";
    RelativeLayout id;
    private DatabaseReference databaseReference, searchNode;
    private FirebaseAuth auth;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        searchNode = FirebaseDatabase.getInstance().getReference().child("userSearchNode");
        id = findViewById(R.id.rel);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Location location = new Location();
                location.getLocationPermission(FinishActivity.this, FinishActivity.this);

                Location.getDeviceLocation(FinishActivity.this, FinishActivity.this);
            }
        });
        thread.start();
        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        try {
            if (!user.getEmail().equals("")) {
                Log.d(TAG, "onCreate: User has an email");
                Log.d(TAG, "onCreate: Users email " + user.getEmail());
                if (!user.isEmailVerified()) {
                    Log.d(TAG, "onCreate: Email is not verified");

                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            id.setVisibility(View.GONE);
                            Log.d(TAG, "onSuccess: User verification email sent");
                            AlertDialog.Builder builder = new AlertDialog.Builder(FinishActivity.this).setCancelable(false).setTitle("Information")
                                    .setIcon(R.drawable.ic_info).setMessage("Hello " + user.getDisplayName() + " You need to verify your Email address A link has been sent to your mail box,Please click on it and then you will be able to sign in.Thank you")
                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            FirebaseAuth.getInstance().signOut();
                                            Intent startMain = new Intent(Intent.ACTION_MAIN);
                                            startMain.addCategory(Intent.CATEGORY_HOME);
                                            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(startMain);
                                            FirebaseAuth.getInstance().signOut();
                                            finish();

                                        }
                                    });

                            builder.show();
                        }
                    });
                } else {
                    databaseReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("isplace")) {
                                id.setVisibility(View.GONE);
                                Log.d(TAG, "onDataChange: User collidees");
                                FirebaseAuth.getInstance().signOut();
                                AlertDialog.Builder builder = new AlertDialog.Builder(FinishActivity.this).setCancelable(false).setTitle("Information")
                                        .setIcon(R.drawable.ic_error).setMessage("Hello " + " User " + " We are sorry to inform you that your details are currently associated with a place account,If you didnt expect this,you can contact the GetAplot team and we will help you out.Thank you")
                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                AuthUI.getInstance().signOut(FinishActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Intent startMain = new Intent(FinishActivity.this, MainActivity.class);

                                                            startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(startMain);
                                                        }
                                                    }
                                                });


                                            }
                                        });

                                builder.show();


                                return;
                            } else {
                                Log.d(TAG, "onDataChange: Users and place relates okay");


                                final Map<String, Object> userMap = new HashMap<String, Object>();
                                if (user.getDisplayName() == null) {
                                    userMap.put("name", "Change Me");
                                } else {
                                    userMap.put("name", Handy.getTrimmedName(user.getDisplayName()));
                                }

                                if (user.getEmail() != null) {
                                    userMap.put("email", user.getEmail());
                                } else {
                                    userMap.put("email", "user@example.com");
                                }
                                if (user.getPhoneNumber() != null) {
                                    userMap.put("phone", user.getPhoneNumber());
                                }
                                userMap.put("status", "What plot tonight");
                                userMap.put("device_token", FirebaseInstanceId.getInstance().getToken());
                                Log.d(TAG, "onDataChange: Instance " + FirebaseInstanceId.getInstance().getToken());
                                userMap.put("image", "default");

                                databaseReference.child(user.getUid()).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isComplete()) {

                                            if (task.isSuccessful()) {
                                                searchNode.child(user.getUid()).updateChildren(userMap, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                        if (databaseError == null) {
                                                            Toast.makeText(FinishActivity.this, "You are logged in!", Toast.LENGTH_SHORT).show();
                                                            Intent i = new Intent(FinishActivity.this, MainActivity.class);
                                                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(i);

                                                        } else {
                                                            AuthUI.getInstance().signOut(FinishActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    startActivity(new Intent(FinishActivity.this, MainActivity.class));
                                                                }
                                                            });
                                                        }
                                                    }
                                                });

                                            }
                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(FinishActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });


                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    Log.d(TAG, "onCreate: Users email is verified");
                    //so its either from google or some other provider


                }


            } else {
                Log.d(TAG, "onCreate: User is on phone");
                //phone number case

                final Map<String, Object> userMap = new HashMap<String, Object>();
                if (user.getDisplayName() == null) {
                    userMap.put("name", "Change Me");
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName("Change Me")
                            .build();
                    user.updateProfile(profileUpdates);
                } else {
                    userMap.put("name", Handy.getTrimmedName(user.getDisplayName()));
                }

                if (user.getEmail() != null) {
                    userMap.put("email", user.getEmail());
                } else {
                    userMap.put("email", "user@example.com");
                }
                if (user.getPhoneNumber() != null) {
                    userMap.put("phone", user.getPhoneNumber());
                }
                userMap.put("status", "What Plot tonight");
                userMap.put("device_token", FirebaseInstanceId.getInstance().getToken());
                userMap.put("image", "default");

                databaseReference.child(user.getUid()).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isComplete()) {
                            searchNode.child(user.getUid()).updateChildren(userMap);
                            if (task.isSuccessful()) {
                                Toast.makeText(FinishActivity.this, "You are logged in!", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(FinishActivity.this, MainActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);

                            }
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FinishActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });


            }
        } catch (NullPointerException e) {
            Log.d(TAG, "onCreate: NullPointerException: " + e.getMessage());
            //looks like its a phone provider


            Map<String, Object> userMap = new HashMap<String, Object>();
            if (user.getDisplayName() == null) {
                userMap.put("name", "Change Me");
            } else {
                userMap.put("name", user.getDisplayName());
            }

            if (user.getEmail() != null) {
                userMap.put("email", user.getEmail());
            } else {
                userMap.put("email", "user@example.com");
            }
            if (user.getPhoneNumber() != null) {
                userMap.put("phone", user.getPhoneNumber());
            }
            userMap.put("status", "What Plot tonight");
            userMap.put("device_token", FirebaseInstanceId.getInstance().getToken());
            userMap.put("image", "default");

            databaseReference.child(user.getUid()).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isComplete()) {
                        if (task.isSuccessful()) {
                            Toast.makeText(FinishActivity.this, "You are logged in!", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(FinishActivity.this, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);

                        }
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(FinishActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

        }


    }


    @Override
    public void onBackPressed() {
        AuthUI.getInstance().signOut(FinishActivity.this);
        finish();
    }
}
