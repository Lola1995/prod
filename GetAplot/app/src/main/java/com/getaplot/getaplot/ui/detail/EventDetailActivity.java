package com.getaplot.getaplot.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.ui.EventAttendeesActivity;
import com.getaplot.getaplot.ui.EventCommentsActivity;
import com.getaplot.getaplot.ui.MainActivity;
import com.getaplot.getaplot.ui.PlaceActivity;
import com.getaplot.getaplot.utils.GetCurTime;
import com.getaplot.getaplot.utils.GetTimeAgo;
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
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EventDetailActivity extends AppCompatActivity {
    private static final int ACTIVITY_NUM = 2;
    private static final String TAG = "EventDetailActivity";
    public String datecomparable;
    public ImageView imageView;
    CardView actionbuttons;
    private Toolbar mToolbar;
    private ImageView event_image;
    private TextView dateview, desc, title, cat, hwmany;
    private DatabaseReference mDatabase, rootRef, eventAttendes, users;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private String event_id, current_user_id,uid;
    private StringBuilder mUsers;
    private String mGoersString;
    private TextView peopeleGoing, postTimestamp;
    private String intentShareText = null;
    private boolean willGo = false;
    private String username;
    RelativeLayout rel2;
    private RelativeLayout share_image, comments_image;
    ImageView going;
    private TextView whatpeopleAreSaying;
    private Context mContext;
    private String event_name;
    private String event_imageUrl, place;
    private String eventDate;
    private TextView place_name;
    private TextView evNameText;
    private Toolbar mEDToolbar;
    ProgressBar progressBar;
    CardView bottomcard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        progressBar=findViewById(R.id.progressBar);
        actionbuttons = findViewById(R.id.actionbuttons);
        bottomcard=findViewById(R.id.bottomcard);
        setupToolbar();
        initWidgets();
        setUpFirebase();
        actionbuttons.setVisibility(View.VISIBLE);
        rel2=findViewById(R.id.rel2);
        setUpWidgets();
        upDateViews();
        doFirst();
        place_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(EventDetailActivity.this,PlaceActivity.class);
                i.putExtra("place_id",uid)
                ;
                EventDetailActivity.this.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                startActivity(i);
            }
        });
     //TODO DEREGISTER ALL THESE LISTENERS ON STOP
    }

    private void doFirst() {
        rootRef.child(getString(R.string.userDb)).child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            username = dataSnapshot.child("name").getValue().toString();
                        } catch (NullPointerException e) {
                            Log.d(TAG, "onDataChange: NullPointerException" + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        //--------------------------LISTENER ON CURRREN T EVENT NODE----------------------//
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot);
//                try {
                    String titl = dataSnapshot.child("name").getValue().toString();
                    uid = dataSnapshot.child("uid").getValue().toString();
                    getSupportActionBar().setTitle(Handy.getTrimmedName(titl));
                    String cats = dataSnapshot.child("category").getValue().toString();
                    cat.setText(cats);
                    place = dataSnapshot.child("place_name").getValue().toString();
                    place_name.setText(Handy.getTrimmedName(place));
                    title.setText(Handy.getTrimmedName(titl));
                    String date = dataSnapshot.child("date").getValue().toString();
                    datecomparable = dataSnapshot.child("date_comparable").getValue().toString();
                    String time = dataSnapshot.child("time").getValue().toString();
                    dateview.setText(date + " " + time);

                    String img = dataSnapshot.child("cover").getValue().toString();
                    String imageToLoad="";
                  try {
                      String image2=getIntent().getStringExtra("image_url");
                      Log.d(TAG, "onDataChange:length "+image2.length());
                      if (image2.length()==0){
                          imageToLoad=img;
                          Log.d(TAG, "onDataChange:fro network ");
                      }
                      else {
                          imageToLoad=image2;
                          Log.d(TAG, "onDataChange: from intent strait");
                      }
                  }catch (NullPointerException e){
                      imageToLoad=img;
                  }



                    try {
                        Glide.with(mContext).load(imageToLoad).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(mContext, "could not load the photo", Toast.LENGTH_SHORT).show();

                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(View.VISIBLE);

                                return false;
                            }
                        })
                                .thumbnail(0.5f)
                                .into(event_image);
                        bottomcard.setVisibility(View.VISIBLE);



                    } catch (Exception e) {

                    }
                    String description = dataSnapshot.child("desc").getValue().toString();
                    long postedOn = (long) dataSnapshot.child("posted").getValue();
                    String fix = "POSTED ";
                    desc.setText(Handy.capitalize(description));
                    try {
                        postTimestamp.setText(fix.concat(GetTimeAgo.getTimeAgo(postedOn, mContext).toUpperCase()));
                    } catch (Exception e) {
                        Log.d(TAG, "onDataChange: " + e.getMessage());
                    }
                    intentShareText = "Do not miss " + titl + " at " + place + " \n"
                            + description + "\n" +
                            "on " +
                            date + " " + time + " \n" + "Lets meet there\nDownload the GetAplot App for your phone ".concat("https://goo.gl/vgE2nk");
//                } catch (NullPointerException ex) {
//                    Log.d(TAG, "onDataChange: NullPointerException:" + ex.getMessage());
//                    ex.printStackTrace();
//                    Toast.makeText(mContext, "Event has been removed by the Place Adminstrator", Toast.LENGTH_SHORT).show();
//                    Intent i = new Intent(EventDetailActivity.this, MainActivity.class);
//                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(i);
//                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    //-------------------------------FIEBASE INITIALISATIONS-------------------------------------------------------//
    private void setUpFirebase() {
        mAuth = FirebaseAuth.getInstance();
        event_id = getIntent().getStringExtra("event_id");
        rootRef = FirebaseDatabase.getInstance().getReference();
        mDatabase = rootRef.child("Events").child(event_id);
        current_user_id = mAuth.getCurrentUser().getUid();
        eventAttendes = rootRef.child("eventAttendees").child(event_id);
    }


    //--------------------------------TOOLBAR-----------------------------------//
    private void setupToolbar() {
        mEDToolbar = findViewById(R.id.eventdetail_toolbar);
        setSupportActionBar(mEDToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.custom_ev_detail_evtoolbar, null);
        actionBar.setCustomView(action_bar_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        evNameText = findViewById(R.id.evtest);
        mContext = EventDetailActivity.this;
        event_id = getIntent().getStringExtra("event_id");


    }

    private void initWidgets() {

        peopeleGoing = findViewById(R.id.peoplegoing);
        place_name = findViewById(R.id.place_name);
        event_image = findViewById(R.id.e_image);
        dateview = findViewById(R.id.dateview);
        desc = findViewById(R.id.desc);
        title = findViewById(R.id.title_v);
        cat = findViewById(R.id.ev_cattt);
        hwmany = findViewById(R.id.peoplegoing);
        share_image = findViewById(R.id.share_image);
        comments_image = findViewById(R.id.comment_image);



        //
        //  progressBar=findViewById(R.id.pe);
    }

    //------------------------------WIDGETS----------------------------------------------//
    private void setUpWidgets() {
        //----------fixme after event loading----------------//
        comments_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(mContext, EventCommentsActivity.class);
                    i.putExtra("event_id", event_id);
                    startActivity(i);
                } catch (Exception e) {
                    startActivity(new Intent(EventDetailActivity.this, MainActivity.class));
                }
            }
        });

        DatabaseReference comments = FirebaseDatabase.getInstance().getReference().child("event_comments").child(event_id);
        comments.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                Log.d(TAG, "onDataChange: TotalComments" + count);
                if (count == 1) {
                    whatpeopleAreSaying.setText("View the comment");
                } else if (count > 1) {
                    whatpeopleAreSaying.setText("View all ".concat(String.valueOf(count) + " " + " comments"));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        whatpeopleAreSaying = findViewById(R.id.all_sayings);
        whatpeopleAreSaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Attempting to navigate to comments");
                try {
                    Intent i = new Intent(mContext, EventCommentsActivity.class);
                    i.putExtra("event_id", event_id);
                    startActivity(i);
                } catch (Exception e) {

                }
            }
        });
        going = findViewById(R.id.going_img);
        postTimestamp = findViewById(R.id.posttimestamp);
        mContext = EventDetailActivity.this;
        going.setVisibility(View.VISIBLE);

//----------------------SHARING EVENT STUFF---------------------------------//
        share_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, intentShareText);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);

                } else {
                    Toast.makeText(mContext, "Looks like no app can handle this data", Toast.LENGTH_SHORT).show();
                }
            }
        });
//-------------------------THE GOING OR NOT STUFF---------------------------------------------------//
        going.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                eventAttendes.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                            Log.d(TAG, "onDataChange: User not going");
                          final Map<String, Object> goingMap = new HashMap<>();
                            goingMap.put("time", GetCurTime.getCurTime());
                            goingMap.put("user_id", mAuth.getCurrentUser().getUid());
                            if (!willGo) {
                                eventAttendes.child(mAuth.getCurrentUser().getUid()).updateChildren(goingMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            Snackbar.make(findViewById(R.id.activity_event_detail),
                                                    "You want to attend", Snackbar.LENGTH_SHORT).show();
                                            willGo = true;
                                            going.setBackgroundResource(R.drawable.ic_stamp);
                                        } else {
                                            willGo = false;

                                        }
                                    }
                                });
                            }
                        }
                        if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                            Log.d(TAG, "onDataChange: User is not going");
                            if (willGo) {
                                eventAttendes.child(mAuth.getCurrentUser().getUid()).removeValue()
                                        .addOnCompleteListener(
                                                new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "onComplete: Remved");
                                                            upDateViews();
                                                            Snackbar.make(findViewById(R.id.activity_event_detail),
                                                                    "You donot want to attend anymore", Snackbar.LENGTH_SHORT).show();
                                                            going.setBackgroundResource(R.drawable.ic_going);

                                                        } else {
                                                            //willGo=true;
                                                            Snackbar.make(findViewById(R.id.activity_event_detail),
                                                                    "Something went wrong", Snackbar.LENGTH_SHORT).show();

                                                        }
                                                    }
                                                }
                                        );
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });

    }

    //-----------------FIX THEM HERE---------------------------------------------------------//
    private void upDateViews() {
        Log.d(TAG, "upDateViews: ");
        //---------------------------------LISTENER STUCK JUST ONCREATE-----------------------------------//
        eventAttendes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final int count = (int) dataSnapshot.getChildrenCount();
                Log.d(TAG, "onDataChange: counting now" + count);
                DatabaseReference reference = rootRef.child("Events").child(event_id);
                //todo checkhere if things go wronf
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            String datec = dataSnapshot.child("date_comparable").getValue().toString();
                            Log.d(TAG, "how " + datec);

                            if (Integer.parseInt(GetCurTime.today()) > Integer.parseInt(datec)) {
                                Log.d(TAG, "pasttrue");

                                peopeleGoing.setText("" + (count) + " people went");
                                going.setVisibility(View.GONE);
                                share_image.setVisibility(View.GONE);
                                rel2.setVisibility(View.GONE);

                                return;
                            }
                        } catch (NullPointerException e) {
                            Log.d(TAG, "onDataChange: NullPointerException");
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                    Log.d(TAG, "onDataChange: user going");
                    willGo = true;
                    try {
                        going.setBackgroundResource(R.drawable.ic_stamp);
                    } catch (NullPointerException e) {

                    }
                    if (count == 1) {
                        peopeleGoing.setText("You are attending this");
                    } else {
                        peopeleGoing.setText("You and " + (count - 1) + " other are going");
                        peopeleGoing.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.d(TAG, "onClick: seetting for the attenddees");
                                try {
                                    Intent i = new Intent(mContext, EventAttendeesActivity.class);
                                    i.putExtra("event_id", event_id);
                                    startActivity(i);
                                } catch (Exception e) {
                                    Log.d(TAG, "onClick: " + e.getMessage());
                                    Toast.makeText(mContext, "sorry the attendes are unavailable at the moment", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }


                } else if (!dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                    willGo = false;
                    going.setBackgroundResource(R.drawable.ic_going);
                    if (count == 1) {
                        peopeleGoing.setText("One person is going");
                    } else if (count == 0) {
                        peopeleGoing.setText("");
                    } else {
                        peopeleGoing.setText(count + " people are going");
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

//TODO GETCHILDREN COUNT



    @Override
    protected void onStart() {
        super.onStart();
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue("true");
    }

    @Override
    protected void onStop() {
        super.onStop();
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    public void onBackPressed() {
        try {
            finish();

        } catch (NullPointerException e) {
            String ca = getIntent().getStringExtra("calling");
            if (!ca.equals("")) {
                String place_id = getIntent().getStringExtra("place_id");
                Intent i = new Intent(mContext, PlaceActivity.class);
                i.putExtra("place_id", place_id);

                startActivity(i);
            }
            Log.d(TAG, "onBackPressed: " + e.getMessage());
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: ");
        getMenuInflater().inflate(R.menu.ev_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: ");
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
