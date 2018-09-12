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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Attendees;
import com.getaplot.getaplot.utils.Handy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventAttendeesActivity extends AppCompatActivity {
    private static final String TAG = "EventAttendeesActivity";
    private static final int ACTIVITY_NUM = 3;
    ImageView b;
    private Toolbar mToolbar;
    private RecyclerView mAttendeesList;
    private Context mContext;
    private DatabaseReference attendeesRef, rootRef, userRef, friendRef;
    private Boolean isBuddy = false;
FirebaseRecyclerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_attendees);
        mContext = EventAttendeesActivity.this;
        setUpToolbar();
        mAttendeesList = findViewById(R.id.attendesRecyclerView);
        mAttendeesList.setItemAnimator(new DefaultItemAnimator());
        mAttendeesList.setHasFixedSize(true);
        mAttendeesList.setLayoutManager(new LinearLayoutManager(mContext));
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.keepSynced(true);

        attendeesRef = rootRef.child("eventAttendees").child(getIntent().getStringExtra("event_id"));
        attendeesRef.keepSynced(true);
        friendRef = rootRef.child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid());


        Log.d(TAG, "onCreate: The current event id" + getIntent().getStringExtra("event_id"));


    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.attendes_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Plot Attendees");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == android.R.id.home) {
            Log.d(TAG, "onOptionsItemSelected: ");
            finish();
        }

        return super.onOptionsItemSelected(item);


    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Attendees> options =
                new FirebaseRecyclerOptions.Builder<Attendees>()
                        .setQuery(attendeesRef, Attendees.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Attendees, EventAttendees>(options) {
            @Override
            public EventAttendees onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_attendee_layout, parent, false);

                return new EventAttendees(view);
            }

            @Override
            protected void onBindViewHolder(final EventAttendees viewHolder, int position, final Attendees model) {
                final String key = getRef(position).getKey();
                friendRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(key)) {
                            isBuddy = true;
                            Log.d(TAG, "onDataChange: " + dataSnapshot);
                            viewHolder.v.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            });


                        } else {
                            isBuddy = false;

                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                viewHolder.v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!isBuddy) {
                            Log.d(TAG, "onClick: notfriends");
                            try {
                                Log.d(TAG, "onClick: Navigating to profiles of user");
                                Intent i = new Intent(mContext, ProfileActivity.class);
                                i.putExtra("user_id", key);
                                startActivity(i);

                            } catch (Exception e) {
                                Log.d(TAG, "onClick: " + e.getMessage());
                            }
                        } else {
                            Log.d(TAG, "onClick: friends");
                            CharSequence options[] = new CharSequence[]{"Open Profile", "Open Chat"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        try {
                                            Intent i = new Intent(mContext, ProfileActivity.class);

                                            i.putExtra("user_id", key);
                                            startActivity(i);
                                        } catch (NullPointerException e) {

                                        }
                                    }
                                    if (which == 1) {
                                        try {
                                            Intent i = new Intent(mContext, ChatActivity.class);

                                            i.putExtra("user_id", key);
                                            i.putExtra("user_name", "User");
                                            startActivity(i);

                                        } catch (NullPointerException e) {

                                        }
                                    }

                                }
                            });
                            builder.show();

                        }


                    }
                });


                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(key);
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        try {
                            Log.d(TAG, "onDataChange: The current user going " + dataSnapshot);
                            try {
                                viewHolder.userPic.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent i = new Intent(mContext, EnlargeImageView.class);
                                        i.putExtra("image_url", dataSnapshot.child("image").getValue(String.class));
                                        startActivity(i);
                                    }
                                });
                            } catch (NullPointerException e) {

                            }
                            String name = dataSnapshot.child("name").getValue().toString();
                            Log.d(TAG, "onDataChange: the user na is " + name);

                            viewHolder.setName(Handy.getTrimmedName(name));
                            String image = dataSnapshot.child("image").getValue().toString();
                            try {
                                viewHolder.setAttendImage(image, mContext);
                            } catch (IllegalArgumentException ex) {
                                Log.d(TAG, "onDataChange: IllegalArgumentException" + ex.getMessage());
                                ex.printStackTrace();
                            }

                            try {
                                viewHolder.setTime(model.getTime());

                            } catch (NullPointerException ex) {
                                Log.d(TAG, "onDataChange: Null p" + ex.getMessage());
                            }
                        } catch (NullPointerException e) {
                            Log.d(TAG, "populateViewHolder: Null pointer" + e.getMessage());
                        }
                        //--------------to user profiles view----------------//
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
                                                            try {
                                                                Intent intent = new Intent(mContext, ProfileActivity.class);
                                                                EventAttendeesActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                intent.putExtra("user_id", key);
                                                                startActivity(intent);

                                                            } catch (Exception e) {

                                                            }
                                                        } else {
                                                            try {
                                                                Intent intent = new Intent(mContext, ChatActivity.class);
                                                                EventAttendeesActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                intent.putExtra("user_id", key);
                                                                startActivity(intent);

                                                            } catch (Exception e) {

                                                            }
                                                        }
                                                    }
                                                });
                                                builder.setTitle("Options").show();
                                            } else {
                                                Intent intent = new Intent(mContext, ProfileActivity.class);
                                                EventAttendeesActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };


        mAttendeesList.setAdapter(adapter);
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


        @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public static class EventAttendees extends RecyclerView.ViewHolder {
        View v;
        CircleImageView userPic;

        public EventAttendees(View itemView) {
            super(itemView);
            v = itemView;
            userPic = v.findViewById(R.id.attendee_pic);


        }

        public void setAttendImage(String imageUrl, Context context) {
            userPic = v.findViewById(R.id.attendee_pic);
            try {
                RequestOptions requestOptions = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.test);
                Glide.with(context).load(imageUrl).apply(requestOptions)

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

        public void setTime(String time) {
            TextView timeView = v.findViewById(R.id.stampdate);
            timeView.setText(time);

        }
    }
}
