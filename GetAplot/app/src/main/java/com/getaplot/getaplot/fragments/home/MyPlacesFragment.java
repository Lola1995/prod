package com.getaplot.getaplot.fragments.home;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.getaplot.getaplot.model.Place;
import com.getaplot.getaplot.ui.EnlargeImageView;
import com.getaplot.getaplot.ui.PlaceActivity;
import com.getaplot.getaplot.utils.GetCurTime;
import com.getaplot.getaplot.utils.Handy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MyPlacesFragment extends Fragment {
    private static final String TAG = "MyPlacesFragment";
    String currentUserId;
    private RecyclerView mPlacesList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, rootRef, placeDb;
    private AppCompatButton rmbtn;
    private TextView count, friendtest;
    private ProgressBar progressBar;
    private FirebaseRecyclerAdapter<Place, PlacesVH> adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public MyPlacesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "onCreate: created");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //--------------------------CREATE CUSTOM VIEW --------------------------\



        //----------ABOUT THE NAME ITSALL ABOUT android.view.InflateException: Binary XML file line #37: Error inflating class <unknown>//
        final View mainView = inflater.inflate(R.layout.fragment_chatss, container, false);
        count = mainView.findViewById(R.id.count);
        progressBar = mainView.findViewById(R.id.loadfav);
        progressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout = mainView.findViewById(R.id.swipe);
        swipeRefreshLayout.setEnabled(false);

        //------------------------FIREBASE-----------------------------
        //------------------------------------FIREBASE AUTH-----------------------------------//
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        mPlacesList = mainView.findViewById(R.id.mPlaces);
        mPlacesList.setHasFixedSize(true);
        friendtest = mainView.findViewById(R.id.nofavourites);
        mPlacesList.setLayoutManager(new LinearLayoutManager(getContext()));
        mDatabase = rootRef.child("Users").child(currentUserId).child("subscribedTo");
        rootRef.keepSynced(true);
        mDatabase.keepSynced(true);

        placeDb = rootRef.child("Place_Users");
        placeDb.keepSynced(true);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Handy.isNetworkAvailable(getContext())) {
                    onStart();
                } else {
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }


                    Snackbar.make(progressBar, "Not connected", Snackbar.LENGTH_LONG).setAction("Connect", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);

                        }
                    }).show();

                }

            }
        });


        return mainView;


    }


    @Override
    public void onStart() {


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {

                    friendtest.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseRecyclerOptions<Place> options =
                new FirebaseRecyclerOptions.Builder<Place>()
                        .setQuery(mDatabase, Place.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Place, PlacesVH>(options) {
            @Override
            public PlacesVH onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_fav_place, parent, false);

                return new PlacesVH(view);
            }

            @Override
            protected void onBindViewHolder(final PlacesVH viewHolder, int position, Place model) {

                //------------------GET UNIQUE ID OF ACH PLAC4 AND PASSS IT IN AN INTENT------------------//
                final String placeId = getRef(position).getKey();
                viewHolder.followbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("This  will remove this place from your favorites list").setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mDatabase.child(placeId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        adapter.notifyDataSetChanged();

                                        Toast.makeText(getContext(), "Place has been successfully unfollowed", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Could not Unfollow this place:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        }).show();

                    }
                });
                upDateNumberofPlots(placeId, viewHolder);

                placeDb.child(placeId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String place_name, district, discription, category;
                        try {
                            place_name = dataSnapshot.child("name").getValue().toString();
                            final String imageUrl = dataSnapshot.child("image").getValue().toString();
                            district = dataSnapshot.child("district").getValue().toString();
                            discription = dataSnapshot.child("description").getValue().toString();
                            category = dataSnapshot.child("category").getValue().toString();

                            viewHolder.nameTextView.setText(Handy.getTrimmedName(place_name));
                            viewHolder.district.setText(district);
                            viewHolder.d.setText(discription);

                            try {
                                RequestOptions requestOptions = new RequestOptions()
                                        .centerCrop()
                                        .placeholder(R.drawable.ptest);
                                Glide.with(getContext())
                                        .load(imageUrl).apply(requestOptions)
                                        .into(viewHolder.placeImage);
                            } catch (IllegalArgumentException e) {

                            }
                            viewHolder.category.setText(category);
                            swipeRefreshLayout.setEnabled(true);
                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(getContext(), PlaceActivity.class);
                                    i.putExtra("place_id", placeId);
                                    i.putExtra("place_name", place_name);
                                    i.putExtra("description", discription);
                                    i.putExtra("imageUrl", imageUrl);
                                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    getActivity().getSupportFragmentManager().popBackStack();

                                    startActivity(i);
                                }
                            });

                            viewHolder.placeImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(getContext(), EnlargeImageView.class);
                                    i.putExtra("image_url", imageUrl);
                                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    getActivity().getSupportFragmentManager().popBackStack();

                                    startActivity(i);
                                }
                            });
                        } catch (Exception e) {

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });

                progressBar.setVisibility(View.GONE);


            }
        };

        mPlacesList.setAdapter(adapter);
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }

        adapter.startListening();
        super.onStart();

    }

    @Override
    public void onStop() {
        adapter.stopListening();
        super.onStop();
    }

    private void upDateNumberofPlots(String placeId, final PlacesVH viewHolder) {
        rootRef.child("Place_Users").child(placeId).child("events").orderByChild("date_comparable").startAt(GetCurTime.today()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int count = (int) dataSnapshot.getChildrenCount();
                    if (count == 1) {
                        viewHolder.evc.setText(String.valueOf(count).concat(" Ongoing plot"));
                        viewHolder.evc.setTypeface(viewHolder.evc.getTypeface(), Typeface.BOLD);
                    } else if (count > 1) {

                        viewHolder.evc.setText(String.valueOf(count).concat(" Ongoing plots"));
                        viewHolder.evc.setTypeface(viewHolder.evc.getTypeface(), Typeface.BOLD);
                    }
                } else {
                    viewHolder.evc.setText("No ongoing plots at the moment");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public static class PlacesVH extends RecyclerView.ViewHolder {
        View mView;
        ImageView placeImage;
        TextView nameTextView, catText, district, category, d;
        ImageView followbtn;
        TextView evc;

        public PlacesVH(View itemView) {
            super(itemView);
            mView = itemView;
            nameTextView = mView.findViewById(R.id.place_nametext);
            placeImage = mView.findViewById(R.id.placeImageView);
            district = mView.findViewById(R.id.pdist);
            category = mView.findViewById(R.id.category);
            followbtn = mView.findViewById(R.id.followbtn);
            evc = mView.findViewById(R.id.evc);
            d = mView.findViewById(R.id.desc);
        }

        public void setPlaceName(String name) {
            nameTextView = mView.findViewById(R.id.place_nametext);
            nameTextView.setText(name);
        }

        public void setCategory(String name) {
            catText = mView.findViewById(R.id.category);
            catText.setText(name);
        }

        public void setImage(String imageUrl, Context context) {
            placeImage = mView.findViewById(R.id.placeImageView);
            try {

                Glide.with(context)
                        .load(imageUrl)
                        .into(placeImage);
            } catch (IllegalArgumentException e) {

            }


            //Picasso.with(context).load(imageUrl).placeholder(R.drawable.ptest).into(placeImage);

        }

        public void setPlaceDescription(String desc) {
            TextView d = mView.findViewById(R.id.desc);
            d.setText(desc);
        }

        public void setPlaceDistrict(String desc) {
            TextView district = mView.findViewById(R.id.pdist);
            district.setText(desc);
        }


    }
}
