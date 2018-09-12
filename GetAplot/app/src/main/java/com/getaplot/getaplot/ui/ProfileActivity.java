package com.getaplot.getaplot.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.adapters.UsersProfilePagerAdapter;
import com.getaplot.getaplot.utils.Handy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {
    public static final int ACTIVITY_NUM = 3;
    private static final String TAG = "ProfileActivity";
    String user_id;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private UsersProfilePagerAdapter adapter;
    private Toolbar mToolbar;
    private ImageView imageView;
    private DatabaseReference mdatabase;
    private ImageView profileimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        user_id = getIntent().getStringExtra("user_id");
        profileimage = findViewById(R.id.profile_image);
        mdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mdatabase.keepSynced(true);

        mdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: ");
                try {
                    RequestOptions requestOptions = new RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.test);
                    Glide.with(getBaseContext()).load(dataSnapshot.child("image").getValue().toString()).apply(requestOptions).into(profileimage);
                } catch (NullPointerException e) {

                } catch (IllegalArgumentException e) {

                }
                try {
                    getSupportActionBar().setTitle(Handy.getTrimmedName(dataSnapshot.child("name").getValue().toString()));
                } catch (NullPointerException e) {

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        tabLayout = findViewById(R.id.tabs);

        viewPager = findViewById(R.id.viewpager);
        adapter = new UsersProfilePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(user_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            return;
                        } else {
                            getMenuInflater().inflate(R.menu.profile_menu, menu);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if(item.getItemId()==android.R.id.home){
        finish();

    }

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.mess:
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Friends");
                databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Intent i = new Intent(getBaseContext(), ChatActivity.class);
                                    i.putExtra("user_id", user_id);
                                    startActivity(i);
                                } else {
                                    item.setEnabled(false);
                                    item.setVisible(false);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("online").setValue("true");
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).child("online")
                .setValue(ServerValue.TIMESTAMP);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}