package com.getaplot.getaplot.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.ui.EnlargeImageView;
import com.getaplot.getaplot.ui.HiveReactionsActivity;
import com.getaplot.getaplot.ui.PlaceActivity;
import com.getaplot.getaplot.utils.GetShortTimeAgo;
import com.getaplot.getaplot.utils.Handy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailActivity extends AppCompatActivity {
    private static final String TAG = "PostDetailActivity";
    public FirebaseAuth mAuth;
    String key;
    private Toolbar toolbar;
    private TextView date, desc;
    private ImageView postimage;
    private Context context;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private CircleImageView poster;
    private TextView postername;
    private TextView postdate;
    private TextView likes;
    private TextView comments;
    private ImageView image, mHeartWhite;
    private TextView text, place;
    private DatabaseReference places, likesdb, commentsdb, infoposts;
    private ImageView imm;
    private ProgressBar progressBar1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_detail);
        init();
        toolbar = findViewById(R.id.postdet);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("The Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        key = getIntent().getStringExtra("post_id");
        mAuth = FirebaseAuth.getInstance();
        mHeartWhite = findViewById(R.id.image_heart);
        progressBar1=findViewById(R.id.progress);
        imm = findViewById(R.id.imm);
        imm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), HiveReactionsActivity.class);
                i.putExtra("hivereplykey", key);

                startActivity(i);
            }
        });

        updateLikes();
        updateComments();
//        likes.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(getBaseContext(), HiveReactionsLikesActivity.class);
//                i.putExtra("key", key);
//
//                startActivity(i);
//            }
//        });
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("postLikes").child(key);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                    mHeartWhite.setBackgroundResource(R.drawable.ic_thumprimary);
                } else {
                    mHeartWhite.setBackgroundResource(R.drawable.ic_like_comment);
                }
                int count = (int) dataSnapshot.getChildrenCount();
                if (count == 0) {
                    likes.setText("");
                } else if (count == 1) {

                    likes.setText(String.valueOf(count).concat(" Like"));
//                            viewHolder.likecount.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    Log.d(TAG, "onLongClick: ");
//                                    Intent intent = new Intent(getContext(), HiveReactionsLikesActivity.class);
//                                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                                    getActivity().getSupportFragmentManager().popBackStack();
//                                    intent.putExtra("key", key);
//                                    startActivity(intent);
//
//                                }
//                            });
                } else {
                    likes.setText(String.valueOf(count).concat(" Likes"));
//                            viewHolder.likecount.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    Intent intent = new Intent(getContext(), HiveReactionsLikesActivity.class);
//                                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                                    getActivity().getSupportFragmentManager().popBackStack();
//                                    intent.putExtra("key", key);
//                                    startActivity(intent);
//                                }
//                            });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mHeartWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                            Log.d(TAG, "onDataChange: liked");
                            ref.child(mAuth.getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(getBaseContext(), "post unliked", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Map map = new HashMap<>();
                            map.put("user", mAuth.getCurrentUser().getUid());
                            map.put("at", Handy.fitnessNumber());

                            ref.child(mAuth.getCurrentUser().getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    Toast.makeText(getBaseContext(), "Like added", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), HiveReactionsActivity.class);
                i.putExtra("hivereplykey", key);

                startActivity(i);
            }
        });
        context = PostDetailActivity.this;
        progressBar = findViewById(R.id.pe);
        places = FirebaseDatabase.getInstance().getReference().child("Place_Users").child(getIntent().getStringExtra("place_id"));
        places.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                try {
                    place.setText(Handy.getTrimmedName(dataSnapshot.child("name").getValue().toString()));
                }catch (Exception e){

                }
                place.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getBaseContext(), PlaceActivity.class);
                        PostDetailActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        i.putExtra("place_name", dataSnapshot.child("name").getValue().toString());
                        i.putExtra("description", dataSnapshot.child("description").getValue().toString());
                        i.putExtra("imageUrl", dataSnapshot.child("image").getValue().toString());
                        i.putExtra("place_id", dataSnapshot.getKey());
                        startActivity(i);
                    }
                });
                Glide.with(getBaseContext()).load(dataSnapshot.child("image").getValue().toString()).into(poster);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        infoposts = FirebaseDatabase.getInstance().getReference().child("placeInfoPosts").child(key);
        infoposts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot);


                String imagee ="";
                try{
                    imagee=getIntent().getStringExtra("image_url");
                }catch (NullPointerException e){

                }

                try{
                    String textt = dataSnapshot.child("post_text").getValue().toString();
                    text.setText(textt);
                }catch (NullPointerException ee){

                }

                try {
                    if (imagee.length()<1){
                        imagee= dataSnapshot.child("image").getValue().toString();
                    }else {
                        try {

                            imagee= dataSnapshot.child("image").getValue().toString();
                        }catch (NullPointerException x){

                        }
                    }
                }catch (NullPointerException e){
                    try {
                        imagee= dataSnapshot.child("image").getValue().toString();
                    }catch (NullPointerException eee){

                    }
                }

                try {
                    Long posted = Long.valueOf(dataSnapshot.child("posted").getValue().toString());
                    date.setText(GetShortTimeAgo.getTimeAgo(posted, getBaseContext()));
                }catch (NullPointerException e){

                }

                final String finalImagee = imagee;
                Glide.with(getBaseContext()).load(imagee).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar1.setVisibility(View.GONE);
                        Log.d(TAG, "onException: "+e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(context, "could not load photo", Toast.LENGTH_SHORT).show();

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getBaseContext(), EnlargeImageView.class);
                                PostDetailActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                PostDetailActivity.this.getSupportFragmentManager().popBackStack();
                                i.putExtra("image_url", finalImagee);
                                startActivity(i);
                            }
                        });

                        progressBar1.setVisibility(View.GONE);
                        return false;
                    }
                }).into(image);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateComments() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("infoHiveComments").child(key);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                if (count > 0) {
                    comments.setText(String.valueOf(count).concat(" Comments"));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateLikes() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("postLikes").child(key);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                if (count > 0) {
                    likes.setText(String.valueOf(count).concat(" Likes"));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void init() {
        comments = findViewById(R.id.hivereplycount);
        image = findViewById(R.id.imagetarget);
        poster = findViewById(R.id.posterpic);
        likes = findViewById(R.id.likecount);
        text = findViewById(R.id.text);
        place = findViewById(R.id.ev_place);
        date = findViewById(R.id.hivetime);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {

            case android.R.id.home:
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

}