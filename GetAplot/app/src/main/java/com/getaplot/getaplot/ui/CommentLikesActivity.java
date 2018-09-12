package com.getaplot.getaplot.ui;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.ExLike;
import com.getaplot.getaplot.utils.GetTimeAgo;
import com.getaplot.getaplot.utils.Handy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommentLikesActivity extends AppCompatActivity {
    private static final String TAG = "CommentLikesFragment";
    DatabaseReference friendRef, rootRef;
    private boolean isBuddy = false;
    private Context mContext;
    FirebaseRecyclerAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//
        setContentView(R.layout.fragment_comment_likes);
        mContext = CommentLikesActivity.this;
        rootRef = FirebaseDatabase.getInstance().getReference();
        Toolbar toolbar = findViewById(R.id.acc);


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Liked by");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        friendRef = rootRef.child("Friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        final RecyclerView recyclerView = findViewById(R.id.likespeople);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("EventCommentLikes")
                .child(getIntent().getStringExtra("commentId"));
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerOptions<ExLike> options =
                new FirebaseRecyclerOptions.Builder<ExLike>()
                        .setQuery(ref.orderByChild("fitnessNum"), ExLike.class)
                        .build();


        adapter = new FirebaseRecyclerAdapter<ExLike, VH>(options) {
            @Override
            public VH onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_experince_layout, parent, false);

                return new VH(view);
            }

            @Override
            protected void onBindViewHolder(final VH viewHolder, int position, final ExLike model) {
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
                            Toast.makeText(mContext, key, Toast.LENGTH_SHORT).show();
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
                            builder.setTitle("Options");
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
                                viewHolder.setTime(model.getTime(), getApplicationContext());

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
                                                                CommentLikesActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                intent.putExtra("user_id", key);
                                                                startActivity(intent);

                                                            } catch (Exception e) {

                                                            }
                                                        } else {
                                                            try {
                                                                Intent intent = new Intent(mContext, ChatActivity.class);
                                                                CommentLikesActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                intent.putExtra("user_id", key);
                                                                startActivity(intent);

                                                            } catch (Exception e) {

                                                            }
                                                        }
                                                    }
                                                });
                                                builder.setTitle("Options").show();
                                            } else {
                                                FirebaseDatabase.getInstance().getReference().child("Users")
                                                        .child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild("isplace")) {
                                                            //go to place

                                                            Intent i = new Intent(mContext, PlaceActivity.class);
                                                            i.putExtra("place_id", key);
                                                            i.putExtra("place_name", dataSnapshot.child("name").getValue().toString());
                                                            startActivity(i);
                                                            finish();
                                                        } else {
                                                            Intent intent = new Intent(mContext, ProfileActivity.class);
                                                            CommentLikesActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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


        recyclerView.setAdapter(adapter);
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

    public static class VH extends RecyclerView.ViewHolder {
        View v;
        CircleImageView userPic;

        public VH(View itemView) {
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

        public void setTime(long time, Context ctx) {
            TextView timeView = v.findViewById(R.id.stampdate);

            timeView.setText(GetTimeAgo.getTimeAgo(time, ctx));

        }
    }
}
