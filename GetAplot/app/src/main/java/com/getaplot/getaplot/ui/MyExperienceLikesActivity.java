package com.getaplot.getaplot.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.ExLike;
import com.getaplot.getaplot.utils.GetTimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyExperienceLikesActivity extends AppCompatActivity {
    private static final String TAG = "MyExperienceLikes";
    private static final int ACTIVITY_NUM = 3;
    ImageView b;
    private Toolbar mToolbar;
    private RecyclerView mAttendeesList;
    private Context mContext;
    private DatabaseReference likesRef, rootRef, userRef, friendRef;
    FirebaseRecyclerAdapter adapter;
    private Boolean isBuddy = false;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_experience_likes);
        Toolbar toolbar = findViewById(R.id.myexlikesbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Liked by");
        mContext = MyExperienceLikesActivity.this;
        progressBar = findViewById(R.id.people);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAttendeesList = findViewById(R.id.exlikesList);
        mAttendeesList.setItemAnimator(new DefaultItemAnimator());
        mAttendeesList.setHasFixedSize(true);
        mAttendeesList.setLayoutManager(new LinearLayoutManager(mContext));
        // setUpBottomNavigationView();
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.keepSynced(true);
        likesRef = rootRef.child("EXLikes").child(getIntent().getStringExtra("exlike_id"));
        Log.d(TAG, "onCreate: " + getIntent().getStringExtra("exlike_id"));
        likesRef.keepSynced(true);
        friendRef = rootRef.child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseRecyclerOptions<ExLike> options =
                new FirebaseRecyclerOptions.Builder<ExLike>()
                        .setQuery(likesRef.orderByChild("fitnessNum"), ExLike.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<ExLike, PeopleLikingMyExperience>(options) {
            @Override
            public PeopleLikingMyExperience onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_experince_layout, parent, false);

                return new PeopleLikingMyExperience(view);
            }

            @Override
            protected void onBindViewHolder(final PeopleLikingMyExperience viewHolder, int position, final ExLike model) {
                final String key = getRef(position).getKey();
                DatabaseReference usersDb = rootRef.child("Users").child(key);
                usersDb.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: found user:" + dataSnapshot);
                        viewHolder.setName(dataSnapshot.child("name").getValue(String.class));
                        viewHolder.setAttendImage(dataSnapshot.child("image").getValue(String.class), getApplicationContext());
                        viewHolder.setTime(model.getTime(), getApplicationContext());
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // --------------to user profiles view----------------//
                viewHolder.v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final CharSequence[] options = {"Open Profile", "Open Chat"};

                        if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(key)) {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                    .child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(key)) {

                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (i == 0) {
                                                    Intent intent = new Intent(mContext, ProfileActivity.class);
                                                    MyExperienceLikesActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                    intent.putExtra("user_id", key);
                                                    startActivity(intent);
                                                } else {
                                                    Intent intent = new Intent(mContext, ChatActivity.class);
                                                    MyExperienceLikesActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                    intent.putExtra("user_id", key);
                                                    startActivity(intent);
                                                }
                                            }
                                        });
                                        builder.setTitle("User Options").show();
                                    } else {
                                        Intent intent = new Intent(mContext, ProfileActivity.class);
                                        MyExperienceLikesActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        intent.putExtra("user_id", key);
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }


                    }
                });
            }
        };



        mAttendeesList.setAdapter(adapter);

    }


    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
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
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PeopleLikingMyExperience extends RecyclerView.ViewHolder {
        View v;
        CircleImageView userPic;

        public PeopleLikingMyExperience(View itemView) {
            super(itemView);
            v = itemView;
            userPic = v.findViewById(R.id.attendee_pic);


        }

        public void setAttendImage(String imageUrl, Context context) {
            userPic = v.findViewById(R.id.attendee_pic);
            try {
                Glide.with(context).load(imageUrl)
                        .into(userPic);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "setAttendImage: ILLEGSL ARGUMET");
                e.printStackTrace();
            }
        }

        public void setName(String name) {
            TextView textView = v.findViewById(R.id.attendee_name);
            textView.setText(name);
        }

        public void setTime(long time, Context context) {
            TextView timeView = v.findViewById(R.id.stampdate);
            timeView.setText(GetTimeAgo.getTimeAgo(time, context));

        }
    }
    @Override
    protected void onStart() {
        adapter.startListening();
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue("true");
        super.onStart();
    }

    @Override
    protected void onStop() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
        adapter.stopListening();
        super.onStop();
    }

}
