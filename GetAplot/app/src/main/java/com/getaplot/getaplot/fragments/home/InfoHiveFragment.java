package com.getaplot.getaplot.fragments.home;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.adapters.FeedAdapter;
import com.getaplot.getaplot.model.Heart;
import com.getaplot.getaplot.model.InfoHive;
import com.getaplot.getaplot.utils.GetShortTimeAgo;
import com.getaplot.getaplot.utils.Handy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfoHiveFragment extends Fragment {
    private static final String TAG = "InfoHiveFragment";
    private static final int STORAGE_REQUEST = 1;
    TextView textView;
    SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mEventsList;
    private DatabaseReference mDatabase, subscribeDb, rootRef, InfoReactionsDb;
    private FirebaseAuth mAuth;
    private Query query;
    private ProgressBar progressBar;
    private RelativeLayout view;
    private CircleImageView posterpic;
    private GestureDetector mGestureDetector;
    private ImageView mHeartRed, mHeartWhite;
    private FeedAdapter adapter;
    private String sortby;
    private List<InfoHive> feedList=new ArrayList<>();

    public InfoHiveFragment() {


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View mainView = inflater.inflate(R.layout.fragment_info_hive, container, false);
        Log.d(TAG, "onCreateView: ");
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        mDatabase = rootRef.child("placeInfoPosts");
        posterpic = mainView.findViewById(R.id.posterpic);
        textView = mainView.findViewById(R.id.not);
        swipeRefreshLayout = mainView.findViewById(R.id.swipe);
        swipeRefreshLayout.setEnabled(false);
        view = mainView.findViewById(R.id.view);
        InfoReactionsDb = rootRef.child("infoHiveComments");
        mEventsList = mainView.findViewById(R.id.infolist);
        mEventsList.setHasFixedSize(true);
        progressBar = mainView.findViewById(R.id.loadfav);
        progressBar.setVisibility(View.VISIBLE);
        mEventsList.setLayoutManager(new LinearLayoutManager(getContext()));


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
swipeRefreshLayout.setEnabled(false);
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
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setEnabled(true);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
mDatabase.orderByChild("fitnessNum").addChildEventListener(new ChildEventListener() {
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        feedList.add(dataSnapshot.getValue(InfoHive.class));
        progressBar.setVisibility(View.GONE);
        mEventsList.setAdapter(adapter);

        swipeRefreshLayout.setEnabled(true);
        if (swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
        }

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
adapter.notifyDataSetChanged();
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
});

        adapter=new FeedAdapter(getContext(),getActivity(),feedList);


        return mainView;

    }

    @Override
    public void onStart() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setEnabled(true);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        }
        super.onStart();


    }
//
//    @Override
//    public void onStop() {
//        adapter.stopListening();
//        super.onStop();
//    }

    public static class InfoHiveVH extends RecyclerView.ViewHolder {
        View mView;
        TextView text, likecount;
        TextView ev_place;
        ImageView image;
        TextView hivereplycount;
        TextInputEditText reply;
        CircleImageView posterpic;
        ImageView mm, mHeartWhite;
        private ImageView ev_cover;
        private Heart mHeart;
        private GestureDetector mGestureDetector;

        public InfoHiveVH(View itemView) {
            super(itemView);
            mView = itemView;
            hivereplycount = mView.findViewById(R.id.hivereplycount);
            image = mView.findViewById(R.id.imagetarget);
            posterpic = mView.findViewById(R.id.posterpic);
            mHeartWhite = mView.findViewById(R.id.image_heart);
            mm = mView.findViewById(R.id.imm);
            likecount = mView.findViewById(R.id.likecount);
        }

        public void setEventDescription(String title) {
            text = mView.findViewById(R.id.text);
            text.setText(Handy.capitalize(title));
        }

        public void setHivePlace(String place) {
            ev_place = mView.findViewById(R.id.ev_place);
            ev_place.setText(place);
        }

        public void setHiveTime(long time, Context context) {
            TextView ev_time = mView.findViewById(R.id.hivetime);
            ev_time.setText(GetShortTimeAgo.getTimeAgo(time, context));
        }

        public void setEventCover(String cover, Context context) {

            try {
                RequestOptions requestOptions = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_feed);
                Glide.with(context)
                        .load(cover).apply(requestOptions)
                        .thumbnail(0.5f)
                        .into(image);
            } catch (Exception e) {
                Log.d(TAG, "setEventCover: outof memory");
            }
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        String key;

        public GestureListener(String key) {
            key = key;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected.");

            Heart mHeart = new Heart(mHeartWhite, mHeartRed);

            mHeart.toggleLike();

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }


    }



}

