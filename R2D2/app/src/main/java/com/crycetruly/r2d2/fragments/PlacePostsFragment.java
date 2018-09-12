package com.crycetruly.r2d2.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crycetruly.r2d2.R;
import com.crycetruly.r2d2.model.InfoHive;
import com.crycetruly.r2d2.utils.GetTimeAgo;
import com.crycetruly.r2d2.utils.Handy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlacePostsFragment extends Fragment {
    private static final String TAG = "InfoHiveFragment2";
    private static final int STORAGE_REQUEST = 1;
    private RecyclerView mEventsList;
    private DatabaseReference mDatabase, subscribeDb, rootRef, InfoReactionsDb;
    private FirebaseAuth mAuth;
    private Query query;
    private ProgressBar progressBar;

    private FirebaseRecyclerAdapter<InfoHive, InfoHiveVH> adapter;
    private String sortby;
    private TextView noInfoTextView;
    private String place_id;

    public PlacePostsFragment() {


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            }
        };


        mAuth = FirebaseAuth.getInstance();
        place_id = getActivity().getIntent().getStringExtra("place_id");


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View mainView = inflater.inflate(R.layout.fragment_place_posts, container, false);
        Log.d(TAG, "onCreateView: ");
        progressBar = mainView.findViewById(R.id.pe);
        noInfoTextView = mainView.findViewById(R.id.infonow);


        setUpFirebase();

        mEventsList = mainView.findViewById(R.id.event_list);
        mEventsList.setHasFixedSize(true);

        mEventsList.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseRecyclerOptions<InfoHive> options =
                new FirebaseRecyclerOptions.Builder<InfoHive>()
                        .setQuery( mDatabase.orderByChild("fitnessNum"), InfoHive.class)
                        .build();


adapter=new FirebaseRecyclerAdapter<InfoHive, InfoHiveVH>(options) {
    @Override
    protected void onBindViewHolder(@NonNull final InfoHiveVH viewHolder, int position, @NonNull InfoHive model) {
        Log.d(TAG, "populateViewHolder: " + model);
        final String key = getRef(position).getKey();
        try {
            viewHolder.setEventDescription(model.getPost_text());
            viewHolder.setHiveTime(model.getPosted(),getContext());
            viewHolder.setEventCover(model.getImage(), getContext());
            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {

        }

        //---------------------SHOULD UPDATE TEXTvIEW--------------------//
        InfoReactionsDb.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                Log.d(TAG, "onDataChange: the count is " + count);

                if (count != 1) {
                    viewHolder.hivereplycount.setText(String.valueOf(count).concat(" Reactions"));
                    viewHolder.hivereplycount.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            Intent intent = new Intent(getContext(), HiveReactionsActivity.class);
//                            intent.putExtra("hivereplykey", key);
//                            startActivity(intent);
                        }
                    });
                }
                if (count == 1) {
                    viewHolder.hivereplycount.setText(String.valueOf(count).concat(" Reaction"));
                    viewHolder.hivereplycount.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            Intent intent = new Intent(getContext(), HiveReactionsActivity.class);
//                            intent.putExtra("hivereplykey", key);
//                            startActivity(intent);
                        }
                    });
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public InfoHiveVH onCreateViewHolder(ViewGroup parent, int viewType) {
      View v=  LayoutInflater.from(parent.getContext()).inflate(R.layout.single_place_posts,parent,false);
        return new InfoHiveVH(v);
    }
};

        mEventsList.setAdapter(adapter);

        // Inflate the layo*//*ut for this fragment*/
        return mainView;

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void setUpFirebase() {
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        Calendar calendar = Calendar.getInstance();
        Log.d(TAG, "setUpFirebase: The Place id is " + place_id);
        mDatabase = rootRef.child("Place_Users").child(place_id).child("posts");
        InfoReactionsDb = rootRef.child("infoHiveComments");

        if (mDatabase != null) {

            mDatabase.keepSynced(true);
        }
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    noInfoTextView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                } else {
                    Log.d(TAG, "onDataChange: Exisists" + dataSnapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        try {
            onStart();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    public static class InfoHiveVH extends RecyclerView.ViewHolder {
        View mView;
        TextView text;
        TextView ev_place;
        ImageView image;
        TextView hivereplycount;
        TextInputEditText reply;
        private ImageView ev_cover;

        public InfoHiveVH(View itemView) {
            super(itemView);
            mView = itemView;
            hivereplycount = mView.findViewById(R.id.hivereplycount);
            image = mView.findViewById(R.id.imagetarget);
        }

        public void setEventDescription(String title) {
            text = mView.findViewById(R.id.text);
            text.setText(Handy.capitalize(title));

        }


        public void setHiveTime(long time,Context context) {
            TextView ev_time = mView.findViewById(R.id.hivetime);
            ev_time.setText("POSTED "+ GetTimeAgo.getTimeAgo(time,context));
        }

        public void setEventCover(String cover, Context context) {
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.test);
            try {
                Glide.with(context).load(cover).apply(requestOptions).into(image);
            } catch (Exception e) {

            }
        }
    }
}
