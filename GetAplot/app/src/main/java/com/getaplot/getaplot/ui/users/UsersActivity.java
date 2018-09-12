package com.getaplot.getaplot.ui.users;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.getaplot.getaplot.model.Point;
import com.getaplot.getaplot.search.SearchPeopleActivity;
import com.getaplot.getaplot.ui.EnlargeImageView;
import com.getaplot.getaplot.ui.ProfileActivity;
import com.getaplot.getaplot.ui.settings.AllSettingsActivity;
import com.getaplot.getaplot.utils.Handy;
import com.getaplot.getaplot.utils.Location;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    private static final String TAG = "UsersActivity";
    RecyclerView recyclerView;
    DatabaseReference databaseReference, friendRef;
    Query query;
    DatabaseReference users;
    boolean isBuddy;
    FirebaseRecyclerAdapter adapter;
    private Context mContext;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Location location = new Location();
                location.getLocationPermission(UsersActivity.this, UsersActivity.this);
                Location.getDeviceLocation(UsersActivity.this, UsersActivity.this);
            }
        });
        thread.start();
        users = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("userName")) {
                    Toast.makeText(UsersActivity.this, "You need to choose a username", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(UsersActivity.this, PickAuserNameActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        initViews();


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    TextView textView = findViewById(R.id.none);
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
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
                                Intent i = new Intent(mContext, EnlargeImageView.class);
                                Pair[] pairs = new Pair[1];
                                pairs[0] = new Pair<View, String>(viewHolder.imageView, "cpic");
                                i.putExtra("image_url", dataSnapshot.child("image").getValue(String.class));
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(UsersActivity.this, pairs);
                                    startActivity(i, activityOptions.toBundle());

                                } else {
                                    startActivity(i);
                                }
                            });
                        } catch (NullPointerException e) {
                            Log.d(TAG, "onDataChange: error" + e.getMessage());
                        }
                        try {
                            RequestOptions requestOptions = new RequestOptions()
                                    .centerCrop()
                                    .placeholder(R.drawable.test);
                            Glide.with(getApplicationContext()).load(dataSnapshot.child("image").getValue().toString()).apply(requestOptions).into(viewHolder.imageView);
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
                if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(key)) {
                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                        Intent i = new Intent(mContext, ProfileActivity.class);
                                        i.putExtra("user_id", key);
                                        i.putExtra("user_name", "User");
                                        startActivity(i);
                                    } else {
                                        startActivity(new Intent(mContext, AllSettingsActivity.class));
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


    private void initViews() {
        toolbar = findViewById(R.id.meet);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Top Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.top_users);
        StaggeredGridLayoutManager staggeredGridLayoutManager=new StaggeredGridLayoutManager(
                2,StaggeredGridLayoutManager.VERTICAL);

        int orientation=getResources().getConfiguration().orientation;
        if (orientation== Configuration.ORIENTATION_LANDSCAPE){

            recyclerView.setLayoutManager(staggeredGridLayoutManager);
        }else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        }
        progressBar = findViewById(R.id.loadtop);
        recyclerView.setHasFixedSize(true);
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(FirebaseAuth.getInstance()

                .getCurrentUser().getUid());
        mContext = getBaseContext();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("userPoints");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.placesmnenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.searchplaces:
                startActivity(new Intent(getBaseContext(), SearchPeopleActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
