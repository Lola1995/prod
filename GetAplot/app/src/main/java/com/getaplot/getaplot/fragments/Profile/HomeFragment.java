package com.getaplot.getaplot.fragments.Profile;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getaplot.getaplot.R;
import com.getaplot.getaplot.ui.PhotosProgressViewActivity;
import com.getaplot.getaplot.utils.GetCurTime;
import com.getaplot.getaplot.utils.Handy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    String user_id;
    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn, mDeclineBtn;
    private DatabaseReference mUsersDatabase;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase, otherUserRecievedRequest;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrent_user;
    private String mCurrent_state;
    private String image;
    private String display_name;
    private Context mContext;
    private TextView name;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private DatabaseReference statusRef, reference;
    private RelativeLayout main;
    private Boolean hasLiked = false;
    private TextView userName;
    private FirebaseAuth mAuth;
    private Context context;
    private LinearLayout photolayout;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView friendscount = view.findViewById(R.id.friendcount);
        photolayout=view.findViewById(R.id.photolayout);
        name = view.findViewById(R.id.profileName);
        mContext = getContext();
        userName = view.findViewById(R.id.userName);
        user_id = getActivity().getIntent().getStringExtra("user_id");
        init();
        initViews(view);
        TextView t = view.findViewById(R.id.photoCount);
        TextView tv = view.findViewById(R.id.placecount);

        placephotocount(t, tv);


        mCurrent_state = "not_friends";

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setMessage("Please wait while we load the user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    final String n = dataSnapshot.child("userName").getValue().toString();
                    if (!n.equals("")) {
                        userName.setText("@".concat(n));
                    }
                    Log.d(TAG, "userNames " + userName);
                } catch (NullPointerException e) {

                }
                try {
                    String status = dataSnapshot.child("status").getValue().toString();
                    mProfileStatus.setText(status);

                } catch (NullPointerException E) {
                    Log.d(TAG, "onDataChange: Loading status issue");
                }


                if (mCurrent_user.getUid().equals(user_id)) {

                    mDeclineBtn.setEnabled(false);
                    mDeclineBtn.setVisibility(View.INVISIBLE);

                    mProfileSendReqBtn.setEnabled(false);
                    mProfileSendReqBtn.setVisibility(View.INVISIBLE);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //--------------- FRIENDS LIST / REQUEST FEATURE -----

        mFriendReqDatabase.child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(user_id)) {

                    String req_type = dataSnapshot.child(user_id).child("request_type")
                            .getValue().toString();

                    if (req_type.equals("received")) {

                        mCurrent_state = "req_received";
                        mProfileSendReqBtn.setText("Accept Friend Request");

                        mDeclineBtn.setVisibility(View.VISIBLE);
                        mDeclineBtn.setEnabled(true);


                    } else if (req_type.equals("sent")) {

                        mCurrent_state = "req_sent";
                        mProfileSendReqBtn.setText("Cancel Friend Request");

                        mDeclineBtn.setVisibility(View.INVISIBLE);
                        mDeclineBtn.setEnabled(false);

                    }

                    try{
                        mProgressDialog.dismiss();
                    }catch (NullPointerException e){

                    }


                } else {


                    mFriendDatabase.child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(user_id)) {

                                mCurrent_state = "friends";
                                mProfileSendReqBtn.setText("Unfriend this Person");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }

try{
                                mProgressDialog.dismiss();
}catch (NullPointerException e){

}

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                            try{
                                mProgressDialog.dismiss();
                            }catch (NullPointerException e){

                            }

                        }
                    });

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Request manager button clicked");

                mProfileSendReqBtn.setEnabled(false);

                // --------------- NOT FRIENDS STATE ------------

                if (mCurrent_state.equals("not_friends")) {

                    Log.d(TAG, "onClick: Notfiends");
                    Log.d(TAG, "onClick: sending request");

                    mProfileSendReqBtn.setText("SENDING REQUEST");
                    DatabaseReference newNotificationref = mRootRef.child("Notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "request");
                    notificationData.put("who", user_id);

                    Map<String, Object> requestMap = new HashMap<>();
                    requestMap.put("FriendRequests/" + mCurrent_user.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("FriendRequests/" + mCurrent_user.getUid() + "/" + user_id + "/user_id", user_id);
                    requestMap.put("FriendRequests/" + mCurrent_user.getUid() + "/" + user_id + "/time", GetCurTime.getCurTime());
                    requestMap.put("FriendRequests/" + user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("FriendRequests/" + user_id + "/" + mCurrent_user.getUid() + "/sent", GetCurTime.getCurTime());
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationId, notificationData);
                    Map<String, Serializable> map = new HashMap<String, Serializable>();
                    map.put("time", GetCurTime.getCurTime());
                    map.put("fitnessNum", Handy.fitnessNumber());
                    map.put("sent", GetCurTime.getCurTime());
                    otherUserRecievedRequest.child(mAuth.getCurrentUser().getUid()).setValue(map);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Toast.makeText(mContext, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                            } else {
                                Log.d(TAG, "onComplete: Request sent");

                                mCurrent_state = "req_sent";
                                Log.d(TAG, "onComplete: state sent");
                                mProfileSendReqBtn.setText("Cancel Friend Request");
                                Toast.makeText(mContext, "Request Sent", Toast.LENGTH_SHORT).show();


                            }

                            mProfileSendReqBtn.setEnabled(true);


                        }
                    });

                }


                // - -------------- CANCEL REQUEST STATE ------------

                if (mCurrent_state.equals("req_sent")) {
                    mProfileSendReqBtn.setText("CANCELING REQUEST");
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            otherUserRecievedRequest.child(mAuth.getCurrentUser().getUid()).setValue(null);
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(mContext, "Cancelled", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onSuccess: cancelled");

                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);


                                }
                            });

                        }
                    });

                }


                // ------------ REQ RECEIVED STATE ----------

                if (mCurrent_state.equals("req_received")) {
                    mDeclineBtn.setEnabled(true);
                    mDeclineBtn.setVisibility(View.VISIBLE);
                    mProfileSendReqBtn.setText("ACCEPTING REQUEST");
                    DatabaseReference newNotificationref = mRootRef.child("RequestAcceptNotifications")
                            .child(mCurrent_user.getUid()).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "requestaccepptance");
                    notificationData.put("name", mAuth.getCurrentUser().getDisplayName());
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map<String, Object> friendsMap = new HashMap<>();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/fitnessNum", Handy.fitnessNumber());
                    friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/fitnessNum", Handy.fitnessNumber());


                    friendsMap.put("FriendRequests/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put("FriendRequests/" + user_id + "/" + mCurrent_user.getUid(), null);
                    friendsMap.put("RequestAcceptNotifications/" + user_id + "/" + newNotificationId, notificationData);
                    mRootRef.child("RecievedRequests").child(mAuth.getCurrentUser().getUid()).child(user_id).setValue(null);
                    otherUserRecievedRequest.child(mAuth.getCurrentUser().getUid()).setValue(null);
                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if (databaseError == null) {

                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileSendReqBtn.setText("Unfriend this Person");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();


                            }

                        }
                    });


                }
                //-----------------------DECLINE FRIENREQUEST---------------------------------//
                // ------------ UNFRIENDS ---------

                if (mCurrent_state.equals("friends")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("This will remove this person from your friends List and you will talk to them no more,Are you sure you want to proceed?");
                    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mProfileSendReqBtn.setText("Unfriending this person");
//mProfileSendReqBtn.setText("UNFRIENDING THIS PERSON");
                            Log.d(TAG, "onClick: We were friends before");
                            Map<String, Object> unfriendMap = new HashMap<>();
                            unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                            unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);

                            mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                                    if (databaseError == null) {

                                        mCurrent_state = "not_friends";
                                        mProfileSendReqBtn.setText("Send Friend Request");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    } else {

                                        String error = databaseError.getMessage();

                                        Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();

                                    }

                                    mProfileSendReqBtn.setEnabled(true);

                                }
                            });
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            return;
                        }
                    }).show();


                }


            }


        });


        //decline test

        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Declining user request", Toast.LENGTH_SHORT).show();
                mDeclineBtn.setEnabled(false);

                Map<String, Object> friendsMap = new HashMap<>();
                friendsMap.put("FriendRequests/" + mCurrent_user.getUid() + "/" + user_id, null);
                friendsMap.put("FriendRequests/" + user_id + "/" + mCurrent_user.getUid(), null);

                otherUserRecievedRequest.child(mAuth.getCurrentUser().getUid()).setValue(null);
                mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                        if (databaseError == null) {
                            mDeclineBtn.setVisibility(View.INVISIBLE);
                            mProfileSendReqBtn.setEnabled(true);
                            mCurrent_state = "not_friends";
                            mProfileSendReqBtn.setText("Send FriendRequest");

                            mDeclineBtn.setVisibility(View.INVISIBLE);
                            mDeclineBtn.setEnabled(false);

                        } else {

                            String error = databaseError.getMessage();

                            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();


                        }

                    }
                });

            }
        });


        return view;
    }

    private void placephotocount(final TextView t, final TextView tv) {
        Log.d(TAG, "placephotocount: ");
        DatabaseReference ph = FirebaseDatabase.getInstance().getReference().child("userPhotos")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        final DatabaseReference pla = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("subscribedTo");
        pla.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                tv.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ph.addListenerForSingleValueEvent(new ValueEventListener() {
                                              @Override
                                              public void onDataChange(DataSnapshot dataSnapshot) {
                                                  int count = (int) dataSnapshot.getChildrenCount();
                                                  t.setText(String.valueOf(count));

                                                  mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                      @Override
                                                      public void onDataChange(DataSnapshot dataSnapshot) {
if (count>0) {


    if (dataSnapshot.hasChild(user_id)) {
        photolayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), PhotosProgressViewActivity.class);
                i.putExtra("count", (int) count);
                i.putExtra("user_id", user_id);
                i.putExtra("from", "user");
                startActivity(i);

                Log.d(TAG, "onClick: ");
            }
        });

    }


}                           }

                                                      @Override
                                                      public void onCancelled(DatabaseError databaseError) {

                                                      }
                                                  });

                                              }

                                              @Override
                                              public void onCancelled(DatabaseError databaseError) {

                                              }


                                          }

        );

    }
            private void updateFriendsView(final TextView friendscount, String user_id) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Friends")
                .child(user_id);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    friendscount.setText("0");
                } else {
                    Log.d(TAG, "onDataChange: friends:" + dataSnapshot.getChildrenCount());
                    int count = (int) dataSnapshot.getChildrenCount();
                    String text = String.valueOf(count);
                    if (count == 1) {
                        friendscount.setText(text.concat(""));
                    } else if (count > 1) {
                        friendscount.setText(text.concat(""));
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initViews(View v) {
        mProfileStatus = v.findViewById(R.id.profile_status);
        mProfileSendReqBtn = v.findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = v.findViewById(R.id.profile_decline_btn);

        TextView textView = v.findViewById(R.id.friends);
        updateFriendsView(textView, user_id);


    }

    private void init() {
        Log.d(TAG, "init: initialising firebase");
        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        otherUserRecievedRequest = mRootRef.child("RecievedRequests").child(user_id);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mCurrent_user = mAuth.getCurrentUser();
    }

}
