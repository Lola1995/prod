package com.crycetruly.r2d2.fragments;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crycetruly.r2d2.PlaceActivity;
import com.crycetruly.r2d2.R;
import com.crycetruly.r2d2.model.Place;
import com.crycetruly.r2d2.utils.Handy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchPlacesActivity extends AppCompatActivity {

    private static final String TAG = "SearchPlacesActivity";

ProgressBar progresss;
DatabaseReference mDatabase;
    private Context mContext = SearchPlacesActivity.this;
FirebaseRecyclerAdapter adapter;
    //widgets
    private EditText mSearchParam;
    private RecyclerView mListView;
    ImageView seachimage;
TextView textView;
    //vars
    private List<Place> mPlaceList;
    Query firebaseSearchQuery;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_places);
        mSearchParam = (EditText) findViewById(R.id.search);
        mListView =  findViewById(R.id.listView);
        Log.d(TAG, "onCreate: started.");
        ImageView close=findViewById(R.id.closewin);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        progresss = findViewById(R.id.progresss);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Place_Users");
        mDatabase.keepSynced(true);
        mContext = this;
        seachimage =findViewById(R.id.seachimage);
        mListView = findViewById(R.id.listView);
        mListView.setHasFixedSize(true);
        textView =findViewById(R.id.textView);
        mListView.setLayoutManager(new LinearLayoutManager(mContext));


        mSearchParam.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    //execute our method for searching
                    firebaseUserSearch(Handy.capitalize(mSearchParam.getText().toString().trim()));
                }
                return false;
            }
        });

        seachimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchText = Handy.getTrimmedName(mSearchParam.getText().toString().trim());
                if (!TextUtils.isEmpty(searchText)) {
                    progresss.setVisibility(View.VISIBLE);
                    firebaseUserSearch(Handy.capitalize(searchText));
                    hideSoftKeyboard();
                } else {
                    Toast.makeText(mContext, "Type a name of a place to search", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void firebaseUserSearch(final String searchText) {
        hideSoftKeyboard();
        textView.setVisibility(View.GONE);
        firebaseSearchQuery = mDatabase.orderByChild("name").startAt(Handy.getTrimmedName(searchText)).endAt(Handy.getTrimmedName(searchText + "\uf8ff"));
        Log.d(TAG, "firebaseUserSearch: names ran");


        firebaseSearchQuery.keepSynced(true);
        firebaseSearchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: No match for search");
                    Toast.makeText(mContext, "No matching places for the search", Toast.LENGTH_SHORT).show();
                    progresss.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseRecyclerOptions<Place> options = new FirebaseRecyclerOptions.Builder<Place>().setQuery(
                firebaseSearchQuery, Place.class
        ).build();

        adapter = new FirebaseRecyclerAdapter<Place, TrulysPeopleVH>(options) {
            @Override
            public TrulysPeopleVH onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.singlesearchplace, parent, false);

                return new TrulysPeopleVH(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull TrulysPeopleVH viewHolder, int position, @NonNull Place model) {
                final String key = getRef(position).getKey();


                viewHolder.setDetails(getBaseContext(), model.getDescription(), model.getName(), model.getImage(), model.getDistrict());
                progresss.setVisibility(View.GONE);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(mContext, PlaceActivity.class);
                        i.putExtra("place_id", key);
                        startActivity(i);
                    }
                });

            }











        };

        mListView.setAdapter(adapter);
        adapter.startListening();

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue("true");



    }

    @Override
    protected void onStop() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
try{
    adapter.stopListening();
}catch (NullPointerException e){

}
        super.onStop();
    }

    public static class TrulysPeopleVH extends RecyclerView.ViewHolder {
        View mView;
        TextView us_name, us_status;
        CircleImageView us_image;
TextView districts;
        public TrulysPeopleVH(View itemView) {
            super(itemView);
            mView = itemView;
            us_name = mView.findViewById(R.id.us_name);
            us_image = mView.findViewById(R.id.attendee_pic);
            us_status = mView.findViewById(R.id.us_status);
            districts=mView.findViewById(R.id.district);
        }

        public void setDetails(Context ctx, String status, String name, String image,String district) {
            TextView us_name = mView.findViewById(R.id.us_name);
            us_name.setText(Handy.getTrimmedName(name));
            districts.setText(district);
            CircleImageView us_image = mView.findViewById(R.id.attendee_pic);
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.ptest);
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