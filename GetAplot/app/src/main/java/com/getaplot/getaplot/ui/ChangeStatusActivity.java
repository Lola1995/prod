package com.getaplot.getaplot.ui;

import android.app.ProgressDialog;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Status;
import com.getaplot.getaplot.utils.GetTimeAgo;
import com.getaplot.getaplot.utils.Handy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ChangeStatusActivity extends AppCompatActivity {

    private static final String TAG = "ChangeStatusActivity";
    EmojiconEditText emojiconEditText;
    ImageView emojiButton, submitButton;
    EmojIconActions emojIconActions;
    RelativeLayout layout;
    private ImageButton updastatusButton;
    private EmojiconEditText statusInputLayout;
    private DatabaseReference databaseReference;
    private FirebaseUser mCurrentUser;
    private ProgressDialog progressDialog;
    private Toolbar statusAppBar;
    private FirebaseAuth auth;
    private Context mContext;
    private DatabaseReference userStatus, usersearch;
    //List<String> mList=new ArrayList<String>();
//private ArrayAdapter arrayAdapter;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter adapter;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Constant constant = null;
//        SharedPreferences.Editor editor;
//        SharedPreferences app_preferences;
//        int appTheme;
//        int themeColor;
//        int appColor;

//        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        appColor = app_preferences.getInt("color", 0);
//        appTheme = app_preferences.getInt("theme", 0);
//        themeColor = appColor;
//        constant.color = appColor;
//
//        if (themeColor == 0){
//            setTheme(Constant.theme);
//        }else if (appTheme == 0){
//            setTheme(Constant.theme);
//        }else{
//            setTheme(appTheme);
//        }
        setContentView(R.layout.activity_change_status);
        statusAppBar = findViewById(R.id.status_app_bar);
        setSupportActionBar(statusAppBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Share about your experiences");
        mContext = ChangeStatusActivity.this;
        usersearch = FirebaseDatabase.getInstance().getReference().child("userSearchNode").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
        ;
        layout = findViewById(R.id.st);
        emojiButton = findViewById(R.id.emoji_button);
        emojiconEditText = findViewById(R.id.ed);
        emojiconEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    //execute our method for searching
                    updateStatus(emojiconEditText.getText().toString().trim());
                }
                return false;
            }
        });

        EmojIconActions emojIcon = new EmojIconActions(this, layout, emojiconEditText, emojiButton,
                "#495C66", "#DCE1E2", "#E6EBEF");
        emojIcon.ShowEmojIcon();


        progressDialog = new ProgressDialog(this);

        updastatusButton = findViewById(R.id.updateStatusButton);
        recyclerView = findViewById(R.id.recent);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setHasFixedSize(true);
        textView = findViewById(R.id.noexperience);

        userStatus = FirebaseDatabase.getInstance().getReference().child("userStatus")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userStatus.keepSynced(true);
        userStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    textView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseRecyclerOptions<Status> options =
                new FirebaseRecyclerOptions.Builder<Status>()
                        .setQuery(userStatus.orderByChild("fitnessNum"), Status.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Status, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ex_other_listitem, parent, false);

                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final ViewHolder viewHolder, final int position, Status model) {
                final String key = getRef(position).getKey();

                viewHolder.setUX(model.getStatus());
                FirebaseDatabase.getInstance().getReference().child("EXLikes").child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: Datasnapsot" + dataSnapshot);
                        int count = (int) dataSnapshot.getChildrenCount();
                        if (count == 1) {
                            viewHolder.likes.setText("1 like");
                            viewHolder.likes.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    Intent i = new Intent(mContext, MyExperienceLikesActivity.class);
                                    i.putExtra("exlike_id", key);
                                    startActivity(i);
                                }
                            });
                        } else if (count > 1) {
                            viewHolder.likes.setText(String.valueOf(count).concat(" Likes"));
                            viewHolder.likes.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    Intent i = new Intent(mContext, MyExperienceLikesActivity.class);
                                    i.putExtra("exlike_id", key);
                                    startActivity(i);
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                viewHolder.setUXUpdateTime(model.getUpdated(), mContext);
                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage("This will remove this experience");
                        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int selectedItems = position;
                                try {
                                    adapter.getRef(selectedItems).removeValue();
                                    adapter.notifyItemRemoved(selectedItems);
                                    recyclerView.invalidate();
                                    Toast.makeText(ChangeStatusActivity.this, "Experience Removed", Toast.LENGTH_SHORT).show();
                                } catch (IndexOutOfBoundsException e) {
                                    Log.d(TAG, "onClick: " + e.getLocalizedMessage());
                                }

                            }
                        })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        builder.show();

                        return true;
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
        String c_status = getIntent().getStringExtra("c_status");

        try {
            if (!c_status.isEmpty()) {
                emojiconEditText.setText(c_status);
            }
        } catch (NullPointerException ex) {

        }


        //firebase
        auth = FirebaseAuth.getInstance();
        mCurrentUser = auth.getCurrentUser();
        String cuid = mCurrentUser.getUid();


        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(cuid);


        updastatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newStatus = emojiconEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(newStatus)) {
                    updateStatus(newStatus);
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(0);
                }
            }
        });
    }


    private void upDateNumberOfLikes(final String key, final ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("EXLikes")
                .child(key);

        Log.d(TAG, "upDateNumberOfLikes: ");
        reference.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                if (count == 0) {
                    holder.likes.setText("");
                } else if (count == 1) {
                    holder.likes.setText("1 like");
                    holder.likes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(mContext, MyExperienceLikesActivity.class);
                            i.putExtra("user_id", key);
                            startActivity(i);
                        }
                    });
                } else {
                    holder.likes.setText(String.valueOf(count).concat(" Likes"));
                    holder.likes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent i = new Intent(mContext, MyExperienceLikesActivity.class);
                            i.putExtra("user_id", key);
                            startActivity(i);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void updateStatus(final String newStatus) {
        Map<String, Object> statusMap = new HashMap<>();
        progressDialog.setMessage("Posting experience");
        progressDialog.show();
        statusMap.put("status", newStatus);
        statusMap.put("fitnessNum", Handy.fitnessNumber());
        statusMap.put("updated", ServerValue.TIMESTAMP);
        usersearch.child("status").setValue(newStatus);
        userStatus.push().setValue(statusMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                databaseReference.child("status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.hide();
                        e.printStackTrace();
                        Toast.makeText(ChangeStatusActivity.this, "Experience Update Failed", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mContext, "Error has occured:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected void onStart() {
        databaseReference.child("online").setValue("true");
        adapter.startListening();
        super.onStart();
    }

    @Override
    protected void onStop() {
        adapter.stopListening();
        databaseReference.child("online").setValue(ServerValue.TIMESTAMP);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView likes;
        ImageView like;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            likes = mView.findViewById(R.id.ex_likes);
            like = mView.findViewById(R.id.ex_like);
            like.setVisibility(View.GONE);

        }

        public void setUX(String ux) {
            TextView ux2 = mView.findViewById(R.id.status);
            ux2.setText(ux);
        }

        public void setUXUpdateTime(long time, Context context) {
            TextView textView = mView.findViewById(R.id.updateTime);
            textView.setText(GetTimeAgo.getTimeAgo(time, context));
        }
    }
}
