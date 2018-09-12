package com.getaplot.getaplot.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Heart;
import com.getaplot.getaplot.model.InfoHive;
import com.getaplot.getaplot.ui.HiveReactionsActivity;
import com.getaplot.getaplot.ui.PlaceActivity;
import com.getaplot.getaplot.ui.detail.PostDetailActivity;
import com.getaplot.getaplot.utils.GetShortTimeAgo;
import com.getaplot.getaplot.utils.Handy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.InfoHiveVH> {
    private static final String TAG = "FeedAdapter";
    private FirebaseAuth auth;
    private Context context;
    private List<InfoHive> feedList;
DatabaseReference InfoReactionsDb;
Activity activity;
    public FeedAdapter(Context context,Activity activity, List<InfoHive> m) {
        this.context = context;
        this.feedList = m;
        this.activity=activity;
        auth = FirebaseAuth.getInstance();

        InfoReactionsDb = FirebaseDatabase.getInstance().getReference().child("infoHiveComments");
    }

    @Override
    public InfoHiveVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
// layout called R.layout.message for each item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_post_card, parent, false);

        return new InfoHiveVH(view);
    }

    @Override
    public void onBindViewHolder(final InfoHiveVH viewHolder, final int position) {
        final String place_name= feedList.get(position).getUid();

        final String[] key = {""};

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("placeInfoPosts");


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
                    try {
                        String kid = singleSnap.child("post_text").getValue().toString();
                        //String place=dataSnapshot.child("place_name").getValue(String.class);
                        //  String newkid=kid.concat(place);
                        String kidsnow = feedList.get(position).getPost_text();
                        if (kidsnow.equals(kid)) {

                          key[0] =singleSnap.getKey();

                            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("postLikes").child(key[0]);

                            ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
try {
    if (dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
        viewHolder.mHeartWhite.setBackgroundResource(R.drawable.ic_thumprimary);
    } else {
        viewHolder.mHeartWhite.setBackgroundResource(R.drawable.ic_like_comment);
    }
}catch (NullPointerException ee){
    Log.d(TAG, "onDataChange: NullPointerException "+ee.getMessage());
    ee.printStackTrace();
}
                                    int count = (int) dataSnapshot.getChildrenCount();
                                    if (count == 0) {
                                        viewHolder.likecount.setText("");
                                    } else if (count == 1) {

                                        viewHolder.likecount.setText(String.valueOf(count).concat(" Like"));
//                            viewHolder.likecount.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    Log.d(TAG, "onLongClick: ");
//                                    Intent intent = new Intent(getContext(), HiveReactionsLikesActivity.class);
//                                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                                    getActivity().getSupportFragmentManager().popBackStack();
//                                    intent.putExtra("key", key);
//                                    startActivity(intent);
//
//                                }
//                            });
                                    } else {
                                        viewHolder.likecount.setText(String.valueOf(count).concat(" Likes"));
//                            viewHolder.likecount.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    Intent intent = new Intent(getContext(), HiveReactionsLikesActivity.class);
//                                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                                    getActivity().getSupportFragmentManager().popBackStack();
//                                    intent.putExtra("key", key);
//                                    startActivity(intent);
//                                }
//                            });

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            viewHolder.mHeartWhite.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                                Log.d(TAG, "onDataChange: liked");
                                                ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(context, "post unliked", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                Map map = new HashMap<>();
                                                map.put("user", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                map.put("at", Handy.fitnessNumber());

                                                ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        Toast.makeText(context, "Like added", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });


                            //---------------------SHOULD UPDATE TEXTvIEW--------------------//
                            InfoReactionsDb.child(key[0]).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final int count = (int) dataSnapshot.getChildrenCount();
                                    Log.d(TAG, "onDataChange: the count is " + count);

                                    if (count != 1) {
                                        viewHolder.hivereplycount.setText(String.valueOf(count).concat(" Comments"));
                                        viewHolder.hivereplycount.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(context, HiveReactionsActivity.class);
                                                intent.putExtra("hivereplykey", key[0]);
                                                context. startActivity(intent);
                                            }
                                        });
                                    }
                                    if (count == 1) {
                                        viewHolder.hivereplycount.setText(String.valueOf(count).concat(" Comment"));
                                        viewHolder.hivereplycount.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(context, HiveReactionsActivity.class);
                                                intent.putExtra("hivereplykey", key[0]);
                                                context.startActivity(intent);
                                            }
                                        });
                                    }


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });




                        } else {
                            Log.d(TAG, "res " + "nop");
                        }
                    } catch (NullPointerException e) {

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Pair[] pairs = new Pair[1];

                pairs[0] = new Pair<View, String>(viewHolder.image, "image_url");

                Intent i = new Intent(context, PostDetailActivity.class);
                i.putExtra("post_id", key[0]);
                i.putExtra("image_url", feedList.get(position).getImage());
                i.putExtra("place_id", place_name);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(activity, pairs);
                   context. startActivity(i, activityOptions.toBundle());

                }else{
                    context. startActivity(i);
                }

            }
        });









        DatabaseReference places = FirebaseDatabase.getInstance().getReference().child("Place_Users").child(place_name);
        places.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                try {
                    Glide.with(context).load(dataSnapshot.child("image").getValue()
                            .toString()).into(viewHolder.posterpic);
                } catch (IllegalArgumentException e) {

                }
try {

    viewHolder.district.setText(
           dataSnapshot.child("district")
                    .getValue()
            .toString());
}catch (NullPointerException e){


}

                viewHolder.setHivePlace(Handy.getTrimmedName(dataSnapshot.child("name").getValue()
                        .toString()));
                viewHolder.setEventDescription(feedList.get(position).getPost_text());
                viewHolder.setHiveTime(feedList.get(position).getPosted(),context);
                viewHolder.setEventCover(feedList.get(position).getImage(),context);
//                progressBar.setVisibility(View.GONE);
//                swipeRefreshLayout.setEnabled(true);


                viewHolder.mm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, HiveReactionsActivity.class);
                        intent.putExtra("hivereplykey", key[0]);
                        context.startActivity(intent);
                    }
                });


                viewHolder.ev_place.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(context, PlaceActivity.class);
                        //.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        i.putExtra("place_name", dataSnapshot.child("name").getValue().toString());
                        i.putExtra("description", dataSnapshot.child("description").getValue().toString());
                        i.putExtra("imageUrl", dataSnapshot.child("image").getValue().toString());
                      //  getActivity().getSupportFragmentManager().popBackStack();
                        i.putExtra("place_id", dataSnapshot.getKey());
                        context.startActivity(i);
                    }
                });
//                   todo find other  ways of letting user view the photo     viewHolder.image.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                Intent i = new Intent(getContext(), EnlargeImageView.class);
//                                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                                getActivity().getSupportFragmentManager().popBackStack();
//                                i.putExtra("image_url", model.getImage());
//                                startActivity(i);
//                            }
//                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    @Override
    public int getItemCount() {
        if (feedList != null) {
            return feedList.size();
        }
        return 0;
    }


    public static class InfoHiveVH extends RecyclerView.ViewHolder {
        View mView;
        TextView text, likecount,district;
        TextView ev_place;
        ImageView image;
        TextView hivereplycount;
        TextInputEditText reply;
        CircleImageView posterpic;
        ImageView mm, mHeartWhite;
        private ImageView ev_cover;
        private Heart mHeart;
        private GestureDetector mGestureDetector;

        public InfoHiveVH(View itemView) {
            super(itemView);
            mView = itemView;
            hivereplycount = mView.findViewById(R.id.hivereplycount);
            image = mView.findViewById(R.id.imagetarget);
            posterpic = mView.findViewById(R.id.posterpic);
            mHeartWhite = mView.findViewById(R.id.image_heart);
            mm = mView.findViewById(R.id.imm);
            district=mView.findViewById(R.id.district);
            likecount = mView.findViewById(R.id.likecount);
        }

        public void setEventDescription(String title) {
            text = mView.findViewById(R.id.text);
            text.setText(Handy.capitalize(title));

            Linkify.addLinks(text,Linkify.ALL);
        }

        public void setHivePlace(String place) {
            ev_place = mView.findViewById(R.id.ev_place);
            ev_place.setText(place);
        }

        public void setHiveTime(long time, Context context) {
            TextView ev_time = mView.findViewById(R.id.hivetime);
            ev_time.setText(GetShortTimeAgo.getTimeAgo(time, context));
        }

        public void setEventCover(String cover, Context context) {

            try {
                RequestOptions requestOptions = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_feed);
                Glide.with(context)
                        .load(cover).apply(requestOptions)
                        .thumbnail(0.5f)
                        .into(image);
            } catch (Exception e) {
                Log.d(TAG, "setEventCover: outof memory");
            }
        }
    }

}
