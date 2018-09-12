package com.crycetruly.r2d2;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crycetruly.r2d2.model.Key;
import com.crycetruly.r2d2.model.Place;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class KeysFragment extends Fragment {
    private RecyclerView keyList;

    private FirebaseRecyclerAdapter adapter;
    private static final String TAG = "KeysFragment";
    private ProgressBar progressBar;
    public KeysFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_keys, container, false);
        keyList = v.findViewById(R.id.keys);
        keyList.setHasFixedSize(true);
        keyList.setLayoutManager(new LinearLayoutManager(getContext()));
progressBar=v.findViewById(R.id.progressBar2);
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("verificationkeys");

        FirebaseRecyclerOptions<Key> options =
                new FirebaseRecyclerOptions.Builder<Key>()
                        .setQuery(query, Key.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Key, KeyHolder>(options) {
            @Override
            public KeyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_key, parent, false);

                return new KeyHolder(view);
            }

             @Override
            protected void onBindViewHolder(@NonNull KeyHolder holder, int position, @NonNull Key model) {
                holder.setKey(model.getKey());
                progressBar.setVisibility(View.GONE);
            }
        };

        keyList.setAdapter(adapter);
        return v;

    }

    public static class KeyHolder extends RecyclerView.ViewHolder {
        View mView;

        public KeyHolder(View itemView) {
            super(itemView);
            mView=itemView;

        }
        public void setKey(String key){
            TextView keys=mView.findViewById(R.id.keys);
            keys.setText(key);
        }
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
}
