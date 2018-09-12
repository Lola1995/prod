package com.getaplot.getaplot.ui;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.getaplot.getaplot.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class PhotosProgressViewActivity extends AppCompatActivity {
    private StoriesProgressView storiesProgressView;
    private int PROGRESS_COUNT;
    private static final String TAG = "PhotosProgressViewActiv";
    List<String> photos = new ArrayList<>();
    ImageView imageView;
    int counter = 0;

    Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos_progress_view);
        imageView = findViewById(R.id.image);
        PROGRESS_COUNT = (int) getIntent().getIntExtra("count", 0);
        storiesProgressView = findViewById(R.id.stories);
        if (getIntent().hasExtra("from")) {
            query = FirebaseDatabase.getInstance().getReference().child("userPhotos")
                    .child(getIntent().getStringExtra("user_id")).orderByChild("lastUpdated");
        } else {
            query = FirebaseDatabase.getInstance().getReference().child("placePhotos")
                    .child(getIntent().getStringExtra("place_id")).orderByChild("fitnessNum");
        }
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange1: " + dataSnapshot);
                for (DataSnapshot single : dataSnapshot.getChildren()) {

                    if (getIntent().hasExtra("from")) {
                        photos.add(single.child("image").getValue(String.class));

                    } else {
                        photos.add(single.child("photo").getValue(String.class));
                    }


                    Log.d(TAG, "onDataChange: progress counting to " + PROGRESS_COUNT);
                }
                Glide.with(getBaseContext()).load(photos.get(0)).into(imageView);
                imageView.setOnClickListener(v -> storiesProgressView.skip());
                storiesProgressView.setStoriesCount(PROGRESS_COUNT); // <- set stories
                storiesProgressView.setStoryDuration(2400L);
                storiesProgressView.startStories();
                storiesProgressView.setStoriesListener(new StoriesProgressView.StoriesListener() {
                    @Override
                    public void onNext() {

                        try {
                            Glide.with(getBaseContext()).load(Uri.parse(photos.get(++counter))).into(imageView);
                        } catch (Exception e) {
                            finish();
                            Toast.makeText(PhotosProgressViewActivity.this, "An error has occured", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onPrev() {

                        try {
                            Glide.with(getBaseContext()).load(Uri.parse(photos.get(--counter))).into(imageView);
                        } catch (Exception ee) {
                            finish();
                            Toast.makeText(PhotosProgressViewActivity.this, "An erro has occured", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(PhotosProgressViewActivity.this, "Done showing photos", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        // Very important !
        storiesProgressView.destroy();
        super.onDestroy();
    }


}
