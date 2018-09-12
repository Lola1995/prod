package com.getaplot.getaplot.search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.UserSearchNode;
import com.getaplot.getaplot.ui.ProfileActivity;
import com.getaplot.getaplot.ui.SettingActivity;
import com.getaplot.getaplot.ui.settings.AllSettingsActivity;
import com.getaplot.getaplot.utils.Handy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchPeopleActivity extends AppCompatActivity {
    Query firebaseSearchQuery;
    private EditText et_search;
    private RecyclerView userList;
    private Toolbar toolbar;
    private Context mContext;
    private DatabaseReference mDatabase;
    private ProgressBar progresss;
    private static final String TAG = "SearchPeopleActivity";
    private DatabaseReference friendRef, users;
    FirebaseRecyclerAdapter adapter;
    private LinearLayout lyt_no_result;
    private ImageView bt_clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_find_users);
        initToolbar();
        initComponent();


        mDatabase = FirebaseDatabase.getInstance().getReference().child("userSearchNode");
        users = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.keepSynced(true);
        mContext = getBaseContext();
        userList = findViewById(R.id.usersList);
        userList.setHasFixedSize(true);
        userList.setLayoutManager(new LinearLayoutManager(mContext));
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(FirebaseAuth.getInstance()

                .getCurrentUser().getUid());

    }

    private void initToolbar() {


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Handy.setSystemBarColor(this, R.color.grey_5);
        Handy.setSystemBarLight(this);
    }

    private void hideSoftKeyboard() {
        if (this.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void initComponent() {
        progresss = findViewById(R.id.progresss);
        bt_clear = (ImageView) findViewById(R.id.bt_clear);
        bt_clear.setVisibility(View.GONE);
        et_search = findViewById(R.id.search);
        lyt_no_result = (LinearLayout) findViewById(R.id.lyt_no_result);

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: "+s);
                firebaseUserSearch(s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                firebaseUserSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                firebaseUserSearch(s.toString());
            }
        });



        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
            }
        });
        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    //execute our method for searching
                    firebaseUserSearch(Handy.capitalize(et_search.getText().toString().trim()));
                }
                return false;
            }
        });


    }


    private void firebaseUserSearch(final String searchText) {
        hideSoftKeyboard();
        lyt_no_result.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        firebaseSearchQuery = mDatabase.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");
        boolean isFirstName = sharedPreferences.getBoolean("search", false);
        if (!isFirstName) {
            firebaseSearchQuery = mDatabase.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");
            } else {
            firebaseSearchQuery = mDatabase.orderByChild("userName").startAt(searchText.toLowerCase()).endAt(searchText.toLowerCase() + "\uf8ff");
        }

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
                            lyt_no_result.setVisibility(View.VISIBLE);
                            Log.d(TAG, "onDataChange: again not");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    Snackbar.make(userList, "Change search filters ?",
                            Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(getBaseContext(), SettingActivity.class));
                        }
                    }).show();
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

                viewHolder.setDetails(mContext, model.getStatus(), model.getName(), model.getImage());
                progresss.setVisibility(View.GONE);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(key)) {
                            Intent i = new Intent(mContext, ProfileActivity.class);
                            i.putExtra("user_id", key);
                            startActivity(i);
                        } else {
                            startActivity(new Intent(mContext, AllSettingsActivity.class));
                        }
                    }
                });


            }

        };


        userList.setAdapter(adapter);
        adapter.startListening();
    }


    @Override
    public void onStop() {
        try {
            adapter.stopListening();
        } catch (NullPointerException e) {

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
