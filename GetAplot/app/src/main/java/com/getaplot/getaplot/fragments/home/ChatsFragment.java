package com.getaplot.getaplot.fragments.home;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Chat;
import com.getaplot.getaplot.ui.ChatActivity;
import com.getaplot.getaplot.utils.GetShortTimeAgo;
import com.getaplot.getaplot.utils.Handy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    private static final String TAG = "ChatsFragment";
    FirebaseRecyclerAdapter adapter;
    TextView nomsgs;
    private Context mContext;
    private RecyclerView mUserFriendsList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, mUserDatabase, ChatsDb;
    private String currentUser;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    GestureDetector gestureDetector;
    public ChatsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_chats, container, false);
        swipeRefreshLayout = v.findViewById(R.id.swipe);
        swipeRefreshLayout.setEnabled(false);
        mContext = getContext();

        progressBar = v.findViewById(R.id.progress);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Chat")
                .child(currentUser);
        mDatabase.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Handy.isNetworkAvailable(getContext())) {
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
        //------------------------------RECYCLERVIEW    ------------------------//
        mUserFriendsList = v.findViewById(R.id.friendList);
        mUserFriendsList.setHasFixedSize(true);
        nomsgs = v.findViewById(R.id.themessos);
        mUserFriendsList.setLayoutManager(new LinearLayoutManager(mContext));
        return v;
    }

    @Override
    public void onStart() {
        try {
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Log.d(TAG, "chats" + dataSnapshot);
                    if (!dataSnapshot.exists()) {
                        try {
                            progressBar.setVisibility(View.GONE);
                            nomsgs.setVisibility(View.VISIBLE);
                            swipeRefreshLayout.setEnabled(true);

                        } catch (NullPointerException e) {
                            swipeRefreshLayout.setRefreshing(false);
                            progressBar.setVisibility(View.GONE);
                            nomsgs.setVisibility(View.INVISIBLE);

                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (NullPointerException e) {

        }
        FirebaseRecyclerOptions<Chat> options =
                new FirebaseRecyclerOptions.Builder<Chat>()
                        .setQuery(mDatabase.orderByChild("fitnessNum"), Chat.class)
                        .build();
         adapter = new FirebaseRecyclerAdapter<Chat, TrulysChatVH>(options) {
            @Override
            public TrulysChatVH onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate( R.layout.single_chat, parent, false);

                return new TrulysChatVH(view,mContext);
            }

            @Override
            protected void onBindViewHolder(final TrulysChatVH viewHolder, int position, Chat model) {
                final String userId = getRef(position).getKey();
                try {
                    if (model.getType().equals("image")) {
                        viewHolder.setMessage("photo");
                    } else {
                        viewHolder.setMessage(model.getMessage());
                    }
                    long time = model.getTimestamp();
                    String times = GetShortTimeAgo.getTimeAgo(time, mContext);
                    viewHolder.setSentDate(times);



viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
    @Override
    public boolean onLongClick(View view) {
        showOptionsMenuForChat(userId);
        return true;
    }
});
                } catch (Exception e) {

                    //----------------GET MESSAGE NOW
                    ChatsDb = FirebaseDatabase.getInstance().getReference().child("Messages")
                            .child(mAuth.getCurrentUser().getUid())
                            .child(userId);
                    ChatsDb.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "test " + dataSnapshot);
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                String messageText = dataSnapshot1.child("message").getValue(String.class);
                                if (messageText.contains("googleapis")) {
                                    viewHolder.setMessage("image");
                                } else {
                                    viewHolder.setMessage(messageText);
                                }
//---------------------AVOIDING SIDE EFFECTS OFUNBOXING------------------------------------//
                                try {

                                    long time = dataSnapshot1.child("time").getValue(Long.class);
                                    String times = GetShortTimeAgo.getTimeAgo(time, mContext);
                                    viewHolder.setSentDate(times);
                                } catch (NullPointerException e) {

                                }


                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                mUserDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        try {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            viewHolder.setName(Handy.getTrimmedName(userName));

                        }catch (NullPointerException e) {

                        }
                            try {
                                RequestOptions requestOptions = new RequestOptions()
                                        .centerCrop()
                                        .placeholder(R.drawable.test);
                                String thumbImage = dataSnapshot.child("image").getValue().toString();
                                viewHolder.setImage(thumbImage, mContext);

                            }catch (Exception x){

                            }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: Cancelled");

                    }
                });

                //----------------------NAVIGATING TO USER PROFILE ACTIVITY------------------------//
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(mContext, ChatActivity.class);
                        getActivity().getSupportFragmentManager().popBackStack();
                        i.putExtra("user_id", userId);
                        startActivity(i);

                    }
                });

                swipeRefreshLayout.setEnabled(true);

                progressBar.setVisibility(View.GONE);

            }
        };


        mUserFriendsList.setAdapter(adapter);
        adapter.startListening();
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        super.onStart();
    }

    private void showOptionsMenuForChat(String from) {
        Log.d(TAG, "showOptionsMenuForChat: "+from);
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        CharSequence [] sequence={"Delete Chat","Delete Conversation"};

        builder.setItems(sequence, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i==0){
                    FirebaseDatabase.getInstance().getReference().child("Chat").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(from).removeValue();

                }else {
                    Snackbar.make(mUserFriendsList,"This will also clear messages",Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FirebaseDatabase.getInstance().getReference().child("Chat").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(from).removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(from).removeValue();

                        }
                    }).show();

                }
            }
        }).show();




    }


    @Override
    public void onStop() {
        adapter.stopListening();
        super.onStop();
    }


    public static class TrulysChatVH extends RecyclerView.ViewHolder {
        View mView;

        GestureDetector gestureDetector;
Context context;
        public TrulysChatVH(View itemView,Context context) {
            super(itemView);
            this.context=context;
            mView = itemView;

        }

        public void setImage(String name, Context context) {
            CircleImageView imageView = mView.findViewById(R.id.userpic);
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.test);
            Glide.with(context).load(name).apply(requestOptions).into(imageView);
        }

        public void setMessage(String message) {
            TextView textView = mView.findViewById(R.id.message);
            Linkify.addLinks(textView,Linkify.WEB_URLS);
            textView.setText(message);
        }

        public void setSentDate(String date) {
            TextView dateTextView = mView.findViewById(R.id.lasttime);
            dateTextView.setText(date);


        }

        public void setName(String name) {
            TextView namet = mView.findViewById(R.id.name);
            namet.setText(String.valueOf(name));


        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        if (v.getId()==R.id.friendList){
            getActivity().getMenuInflater().inflate(R.menu.context_chats,menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d(TAG, "onContextItemSelected: "+item.getItemId());
        return super.onContextItemSelected(item);
    }
}

