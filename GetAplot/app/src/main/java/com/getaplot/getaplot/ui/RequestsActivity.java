package com.getaplot.getaplot.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.getaplot.getaplot.R;
import com.getaplot.getaplot.adapters.RequestPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class RequestsActivity extends AppCompatActivity {
    private static final String TAG = "RequestsActivity";
    private static final int ACTIVITY_NUM = 4;
    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference, rootRef;
    private ViewPager requestsPager;
    private TabLayout reqtabs;
    private RequestPagerAdapter pagerAdapter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        mToolbar = findViewById(R.id.re_toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("Friend Requests ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = RequestsActivity.this;
        // setUpBottomNavigationView();

        pagerAdapter = new RequestPagerAdapter(getSupportFragmentManager());
        reqtabs = findViewById(R.id.requeststablayout);

        requestsPager = findViewById(R.id.requestpager);
        requestsPager.setAdapter(pagerAdapter);
        reqtabs.setupWithViewPager(requestsPager);
        rootRef = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue("true");
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)

    {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}