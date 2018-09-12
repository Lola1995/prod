package com.getaplot.getaplot.fragments.requests;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Request;
import com.getaplot.getaplot.ui.ChatActivity;
import com.getaplot.getaplot.ui.EnlargeImageView;
import com.getaplot.getaplot.ui.ProfileActivity;
import com.getaplot.getaplot.utils.Handy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecievedRequestsFragment extends Fragment {
    private static final String TAG = "RecievedRequests";
    private DatabaseReference databaseReference, rootRef, Users;
    private FirebaseAuth mAuth;
    private Query query;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
FirebaseRecyclerAdapter adapter;
public String friendsname;

    public RecievedRequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_recieved_requests, container, false);
        mAuth = FirebaseAuth.getInstance();
        progressBar = v.findViewById(R.id.pe);
        rootRef = FirebaseDatabase.getInstance().getReference();
        databaseReference = rootRef.child("RecievedRequests").child(mAuth.getCurrentUser().getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    try {
                        LinearLayout friendtest = v.findViewById(R.id.inquition);
                        friendtest.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    } catch (NullPointerException ex) {
                        Log.d(TAG, "onDataChange: Thats a null pointer " + ex.getLocalizedMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        databaseReference.keepSynced(true);

        recyclerView = v.findViewById(R.id.reqlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery( databaseReference.orderByChild("fitnessNum"), Request.class)
                        .build();
         adapter = new FirebaseRecyclerAdapter<Request, SentsViewHolder>(options) {
            @Override
            public SentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_request, parent, false);

                return new SentsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final SentsViewHolder viewHolder, int position, final Request model) {
                final String key = getRef(position).getKey();
                Users = rootRef.child("Users").child(key);
                Users.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                         String image = null;
                        try {

                            String usersName = dataSnapshot.child("name").getValue().toString();
                            viewHolder.setUserName(Handy.getTrimmedName(usersName));
                            friendsname=Handy.getTrimmedName(usersName);
                             image = dataSnapshot.child("image").getValue().toString();

                            viewHolder.setName(image, getContext());
                        } catch (IllegalArgumentException e) {

                        }catch (NullPointerException e){

                        }


                        viewHolder.setWhen(model.getTime());
                        progressBar.setVisibility(View.GONE);
                        final String finalImage = image;
                        viewHolder.username.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getContext(), EnlargeImageView.class);
                                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                getActivity().getSupportFragmentManager().popBackStack();

                                i.putExtra("image_url", finalImage);
                                startActivity(i);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewHolder.cancel.setText("ACCEPTING REQUEST");
                        viewHolder.cancel.setEnabled(false);

                        DatabaseReference newNotificationref = rootRef.child("RequestAcceptNotifications")
                                .child(mAuth.getCurrentUser().getUid()).push();
                        String newNotificationId = newNotificationref.getKey();

                        HashMap<String, String> notificationData = new HashMap<>();
                        notificationData.put("from", mAuth.getCurrentUser().getUid());
                        notificationData.put("type", "requestaccepptance");
                        notificationData.put("name", mAuth.getCurrentUser().getDisplayName());
                        final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                        Map<String, Object> friendsMap = new HashMap<>();
                        friendsMap.put("Friends/" + mAuth.getCurrentUser().getUid() + "/" + key + "/date", currentDate);
                        friendsMap.put("Friends/" + key + "/" + mAuth.getCurrentUser().getUid() + "/date", currentDate);


                        friendsMap.put("FriendRequests/" + mAuth.getCurrentUser().getUid() + "/" + key, null);
                        friendsMap.put("FriendRequests/" + key + "/" + mAuth.getCurrentUser().getUid(), null);
                        friendsMap.put("RequestAcceptNotifications/" + key + "/" + newNotificationId, notificationData);

                        databaseReference.child(key).setValue(null);
                        rootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                try {
                                    Snackbar.make(view,"You are now friends with "+friendsname,Snackbar.LENGTH_LONG).setAction("Say hello",listener->{
                                        Intent intent=new Intent(getContext(), ChatActivity.class);
                                        intent.putExtra("user_id",key);
                                        startActivity(intent);
                                    }).show();
                                }catch (NullPointerException e){

                                }
                            }
                        });


                    }
                });

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getContext(), ProfileActivity.class);
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        getActivity().getSupportFragmentManager().popBackStack();

                        i.putExtra("user_id", key);
                        startActivity(i);

                    }
                });

            }
        };

        recyclerView.setAdapter(adapter);
        return v;
    }

    @Override
    public void onStart() {
adapter.startListening();

        super.onStart();

    }

    @Override
    public void onStop() {
        adapter.stopListening();
        super.onStop();
    }

    public static class SentsViewHolder extends RecyclerView.ViewHolder {
        public Button cancel;
        View mView;
        CircleImageView username;

        public SentsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            cancel = mView.findViewById(R.id.cancel_button);
            username = mView.findViewById(R.id.attendee_pic);

        }

        public void setName(String picUrl, Context context) {
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.test).error(R.drawable.test);
            Glide.with(context)
                    .load(picUrl).apply(requestOptions)
                    .into(username);
        }


        public void setUserName(String usnae) {
            TextView textView = mView.findViewById(R.id.us_n);
            textView.setText(usnae);
        }

        public void setWhen(String when) {
            TextView whenv = mView.findViewById(R.id.us_times);
            whenv.setText(when);
        }

    }
}
