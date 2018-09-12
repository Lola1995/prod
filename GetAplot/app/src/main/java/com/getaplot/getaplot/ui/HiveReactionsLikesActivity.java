package com.getaplot.getaplot.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Like;
import com.getaplot.getaplot.utils.Handy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class HiveReactionsLikesActivity extends AppCompatActivity {
    private static final String TAG = "HiveReactionsLikesActiv";
    String key;
    ProgressBar progress;
    TextView none;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private DatabaseReference reference;
    FirebaseRecyclerAdapter adapter;
    private DatabaseReference reference2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hive_reactions_likes);
        toolbar = findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Liked by");

        progress = findViewById(R.id.progress);
        none = findViewById(R.id.none);
        recyclerView = findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        key = getIntent().getStringExtra("key");

        reference = FirebaseDatabase.getInstance().getReference().child("postLikes").child(key);
        reference2 = FirebaseDatabase.getInstance().getReference().child("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    none.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "onDataChange: " + dataSnapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseRecyclerOptions<Like> options =
                new FirebaseRecyclerOptions.Builder<Like>()
                        .setQuery(reference, Like.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Like, LikedByHolder>(options) {
            @Override
            public LikedByHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.singlelikerhive, parent, false);

                return new LikedByHolder(view);
            }

            @Override
            protected void onBindViewHolder(final LikedByHolder viewHolder, int position, Like model) {
                final String key = getRef(position).getKey();
                reference2.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        String pic = dataSnapshot.child("image").getValue().toString();
                        String username = dataSnapshot.child("userName").getValue().toString();

                        viewHolder.setname(Handy.getTrimmedName(name));
                        viewHolder.setUserName("@" + username);
                        viewHolder.setPic(pic, getBaseContext());

                        progress.setVisibility(View.GONE);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                                                       @Override
                                                       public void onClick(View view) {
                                                           Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
                                                           HiveReactionsLikesActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                           intent.putExtra("user_id", key);
                                                           startActivity(intent);
                                                       }
                                                   }
                );
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


    public class LikedByHolder extends RecyclerView.ViewHolder {
        View view;

        public LikedByHolder(View itemView) {
            super(itemView);
            view = itemView;

        }

        public void setname(String names) {
            TextView name = view.findViewById(R.id.us_name);
            name.setText(names);
        }

        public void setUserName(String userName) {

            TextView usernme = view.findViewById(R.id.userName);
            usernme.setText(userName);

        }

        public void setPic(String image, Context context) {
            CircleImageView circleImageView = view.findViewById(R.id.attendee_pic);
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.test);
            Glide.with(context).load(image).apply(requestOptions).into(circleImageView);
        }

    }
}
