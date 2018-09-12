package com.getaplot.getaplot.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Comment;
import com.getaplot.getaplot.utils.GetCurTime;
import com.getaplot.getaplot.utils.GetShortTimeAgo;
import com.getaplot.getaplot.utils.Handy;
import com.getaplot.getaplot.utils.ImageManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.media.MediaRecorder.VideoSource.CAMERA;

public class EventCommentsActivity extends AppCompatActivity {
    public static final int READ_EXTERNAL_STORAGE = 0;
    private static final String TAG = "EventCommentsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION_ACCESS = ACCESS_COARSE_LOCATION;
    private static final String STORAGE_PERMISSIN = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final int CAMERA_REQUEST = 4;
    private static final int GALLERY_INTENT = 2;
    private static final int VIDEO_REQUEST_CODE = 6;
    private static final int PERMISSION_INT = 1234;
    private static final int PLACE_PICKER_REQUEST = 909;
    boolean isGPS = false;
    boolean isNetwork = false;
    boolean canGetLocation = true;
    LocationManager locationManager;
    Location loc;
    //Add Emojicon
    EmojiconEditText emojiconEditText;
    ImageView emojiButton, submitButton;
    EmojIconActions emojIconActions;
    RelativeLayout layout;
    private ProgressBar mProgress;
    private ProgressBar progressBar;
    private String event_id, event_name, name, photoUrl, eventDate, eventImageUrl;
    private DatabaseReference rootRef, commentsDatabase, userDb, cdb, likeDb;
    private ImageButton sendbtn;
    private ImageView back;
    private CircleImageView evImage;
    private EditText commentInput;
    private RelativeLayout relativeLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView commentsList;
    private FirebaseAuth mAuth;
    private Query query;
    private TextView dt, nameEv;
    private Context mContext;
    private boolean processLike = false;
    private Toolbar mCommentsBar;
    private ImageView backbtn;
    private ProgressDialog progressDialog;
    private boolean mPermissionGranted = false;
    private StorageReference mStorage;
    private GoogleMap mgoogleMap;
    private FirebaseRecyclerAdapter<Comment, CommentsVH> adapter;
    private TextView nocommntsyt;
    private double points;
    private Boolean isImage = false;
    private double mProgres = 0;
    private DatabaseReference userPhotoNode;
    private DatabaseReference userPoints;
    private static int VIDEO_CAPTURE_REQUEST_CODE = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event_comments);
        progressBar = findViewById(R.id.pe);
        userPoints = FirebaseDatabase.getInstance().getReference().child("userPoints").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        Log.d(TAG, "onCreate: created");
        mContext = EventCommentsActivity.this;
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        nocommntsyt = findViewById(R.id.nocommentsyet);
        rootRef = FirebaseDatabase.getInstance().getReference();
        cdb = rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("online");
        progressDialog = new ProgressDialog(this);
        userPhotoNode = rootRef.child("userPhotos").child(mAuth.getCurrentUser().getUid());


        layout = findViewById(R.id.commentssection);
        emojiButton = findViewById(R.id.emoji_button);
        sendbtn = findViewById(R.id.comment_add_btn);
        emojiconEditText = findViewById(R.id.comment_message_view);

        EmojIconActions emojIcon = new EmojIconActions(this, layout, emojiconEditText, emojiButton,
                "#495C66", "#DCE1E2", "#E6EBEF");
        emojIcon.ShowEmojIcon();
        //----------------------TOOLBAR FIX--------------------------------------//
        mCommentsBar = findViewById(R.id.commentsappbar);

        setSupportActionBar(mCommentsBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.custom_comments_bar, null);

        ActionBar ac = getSupportActionBar();
        ac.setDisplayShowCustomEnabled(true);
        ac.setCustomView(action_bar_view);
        ac.setDisplayHomeAsUpEnabled(true);
        ac.setDisplayShowTitleEnabled(false);


//-------------------------------GET CURRENT POINTS
        userPoints.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    points = Double.parseDouble(dataSnapshot.child("points").getValue().toString());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //---------------------THE BACK BUTTN FIX---------------------------//


        commentsList = findViewById(R.id.comments_list);
        //swipeRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        nameEv = findViewById(R.id.event_name_tocomment);
        // evImage = findViewById(R.id.leavecomment);

        //----------------------------------------------------------------------------//

        try {
            //fixme i dont know what thiz were for remind me lator
            likeDb = rootRef.child("EventCommentLikes");
            likeDb.keepSynced(true);

            //--------------------------------GET EVENT ID --------------------------------------//
            try {
                event_id = getIntent().getStringExtra("event_id");
                Log.d(TAG, "onCreate: Event id" + event_id);

                DatabaseReference events = rootRef.child("Events").child(event_id);
                events.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "event " + dataSnapshot);
                        event_name = dataSnapshot.child("name").getValue().toString();
                        eventDate = dataSnapshot.child("date").getValue().toString();
                        eventImageUrl = dataSnapshot.child("cover").getValue().toString();
                        dt = findViewById(R.id.evdate);
                        nameEv.setText(Handy.getTrimmedName(event_name));
                        dt.setText(eventDate);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } catch (NullPointerException e) {
                Toast.makeText(mContext, "Event has been removed by the Place Adminstrator", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(mContext, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
            Log.d(TAG, "onCreate: Event Name" + event_name);
            //-------------------------------VIEW INITIAILISATIONS--------------------------//

            //------------------------------------------------------------------------------------------------

        } catch (Exception e) {

        }
        commentsList.setLayoutManager(new LinearLayoutManager(EventCommentsActivity.this));
        commentsList.setHasFixedSize(true);
        //------------------------------
        rootRef = FirebaseDatabase.getInstance().getReference();
        commentsDatabase = rootRef.child("event_comments").child(event_id);
        query = commentsDatabase.orderByChild("lastcommentedon");
        Log.d(TAG, "onCreate: commentsdb");
        //------------------------------clear defaults if we have dta
        commentsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    nocommntsyt.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    nocommntsyt.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void handeTheToggling() {
        Log.d(TAG, "handeTheToggling: ");
        Log.d(TAG, "onClick: trying to choose ATTACHEMENT");
        AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
        CharSequence[] options = {"Gallery", "Camera"};
        ad.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    //Check for Runtime Permission
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
                        }
                    } else {
                        callgalary();
                    }
                }

                if (i == 1) {
                    //Check for camera Runtime Permission
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA);
                        }
                    } else {
                        callCamera();


                    }
                }

                if (i == 2) {
//                    //Check for camera Runtime Permission
//                    getLocationPersion();
//                    //getDeviceLocation();
//
//                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
//
//                    try {
//                        startActivityForResult(builder.build(EventCommentsActivity.this), PLACE_PICKER_REQUEST);
//                    } catch (GooglePlayServicesRepairableException e) {
//                        Log.e(TAG, "onClick: GooglePlayServicesRepairableException: " + e.getMessage());
//                    } catch (GooglePlayServicesNotAvailableException e) {
//                        Log.e(TAG, "onClick: GooglePlayServicesNotAvailableException: " + e.getMessage());
//                    }
//
//
//                }
//
//            }
//


                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    File mediaFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "newvideo.mp4");
                    Uri uri = Uri.fromFile(mediaFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 180);
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    startActivityForResult(intent, VIDEO_REQUEST_CODE);


                }
            }

        }).show();

    }

    //-----------------------------ADD A NEW COMMENT-------------------------------//
    private void sendComment(final String comment, final String type) {
        emojiconEditText.setText("");

        //-----------save users pic
        if (type.equals("image")) {
            Map hmap = new HashMap();
            hmap.put("image", comment);
            hmap.put("thumb_image", comment);
            hmap.put("lastUpdated", Handy.fitnessNumber());
            userPhotoNode.push().setValue(hmap);
            Map mapp = new HashMap();
            Double v = points + 1.25;
            mapp.put("points", v);
            mapp.put("fitnessPoint", Handy.fitnessPoint(v));
            userPoints.setValue(mapp);
        } else {
            Map mapp = new HashMap();
            Double v = points + 0.25;
            mapp.put("points", v);
            mapp.put("fitnessPoint", Handy.fitnessPoint(v));
            userPoints.setValue(mapp);
        }
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        rootRef.child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String image = dataSnapshot.child("image").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("text", comment);
                commentMap.put("username", mAuth.getCurrentUser().getDisplayName());
                commentMap.put("user_picture", image);
                commentMap.put("type", type);
                commentMap.put("uid", user.getUid());
                commentMap.put("lastcommentedon", ServerValue.TIMESTAMP);
                commentMap.put("fitnessNum", Handy.fitnessNumber());
                commentsDatabase.push().setValue(commentMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            //todo anything
                            Log.d(TAG, "onComplete: Comment posted");
                            com.google.firebase.messaging.FirebaseMessaging.getInstance()
                                    .subscribeToTopic(event_id);
                            DatabaseReference reference = rootRef.child("CommentsNotifications");
                            Map<String, String> notiidata = new HashMap<>();
                            notiidata.put("username", mAuth.getCurrentUser().getDisplayName());
                            notiidata.put("name", event_name);
                            notiidata.put("text", comment);
                            notiidata.put("imageUrl", eventImageUrl);
                            notiidata.put("event_id", event_id);
                            notiidata.put("topic", event_id);
                            reference.push().setValue(notiidata).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onComplete: Comment posted");
                                }
                            });

                            emojiconEditText.setText("");
                            //FIXME FIX SCROLLING TO LAST
                            //TODO
                            Toast.makeText(mContext, "Commented added", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                Snackbar.make(relativeLayout, "could nt post ur comment ", Snackbar
                                        .LENGTH_SHORT).show();
                            } catch (Exception e) {

                            }
                            Log.d(TAG, "onComplete: Database error" + databaseError);

                        }
                        sendbtn.setEnabled(true);
                        emojiconEditText.setText("");

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onStart() {



            FirebaseRecyclerOptions<Comment> options =
                    new FirebaseRecyclerOptions.Builder<Comment>()
                            .setQuery(query, Comment.class)
                            .build();

            adapter = new FirebaseRecyclerAdapter<Comment, CommentsVH>(options) {
                @Override
                public CommentsVH onCreateViewHolder(ViewGroup parent, int viewType) {
                    // Create a new instance of the ViewHolder, in this case we are using a custom
                    // layout called R.layout.message for each item
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.singleeventcomment, parent, false);

                    return new CommentsVH(view);
                }

                @Override
                protected void onBindViewHolder(final CommentsVH commentsVH, final int i, final Comment comment) {
                    final String commentId = getRef(i).getKey();

                    commentsDatabase.child(commentId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "onDataChange: Hurry" + dataSnapshot);
                            isImage = false;
                            try {
                                String type = dataSnapshot.child("type").getValue(String.class);
                                final String commentText = dataSnapshot.child("text").getValue().toString();
                                assert type != null;
                                switch (type) {

                                    case "image":
                                        isImage = true;
                                        commentsVH.mapView.setVisibility(View.GONE);
                                        commentsVH.comment.setVisibility(View.GONE);
                                        commentsVH.commentImage.setVisibility(View.VISIBLE);
                                        Glide.with(mContext)
                                                .load(commentText)
                                                .into(commentsVH.commentImage);
                                        commentsVH.commentImage.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Log.d(TAG, "onClick: ImageClicked try n navigate");
                                                try {
                                                    Intent i = new Intent(mContext, EnlargeImageView.class);
                                                    i.putExtra("image_url", comment.getText());
                                                    EventCommentsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                    startActivity(i);
                                                } catch (Exception e) {
                                                    Log.d(TAG, "onClick: " + e.getMessage());
                                                }
                                            }
                                        });
                                        break;
                                    case "text":
                                        isImage = false;
                                        commentsVH.commentImage.setVisibility(View.GONE);
                                        commentsVH.comment.setVisibility(View.VISIBLE);
                                        commentsVH.mapView.setVisibility(View.GONE);
                                        commentsVH.setCommentText(commentText);
                                        break;
                                    case "location":
                                        isImage = false;
                                        commentsVH.comment.setVisibility(View.GONE);
                                        commentsVH.mapView.setVisibility(View.VISIBLE);
                                        commentsVH.commentImage.setVisibility(View.GONE);
                                        commentsVH.setCommentUser(dataSnapshot.child("username").getValue(String.class));
                                        commentsVH.setCommentUserImage(dataSnapshot.child("user_picture").getValue(String.class), getApplicationContext());

                                        Glide.with(mContext).load(R.drawable.ptest).into(commentsVH.mapView);
                                        commentsVH.comment.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Log.d(TAG, "onClick: Navigating location activity");
                                                final String lat = comment.getText().substring(0, commentText.indexOf(" "));
                                                final String lng = comment.getText().substring(commentText
                                                        .lastIndexOf(" ", comment.getText().length()));

                                                Log.d(TAG, "onClick: Latitude" + lat);
                                                Log.d(TAG, "onClick: Longtitude" + lng);
                                                Intent i = new Intent(mContext, LocationActivity.class);
                                                i.putExtra("latitude", lat);
                                                i.putExtra("longtitude", lng);
                                                i.putExtra("event_id", event_id);
                                                EventCommentsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                startActivity(i);

                                            }
                                        });
                                        break;

                                    default:
                                        return;
                                }
                            } catch (NullPointerException e) {
                                //--------------------PREVIOUS COMMENTS FROM UNKNOWN PLACES/PLACES WITH NO IDS IN USER NODE
                            }
                            //actual fix seemed the model doesnt get update when data changes,need listeners now
                            try {
                                FirebaseDatabase.getInstance().getReference().child("Users").child(comment.getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        try {
                                            commentsVH.setCommentUser(Handy.getTrimmedName(dataSnapshot.child("name").getValue()
                                                    .toString()));
                                        } catch (NullPointerException e) {
                                            Log.d(TAG, "onDataChange: NullPointerException" + e.getMessage());
                                        }


                                        try {
                                            commentsVH.setCommentUserImage(dataSnapshot.child("image").getValue()
                                                            .toString(),
                                                    getApplicationContext());
                                        } catch (NullPointerException e) {
                                            Log.d(TAG, "onDataChange: NullPointerException:" + e.getMessage());
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } catch (NullPointerException e) {

                            }
                            commentsVH.setLastCommentedOn(GetShortTimeAgo.getTimeAgo(comment.getLastcommentedon(), mContext));


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    final String user;

                    //---------------------DELETING THE COMMENT--------------------//
                    //OnClick Item it will Delete data from Database

                    commentsVH.mView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            if (mAuth.getCurrentUser().getUid().equals(comment.getUid())) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setMessage("This will delete your comment").setCancelable(false)
                                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                int selectedItems = i;
                                                try {
                                                    adapter.getRef(selectedItems).removeValue();
                                                    adapter.notifyItemRemoved(selectedItems);
                                                    commentsList.invalidate();
                                                    onStart();
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
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                return true;
                            } else {
                                return false;
                            }

                        }
                    });

                    commentsVH.commI.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final CharSequence[] options = {"View Profile", "Open Chat"};
                            FirebaseDatabase.getInstance().getReference().child("Users")
                                    .child(comment.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(final DataSnapshot dataSnapshot1) {
                                    if (!dataSnapshot1.hasChild("isplace")) {
                                        if (!mAuth.getCurrentUser().getUid().equals(comment.getUid())) {
                                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                                    .child("Friends").child(mAuth.getCurrentUser().getUid());
                                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.hasChild(comment.getUid())) {

                                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                if (i == 0) {
                                                                    Intent intent = new Intent(mContext, ProfileActivity.class);
                                                                    EventCommentsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                    intent.putExtra("user_id", comment.getUid());
                                                                    intent.putExtra("user_name", dataSnapshot1.child("name").getValue().toString());
                                                                    startActivity(intent);
                                                                } else {
                                                                    try {
                                                                        Intent intent = new Intent(mContext, ChatActivity.class);
                                                                        EventCommentsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                        intent.putExtra("user_id", comment.getUid());
                                                                        startActivity(intent);

                                                                    } catch (NullPointerException ex) {
                                                                        Log.d(TAG, "onClick: NullPointerException " + ex.getMessage());
                                                                        ex.printStackTrace();


                                                                    }
                                                                }
                                                            }
                                                        });
                                                        builder.setTitle("User Options").show();
                                                    } else {
                                                        Intent intent = new Intent(mContext, ProfileActivity.class);
                                                        EventCommentsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                        intent.putExtra("user_id", comment.getUid());
                                                        intent.putExtra("user_name", dataSnapshot1.child("name").getValue().toString());
                                                        intent.putExtra("user_name", dataSnapshot1.child("name").getValue().toString());

                                                        startActivity(intent);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }


                                    } else {


                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("Place Options").setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (i == 0) {
                                                    Intent intent = new Intent(mContext, PlaceActivity.class);
                                                    EventCommentsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                    intent.putExtra("place_id", comment.getUid());
                                                    try {
                                                        intent.putExtra("image_url", comment.getUser_picture());
                                                    } catch (NullPointerException e) {

                                                    }
                                                    startActivity(intent);
                                                } else {


                                                    Intent intent = new Intent(mContext, ChatActivity.class);
                                                    EventCommentsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                    intent.putExtra("user_id", comment.getUid());
                                                    try {
                                                        intent.putExtra("image_url", comment.getUser_picture());
                                                    } catch (NullPointerException e) {

                                                    }
                                                    startActivity(intent);
                                                }
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


                    //-------------------------INTENT FOR PROFILES----------------------------------//
//FRST CHECKS IF WE ARE FRIENDS FOR MORE OPTIONS
                    commentsVH.comm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final CharSequence[] options = {"View Profile", "Open Chat"};
                            FirebaseDatabase.getInstance().getReference().child("Users")
                                    .child(comment.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(final DataSnapshot dataSnapshot1) {
                                    if (!dataSnapshot1.hasChild("isplace")) {
                                        if (!mAuth.getCurrentUser().getUid().equals(comment.getUid())) {
                                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                                    .child("Friends").child(mAuth.getCurrentUser().getUid());
                                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.hasChild(comment.getUid())) {

                                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                if (i == 0) {
                                                                    Intent intent = new Intent(mContext, ProfileActivity.class);
                                                                    EventCommentsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                    intent.putExtra("user_id", comment.getUid());
                                                                    intent.putExtra("user_name", dataSnapshot1.child("name").getValue().toString());
                                                                    startActivity(intent);
                                                                } else {
                                                                    try {
                                                                        Intent intent = new Intent(mContext, ChatActivity.class);
                                                                        EventCommentsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                                        intent.putExtra("user_id", comment.getUid());
                                                                        startActivity(intent);

                                                                    } catch (NullPointerException ex) {
                                                                        Log.d(TAG, "onClick: NullPointerException " + ex.getMessage());
                                                                        ex.printStackTrace();


                                                                    }
                                                                }
                                                            }
                                                        });
                                                        builder.setTitle("User Options").show();
                                                    } else {
                                                        Intent intent = new Intent(mContext, ProfileActivity.class);
                                                        EventCommentsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                        intent.putExtra("user_id", comment.getUid());
                                                        intent.putExtra("user_name", dataSnapshot1.child("name").getValue().toString());
                                                        intent.putExtra("user_name", dataSnapshot1.child("name").getValue().toString());

                                                        startActivity(intent);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }


                                    } else {


                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("Place Options").setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (i == 0) {
                                                    Intent intent = new Intent(mContext, PlaceActivity.class);
                                                    EventCommentsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                    intent.putExtra("place_id", comment.getUid());
                                                    try {
                                                        intent.putExtra("image_url", comment.getUser_picture());
                                                    } catch (NullPointerException e) {

                                                    }
                                                    startActivity(intent);
                                                } else {


                                                    Intent intent = new Intent(mContext, ChatActivity.class);
                                                    EventCommentsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                    intent.putExtra("user_id", comment.getUid());
                                                    try {
                                                        intent.putExtra("image_url", comment.getUser_picture());
                                                    } catch (NullPointerException e) {

                                                    }
                                                    startActivity(intent);
                                                }
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

                    try {
                        commentsVH.handlereplyButton(mContext, commentId, event_id, comment, comment.getText(), comment.getUsername());

                    } catch (Exception e) {

                    }


                    //--------------------------NEW INTENT TO VIEW THE REPLIES------------------------------------------//
                    commentsVH.replyCount.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(EventCommentsActivity.this, CommentRepliesActivity.class);
                            intent.putExtra("CommentId", commentId);
                            intent.putExtra("uid", comment.getUid());
                            intent.putExtra("event_id", event_id);
                            intent.putExtra("userName", comment.getUsername());
                            intent.putExtra("commentText", comment.getText());
                            intent.putExtra("lastcommentedOn", comment.getLastcommentedon());
                            startActivity(intent);
                        }

                    });


                    //---------------------CALL METHODS TO UPATE VIEWS---------------------------------//
                    commentsVH.setLikeStatus(commentId);
                    commentsVH.getCommentLikeCount(commentId);
                    commentsVH.upDateNumberOfReplies(commentId);

                    commentsVH.likeImage.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            //todo check here
                            likeDb.child(commentId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getChildrenCount() > 0) {

                                        Intent intent = new Intent(EventCommentsActivity.this, CommentLikesActivity.class);
                                        intent.putExtra("commentId", commentId);
                                        startActivity(intent);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            return true;
                        }
                    });


                    //fixme this needs to be done
                    //todo try using the default yes buttons
                    //todo can look for the one

                    commentsVH.likeImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: liking comment " + commentId);
                            try {
                                likeDb.child(commentId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Log.d(TAG, "onDataChange: Datasnapdhot is " + dataSnapshot);
                                        if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                                            Toast.makeText(EventCommentsActivity.this, "Removing Like", Toast.LENGTH_SHORT).show();

                                            likeDb.child(commentId).child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(EventCommentsActivity.this, "like Removed", Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                            commentsVH.getCommentLikeCount(commentId);
                                            if (dataSnapshot.getChildrenCount() == 0) {
                                                commentsVH.LikesCount.setText("");
                                            }

                                            commentsVH.LikesCount.setText(String.valueOf((int) dataSnapshot.getChildrenCount()));

                                            processLike = false;
                                        } else {
                                            try {
                                                Toast.makeText(EventCommentsActivity.this, "Adding Like", Toast.LENGTH_SHORT).show();

                                                //todo sending like notifications has been disabled for issues so the function doesnt actually trigger
                                                DatabaseReference newNotificationref = rootRef.child("LikeNots")

                                                        .child(comment.getUid()).push();
                                                String newNotificationId = newNotificationref.getKey();

                                                HashMap<String, String> notificationData = new HashMap<>();

                                                notificationData.put("from", mAuth.getCurrentUser().getUid());
                                                notificationData.put("type", "like");
                                                notificationData.put("commentId", commentId);
                                                final Map<String, Object> likeMap = new HashMap<>();
                                                likeMap.put("time", ServerValue.TIMESTAMP);
                                                likeMap.put("fitnessNum", Handy.fitnessNumber());
                                                likeMap.put("user_id", mAuth.getCurrentUser().getUid());
                                                if (comment.getUid().equals(mAuth.getCurrentUser().getUid())) {
                                                    likeDb.child(commentId).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                            .updateChildren(likeMap, new DatabaseReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                    Log.d(TAG, "onComplete: LikedThis");
                                                                    Toast.makeText(EventCommentsActivity.this, "Like Added", Toast.LENGTH_SHORT).show();
                                                                    processLike = false;
                                                                    commentsVH.getCommentLikeCount(commentId);

                                                                }
                                                            });
                                                } else {
                                                    rootRef.child("LikeNotifications").child(comment.getUid()).child(newNotificationId)
                                                            .setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            likeDb.child(commentId).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                    .updateChildren(likeMap, new DatabaseReference.CompletionListener() {
                                                                        @Override
                                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                            Log.d(TAG, "onComplete: LikedThis");
                                                                            processLike = false;
                                                                            commentsVH.getCommentLikeCount(commentId);

                                                                        }
                                                                    });
                                                        }
                                                    });
                                                }


                                            } catch (NullPointerException e) {
//issue with pld apps
                                                Toast.makeText(EventCommentsActivity.this, "Could not like this", Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } catch (Exception e) {
                                Toast.makeText(EventCommentsActivity.this, "Could not like this", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
            };


            ImageButton imageButton = findViewById(R.id.action_clip);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handeTheToggling();
                }
            });

            sendbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: clicked");
                    sendbtn.setEnabled(false);
                    String comment = emojiconEditText.getText().toString().trim();
                    if (!TextUtils.isEmpty(comment)) {
                        sendComment(comment, "text");
                    }
                }
            });
            cdb.setValue("true");
            commentsList.setAdapter(adapter);

        adapter.startListening();
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            cdb.setValue(ServerValue.TIMESTAMP);
            adapter.stopListening();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }


    //--------------------LOOKING FOR THE CAMERA--------------------------//
    private void callCamera() {
        Log.d(TAG, "onClick: starting camera");
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);


    }

    //----------------GALLERY FOR IMG
    private void callgalary() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_INTENT);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            Uri mImageUri = data.getData();

//prepea fpr bitmap uploading
            StorageReference filePath = mStorage.child("CommentImageShares").child(event_id).child(mAuth.getCurrentUser().getUid()
            ).child(event_id).child(String.valueOf(ServerValue.TIMESTAMP));

            progressDialog.setMessage("Uploading Image....");
            progressDialog.show();
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();  //Ignore This error
                    sendComment(downloadUri.toString(), "image");
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: " + e.getMessage());
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if (currentProgress > (mProgres + 15)) {
                        mProgres = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: upload is " + mProgress + "& done");

                    }
                }
            });

        }
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: Getting from camera");
                Bitmap bitmap;
                bitmap = (Bitmap) data.getExtras().get("data");
                byte[] bytes = ImageManager.getBytesFromBitmap(bitmap, 100);

                StorageReference filePath = mStorage.child("MessageImageShares").child(mAuth.getCurrentUser().getUid()
                ).child(event_id).child(String.valueOf(ServerValue.TIMESTAMP));

                progressDialog.setMessage("Uploading Image....");
                progressDialog.show();
                filePath.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUri = taskSnapshot.getDownloadUrl();  //Ignore This error
                        sendComment(downloadUri.toString(), "image");
                        progressDialog.dismiss();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        if (currentProgress > (mProgres + 15)) {
                            mProgres = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            Log.d(TAG, "onProgress: upload is " + mProgress + "& done");
                            Toast.makeText(mContext, mProgress + "%", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        if (requestCode == VIDEO_REQUEST_CODE) {


        }
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
//                Place place = PlacePicker.getPlace(this, data);
//                Log.d(TAG, "onActivityResult: About place" + place);
//                LatLng latLng = place.getLatLng();
//                Double latitude = latLng.latitude;
//                Double longtitude = latLng.longitude;
//                String message = String.valueOf(latitude + " " + longtitude);
//                Log.d(TAG, "locmeso " + message);
//                sendComment(message, "location");

            }


        }
    }
    //----------------LOCATION PERMISIIONS-------------------//

    public void getLocationPersion() {
        Log.d(TAG, "getLocationPersion: getting permissions");
        String[] permissions = {ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(mContext, COURSE_LOCATION_ACCESS) == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(mContext, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "getLocationPersion: Permissions granted");
                mPermissionGranted = true;
            } else {
                Log.d(TAG, "getLocationPersion: Permissions not .Now requesting");
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_INT);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionGranted = false;
        switch (requestCode) {
            case PERMISSION_INT:
                if (grantResults.length > 0) {
                    for (int counter = 0; counter < grantResults.length; counter++) {
                        mPermissionGranted = grantResults[counter] == PackageManager.PERMISSION_GRANTED;
                    }
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.commentsmenu, menu);
        return true;
    }


    //--------------FIGURING OUT TURNING OFF NOTIFICATIONS FOR COMMENTS-----------------------//

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:

                Intent i = new Intent(mContext, UserInAppNotiSettingsActivity.class);
                i.putExtra("from", "comments");
                i.putExtra("event_id", event_id);
                startActivity(i);
                break;


            case android.R.id.home:
                finish();
                break;


        }

        return super.onOptionsItemSelected(item);
    }


    public static class CommentsVH extends RecyclerView.ViewHolder {

        View mView;
        TextView comm, comment;
        TextView LikesCount, replyCount;
        ImageView likeImage, replyImage, commentImage;
        DatabaseReference likesDb;
        FirebaseAuth mAuth;
        TextInputEditText replyTextInput;
        ImageView commI;
        ImageView mapView;
        ProgressBar progressBar;

        public CommentsVH(View itemView) {
            super(itemView);
            mView = itemView;
            replyImage = mView.findViewById(R.id.replyimage);
            replyCount = mView.findViewById(R.id.commentsText);
            likeImage = mView.findViewById(R.id.likeacomment);
            LikesCount = mView.findViewById(R.id.likesCount);
            comm = mView.findViewById(R.id.us_nam);
            commentImage = mView.findViewById(R.id.us_comment2);
            mapView = mView.findViewById(R.id.us_comment3);
            likesDb = FirebaseDatabase.getInstance().getReference().child("EventCommentLikes");
            likesDb.keepSynced(true);
            mAuth = FirebaseAuth.getInstance();
            commI = mView.findViewById(R.id.user_image);
            comment = mView.findViewById(R.id.us_comment);
           // Linkify.addLinks(comm,Linkify.ALL);

            //added link mgt using lingfy 9/5/18

        }

        public void handlereplyButton(final Context context, final String commentId,
                                      final String event_id, final Comment comment1, final String comment,
                                      final String username) {
            replyImage.setOnClickListener(new View.OnClickListener() {
                EditText editText = new EditText(context);

                @Override
                public void onClick(View view) {
                    try {
                        editText.setWidth(1000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Reply to " + Handy.getTrimmedName(username) + "`s comment");
                        try {
                            if (comment1.getType().equals("location")) {
                                builder.setMessage("location");
                            } else if (comment1.getType().equals("image")) {
                                builder.setMessage("photo");
                            } else if (comment1.getType().equals("text")) {
                                builder.setMessage(comment);

                            }
                        } catch (NullPointerException e) {
                            builder.setMessage(comment);
                        }

                        builder.setIcon(R.drawable.ic_comment_reply).setView(editText)
                                .setPositiveButton("Reply", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        String replyText = editText.getText().toString();
                                        if (!TextUtils.isEmpty(replyText)) {
                                            if (replyText.length() > 300) {

                                                Toast.makeText(context, "Too long", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            Log.d(TAG, "onClick: Text was not empty");
                                            //------------REPLY TO COMMENT--------------------------------//
                                            Toast.makeText(context, "Posting your reply", Toast.LENGTH_SHORT).show();
                                            commentNow(commentId, replyText, event_id, comment1);

                                        }
                                    }
                                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        }).setCancelable(false)
                                .show();
                    } catch (IllegalStateException e) {
                        //fixme the whatver exception cause the user cannot reply again instatntly fix
                        //todo try to ask astack
                        Log.d(TAG, "onLongClick: " + e.getMessage());
                    }

                }

                //------------------------REPLY TO OTHER USERS COMMENT----------------------------------------------//
                private void commentNow(String commentId, String replyText, String event_id, Comment comment1) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                            .child("EventCommentsReplies").child(commentId);
                    final String[] event_name = new String[1];
                    DatabaseReference newref = FirebaseDatabase.getInstance().getReference().child("Events").child(event_id);
                    newref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            event_name[0] = dataSnapshot.child("name").getValue().toString();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    Map<String, Object> commentReplyMap = new HashMap<>();
                    commentReplyMap.put("relyText", replyText);
                    commentReplyMap.put("event_id", event_id);
                    commentReplyMap.put("eventname", event_name[0]);
                    commentReplyMap.put("time", GetCurTime.getCurTime());
                    commentReplyMap.put("user", mAuth.getCurrentUser().getUid());
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("CommentReplyNotifications");
                    String pushkey = reference1.push().getKey();
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("from", mAuth.getCurrentUser().getUid());
                    map.put("reply", replyText);
                    map.put("event_id", event_id);
                    map.put("commentText", comment1.getText());
                    map.put("lastcommentedOn", String.valueOf(System.currentTimeMillis()));
                    map.put("uid", mAuth.getCurrentUser().getUid());
                    map.put("CommentId", commentId);
                    try {
                        if (!comment1.getUid().equals(mAuth.getCurrentUser().getUid())) {
                            reference1.child(comment1.getUid()).child(pushkey).setValue(map);

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
                    } catch (Exception e) {


                        try {
                            if (!comment1.getUid().equals(mAuth.getCurrentUser().getUid())) {
                                reference1.child(comment1.getUid()).child(pushkey).setValue(map);

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
                        } catch (NullPointerException e1) {
                            Log.d(TAG, "commentNow: NullPointerException:" + e.getMessage());
                            Toast.makeText(context, "Reply wasnot added", Toast.LENGTH_SHORT).show();

                        }


                    }

                }

            });
        }

        //--------------------------------UPDATE THE TEXT ON REPLIES----------------------------------//
        private void upDateNumberOfReplies(String commentId) {
            Log.d(TAG, "upDateNumberOfReplies: fetching count for current comment for comment" + commentId);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().
                    child("EventCommentsReplies").child(commentId);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "reps: Snap" + dataSnapshot);
                    int count = (int) dataSnapshot.getChildrenCount();
                    Log.d(TAG, "repscount: The currentCount" + count);
                    if (count == 1) {
                        replyCount.setText("View the 1 reply");
                    }
                    if (!dataSnapshot.exists()) {
                        replyCount.setText("");
                    }
                    if (count > 1) {
                        replyCount.setText("View the " + count + " Replies");
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        public void setCommentText(String commentText) {
            comment.setText(commentText);
        }

        public void setCommentUser(String commentUser) {

            comm.setText(commentUser);
        }

        public void setCommentUserImage(String imageUrl, Context context) {
            RequestOptions options=new RequestOptions().placeholder(R.drawable.test);
            Glide.with(context).load(imageUrl).apply(options).into(commI);
        }

        public void setLastCommentedOn(String timestamp) {
            TextView comm = mView.findViewById(R.id.commenttime);
            comm.setText(timestamp);

        }

        public void setLikeStatus(final String commentId) {
            likesDb.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(commentId).hasChild(mAuth.getCurrentUser().getUid())) {
                        likeImage.setImageResource(R.drawable.ic_thumprimary_comments);
                    } else {
                        likeImage.setImageResource(R.drawable.ic_like_comment_comment);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void getCommentLikeCount(String commentId) {

            likesDb.child(commentId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int count = (int) dataSnapshot.getChildrenCount();
                    if (count == 0) {
                        LikesCount.setText("");
                        likeImage.setImageResource(R.drawable.ic_like_comment_comment);
                    }
                    LikesCount.setVisibility(View.VISIBLE);
                    LikesCount.setText(Integer.toString(count));
                    Log.d(TAG, "onDataChange: the count is " + count);


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }
}
