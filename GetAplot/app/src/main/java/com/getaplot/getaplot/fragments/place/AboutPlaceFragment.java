package com.getaplot.getaplot.fragments.place;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.ui.MapActivity;
import com.getaplot.getaplot.ui.PhotosProgressViewActivity;
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

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutPlaceFragment extends Fragment {

    private static final String TAG = "AboutPlaceFragment";
    LinearLayout maps;
    private String category, pname, description, image, email;
    private DatabaseReference rootRef, mDatabaseo, mCurrentUserDb;
    private RelativeLayout relativeLayout;
    private Context mContext;
    private TextView nameText;
    private TextView descriptionText, ccunt;
    private ImageView placeImage;
    private String place_id, currentUser;
    private RecyclerView mEventList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabasePlace_users;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Toolbar main;
    private ProgressBar mProgress;
    private ProgressBar progressBar;
    private ListView mylist;
    private TextView mail, phone;
    private TextView eventCount, photoCount, loc,website;
    private LinearLayout deleteAccount, followers;
    private LinearLayout linearLayout;
    private ImageView pl;
    private Button follow;

    public AboutPlaceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about_place, container, false);
        mContext = getContext();
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.keepSynced(true);
        follow = view.findViewById(R.id.follow);
        place_id = getActivity().getIntent().getStringExtra("place_id");
        mDatabaseo = rootRef.child("Place_Users").child(place_id);
        mDatabaseo.keepSynced(true);
        mProgress = view.findViewById(R.id.pe);
        loc = view.findViewById(R.id.loc);
        phone = view.findViewById(R.id.phone);
        website=view.findViewById(R.id.website);
        website.setPaintFlags(website.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        mAuth =  FirebaseAuth.getInstance();
        mail = view.findViewById(R.id.email);
        mail.setPaintFlags(mail.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        mDatabase = rootRef.child("Events").child(place_id);
        eventCount = view.findViewById(R.id.eventCount);
        photoCount = view.findViewById(R.id.photoCount);
        linearLayout = view.findViewById(R.id.photolayout);
        ccunt = view.findViewById(R.id.ccunt);
        pl = view.findViewById(R.id.pImag);

        initFirebaseDatabase();

        maps = view.findViewById(R.id.maps);

        //--------------------------THROW THE TWO  QUERRIES TO THE BACKGROUND
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                setUpCounts(place_id);
                setUpProfileSection(place_id);
                checkifUserFollowed(place_id, follow);

            }
        };
        Thread thread1 = new Thread(r1);
        thread1.start();

        maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               locateCurrentPlace();
            }
        });

        descriptionText = view.findViewById(R.id.pdescription);
        descriptionText.setText(getActivity().getIntent().getStringExtra("description"));

        return view;
    }

    private void locateCurrentPlace() {
        Intent i = new Intent(mContext, MapActivity.class);
        i.putExtra("place_id", place_id);
        startActivity(i);
    }

    private void checkifUserFollowed(final String place_id, final Button follow) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getCurrentUser().getUid()).child("subscribedTo");
        reference.keepSynced(true);

        reference.child(place_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    follow.setText("Following");

                    follow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setMessage("This will remove this place from your favorites list").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    return;
                                }
                            }).setPositiveButton("okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    removeFromFavourites(reference, place_id, follow);
                                }
                            }).show();

                        }
                    });

                } else {
                    Log.d(TAG, "onDataChange: Not subscribed");
                    follow.setText("Follow");

                    //viewHolder.follow_btn.setBackgroundResource(R.drawable.primarycolor_borders);
                    follow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addToFavourites(place_id, follow);
                        }
                    });

                }
                //       progressBar.setVisibility(View.GONE);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void removeFromFavourites(DatabaseReference reference, final String place_id, final Button follow) {
        follow.setText("UnFollowing");
        reference.child(place_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                follow.setText("Follow");
                checkifUserFollowed(place_id, follow);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                Toast.makeText(mContext, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                follow.setText("Following");
//        viewHolder.follow_btn.setBackgroundResource(R.drawable.secondarycolors);

                checkifUserFollowed(place_id, follow);
            }
        });
    }

    private void addToFavourites(final String place_id, final Button follow) {
        follow.setText("Following");

        // Toast.makeText(getContext(), "Following place", Toast.LENGTH_SHORT).show();
        // viewHolder.follow_btn.setBackgroundResource(R.drawable.secondarycolors);
        follow.setEnabled(false);
        //--------------GET KEY FIRST-----------------------
        GetCurTime t = new GetCurTime();
        String currentDate = GetCurTime.getCurTime();
        rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference newNotificationRef = rootRef.child("placeNotications").child(place_id).push();
        String notificationKey = newNotificationRef.getKey();
        HashMap<String, String> notificationData = new HashMap<>();
        notificationData.put("who", mAuth.getCurrentUser().getUid());
        notificationData.put("type", "favAddition");
        notificationData.put("at", currentDate);

        Map<String, String> placedata = new HashMap<String, String>();
        placedata.put("uid", mAuth.getCurrentUser().getUid());
        placedata.put("since", currentDate);

        Map<String, Object> subcriptionMap = new HashMap<>();

        subcriptionMap.put("placeSubscribers/" + place_id + "/" + mAuth.getCurrentUser().getUid() + "/date", currentDate);
        subcriptionMap.put("Users/" + mAuth.getCurrentUser().getUid() + "/subscribedTo/" + place_id + "/", placedata);


        subcriptionMap.put("placeNotifications/" + place_id + "/" + notificationKey, notificationData);
        rootRef.updateChildren(subcriptionMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(final DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {

                    com.google.firebase.messaging.FirebaseMessaging.getInstance()
                            .subscribeToTopic(place_id);
                    Toast.makeText(mContext, "Added to your favourites Place List", Toast.LENGTH_SHORT).show();
                    follow.setText("UnFollow");

                    follow.setEnabled(true);

                }
            }
        });

    }

    public void initFirebaseDatabase() {
        //--------------------------------------FIREBASE------------------------------------------//
        //TODO PULL THIS PLACE ID TO A SAFER PLACE
        place_id = getActivity().getIntent().getStringExtra("place_id");
        //for the current place
        mDatabaseo = FirebaseDatabase.getInstance().getReference().child("Place_Users").child(place_id);
        //to check if thy r subscribers first
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();
        mCurrentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser);

        rootRef = FirebaseDatabase.getInstance().getReference();

    }

    private void setUpCounts(String uid) {
        final DatabaseReference photoNode = FirebaseDatabase.getInstance().getReference().child("placePhotos").child(uid);
        DatabaseReference myEvents = FirebaseDatabase.getInstance().getReference().child("Place_Users").child(place_id).child("events");
        myEvents.orderByChild("date_comparable").startAt(GetCurTime.today()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot);
                int count = (int) dataSnapshot.getChildrenCount();
                eventCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        photoNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange1: " + dataSnapshot);
                int count = (int) dataSnapshot.getChildrenCount();

                    photoCount.setText(String.valueOf(count));
                if (count > 0) {
                    photoCount.setOnClickListener(v -> {
                        Intent i = new Intent(getActivity(), PhotosProgressViewActivity.class);
                        i.putExtra("count", (int) count);
                        i.putExtra("place_id", uid);
                        startActivity(i);
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void setUpProfileSection(String currentUser) {
        mDatabaseo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              try {
                  RequestOptions requestOptions = new RequestOptions()
                          .centerCrop()
                          .placeholder(R.drawable.ptest);
             try {
                 Glide.with(getContext()).load(dataSnapshot.child("image").getValue().toString()).apply(requestOptions).listener(new RequestListener<Drawable>() {
                     @Override
                     public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                         pl.setImageResource(R.drawable.ptest);
                         mProgress.setVisibility(View.GONE);
                         Toast.makeText(getContext(), "Sorry could not load photo", Toast.LENGTH_SHORT).show();

                         return false;
                     }

                     @Override
                     public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                         mProgress.setVisibility(View.GONE);
                         return false;
                     }
                 })
                         .into(pl);
             }catch (Exception e){

             }
              }catch (IllegalArgumentException ex){
                  Log.d(TAG, "onDataChange: IllegalArgumentException "+ex.getMessage());
              }




                Log.d(TAG, "onDataChange: " + dataSnapshot);
try{
    descriptionText.setText(dataSnapshot.child("description").getValue().toString());
}catch (NullPointerException e){

}

                try{
                    website.setText(dataSnapshot.child("website").getValue().toString());
                    website.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                          
                           String websurl=website.getText().toString();
                            Log.d(TAG, "onClick: onclick "+websurl);
                           if (Handy.isValidURL(websurl)){
                               Log.d(TAG, "onClick: website valid");
                               goToSite(websurl);
                           }else {
                               Snackbar.make(linearLayout,"Website is not defined properly",Snackbar.LENGTH_SHORT).show();
                           }
                        }
                    });
                }catch (NullPointerException e){

                }
                try {
                    loc.setText(dataSnapshot.child("address").getValue().toString());
                }catch (NullPointerException e){
try {
    loc.setText(dataSnapshot.child("district").getValue().toString());
}catch (NullPointerException e1){

}

loc.setOnClickListener(view->{
    locateCurrentPlace();
});

                }


                try{
                    mail.setText(dataSnapshot.child("public_email").getValue().toString());
                }catch (NullPointerException e){
                    try{
                        mail.setText(dataSnapshot.child("email").getValue().toString());
                    }catch (NullPointerException e1){

                    }

                }

                String emaddre=mail.getText().toString();

                if (Handy.isValidEmail(emaddre)){
                    mail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendEmailToBusiness(emaddre);
                        }
                    });
                }

try {
                    phone.setText(dataSnapshot.child("phone").getValue().toString());
}catch (NullPointerException E){

}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference followers = FirebaseDatabase.getInstance().getReference().child("placeSubscribers").child(currentUser);
        followers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                ccunt.setText(Integer.toString(count));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void goToSite(String websurl) {
        Log.d(TAG, "goToSite: visting website "+websurl);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(websurl));
        if (i.resolveActivity(getActivity().getPackageManager())!=null){
            startActivity(i);
        }else {
            Snackbar.make(linearLayout,"No apps to view website on ur phone",Snackbar.LENGTH_SHORT).show();
        }

    }

    private void sendEmailToBusiness(String emaddre) {
            Log.d(TAG, "sending email to : "+emaddre);
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", emaddre, null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Customer from GetAplot");
        if (emailIntent.resolveActivity(getActivity().getPackageManager())!=null){
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        }else {
            Snackbar.make(linearLayout,"No apps to send emails on ur phone",Snackbar.LENGTH_SHORT).show();
        }



    }
}

