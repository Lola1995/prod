package com.crycetruly.r2d2.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.crycetruly.r2d2.R;
import com.crycetruly.r2d2.model.Event;
import com.crycetruly.r2d2.utils.Handy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaceEventsFragment extends Fragment {
    private static final String TAG = "PlaceEventsFragment";
    TextView novents;
    String place_id;
    FirebaseRecyclerAdapter<Event, PlaceEventVH> adapter;
    private RecyclerView mEventsList;
    private DatabaseReference eventsDatabase;
    private LinearLayout noevents, loadevents;
    private Query query;
    private Context mContext;
    private DatabaseReference rootRef, mDatabase;
    private ProgressBar progressBar;
    private TextView noeventsyet;

    public PlaceEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_place_events, container, false);
        mContext = getContext();
        mEventsList = v.findViewById(R.id.placeevents);
        noeventsyet = v.findViewById(R.id.none);
        mEventsList.setHasFixedSize(true);
        Log.d(TAG, "onCreateView: Placeevent fragment has been crted-----------------");
        mEventsList.setLayoutManager(new LinearLayoutManager(mContext));

        progressBar = v.findViewById(R.id.pe);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd", Locale.ENGLISH);
        String time = simpleDateFormat.format(calendar.getTime());
        rootRef = FirebaseDatabase.getInstance().getReference();
        place_id = getActivity().getIntent().getStringExtra("place_id");
        try {
            mDatabase = rootRef.child("Place_Users").child(place_id).child("events");
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        progressBar.setVisibility(View.GONE);
                        noeventsyet.setVisibility(View.VISIBLE);
                    } else {

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (NullPointerException e) {

        }

        Log.d(TAG, "onCreateView: The place_id is " + place_id);
        try {
            query = mDatabase.orderByChild("fitnessNum");
            query.keepSynced(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        FirebaseRecyclerOptions<Event> options =
                new FirebaseRecyclerOptions.Builder<Event>()
                        .setQuery(query, Event.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Event, PlaceEventVH>(options) {
            @Override
            public PlaceEventVH onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_ev, parent, false);

                return new PlaceEventVH(view);
            }

            @Override
            protected void onBindViewHolder(PlaceEventVH eventVH, int i, Event event) {

                progressBar.setVisibility(View.VISIBLE);

                final String ev_id = getRef(i).getKey();
                eventVH.setEventTitle(Handy.getTrimmedName(event.getName()));
                eventVH.setEventPlace(Handy.getTrimmedName(event.getPlace_name()));
                //TODO DESCRIPTION IS NOT SET
                eventVH.setEventCover(event.getCover(), mContext);
                eventVH.setEventTime(event.getDate(), event.getTime());
                eventVH.setEventDescriptionn(Handy.capitalize(event.getDesc()));
                progressBar.setVisibility(View.GONE);
                final String uid = event.getUid();
                Log.w(TAG, "populateViewHolder: called");


//                eventVH.mView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent i = new Intent(mContext, EventDetailActivity.class);
//                        getActivity().overridePendingTransition(R.anim.fui_slide_out_left, R.anim.fui_slide_in_right);
//                        i.putExtra("event_id", ev_id);
//                        i.putExtra("calling", "calling");
//                        i.putExtra("place_id", place_id);
//                        getActivity().getSupportFragmentManager().popBackStack();
//
//                        startActivity(i);
//                    }
//                });
            }
        };
        mEventsList.setAdapter(adapter);
        return v;

    }

    @Override
    public void onStart() {
        adapter.startListening();
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            adapter.startListening();
        } catch (NullPointerException e) {

        }
    }

    public static class PlaceEventVH extends RecyclerView.ViewHolder {
        public View mView;
        public ImageView ev_cover;
        public TextView ev_place;
        TextView ev_title;

        public PlaceEventVH(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setEventTitle(String title) {
            ev_title = mView.findViewById(R.id.ev_title);
            ev_title.setText(title);
        }

        public void setEventPlace(String place) {
            ev_place = mView.findViewById(R.id.ev_place);
            ev_place.setText(place);
            ev_place.setVisibility(View.GONE);
        }

        public void setEventTime(String date, String time) {
            TextView ev_time = mView.findViewById(R.id.ev_time);
            ev_time.setText(date + "  " + time);
        }

        public void setEventDescriptionn(String descriptionn) {
            TextView ev_time = mView.findViewById(R.id.desc);
            ev_time.setText(descriptionn);
        }

        public void setEventCover(String cover, Context context) {
            ev_cover = mView.findViewById(R.id.ev_cover);
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.ev_test);
            Glide.with(context)
                    .load(cover).apply(requestOptions)
                    .thumbnail(0.5f)
                    .into(ev_cover);
        }
    }

}
