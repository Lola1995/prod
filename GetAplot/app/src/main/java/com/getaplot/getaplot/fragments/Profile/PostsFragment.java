package com.getaplot.getaplot.fragments.Profile;


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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Status;
import com.getaplot.getaplot.ui.MyExperienceLikesActivity;
import com.getaplot.getaplot.utils.GetTimeAgo;
import com.getaplot.getaplot.utils.Handy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostsFragment extends Fragment {
    private static final String TAG = "PostsFragment";
    private Boolean hasLiked = false;
    private TextView userName;
    private DatabaseReference statusRef, mRootRef, reference;
    private RecyclerView recyclerView;
    private String user_id;
    private FirebaseAuth mAuth;
    private Context mContext;
    FirebaseRecyclerAdapter adapter;
    private TextView not;

    public PostsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: ");
        View v = inflater.inflate(R.layout.fragment_posts, container, false);
        reference = FirebaseDatabase.getInstance().getReference().child("EXLikes");
        reference.keepSynced(true);
        mContext = getContext();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        recyclerView = v.findViewById(R.id.list_item);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setHasFixedSize(true);
        not = v.findViewById(R.id.friendsnot);
        user_id = getActivity().getIntent().getStringExtra("user_id");
        statusRef = mRootRef.child("userStatus").child(user_id);
        statusRef.keepSynced(true);
        return v;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");
        FirebaseRecyclerOptions<Status> options=new FirebaseRecyclerOptions.Builder<Status>().setQuery(
                statusRef.orderByChild("fitnessNum"),Status.class
        ).build();
       adapter = new FirebaseRecyclerAdapter<Status, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ex_other_listitem, parent, false);

                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final ViewHolder viewHolder, int position, Status model) {
                final String key = getRef(position).getKey();
                Log.d(TAG, "populateViewHolder: ");

                //update the view and initialise the wigets
                final DatabaseReference reference1 = reference.child(key);
                reference1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: ");
                        try {
                            if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                                viewHolder.ex_like.setImageResource(R.drawable.ic_heart_red);
                            } else {
                                viewHolder.ex_like.setImageResource(R.drawable.ic_heart_white);
                            }
                        } catch (NullPointerException e) {

                        }
                        // viewHolder.ex_likes.setText(Integer.parseInt(String.valueOf(dataSnapshot.getChildrenCount()).concat(" Likes")));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                reference1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: ");
                        Log.d(TAG, "Likes: " + dataSnapshot);
                        int count = (int) dataSnapshot.getChildrenCount();
                        Log.d(TAG, "onDataChange: LikesCount:" + count);
                        if (count == 0) {
                            viewHolder.ex_likes.setText("");
                        } else if (count == 1) {
                            viewHolder.ex_likes.setText("1 like");
                            viewHolder.ex_likes.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(mContext, MyExperienceLikesActivity.class);
                                    i.putExtra("exlike_id", key);
                                    startActivity(i);

                                }
                            });
                        } else {
                            viewHolder.ex_likes.setText(String.valueOf(count).concat(" Likes"));
                            viewHolder.ex_likes.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(mContext, MyExperienceLikesActivity.class);
                                    i.putExtra("exlike_id", key);
                                    startActivity(i);

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                viewHolder.ex_like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "onClick: ");

                        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                                    Toast.makeText(mContext, "Loving this", Toast.LENGTH_SHORT).show();
//                    viewHolder.ex_like.setImageResource(R.drawable.ic_heart_red);
                                    viewHolder.ex_like.setImageResource(R.drawable.ic_heart_red);

                                    Log.d(TAG, "onClick: Hadnot Liked,Attempting to like this");
                                    Map<String, Object> likeMap = new HashMap<>();
                                    likeMap.put("user_id", user_id);
                                    likeMap.put("fitnessNum", Handy.fitnessNumber());
                                    likeMap.put("time", System.currentTimeMillis());
                                    reference1.child(mAuth.getCurrentUser().getUid()).updateChildren(likeMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    hasLiked = true;
                                                    viewHolder.ex_like.setImageResource(R.drawable.ic_heart_red);

                                                    Toast.makeText(mContext, "You love this", Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                    hasLiked = true;
                                } else {
                                    viewHolder.ex_like.setImageResource(R.drawable.ic_heart_white);

                                    reference1.child(mAuth.getCurrentUser().getUid()).removeValue().
                                            addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "onSuccess: Unliked good");
                                                    hasLiked = false;
                                                    Toast.makeText(mContext, "You love this no more", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    hasLiked = false;
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                });

//seting values to widgets
                viewHolder.setUX(model.getStatus());
                viewHolder.setUXUpdateTime(model.getUpdated(), mContext);
            }
        };





        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    recyclerView.setAdapter(adapter);
                } else {
                    not.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        adapter.startListening();
        super.onStart();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.stopListening();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView ex_like;
        TextView ex_likes;
        TextView not;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            ex_like = mView.findViewById(R.id.ex_like);
            ex_likes = mView.findViewById(R.id.ex_likes);

        }

        public void setUX(String ux) {
            TextView ux2 = mView.findViewById(R.id.status);
            ux2.setText(ux);
            ex_like = mView.findViewById(R.id.ex_like);
        }

        public void setUXUpdateTime(long time, Context context) {
            TextView textView = mView.findViewById(R.id.updateTime);
            textView.setText(GetTimeAgo.getTimeAgo(time, context));
        }
    }
}