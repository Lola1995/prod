package com.getaplot.getaplot.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.InflateException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.getaplot.getaplot.R;
import com.getaplot.getaplot.adapters.EventsAdapter;
import com.getaplot.getaplot.model.Event;
import com.getaplot.getaplot.utils.Handy;
import com.getaplot.getaplot.utils.UserPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SerchActivity extends AppCompatActivity {
    private static final String TAG = "SerchActivity";
    private static final int ACTIVITY_NUM = 1;
    private final List<Event> eventList = new ArrayList<>();
    EventsAdapter adapter;
    private Toolbar mToolbar;
    private Context mContext = SerchActivity.this;
    private FirebaseAuth mAuth;
    private FirebaseUser currentfirebaseUser;
    private DatabaseReference mUserRef, mRootRef;
    private String current_uid;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private DatabaseReference ref;
    private Toolbar toolbar;
    private boolean isSearch = false;

    public SerchActivity() throws IOException, IndexOutOfBoundsException, InflateException, OutOfMemoryError, NullPointerException {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Log.d(TAG, "onCreate: started");
        toolbar = findViewById(R.id.searchToolbar);

       initToolbar();
        UserPreferences.getUserSearchByPrefence(mContext);


        //SET UP PREFERENCES TO SORT THE SEARCHES------------------------------//
        recyclerView = findViewById(R.id.evv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        hideSoftKeyboard();
        recyclerView.setHasFixedSize(true);
        adapter = new EventsAdapter(eventList, SerchActivity.this);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        loadEvents();

        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        SearchView searchView = findViewById(R.id.ss);
        searchView.setIconified(false);
        searchView.setQueryHint("Search for plot...");
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.d(TAG, "onFocusChange: ");
                hideSoftKeyboard();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                hideSoftKeyboard();
                Log.d(TAG, "onQueryTextSubmit: ");
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: " + newText);
                adapter.getFilter().filter(newText);
                hideSoftKeyboard();
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return true;
            }
        });

    }

    private void initToolbar() {
            toolbar = (Toolbar) findViewById(R.id.searchToolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Handy.setSystemBarColor(this, R.color.grey_5);
            Handy.setSystemBarLight(this);

    }


    private void loadEvents() {
        ref = FirebaseDatabase.getInstance().getReference().child("Events");
        Query q = ref.orderByChild("date_comparable");

        //TODO MOVE PLACE EVENTS SOMEWHERE SAFE
        //Query q=ref.orderByChild("date_comparable").startAt(GetCurTime.today());
        q.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded: hr" + dataSnapshot);
                Event event = dataSnapshot.getValue(Event.class);
                eventList.add(event);
                adapter.notifyDataSetChanged();


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void hideSoftKeyboard() {
//
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    //---------------------------SETUP NAVIGATION VIEW------------------------------/

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onBackPressed() {
        finish();
    }

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


}
