package com.getaplot.getaplot.ui.settings;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.getaplot.getaplot.*;
import com.getaplot.getaplot.ui.ChangeStatusActivity;
import com.getaplot.getaplot.ui.EditProfileActivity;
import com.getaplot.getaplot.ui.SettingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllSettingsActivity extends AppCompatActivity {
    private static final String TAG = "AllSettingsActivity";
    String current_uid;
    TextView us;
    private ListView listView;
    private ImageView backArrow;
    private FirebaseUser currentfirebaseUser;
    private CircleImageView profile_pic;
    private TextView mName, mStatus;
    private StorageReference mStorage;
    private ProgressDialog progressDialog;
    private ProgressBar profileProgress;
    private ImageView isactive;
    private String name, status;
    private Context mContext = this;
    private DatabaseReference mUserRef, rootRef, friendsRef, placeRef;
    private TextView display, website, textEdityourprofile;
    private FirebaseAuth mAuth;
    private RelativeLayout top, me, meme;
    private String experiencefinal;
    private TextView userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_settings);
        Toolbar toolbar = findViewById(R.id.relLayout2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account and Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        top = findViewById(R.id.top);
        me = findViewById(R.id.me);
        us = findViewById(R.id.us);
        mContext = this;
        userName = findViewById(R.id.userName);
        meme=findViewById(R.id.meme);
        isactive=findViewById(R.id.isactive);

        meme.setOnClickListener(v -> startActivity(new Intent(getBaseContext(), com.getaplot.getaplot.SettingsActivity.class)));

        //-----------------DATABASE-----------------------------------

        //-----------------------DATABASE--------------------------
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        current_uid = mAuth.getCurrentUser().getUid();
        mUserRef = rootRef.child("Users").child(current_uid);
        mUserRef.keepSynced(true);
        friendsRef = rootRef.child("Friends").child(current_uid);
        placeRef = rootRef.child("Users").child(current_uid).child("subscribedTo");
        friendsRef.keepSynced(true);
        placeRef.keepSynced(true);
        mStorage = FirebaseStorage.getInstance().getReference();
//        progressDialog = new ProgressDialog(mContext);
        mAuth = FirebaseAuth.getInstance();
        currentfirebaseUser = mAuth.getCurrentUser();
        current_uid = currentfirebaseUser.getUid();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);


        //connection state
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    System.out.println("connected");
                } else {
                    System.out.println("not connected");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });

        textEdityourprofile=findViewById(R.id.textEditProfile);
        textEdityourprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, EditProfileActivity.class));
            }
        });

        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }

        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    getPlaceFavouritesCount();
                    getUserFriendCount();
                }
                handler.sendEmptyMessage(0);
            }

        };
        Thread thread = new Thread(runnable);
        thread.start();


        ///LOAD DATA--------------------------------
         profileProgress=findViewById(R.id.profileloadingbar);
        profileProgress.setVisibility(View.VISIBLE);
        mUserRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    String username = dataSnapshot.child("userName").getValue().toString();
                    userName.setText(username);
                } catch (NullPointerException e) {
                    userName.setText("Username unavailable");
                }
                try {
                    name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();
                    RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.test);
                    us.setText(name);
                    try {
                        Glide.with(mContext)
                                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).apply(requestOptions)
                                .into(profile_pic);
                    }catch (NullPointerException x){
                        Glide.with(mContext)
                                .load(image).apply(requestOptions)
                                .into(profile_pic);
                    }catch (IllegalArgumentException e){

                    }
                    profileProgress.setVisibility(View.INVISIBLE);

                } catch (Exception e) {

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        //-------------------CREATE SETTINGS OBJECTS---//

        top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, AccountConfigsettingsActivity.class);
                AllSettingsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                i.putExtra("key", "general");
                startActivity(i);
            }
        });
        me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, SettingActivity.class);
                AllSettingsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                i.putExtra("key", "general");
                startActivity(i);
            }
        });

    }

    private void getPlaceFavouritesCount() {
        Log.d(TAG, "getPlaceFavouritesCount: ");
        placeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "got places: datasnapsht " + dataSnapshot);
                int count = (int) dataSnapshot.getChildrenCount();
                Log.d(TAG, "userfollows " + count);
                TextView tvPlaces = findViewById(R.id.tvPlaces);
                tvPlaces.setText(String.valueOf(count));
                TextView textPlaces = findViewById(R.id.textPlaces);
                if (count == 1) {
                    String text = (String) textPlaces.getText();
                    String finaltext = text.replace("s", "");
                    textPlaces.setText(finaltext);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUserFriendCount() {
        Log.d(TAG, "getUserFriendCount: ");
        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                Log.d(TAG, "onChildAdded: +current children" + count);
                TextView tvFriends = findViewById(R.id.tvFriensds);
                tvFriends.setText(Integer.toString(count));
                TextView textfriends = findViewById(R.id.textFriends);
                if (count == 1) {
                    String text = (String) textfriends.getText();
                    String finaltext = text.replace("s", "");
                    textfriends.setText(finaltext);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void handleChangeActivity(String status) {
        Intent statusIntent = new Intent(mContext, ChangeStatusActivity.class);
        AllSettingsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        statusIntent.putExtra("c_status", status);
        startActivity(statusIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
