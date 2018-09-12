package com.crycetruly.dailydreams;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.crycetruly.dailydreams.Retro.Api;
import com.crycetruly.dailydreams.Retro.RetroClient;
import com.crycetruly.dailydreams.adapters.DayHolder;
import com.crycetruly.dailydreams.models.Day;

import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.supercharge.shimmerlayout.ShimmerLayout;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Api api;
    RecyclerView recyclerView;
    CompositeDisposable compositeDisposable=new CompositeDisposable();
    ShimmerLayout shimmerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              goToAddActivity();
            }
        });


        //init api
        Retrofit retrofit= RetroClient.getRetrofit();
        api=retrofit.  create(Api.class);
        
        shimmerLayout=findViewById(R.id.shimmerLayout);
        recyclerView=findViewById(R.id.daysList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    fetchData();
    }

    private void fetchData() {
        shimmerLayout.startShimmerAnimation();
        compositeDisposable.add(api.getDays().subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<List<Day>>() {
            @Override
            public void accept(List<Day> days) throws Exception {
                displayData(days);

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void displayData(List<Day> days) {
        DayHolder adapter=new DayHolder(getBaseContext(),days);
        recyclerView.setAdapter(adapter);

        shimmerLayout.stopShimmerAnimation();

        shimmerLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add) {
            goToAddActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToAddActivity() {
        startActivity(new Intent(this,AddActivity.class));
    }

    @Override
    protected void onStop() {
         compositeDisposable.clear();
        super.onStop();
    }
}
