package com.getaplot.getaplot.fragments.users;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Friend;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {
    private static final String TAG = "FriendsFragment";
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    FirebaseRecyclerAdapter<Friend, FriendsViewHolder> adapter;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, mUserDatabase;
    private RecyclerView mUserFriendsList;
    private String currentUser;
    private Query query;
    private ProgressBar progressBar;

    public FriendsFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //--------------------------CREATE CUSTOM VIEW --------------------------\
        try {


            final View mainView = inflater.inflate(R.layout.fragmentfriends, container, false);
            progressBar = mainView.findViewById(R.id.progress2);

            //------------------------FIREBASE-----------------------------
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser().getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUser);
            query = mDatabase.orderByChild("online");
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            mDatabase.keepSynced(true);

            mUserFriendsList = mainView.findViewById(R.id.friendList);
            mUserFriendsList.setHasFixedSize(true);
            mUserFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {

                        TextView friendtest = mainView.findViewById(R.id.nofriends);
                        friendtest.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            // Inflate the layout for this fragment
            return mainView;

        } catch (Exception e) {
            Log.d(TAG, "onCreateView: " + e.getMessage());
            return null;
        }
    }


    @Override
    public void onStart() {
        FirebaseRecyclerOptions<Friend> options =
                new FirebaseRecyclerOptions.Builder<Friend>()
                        .setQuery(query, Friend.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Friend, FriendsViewHolder>(options) {
            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.singlefriend, parent, false);

                return new FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final FriendsViewHolder viewHolder, int position, final Friend model) {
                final String userId = getRef(position).getKey();
                mUserDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            final String userName = dataSnapshot.child("userName").getValue().toString();
                            if (!userName.equals("")) {
                                viewHolder.setUserName("@".concat(userName));
                            }
                            Log.d(TAG, "userNames " + userName);
                        } catch (NullPointerException e) {

                        }


                        try {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            Log.d(TAG, "userNames " + userName);
                            final String thumbImage = dataSnapshot.child("image").getValue().toString();
                            String status = dataSnapshot.child("status").getValue().toString();
                            viewHolder.setUserStatus(status);
                            viewHolder.setName(Handy.getTrimmedName(userName));
                            viewHolder.setDate("since ".concat(model.getDate().substring(0, 12)));
                            try {
                                viewHolder.setProfilePcture(thumbImage, getContext());
                            } catch (Exception e) {

                            }
                            progressBar.setVisibility(View.GONE);
                            viewHolder.dp.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(getContext(), EnlargeImageView.class);
                                    i.putExtra("image_url", thumbImage);
                                    startActivity(i);
                                }
                            });


                            if (dataSnapshot.hasChild("online")) {
                                String userOnline = dataSnapshot.child("online").getValue().toString();
                                viewHolder.setUserOnline(userOnline);

                            }

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[] = new CharSequence[]{"View Profile", "Open Chat"};
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Options");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                Intent i = new Intent(getContext(), ProfileActivity.class);
                                                i.putExtra("user_id", userId);
                                                i.putExtra("user_name", userName);
                                                i.putExtra("extra", "extra");
                                                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                getActivity().getSupportFragmentManager().popBackStack();
                                                startActivity(i);
                                            }
                                            if (which == 1) {
                                                Intent i = new Intent(getContext(), ChatActivity.class);
                                                i.putExtra("user_id", userId);
                                                i.putExtra("user_name", userName);
                                                Log.d(TAG, "onsend " + userName);
                                                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                getActivity().getSupportFragmentManager().popBackStack();
                                                //Toast.makeText(getContext(), userName, Toast.LENGTH_SHORT).show();
                                                startActivity(i);
                                            }

                                        }
                                    });
                                    builder.show();
                                }
                            });
                        } catch (NullPointerException e) {
                            Log.d(TAG, "onDataChange: " + e.getMessage());
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: Cancelled");

                    }
                });


            }
        };
        mUserFriendsList.setAdapter(adapter);
adapter.startListening();
        super.onStart();

    }

    @Override
    public void onStop() {
        adapter.stopListening();
        super.onStop();
    }

    public void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        try {
            ActivityCompat.requestPermissions(null,
                    permissions,
                    VERIFY_PERMISSIONS_REQUEST
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

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public CircleImageView dp;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView nameTextView = mView.findViewById(R.id.us_name);
            nameTextView.setText(name);
        }

        public void setUserName(String name) {
            TextView nameTextView = mView.findViewById(R.id.userName);
            nameTextView.setText(name);
        }

        public void setDate(String date) {
            TextView dateTextView = mView.findViewById(R.id.us_status);
            dateTextView.setText(date);

        }

        public void setProfilePcture(String profilePic, Context ctx) {
            dp = mView.findViewById(R.id.attendee_pic);
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.test);
            try {
                Glide.with(ctx)
                        .load(profilePic).apply(requestOptions)
                        .into(dp);
            } catch (IllegalArgumentException e) {

            }

        }

        public void setUserOnline(String online_status) {
            ImageView userOnline = mView.findViewById(R.id.online_status);
            if (online_status.equals("true")) {
                userOnline.setVisibility(View.VISIBLE);

            } else {
                userOnline.setVisibility(View.INVISIBLE);

            }

        }

        public void setUserStatus(String status) {
            TextView textView = mView.findViewById(R.id.usersstatus);
            textView.setText(status);
        }
    }
}
