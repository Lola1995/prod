package com.getaplot.getaplot.fragments.users;


import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Point;
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

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class TopUsersFragment extends Fragment {
    private static final String TAG = "TopUsersFragment";
    RecyclerView recyclerView;
    DatabaseReference databaseReference, friendRef;
    Query query;
    DatabaseReference users;
    boolean isBuddy;
    FirebaseRecyclerAdapter  adapter;
    private Context mContext;
    private ProgressBar progressBar;

    public TopUsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_top_users, container, false);
        recyclerView = view.findViewById(R.id.top_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar = view.findViewById(R.id.loadtop);
        recyclerView.setHasFixedSize(true);
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(FirebaseAuth.getInstance()

                .getCurrentUser().getUid());
        mContext = getContext();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("userPoints");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    TextView textView = view.findViewById(R.id.none);
                    textView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        users = FirebaseDatabase.getInstance().getReference().child("Users");
        users.keepSynced(true);
        databaseReference.keepSynced(true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String count = sharedPreferences.getString("topusercount", "30");

        Log.d(TAG, "onCreateView: count" + count);
        query = databaseReference.orderByChild("fitnessPoint").limitToFirst(50);
        FirebaseRecyclerOptions<Point> options =
                new FirebaseRecyclerOptions.Builder<Point>()
                        .setQuery(query, Point.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Point, TrulysPointsVH>(options) {
            @Override
            public TrulysPointsVH onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.singletopuser, parent, false);

                return new TrulysPointsVH(view);
            }

            @Override
            protected void onBindViewHolder(final TrulysPointsVH viewHolder, int position, Point model) {
                final String key = getRef(position).getKey();

                viewHolder.points.setText(String.format(Locale.ENGLISH, "%.00f", model.getPoints()).concat("points"));


                users.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        try {
                            final String userName = dataSnapshot.child("userName").getValue().toString();
                            if (!userName.equals("")) {
                                viewHolder.setUserName("@".concat(userName));
                            }
                            Log.d(TAG, "userNames " + userName);
                        } catch (NullPointerException e) {

                        }
                        try {
                            viewHolder.us_name.setText(Handy.getTrimmedName(dataSnapshot.child("name").getValue().toString()));
                        } catch (NullPointerException e) {

                        }
                        try {
                            viewHolder.status.setText(dataSnapshot.child("status").getValue().toString());
                        } catch (NullPointerException e) {

                        }
                        try {
                            viewHolder.imageView.setOnClickListener(view1 -> {
                                Intent i = new Intent(getContext(), EnlargeImageView.class);
                                Pair[] pairs = new Pair[1];
                                pairs[0] = new Pair<View, String>( viewHolder.imageView, "cpic");
                                i.putExtra("image_url", dataSnapshot.child("image").getValue(String.class));
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), pairs);
                                    startActivity(i, activityOptions.toBundle());

                                }else{
                                    startActivity(i);
                                }
                            });
                        } catch (NullPointerException e) {
                            Log.d(TAG, "onDataChange: error"+e.getMessage());
                        }
                        try {
                            RequestOptions requestOptions = new RequestOptions()
                                    .centerCrop()
                                    .placeholder(R.drawable.test);
                            Glide.with(getContext()).load(dataSnapshot.child("image").getValue().toString()).apply(requestOptions).into(viewHolder.imageView);
                        } catch (IllegalArgumentException e) {
                            Log.d(TAG, "onDataChange: You cannot start a load on a null Context");
                        } catch (NullPointerException e) {

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "populateViewHolder: " + key + " at position " + position);

                Log.d(TAG, "populateViewHolder: " + model.getPoints());
                if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(key)) {
                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            friendRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(key)) {
                                        Log.d(TAG, "onClick: friends");
                                        CharSequence options[] = new CharSequence[]{"Open Profile", "Open Chat"};
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (which == 0) {
                                                    try {
                                                        Intent i = new Intent(mContext, ProfileActivity.class);

                                                        i.putExtra("user_id", key);
                                                        startActivity(i);
                                                    } catch (NullPointerException e) {

                                                    }
                                                }
                                                if (which == 1) {
                                                    try {
                                                        Intent i = new Intent(mContext, ChatActivity.class);

                                                        i.putExtra("user_id", key);
                                                        i.putExtra("user_name", "User");
                                                        startActivity(i);

                                                    } catch (NullPointerException e) {

                                                    }
                                                }

                                            }
                                        });
try {
    builder.show();
}catch (Exception r){

}
                                    } else {
                                        try {
                                            Log.d(TAG, "onClick: Navigating to profiles of user");
                                            Intent i = new Intent(mContext, ProfileActivity.class);
                                            i.putExtra("user_id", key);
                                            startActivity(i);

                                        } catch (Exception e) {
                                            Log.d(TAG, "onClick: " + e.getMessage());
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

            }
        };
recyclerView.setAdapter(adapter);
        return view;
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

    public static class TrulysPointsVH extends RecyclerView.ViewHolder {
        View mView;
        TextView us_name, status, points;
        ImageView imageView;

        public TrulysPointsVH(View itemView) {
            super(itemView);
            mView = itemView;
            us_name = mView.findViewById(R.id.name);
            status = mView.findViewById(R.id.message);
            imageView = mView.findViewById(R.id.userpic);
            points = mView.findViewById(R.id.lasttime);
        }

        public void setDetails(Context ctx, String status, String name, String image) {
            TextView us_name = mView.findViewById(R.id.us_name);
            us_name.setText(name);
            CircleImageView us_image = mView.findViewById(R.id.attendee_pic);


            TextView us_status = mView.findViewById(R.id.us_status);
            us_status.setText(status);

        }

        public void setUserName(String name) {
            TextView nameTextView = mView.findViewById(R.id.userName);
            nameTextView.setText(name);
        }


    }

}
