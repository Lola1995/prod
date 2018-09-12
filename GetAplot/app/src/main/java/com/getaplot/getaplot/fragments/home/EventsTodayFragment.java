package com.getaplot.getaplot.fragments.home;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Event;
import com.getaplot.getaplot.ui.EnlargeImageView;
import com.getaplot.getaplot.ui.EventCommentsActivity;
import com.getaplot.getaplot.ui.PlaceActivity;
import com.getaplot.getaplot.ui.SettingActivity;
import com.getaplot.getaplot.ui.detail.EventDetailActivity;
import com.getaplot.getaplot.ui.users.PickAuserNameActivity;
import com.getaplot.getaplot.utils.GetCurTime;
import com.getaplot.getaplot.utils.Handy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EventsTodayFragment extends Fragment {
    private static final String TAG = "EventsTodayFragment";
    private static final int STORAGE_REQUEST = 1;
    private static String key;
    TextView novents;
    RelativeLayout relativeLayout;
    TextView textViewCount;
    SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mEventsList;
    private DatabaseReference mDatabase, rootRef, users;
    private FirebaseAuth mAuth;
    private LinearLayout noevents, loadevents;
    private FirebaseRecyclerAdapter adapter;
    private RelativeLayout progressBar;
    private Query query;
    private RelativeLayout view;

    public EventsTodayFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View mainView = inflater.inflate(R.layout.fragment_eventstoday, container, false);
        Log.d(TAG, "onCreateView: ");
        progressBar = mainView.findViewById(R.id.pe);
        mEventsList = mainView.findViewById(R.id.todaysEventsList);
        novents = mainView.findViewById(R.id.noeventsyet);
        swipeRefreshLayout = mainView.findViewById(R.id.swipe);
        relativeLayout = mainView.findViewById(R.id.view);
        swipeRefreshLayout.setEnabled(false);
        mEventsList.setHasFixedSize(true);
        users = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("userName")) {

                    Snackbar.make(progressBar, "Your username is not set", Snackbar.LENGTH_INDEFINITE).setAction("Add userName", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(getContext(), PickAuserNameActivity.class);
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

        DatabaseReference versioning = FirebaseDatabase.getInstance().getReference().child("versioning");
        try {
            versioning.orderByChild("version").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        int currentversion = Integer.parseInt(dataSnapshot.child("latest").getValue().toString());

                        if (currentversion > Handy.getVersionCode(getContext())) {
                            Snackbar.make(progressBar, "GetAplot Update is available", Snackbar.LENGTH_INDEFINITE).setAction("GET", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goo.gl/vgE2nk"));
                                    startActivity(i);
                                }
                            }).show();
                        }
                    } catch (NullPointerException e) {

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (NullPointerException e) {

        }

        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    Toast.makeText(getContext(), "We are having trouble connecting to the internet,make sure you have an active internet access", Toast.LENGTH_LONG).show();
                } catch (NullPointerException e) {

                }
            }
        };
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Handy.isNetworkAvailable(getContext())) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (this) {
                                if (Handy.checkActiveInternetConnection(getContext())) {
                                } else {
                                    handler.sendEmptyMessage(0);

                                }
                            }

                        }
                    });
                    thread.start();

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
        // Inflate the layout for this fragment
        return mainView;

    }


    private void setUpFirebase() {
        mAuth = FirebaseAuth.getInstance();
        String currentUser = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd", Locale.ENGLISH);
        String time = simpleDateFormat.format(calendar.getTime());


    }


    @Override
    public void onStart() {
        Query query = null;

        setUpFirebase();
        mDatabase = rootRef.child("Events");
        mEventsList.setLayoutManager(new LinearLayoutManager(getContext()));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String key = preferences.getString("event", "");

        Log.d(TAG, "onStart: from a file we are able to detect use choice to be " + key);
        switch (key) {
            case "Current Location":
                if (!Handy.getDistrict(getContext()).equals("")) {
                    Log.d(TAG, "onStart: loading events from current district" + Handy.getDistrict(getContext()));
  query = FirebaseDatabase.getInstance().getReference().child("localevents").child(Handy.getDistrict(getContext()))
          .orderByChild("date_comparable").startAt
                            (GetCurTime.today());
                } else {
                    query = mDatabase.orderByChild("date_comparable").startAt(GetCurTime.today());
                    Log.d(TAG, "onStart: user chose current location but app fails to get the local will resolve to use all events");
                }
                break;
            case "Rest Of Uganda":
                Log.d(TAG, "onStart: here is rest");
                query = mDatabase.orderByChild("date_comparable").startAt(GetCurTime.today());
                break;
            default:
                Log.d(TAG, "onStart: everythigng else failed will reolve to use the local ");
                query = FirebaseDatabase.getInstance().getReference().child("localevents").child(Handy.getDistrict(getContext()))
                        .orderByChild("date_comparable").startAt
                                (GetCurTime.today());

        }


        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    novents.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(progressBar, "No plots to show at the moment,adjust what plots you can see in settings",
                            Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getContext(), SettingActivity.class);
                            i.putExtra("key", "general");
                            startActivity(i);
                        }
                    }).show();
                    swipeRefreshLayout.setEnabled(true);

                }else{
                    Log.d(TAG, "onDataChange: the current are "+dataSnapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseRecyclerOptions<Event> options =
                new FirebaseRecyclerOptions.Builder<Event>()
                        .setQuery(query, Event.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Event, EventVH>(options) {
            @Override
            public EventVH onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_ev, parent, false);

                return new EventVH(view);
            }

            @Override
            protected void onBindViewHolder(EventVH eventVH, int position, final Event event) {
                final String ev_id = getRef(position).getKey();
                updateCommentCount(ev_id, eventVH);
                eventVH.setEventTitle(Handy.getTrimmedName(event.getName()));
                eventVH.setEventPlace(Handy.getTrimmedName(event.getPlace_name()));
                //TODO DESCRIPTION IS NOT SET
                eventVH.setEventCover(event.getCover(), getContext());
                eventVH.setEventTime(event.getDate(), event.getTime());
                eventVH.setEventDescriptionn(Handy.capitalize(event.getDesc()));
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setEnabled(true);
                final String uid = event.getUid();
                eventVH.ev_place.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), PlaceActivity.class);
                        intent.putExtra("place_id", uid);
                        startActivity(intent);
                    }
                });


                Log.w(TAG, "populateViewHolder: called");

                eventVH.ev_cover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getContext(), EnlargeImageView.class);
                        Pair[] pairs = new Pair[1];
                        pairs[0] = new Pair<View, String>(eventVH.ev_cover, "cpic");
                        i.putExtra("image_url", event.getCover());
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), pairs);
                            startActivity(i, activityOptions.toBundle());

                        } else {
                            startActivity(i);
                        }
                    }
                });
                eventVH.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {

                            Intent i = new Intent(getContext(), EventDetailActivity.class);
                            Pair[] pairs = new Pair[1];
                            pairs[0] = new Pair<View, String>(eventVH.ev_cover, "cpic");
                            i.putExtra("image_url", event.getCover());
                            i.putExtra("event_id", ev_id);
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), pairs);
                                startActivity(i, activityOptions.toBundle());

                            } else {
                                startActivity(i);
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "onClick: " + e.getMessage());
                        }
                    }
                });
                progressBar.setVisibility(View.GONE);
            }
        };


        mEventsList.setAdapter(adapter);
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }

        adapter.startListening();
        super.onStart();
    }


    private void updateCommentCount(final String ev_id, final EventVH eventVH) {
        eventVH.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(getContext(), EventCommentsActivity.class);
                    i.putExtra("event_id", ev_id);
                    startActivity(i);
                } catch (Exception e) {

                }
            }
        });
        rootRef.child("event_comments").child(ev_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    int count = (int) dataSnapshot.getChildrenCount();
                    eventVH.textViewCount.setText(String.valueOf(count));
                } catch (NullPointerException e) {
                    int count = (int) dataSnapshot.getChildrenCount();
                    eventVH.textViewCount.setText(String.valueOf(count));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onStop() {
        adapter.startListening();
        super.onStop();
    }

    public void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        try {
            ActivityCompat.requestPermissions(null,
                    permissions,
                    STORAGE_REQUEST
            );
        } catch (Exception e) {
            Toast.makeText(getContext(), "Something is not working good,please enable \npermissions for getaplot to save images", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "verifyPermissions: error" + e.getMessage());
        }
    }

    /**
     * Check a single permission is it has been verified
     *
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission) {
        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(getContext(), permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: \n Permission was not granted for: " + permission);
            Toast.makeText(getContext(), "Permissions not granted to access the phones storage,\n" +
                    "please give permissions to GetAplot", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
            return true;
        }
    }

    public static class EventVH extends RecyclerView.ViewHolder {
        public View mView;
        public ImageView ev_cover;
        public TextView ev_place;
        TextView ev_title;
        RelativeLayout relativeLayout;
        TextView textViewCount;
        ImageView imageView;

        public EventVH(View itemView) {
            super(itemView);
            mView = itemView;
            relativeLayout = mView.findViewById(R.id.appp);
            textViewCount = mView.findViewById(R.id.count);
            imageView = mView.findViewById(R.id.ch);
            imageView.setImageResource(R.drawable.action_comment_icon);


        }

        public void setEventTitle(String title) {
            ev_title = mView.findViewById(R.id.ev_title);
            ev_title.setText(title);
        }

        public void setEventPlace(String place) {
            ev_place = mView.findViewById(R.id.ev_place);
            try {
                ev_place.setText(place);
            } catch (NullPointerException ex) {
                Log.d(TAG, "setEventPlace: NullPointerException:" + ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        }

        public void setEventTime(String date, String time) {
            TextView ev_time = mView.findViewById(R.id.ev_time);
            ev_time.setText(date + "  " + time);
        }

        public void setEventDescriptionn(String descriptionn) {
            TextView ev_time = mView.findViewById(R.id.desc);
            ev_time.setText(descriptionn);
            Linkify.addLinks(ev_time,Linkify.WEB_URLS);
        }

        public void setEventCover(String cover, Context context) {
            ev_cover = mView.findViewById(R.id.ev_cover);
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.ev_test);
            try {
                Glide.with(context)
                        .load(cover).apply(requestOptions)
                        .thumbnail(0.5f)
                        .into(ev_cover);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "setEventCover: IllegalArgumentException You cannot load from a destroyed activity" + e.getMessage());
            }
        }
    }

}
