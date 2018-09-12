package com.getaplot.getaplot.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.HiveReactions;
import com.getaplot.getaplot.utils.GetShortTimeAgo;
import com.getaplot.getaplot.utils.Handy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class HiveReactionsActivity extends AppCompatActivity {
    private static final String TAG = "HiveReactionsActivity";
    private static final int ACTIVITY_NUM = 2;
    TextView ev_place;
    ImageView image;
    TextView hivereplycount;

    EditText reply;
    ImageButton sendFab;
    FirebaseRecyclerAdapter adapter;
    private String hivekey;
    private RecyclerView mIHList;
    private DatabaseReference mDatabase, rootRef, users;
    private Context mContext;
    private ImageView b;
    private ImageView ev_cover;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView noreactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hive_reactions);
        Log.d(TAG, "onCreate: ");

        initWidgets();
        checkReactionAvailability();
        setuptoolbar();
        populateRecyclerView();
        sendFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String repl = reply.getText().toString();
                if (!TextUtils.isEmpty(repl)) {
                    sendCommentToFeed(mAuth.getCurrentUser().getUid(), hivekey, repl);
                }


            }
        });
    }

    private void populateRecyclerView() {
        FirebaseRecyclerOptions<HiveReactions> options =
                new FirebaseRecyclerOptions.Builder<HiveReactions>()
                        .setQuery(mDatabase, HiveReactions.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<HiveReactions, HiveHolder>(options) {
            @Override
            public HiveHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_hive_reaction, parent, false);

                return new HiveHolder(view);
            }

            @Override
            protected void onBindViewHolder(final HiveHolder viewHolder, final  int position, final HiveReactions model) {
                Log.d(TAG, "populateViewHolder: " + model);
                viewHolder.setDetails(model.getSaid(), model.getTime(), getApplicationContext());
                final String userId = model.getUser();
                progressBar.setVisibility(View.GONE);
                users.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        Log.d(TAG, "run: " + dataSnapshot);
                            viewHolder.name.setText(Handy.getTrimmedName(dataSnapshot.child("name").getValue().toString()));
try {
    RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.test);
    Glide.with(mContext).load(dataSnapshot.child("image").getValue().toString()).apply(requestOptions)
            .into(viewHolder.imageView);
}catch (IllegalArgumentException e){

}
                            if (!mAuth.getCurrentUser().getUid().equals(userId)) {
                                viewHolder.name.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        //-----------------CHECKING TYPE OF USER-----------------------//
                                        if (dataSnapshot.hasChild("isplace")) {
                                            Intent i = new Intent(mContext, PlaceActivity.class);
                                            HiveReactionsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                            i.putExtra("place_id", userId);
                                            i.putExtra("image_url", dataSnapshot.child("image").getValue().toString());
                                            i.putExtra("place_name", dataSnapshot.child("name").getValue().toString());
                                            startActivity(i);
                                            Log.d(TAG, "onDataChange: Place");
                                        } else if (!dataSnapshot.hasChild("isplace")) {
                                            final CharSequence[] options = {"Open Profile", "Open Chat"};
                                            if (!mAuth.getCurrentUser().getUid().equals(userId)) {
                                                DatabaseReference reference = FirebaseDatabase.getInstance()
                                                        .getReference().child("Friends").child(mAuth.getCurrentUser().getUid());
                                                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild(userId)) {

                                                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    if (i == 0) {
                                                                        Intent intent = new Intent(mContext, ProfileActivity.class);
                                                                        HiveReactionsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                        intent.putExtra("user_id", userId);
                                                                        startActivity(intent);
                                                                    } else {
                                                                        Intent intent = new Intent(mContext, ChatActivity.class);
                                                                        HiveReactionsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                        intent.putExtra("user_id", userId);
                                                                        startActivity(intent);
                                                                    }
                                                                }
                                                            });
                                                            builder.setTitle("User Options").show();
                                                        } else {
                                                            Intent intent = new Intent(mContext, ProfileActivity.class);
                                                            HiveReactionsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                            intent.putExtra("user_id", userId);

                                                            startActivity(intent);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }

                                        }

                                        //todo think if we need another case here
                                        else {

                                        }
                                    }
                                });
                            }

                            //------------------------------------------------------------------------//
                            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(mContext, EnlargeImageView.class);
                                    HiveReactionsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    i.putExtra("image_url", dataSnapshot.child("image").getValue(String.class));
                                    startActivity(i);
                                }
                            });



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //---------------------DELETING THE COMMENT--------------------//
                //OnClick Item it will Delete data from Database
                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (!mAuth.getCurrentUser().getUid().equals(model.getUser())) {
                            Log.d(TAG, "onClick: Not ur comment sucker");
                            return false;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage("Do you want to delete your reaction?").setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        try {
                                            int selectedItems = position;
                                            adapter.getRef(selectedItems).removeValue();
                                            adapter.notifyItemRemoved(selectedItems);
                                            mIHList.invalidate();
                                        } catch (NullPointerException e) {
                                        } catch (IndexOutOfBoundsException e) {
                                            Toast.makeText(HiveReactionsActivity.this, "Sorry couldnt remove post,please try again!", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "onClick: IndexOutOfBoundsException " + e.getMessage());

                                        }


                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        return;
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.setTitle("Confirm");
                        dialog.show();


                        return true;
                    }
                });

            }
        };

        mIHList.setAdapter(adapter);

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

    private void checkReactionAvailability() {
        Log.d(TAG, "checkReactionAvailability: ");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    noreactions.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void sendCommentToFeed(String uid, String key, String userreply) {
        Log.d(TAG, "sendCommentToFeed: ");
        DatabaseReference reference = rootRef.child("infoHiveComments").child(key);
        Map<String, Object> infofeed = new HashMap<>();
        infofeed.put("user", uid);
        infofeed.put("said", userreply);
        infofeed.put("time", ServerValue.TIMESTAMP);
        Toast.makeText(mContext, "Adding your reaction", Toast.LENGTH_SHORT).show();
        reference.push().setValue(infofeed).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(mContext, "Reaction added", Toast.LENGTH_SHORT).show();
                reply.setText("");
                noreactions.setVisibility(View.GONE);
                sendFab.setEnabled(true);
            }
        });


    }

    private void setuptoolbar() {
        Log.d(TAG, "setuptoolbar: ");
        Toolbar toolbar = findViewById(R.id.infohivereactionsbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initWidgets() {
        Log.d(TAG, "initWidgets: ");
        mIHList = findViewById(R.id.hivelists);
        progressBar = findViewById(R.id.progress3);
        mIHList.setLayoutManager(new LinearLayoutManager(mContext));
        mIHList.setHasFixedSize(true);
        noreactions = findViewById(R.id.noreactions);
        mContext = HiveReactionsActivity.this;
        hivekey = getIntent().getStringExtra("hivereplykey");
        Log.d(TAG, "onCreate: " + hivekey);
        reply = findViewById(R.id.hivereplytext);
        sendFab = findViewById(R.id.hivesendbtn);
        hivereplycount = findViewById(R.id.hivereplycount);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        mDatabase = rootRef.child("infoHiveComments").child(hivekey);
        users = rootRef.child("Users");
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

    public static class HiveHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView name, said, timetext;
        CircleImageView imageView;

        public HiveHolder(View itemView) {
            super(itemView);
            mView = itemView;

            name = mView.findViewById(R.id.username);
            imageView = mView.findViewById(R.id.userpic);

        }

        public void setDetails(String said, long timetext, Context context) {
            TextView timetex = mView.findViewById(R.id.hive_time);
            timetex.setText(GetShortTimeAgo.getTimeAgo(timetext, context));
            TextView sayd = mView.findViewById(R.id.descc);
          //  Linkify.addLinks(sayd,Linkify.ALL);
            sayd.setText(said);
        }
    }
}