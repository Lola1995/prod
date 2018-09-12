package com.crycetruly.r2d2;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.crycetruly.r2d2.model.Place;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
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
    private Query query;
    private Context mContext;
    private TextView textView;
    private TextView email;
    FirebaseRecyclerAdapter adapter;
    RelativeLayout relativeLayout;
    FragmentTransaction fragmentTransaction;
    ProgressBar progressBar;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_main, container, false);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        mPlacesList =v. findViewById(R.id.places);
        mPlacesList.setHasFixedSize(true);
progressBar=v.findViewById(R.id.progressBar2);
        mContext = getContext();
        StaggeredGridLayoutManager staggeredGridLayoutManager=new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);

        mPlacesList.setLayoutManager(staggeredGridLayoutManager);


        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Place_Users");

        FirebaseRecyclerOptions<Place> options =
                new FirebaseRecyclerOptions.Builder<Place>()
                        .setQuery(query, Place.class)
                        .build();


        adapter = new FirebaseRecyclerAdapter<Place, PlaceHolder>(options) {
            @Override
            public PlaceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.singleplace, parent, false);

                return new PlaceHolder(view);
            }

            @Override
            protected void onBindViewHolder(PlaceHolder viewHolder, int position, Place model) {
                // Bind the Chat object to the ChatHolder
                // ...

                final String key=getRef(position).getKey();
viewHolder.mView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent i=new Intent(getContext(),PlaceActivity.class);
        i.putExtra("place_id",key);
        startActivity(i);
    }
});
                try {
                    viewHolder.setPlaceName(model.getName());
                } catch (NullPointerException e) {
                    Log.d(TAG, "populateViewHolder: NullPointerException" + e.getMessage());

                }
                viewHolder.setCategory(model.getCategory());
                viewHolder.setImage(model.getImage(), mContext);
                viewHolder.setPlaceDescription(model.getDescription());

                try {
                    viewHolder.setPlaceDistrict(model.getDistrict());
                } catch (NullPointerException e) {

                }
                progressBar.setVisibility(View.GONE);
            }




        };
        mPlacesList.setHasFixedSize(true);
        mPlacesList.setAdapter(adapter);

        return v;
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

    public static class PlaceHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageButton addbtn, rmbtn;
        ImageView placeImage;
        Button follow_btn;
        ImageView followbtn;
        TextView evc;
        public PlaceHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }
        public void setPlaceName(String name) {
            TextView nameTextView = mView.findViewById(R.id.place_nametext);
            nameTextView.setText(name);
        }

        public void setCategory(String name) {
            TextView catText = mView.findViewById(R.id.category);
            catText.setText(name);
        }

        public void setImage(String imageUrl, Context context) {
            placeImage = mView.findViewById(R.id.placeImageView);
            try {
                Glide.with(context)
                        .load(imageUrl)
                        .thumbnail(0.5f)
                        .into(placeImage);
                //


            } catch (Exception e) {
                Log.d(TAG, "onDataChange:Picasso " + e.getMessage());
            }


        }

        public void setPlaceDescription(String desc) {
            TextView textView = mView.findViewById(R.id.desc);
            textView.setText(desc);
        }

        public void setPlaceDistrict(String dist) {
            TextView textView = mView.findViewById(R.id.pdist);
            textView.setText(dist);
        }

    }
}



