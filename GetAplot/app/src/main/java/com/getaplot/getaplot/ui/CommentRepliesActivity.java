package com.getaplot.getaplot.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.CommentReplies;
import com.getaplot.getaplot.preferences.Constant;
import com.getaplot.getaplot.utils.GetCurTime;
import com.getaplot.getaplot.utils.Handy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;


public class CommentRepliesActivity extends AppCompatActivity {
    private static final String TAG = "CommentRepliesActivity";
    private static final int ACTIVITY_NUM = 3;
    CircleImageView user_image;
    EmojiconEditText emojiconEditText;
    ImageView emojiButton, submitButton;
    RelativeLayout chating;
    private Toolbar mToolbar;
    private Context context;
    private TextView earlyCommentText, commenterName;
    private DatabaseReference repliesDb, userDb, eventsDb, rootRef, curUser;
    private RecyclerView recyclerView;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private String comment_id;
    private FirebaseRecyclerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constant constant = null;
//
        setContentView(R.layout.activity_comment_replies);
        context = CommentRepliesActivity.this;
        recyclerView = findViewById(R.id.recyclerreplies);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        mToolbar = findViewById(R.id.commentrepliesappbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Event Comment Replies");
        //REPLING TO
        chating = findViewById(R.id.replies);
        emojiButton = findViewById(R.id.emoji_button);
        submitButton = findViewById(R.id.chat_send_btn);
        emojiconEditText = findViewById(R.id.emojicon_edit_text);

        try {
            EmojIconActions emojIcon = new EmojIconActions(this, chating, emojiconEditText, emojiButton, "#495C66", "#DCE1E2", "#E6EBEF");
            emojIcon.ShowEmojIcon();
        } catch (NullPointerException e) {
            Log.d(TAG, "emojis");
        }


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String replyText = emojiconEditText.getText().toString().trim();
                if (TextUtils.isEmpty(replyText)) {
                    return;
                }
                emojiconEditText.setText("");
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                        .child("EventCommentsReplies").child(getIntent()
                                .getStringExtra("CommentId"));
                Map<String, Object> commentReplyMap = new HashMap<>();
                commentReplyMap.put("relyText", replyText);
                commentReplyMap.put("event_id", getIntent().getStringExtra("event_id"));
                commentReplyMap.put("time", GetCurTime.getCurTime());
                commentReplyMap.put("user", FirebaseAuth.getInstance().getCurrentUser().getUid());
                DatabaseReference reference1 = FirebaseDatabase.getInstance()
                        .getReference().child("CommentReplyNotifications");
                String pushkey = reference1.push().getKey();
                Map<String, String> map = new HashMap<String, String>();
                map.put("from", FirebaseAuth.getInstance().getCurrentUser().getUid());
                map.put("reply", replyText);
                map.put("comment_id", comment_id);
                try {
                    if (!getIntent().getStringExtra("uid").equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        reference1.child(getIntent().getStringExtra("uid")).child(pushkey).setValue(map);

                        reference.push().setValue(commentReplyMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Log.d(TAG, "onComplete: Completed");
                                    Toast.makeText(context, "Reply added", Toast.LENGTH_SHORT).show();
                                    onStart();
                                } else {
                                    Toast.makeText(context, "Reply wasnot added", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {

                        reference.push().setValue(commentReplyMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Log.d(TAG, "onComplete: Completed");
                                    Toast.makeText(context, "Reply added", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Reply wasnot added", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } catch (Exception e) {


                    if (!getIntent().getStringExtra("uid").equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        reference1.child(getIntent().getStringExtra("uid")).child(pushkey).setValue(map);

                        reference.push().setValue(commentReplyMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Log.d(TAG, "onComplete: Completed");
                                    Toast.makeText(context, "Reply added", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Reply wasnot added", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {

                        reference.push().setValue(commentReplyMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Log.d(TAG, "onComplete: Completed");
                                    Toast.makeText(context, "Reply added", Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(context, "Reply wasnot added", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }


                }
            }
        });

        earlyCommentText = findViewById(R.id.earlyCommentText);
        ImageView imageView = findViewById(R.id.ddp);
        progressBar = findViewById(R.id.progress);
        auth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        repliesDb = rootRef.child("EventCommentsReplies").child(getIntent()
                .getStringExtra("CommentId"));
        repliesDb.keepSynced(true);
        userDb = rootRef.child("Users").child(getIntent().getStringExtra("uid"));
        userDb.keepSynced(true);
        eventsDb = rootRef.child("Events").child(getIntent().getStringExtra("event_id"));
        eventsDb.keepSynced(true);
        curUser = rootRef.child("Users").child(auth.getCurrentUser().getUid());


        String text = getIntent().getStringExtra("commentText");
        if (text.startsWith("https://firebasestorage.googleapis.com")) {
            imageView.setVisibility(View.VISIBLE);

            Glide.with(context).load(text).into(imageView);
        } else if (text.startsWith("-")) {
            earlyCommentText.setText("Location");
        } else {
            earlyCommentText.setText(getIntent().getStringExtra("commentText"));
        }
        commenterName = findViewById(R.id.commenterName);
        TextView textView = findViewById(R.id.replytime);
        String time = GetCurTime.toDateTime(getIntent().getLongExtra("lastcommentedOn", System.currentTimeMillis()));
        textView.setText(time);

        userDb.keepSynced(true);
        eventsDb.keepSynced(true);

        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    String username = dataSnapshot.child("name").getValue().toString().concat(" :");
                    commenterName.setText(Handy.getTrimmedName(username));
                } catch (NullPointerException e) {

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //-----------get the replies to this comment

        curUser.child("online").setValue("true");


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<CommentReplies> options =
                new FirebaseRecyclerOptions.Builder<CommentReplies>()
                        .setQuery(repliesDb, CommentReplies.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<CommentReplies, CommentsRepliesVH>(options) {
            @Override
            public CommentsRepliesVH onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.snippet_single_reply_view, parent, false);

                return new CommentsRepliesVH(view);
            }

            @Override
            protected void onBindViewHolder(final CommentsRepliesVH viewHolder, int position, final CommentReplies model) {
                viewHolder.setCommentsText(model.getCommentText());
                viewHolder.setCommentsTime(model.getTime());
                DatabaseReference newdatabase = rootRef.child("Users").child(model.getUser());
                newdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        viewHolder.setCommentReplierName(Handy.getTrimmedName(name));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                progressBar.setVisibility(View.GONE);

                //--------------INTENT FR USER
                viewHolder.tname.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        FirebaseDatabase.getInstance().getReference().child("Users").child(model.getUser())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("isplace")) {
                                            Log.d(TAG, "onDataChange: place yes");
                                            Log.d(TAG, "onDataChange: " + dataSnapshot);
                                            Intent i = new Intent(context, PlaceActivity.class);
                                            CommentRepliesActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                            i.putExtra("place_id", model.getUser());
                                            i.putExtra("place_name", dataSnapshot.child("name").getValue().toString());
                                            i.putExtra("image_url", dataSnapshot.child("image").getValue().toString());
                                            startActivity(i);

                                        } else {
                                            Intent i = new Intent(context, ProfileActivity.class);
                                            CommentRepliesActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                            i.putExtra("user_id", model.getUser());
                                            i.putExtra("user_name", dataSnapshot.child("name").getValue().toString());
                                            i.putExtra("extra", "extra");

                                            startActivity(i);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                    }
                });

                viewHolder.setCommentsText(model.getRelyText());
                viewHolder.setCommentsTime(model.getTime());
            }
        };





        recyclerView.setAdapter(adapter);
        adapter.startListening();
        curUser.child("online").setValue("true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        curUser.child("online").setValue(ServerValue.TIMESTAMP);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.replies, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public static class CommentsRepliesVH extends RecyclerView.ViewHolder {
        View mView;
        CircleImageView user_account_image;
        TextView tname;

        public CommentsRepliesVH(View itemView) {
            super(itemView);
            mView = itemView;
            tname = mView.findViewById(R.id.nname);
        }

        public void setCommentReplierName(String name) {
            tname = mView.findViewById(R.id.nname);
            tname.setText(name);
        }

        public void setCommentsTime(String time) {
            TextView ttime = mView.findViewById(R.id.replydatetime);
            ttime.setText(time);

        }

        public void setCommentsText(String text) {
            TextView ttext = mView.findViewById(R.id.replyText);
            Linkify.addLinks(ttext,Linkify.ALL);
            ttext.setText(text);
        }

    }
}
