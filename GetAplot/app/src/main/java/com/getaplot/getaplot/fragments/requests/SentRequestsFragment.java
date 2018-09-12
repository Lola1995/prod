package com.getaplot.getaplot.fragments.requests;


import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
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
import com.getaplot.getaplot.ui.EnlargeImageView;
import com.getaplot.getaplot.ui.ProfileActivity;
import com.getaplot.getaplot.utils.Handy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SentRequestsFragment extends Fragment {
    private static final String TAG = "SentRequestsFragment";
    private DatabaseReference databaseReference, rootRef, Users;
    private FirebaseAuth mAuth;
    private Query query;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FirebaseRecyclerAdapter adapter;


    public SentRequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_sent_requests, container, false);
        mAuth = FirebaseAuth.getInstance();
        progressBar = v.findViewById(R.id.pe);
        rootRef = FirebaseDatabase.getInstance().getReference();
        databaseReference = rootRef.child("FriendRequests")
                .child(mAuth.getCurrentUser().getUid());
        databaseReference.orderByChild("request_type").equalTo("sent").addListenerForSingleValueEvent(new ValueEventListener() {
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

        recyclerView = v.findViewById(R.id.reqisent);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        return v;
    }

    @Override
    public void onStart() {
        //todo fixme query=databaseReference.child(model.getUser_id()).orderByChild("request_type").equalTo("sent");

        query = databaseReference.orderByChild("request_type").equalTo("sent");
        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery(query, Request.class)
                        .build();
         adapter = new FirebaseRecyclerAdapter<Request, SentsViewHolder>(options) {
            @Override
            public SentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_sent_request, parent, false);

                return new SentsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final SentsViewHolder viewHolder, int position, final Request model) {
                final String key = getRef(position).getKey();
                Users = rootRef.child("Users").child(key);
                Users.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                     try {
                         final String image = dataSnapshot.child("image").getValue().toString();

                         String usersName = dataSnapshot.child("name").getValue().toString();
                         viewHolder.setUserName(Handy.getTrimmedName(usersName));

                        viewHolder.setName(image, getContext());

                         viewHolder.username.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View view) {
                                 Intent i = new Intent(getContext(), EnlargeImageView.class);
                                 Pair[] pairs = new Pair[1];
                                 pairs[0] = new Pair<View, String>(viewHolder.username, "cpic");
                                 i.putExtra("image_url", image);
                                 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                     ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), pairs);
                                     startActivity(i, activityOptions.toBundle());

                                 }else{
                                     startActivity(i);
                                 }
                             }
                         });
                     }catch (Exception e)

                    {

                    }

                        viewHolder.setWhen(model.getTime());
                        progressBar.setVisibility(View.GONE);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                viewHolder.cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).
                                setMessage("This will cancel the request,action is not reversible").setPositiveButton("okay",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        viewHolder.cancel.setText("CANCELLING REQUEST");
                                        viewHolder.cancel.setEnabled(false);

                                        // - -------------- CANCEL REQUEST STATE ------------
                                        rootRef.child("FriendRequests").child(mAuth.getCurrentUser().getUid()).
                                                child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                rootRef.child("FriendRequests").child(key).child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getContext(), "Request Cancelled", Toast.LENGTH_SHORT).show();
                                                        viewHolder.cancel.setEnabled(true);

                                                    }
                                                });

                                            }
                                        });
                                    }
                                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        });
                        builder.show();


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
                    .placeholder(R.drawable.test);
            try {
                Glide.with(context)
                        .load(picUrl)
                        .thumbnail(0.5f).apply(requestOptions)
                        .into(username);
            } catch (IllegalArgumentException e) {


            }
        }

        public View getButton() {

            return cancel;
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
