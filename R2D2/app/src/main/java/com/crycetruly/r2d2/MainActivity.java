package com.crycetruly.r2d2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.crycetruly.r2d2.model.Place;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;
    private static final String TAG = "MainActivity";
    private Context context;
    private RecyclerView mPlacesList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, rootRef;
    private AppCompatButton rmbtn;
    private String currentUserId;
    private TextView count;
    private RelativeLayout progressBar;
    private Query query;
    private Context mContext;
    private TextView textView;
    private TextView email;
    FirebaseRecyclerAdapter adapter;
    RelativeLayout relativeLayout;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//         FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        setSupportActionBar(toolbar);
        email = findViewById(R.id.email);
        FirebaseAuth.getInstance().signInWithEmailAndPassword("chrisahebwa@gmail.com", "909090").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
//                 Log.d(TAG, "onComplete: logged in" + FirebaseAuth.getInstance().getCurrentUser().getUid());
              DatabaseReference ref=  FirebaseDatabase.getInstance()
                        .getReference()
                        .child("verificationkeys");

              ref.child("one").child("key").setValue("678132");
                ref.child("two").child("key").setValue("678032");
                ref.child("three").child("key").setValue("671132");
                ref.child("four").child("key").setValue("618132");
            }
        });
        context = this;
        auth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Log.d(TAG, "onAuthStateChanged: user is not logged in");
                    Intent i = new Intent(context, StartActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else {
                    //email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                }
            }
        };
        relativeLayout = findViewById(R.id.rel_layout);
        MainFragment mainFragment = new MainFragment();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.rel_layout, mainFragment);
        fragmentTransaction.commit();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.keys) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.keys:
                Log.d(TAG, "onNavigationItemSelected: " + item.getTitle());
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                KeysFragment keysFragment = new KeysFragment();
                fragmentTransaction.replace(R.id.rel_layout, keysFragment, "keysFragment");
                fragmentTransaction.commit();
                getSupportActionBar().setTitle("Verification Keys");
                break;
            case R.id.places:
                Log.d(TAG, "onNavigationItemSelected: " + item.getTitle());
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                MainFragment mainFragment = new MainFragment();
                fragmentTransaction.replace(R.id.rel_layout, mainFragment, "mainFragment");
                fragmentTransaction.commit();
                getSupportActionBar().setTitle("Places");
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}