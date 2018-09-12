package com.getaplot.getaplot.ui;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.fragments.place.SearchPlacesActivity;
import com.getaplot.getaplot.model.Place;
import com.getaplot.getaplot.ui.settings.AllSettingsActivity;
import com.getaplot.getaplot.ui.users.PickAuserNameActivity;
import com.getaplot.getaplot.utils.GetCurTime;
import com.getaplot.getaplot.utils.Handy;
import com.getaplot.getaplot.utils.Location;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;

public class PlacesActivity extends AppCompatActivity {
    private static final String TAG = "PlacesActivity";
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    private static final int ACTIVITY_NUM = 0;
    RelativeLayout relativeLayout;
    private RecyclerView mPlacesList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, rootRef;
    private AppCompatButton rmbtn;
    private String currentUserId;
    private TextView count;
    private RelativeLayout progressBar;
    private FirebaseRecyclerAdapter<Place, PlacesVH> adapter;
    private Query query;
    private Context mContext;
    private TextView textView, txt, no;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_places);
        count = findViewById(R.id.count);
        Toolbar toolbar = findViewById(R.id.toolbar);
        relativeLayout = findViewById(R.id.id);
        txt = findViewById(R.id.txt);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar = findViewById(R.id.loadfav);
        no = findViewById(R.id.nolo);
        swipeRefreshLayout = findViewById(R.id.swipe);

        DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("userName")) {

                    Snackbar.make(progressBar, "Your username is not set", Snackbar.LENGTH_INDEFINITE).setAction("Add userName", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(mContext, PickAuserNameActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                        }
                    }).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //------------------------FIREBASE-----------------------------
        //------------------------------------FIREBASE AUTH-----------------------------------//
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        mPlacesList = findViewById(R.id.placelist);
        mPlacesList.setHasFixedSize(true);
        mPlacesList.setAdapter(adapter);
        mContext = PlacesActivity.this;
        mPlacesList.setLayoutManager(new LinearLayoutManager(mContext));
        mDatabase = rootRef.child("Place_Users");
        mDatabase.keepSynced(true);
        textView = findViewById(R.id.txt2);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Handy.isNetworkAvailable(mContext)) {
                    onStart();
                } else {
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    Snackbar.make(progressBar, "Not connected", Snackbar.LENGTH_LONG).setAction("Connect", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);

                        }
                    }).show();

                }

            }
        });


    }

    @Override
    protected void onStart() {

        Location location = new Location();
        location.getLocationPermission(this, this);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);


        final String userplace = preferences.getString("province", "");
        Log.d(TAG, "onCreate: Current selection" + userplace);
        if (userplace.equals("") || userplace.equals("Current Location")) {
            Location.getDeviceLocation(this, this);
            SharedPreferences sharedPreferences = getSharedPreferences("district", Context.MODE_PRIVATE);
            String nn = sharedPreferences.getString("district", "");
            query = mDatabase.orderByChild("district").equalTo(nn);
            textView.setVisibility(View.INVISIBLE);
            if (nn.equals("")) {
                query = mDatabase.orderByChild("name");
            }
//                txt.setVisibility(View.INVISIBLE);
//                progressBar.setVisibility(View.INVISIBLE);
//                no.setVisibility(View.VISIBLE);
//                textView.setVisibility(View.INVISIBLE);
//                swipeRefreshLayout.setEnabled(true);

        } else {
            query = mDatabase.orderByChild("name");
            no.setVisibility(View.INVISIBLE);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        progressBar.setVisibility(GONE);
                        textView.setVisibility(View.VISIBLE);
                        textView.setText("No places to show from selected region");
                        Snackbar.make(relativeLayout, "No places to show", Snackbar.LENGTH_INDEFINITE).setAction("Change Region", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(mContext, SettingActivity.class));
                            }
                        }).show();

                        swipeRefreshLayout.setEnabled(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        FirebaseRecyclerOptions<Place> options =
                new FirebaseRecyclerOptions.Builder<Place>()
                        .setQuery(query, Place.class)
                        .build();


        adapter = new FirebaseRecyclerAdapter<Place, PlacesVH>(options) {
            @Override
            public PlacesVH onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.singletestplace, parent, false);

                return new PlacesVH(view);
            }

            @Override
            protected void onBindViewHolder(PlacesVH viewHolder, int position, final Place model) {
                // Bind the Chat object to the ChatHolder
                // ...


                try {
                    viewHolder.setPlaceName(Handy.getTrimmedName(model.getName()));
                } catch (NullPointerException e) {
                    Log.d(TAG, "populateViewHolder: NullPointerException" + e.getMessage());

                }
                viewHolder.setCategory(model.getCategory());
                viewHolder.setImage(model.getImage(), mContext);
                viewHolder.setPlaceDescription(model.getDescription());

                try {
                    viewHolder.setPlaceDistrict(model.getDistrict());
                } catch (NullPointerException e) {

                }

                //------------------GET UNIQUE ID OF ACH PLAC4 AND PASSS IT IN AN INTENT------------------//
                final String placeId = getRef(position).getKey();

                //-----------------check user existance-------------------//
                checkifUserFollowed(placeId, viewHolder);
                updateEventCount(placeId, viewHolder);
                progressBar.setVisibility(GONE);
                swipeRefreshLayout.setEnabled(true);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, PlaceActivity.class);
                        i.putExtra("place_id", placeId);
                        i.putExtra("place_name", model.getName());
                        i.putExtra("description", model.getDescription());
                        i.putExtra("image_url", model.getImage());

                        startActivity(i);

                    }
                });
                try {
                    viewHolder.placeImage.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     Intent i = new Intent(mContext, EnlargeImageView.class);
                     Pair[] pairs = new Pair[1];
                     pairs[0] = new Pair<View, String>(viewHolder.placeImage, "cpic");
                     i.putExtra("image_url", model.getImage());
                     if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(PlacesActivity.this, pairs);
                         startActivity(i, activityOptions.toBundle());

                     } else {
                         startActivity(i);
                     }
                 }
                                                             }

                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //
            }
        };


        mPlacesList.setAdapter(adapter);

        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }

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

    private void updateEventCount(String placeId, final PlacesVH viewHolder) {
        rootRef.child("Place_Users").child(placeId).child("events").orderByChild("date_comparable").startAt(GetCurTime.today()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int count = (int) dataSnapshot.getChildrenCount();
                    if (count == 1) {
                        viewHolder.evc.setText(String.valueOf(count).concat(" ongoing plot"));
                        viewHolder.evc.setTypeface(viewHolder.evc.getTypeface(), Typeface.BOLD);
                    } else if (count > 1) {

                        viewHolder.evc.setText(String.valueOf(count).concat(" ongoing plots"));
                        viewHolder.evc.setTypeface(viewHolder.evc.getTypeface(), Typeface.BOLD);
                    }
                } else {
                    viewHolder.evc.setText("No ongoing plots at the moment");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void checkifUserFollowed(final String placeId, final PlacesVH viewHolder) {

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getCurrentUser().getUid()).child("subscribedTo");
        reference.keepSynced(true);

        reference.child(placeId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    viewHolder.followbtn.setImageResource(R.drawable.ic_star);
//        viewHolder.follow_btn.setText("Following");
//        viewHolder.follow_btn.setBackgroundResource(R.drawable.secondarycolors);
                    Log.d(TAG, "onDataChange:  usersubscribed");
                    //  viewHolder.followbtn.setImageResource(R.drawable.ic_follow);
                    viewHolder.followbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setMessage("This will remove this place from your favorites list").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    return;
                                }
                            }).setPositiveButton("okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    removeFromFavourites(reference, placeId, viewHolder);
                                }
                            }).show();

                        }
                    });

                } else {
                    Log.d(TAG, "onDataChange: Not subscribed");
//        viewHolder.follow_btn.setText("Follow");
                    viewHolder.followbtn.setImageResource(R.drawable.ic_unstar);
                    //viewHolder.follow_btn.setBackgroundResource(R.drawable.primarycolor_borders);
                    viewHolder.followbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addToFavourites(placeId, viewHolder);
                        }
                    });

                }
                //       progressBar.setVisibility(View.GONE);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addToFavourites(final String placeId, final PlacesVH viewHolder) {
//        viewHolder.follow_btn.setText("Following");
        viewHolder.followbtn.setImageResource(R.drawable.ic_star);
        // Toast.makeText(getContext(), "Following place", Toast.LENGTH_SHORT).show();
        // viewHolder.follow_btn.setBackgroundResource(R.drawable.secondarycolors);
        viewHolder.followbtn.setEnabled(false);
        //--------------GET KEY FIRST-----------------------
        GetCurTime t = new GetCurTime();
        String currentDate = GetCurTime.getCurTime();
        rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference newNotificationRef = rootRef.child("placeNotications").child(placeId).push();
        String notificationKey = newNotificationRef.getKey();
        HashMap<String, String> notificationData = new HashMap<>();
        notificationData.put("who", mAuth.getCurrentUser().getUid());
        notificationData.put("type", "favAddition");
        notificationData.put("at", currentDate);

        Map<String, String> placedata = new HashMap<String, String>();
        placedata.put("uid", mAuth.getCurrentUser().getUid());
        placedata.put("since", currentDate);

        Map<String, Object> subcriptionMap = new HashMap<>();

        subcriptionMap.put("placeSubscribers/" + placeId + "/" + mAuth.getCurrentUser().getUid() + "/date", currentDate);
        subcriptionMap.put("Users/" + mAuth.getCurrentUser().getUid() + "/subscribedTo/" + placeId + "/", placedata);


        subcriptionMap.put("placeNotifications/" + placeId + "/" + notificationKey, notificationData);
        rootRef.updateChildren(subcriptionMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(final DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {

                    com.google.firebase.messaging.FirebaseMessaging.getInstance()
                            .subscribeToTopic(placeId);
                    Toast.makeText(mContext, "Added to your favourites Place List", Toast.LENGTH_SHORT).show();
//        viewHolder.follow_btn.setText("UnFollow");

                    viewHolder.followbtn.setImageResource(R.drawable.ic_star);
                    viewHolder.followbtn.setEnabled(true);

                }
            }
        });
    }

    private void removeFromFavourites(DatabaseReference reference, final String placeId, final PlacesVH viewHolder) {
//        viewHolder.follow_btn.setText("UnFollowing");
        reference.child(placeId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//        viewHolder.follow_btn.setText("Follow");
//        viewHolder.follow_btn.setBackgroundResource(R.drawable.primarycolor_borders);

                viewHolder.followbtn.setImageResource(R.drawable.ic_unstar);
                //checkifUserFollowed(placeId, viewHolder);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                Toast.makeText(mContext, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        viewHolder.follow_btn.setText("Following");
//        viewHolder.follow_btn.setBackgroundResource(R.drawable.secondarycolors);

                viewHolder.followbtn.setImageResource(R.drawable.ic_star);
                // checkifUserFollowed(placeId, viewHolder);
            }
        });
    }

    public void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        try {
            ActivityCompat.requestPermissions(null,
                    permissions,
                    VERIFY_PERMISSIONS_REQUEST
            );
        } catch (Exception e) {
            Toast.makeText(mContext, "Something is not working good,please enable \npermissions for getaplot to save images", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "verifyPermissions: error" + e.getMessage());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)

    {
        getMenuInflater().inflate(R.menu.placesmnenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.app:
                startActivity(new Intent(mContext, AllSettingsActivity.class));
                finish();
                break;
            case R.id.searchplaces:
                startActivity(new Intent(mContext, SearchPlacesActivity.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //---------------------------SETUP NAVIGATION VIEW------------------------------/


    public static class PlacesVH extends RecyclerView.ViewHolder {
        View mView;
        ImageButton addbtn, rmbtn;
        ImageView placeImage;
        Button follow_btn;
        ImageView followbtn;
        TextView evc;

        public PlacesVH(View itemView) {
            super(itemView);
            mView = itemView;
//        follow_btn = mView.findViewById(R.id.followbtn);
            followbtn = mView.findViewById(R.id.followbtn);
            evc = mView.findViewById(R.id.evc);
        }

        public void setPlaceName(String name) {
            TextView nameTextView = mView.findViewById(R.id.place_nametext);
            nameTextView.setText(name);
        }

        public void setCategory(String name) {
            TextView catText = mView.findViewById(R.id.category);
            catText.setText(name);
        }

        public void setImage(String imageUrl, Context context) {
            placeImage = mView.findViewById(R.id.placeImageView);
            try {
                RequestOptions requestOptions = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ptest);
                Glide.with(context)
                        .load(imageUrl).apply(requestOptions)
                        .into(placeImage);
                //


            } catch (Exception e) {
                Log.d(TAG, "onDataChange:Picasso " + e.getMessage());
            }


        }

        public void setPlaceDescription(String desc) {
            TextView textView = mView.findViewById(R.id.desc);
            textView.setText(desc);
        }

        public void setPlaceDistrict(String dist) {
            TextView textView = mView.findViewById(R.id.pdist);
            textView.setText(dist);
        }


    }
}




