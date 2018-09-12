package com.getaplot.getaplot.fragments.users;


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
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.UserSearchNode;
import com.getaplot.getaplot.ui.ChatActivity;
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

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class FindUsersFragment extends Fragment {
    private static final String TAG = "FindUsersFragment";
    Query firebaseSearchQuery;
    private EditText seachimage;
    private EditText searchedittext;
    private RecyclerView userList;
    private Context mContext;
    private DatabaseReference mDatabase;
    private ProgressBar progresss;
    private TextView textView;
    private DatabaseReference friendRef, users;
FirebaseRecyclerAdapter adapter;
    public FindUsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_find_users, container, false);
        progresss = v.findViewById(R.id.progresss);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("userSearchNode");
        users = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.keepSynced(true);
        mContext = getContext();
        seachimage = v.findViewById(R.id.search);
        searchedittext = v.findViewById(R.id.searchUserField);
        userList = v.findViewById(R.id.usersList);
        userList.setHasFixedSize(true);
        textView = v.findViewById(R.id.textView);
        userList.setLayoutManager(new LinearLayoutManager(mContext));
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(FirebaseAuth.getInstance()

                .getCurrentUser().getUid());

        searchedittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    //execute our method for searching
                    firebaseUserSearch(Handy.capitalize(searchedittext.getText().toString().trim()));
                }
                return false;
            }
        });

        seachimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchText = Handy.capitalize(searchedittext.getText().toString().trim());
                if (!TextUtils.isEmpty(searchText)) {
                    progresss.setVisibility(View.VISIBLE);
                    firebaseUserSearch(Handy.capitalize(searchText));
                    hideSoftKeyboard();
                } else {
                    Toast.makeText(mContext, "Type a name of user to search", Toast.LENGTH_SHORT).show();
                }

            }
        });
        return v;
    }

    private void hideSoftKeyboard() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void firebaseUserSearch(final String searchText) {
        hideSoftKeyboard();
        textView.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        firebaseSearchQuery = mDatabase.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");
        boolean isFirstName = sharedPreferences.getBoolean("search", false);
//        if (!isFirstName) {
//            firebaseSearchQuery = mDatabase.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");
//            Log.d(TAG, "firebaseUserSearch: names ran");
//        } else {
//            Log.d(TAG, "firebaseUserSearch: username ran");
//            firebaseSearchQuery = mDatabase.orderByChild("userName").startAt(searchText.toLowerCase()).endAt(searchText.toLowerCase() + "\uf8ff");
//        }

        firebaseSearchQuery.keepSynced(true);
        firebaseSearchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: not exists");
 firebaseSearchQuery = mDatabase.orderByChild("userName").startAt(searchText.toLowerCase()).endAt(searchText.toLowerCase() + "\uf8ff");
 firebaseSearchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
     @Override
     public void onDataChange(DataSnapshot dataSnapshot) {
         if (!dataSnapshot.exists())
         Toast.makeText(mContext, "No matching users for the search", Toast.LENGTH_SHORT).show();
         progresss.setVisibility(View.GONE);
         textView.setVisibility(View.VISIBLE);
         Log.d(TAG, "onDataChange: again not");
     }

     @Override
     public void onCancelled(DatabaseError databaseError) {

     }
 });

 //

//                    Snackbar.make(userList, "Change search filters ?",
//                            Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            startActivity(new Intent(getContext(), SettingActivity.class));
//                        }
//                    }).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseRecyclerOptions<UserSearchNode> options =
                new FirebaseRecyclerOptions.Builder<UserSearchNode>()
                        .setQuery(firebaseSearchQuery, UserSearchNode.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<UserSearchNode, TrulysPeopleVH>(options) {
            @Override
            public TrulysPeopleVH onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.singlesearch, parent, false);

                return new TrulysPeopleVH(view);
            }

            @Override
            protected void onBindViewHolder(final TrulysPeopleVH viewHolder, int position, UserSearchNode model) {
                final String key = getRef(position).getKey();


                users.child(key).addValueEventListener(new ValueEventListener() {
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
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.setDetails(getContext(), model.getStatus(), model.getName(), model.getImage());
                progresss.setVisibility(View.GONE);
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
                                                        startActivity(i);

                                                    } catch (NullPointerException e) {

                                                    }
                                                }

                                            }
                                        });
                                        try {
                                            builder.show();
                                        }catch (Exception e){


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


        userList.setAdapter(adapter);
        adapter.startListening();
    }



    @Override
    public void onStop() {
       try {
           adapter.stopListening();
       }catch (NullPointerException e){

       }
        super.onStop();
    }

    public static class TrulysPeopleVH extends RecyclerView.ViewHolder {
        View mView;
        TextView us_name, us_status;
        CircleImageView us_image;

        public TrulysPeopleVH(View itemView) {
            super(itemView);
            mView = itemView;
            us_name = mView.findViewById(R.id.us_name);
            us_image = mView.findViewById(R.id.attendee_pic);
            us_status = mView.findViewById(R.id.us_status);
        }

        public void setDetails(Context ctx, String status, String name, String image) {
            TextView us_name = mView.findViewById(R.id.us_name);
            us_name.setText(Handy.getTrimmedName(name));
            CircleImageView us_image = mView.findViewById(R.id.attendee_pic);
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.test);
            try {
                Glide.with(ctx)
                        .load(image).apply(requestOptions)
                        .into(us_image);
            } catch (IllegalArgumentException e) {

            }

            TextView us_status = mView.findViewById(R.id.us_status);
            us_status.setText(status);

        }

        public void setUserName(String name) {
            TextView nameTextView = mView.findViewById(R.id.userName);
            nameTextView.setText(name);
        }
    }

}
