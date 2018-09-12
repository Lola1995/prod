package com.getaplot.getaplot.adapters;/*
package com.getaplot.getaplot.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Comment;
import com.getaplot.getaplot.model.Status;
import com.getaplot.getaplot.utils.GetCurTime;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

*/
/**
 * Created by Elia on 11/14/2017.
 *//*


public class CommentsAdapter extends ArrayAdapter<Status> {
    private static final String TAG = "CommentsAdapter";
    private LayoutInflater mLayoutInflater;
    private int layoutResource;
    private Context mContext;
    public CommentsAdapter(@NonNull Context context, int resource, @NonNull List<Status> objects) {
        super(context, resource, objects);
        mLayoutInflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource=resource;
        mContext=context;
    }
    View mView;
    TextView comm, comment;
    TextView LikesCount, replyCount;
    ImageView likeImage, replyImage, commentImage;
    DatabaseReference likesDb;
    FirebaseAuth mAuth;
    TextInputEditText replyTextInput;
    ImageView commI;
    ImageView mapView;
    ProgressBar progressBar;
    public CommentsVH(View itemView) {
        super(itemView);
        mView = itemView;
        replyImage = mView.findViewById(R.id.replyimage);
        replyCount = mView.findViewById(R.id.commentsText);
        likeImage = mView.findViewById(R.id.likeacomment);
        LikesCount = mView.findViewById(R.id.likesCount);
        comm = mView.findViewById(R.id.us_nam);
        commentImage = mView.findViewById(R.id.us_comment2);
        mapView = mView.findViewById(R.id.us_comment3);
        likesDb = FirebaseDatabase.getInstance().getReference().child("EventCommentLikes");
        likesDb.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        commI = mView.findViewById(R.id.user_image);
        comment = mView.findViewById(R.id.us_comment);

    }

    public void handlereplyButton(final Context context, final String commentId,
                                  final String event_id, final Comment comment1, final String comment,
                                  final String username) {
        replyImage.setOnClickListener(new View.OnClickListener() {
            EditText editText = new EditText(context);

            @Override
            public void onClick(View view) {
                try {
                    editText.setWidth(1000);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Reply to " + username + "`s comment");
                    try {
                        if (comment1.getType().equals("location")) {
                            builder.setMessage("A location");
                        } else if (comment1.getType().equals("image")) {
                            builder.setMessage("A Photo");
                        } else if (comment1.getType().equals("text")) {
                            builder.setMessage(comment);

                        }
                    } catch (NullPointerException e) {
                        builder.setMessage(comment);
                    }

                    builder.setIcon(R.drawable.ic_comment_reply).setView(editText)
                            .setPositiveButton("Reply", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String replyText = editText.getText().toString();
                                    if (!TextUtils.isEmpty(replyText)) {
                                        if (replyText.length() > 300) {

                                            Toast.makeText(context, "Too long", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        Log.d(TAG, "onClick: Text was not empty");
                                        //------------REPLY TO COMMENT--------------------------------//
                                        Toast.makeText(context, "Posting your reply", Toast.LENGTH_SHORT).show();
                                        commentNow(commentId, replyText, event_id, comment1);

                                    }
                                }
                            }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            return;
                        }
                    }).setCancelable(false)
                            .show();
                } catch (IllegalStateException e) {
                    //fixme the whatver exception cause the user cannot reply again instatntly fix
                    //todo try to ask astack
                    Log.d(TAG, "onLongClick: " + e.getMessage());
                }

            }

            //------------------------REPLY TO OTHER USERS COMMENT----------------------------------------------//
            private void commentNow(String commentId, String replyText, String event_id, Comment comment1) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                        .child("EventCommentsReplies").child(commentId);
                Map<String, Object> commentReplyMap = new HashMap<>();
                commentReplyMap.put("relyText", replyText);
                commentReplyMap.put("event_id", event_id);
                commentReplyMap.put("time", GetCurTime.getCurTime());
                commentReplyMap.put("user", mAuth.getCurrentUser().getUid());
                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("CommentReplyNotifications");
                String pushkey = reference1.push().getKey();
                Map<String, String> map = new HashMap<String, String>();
                map.put("from", mAuth.getCurrentUser().getUid());
                map.put("reply", replyText);
                map.put("comment_id", commentId);
                try {
                    if (!comment1.getUid().equals(mAuth.getCurrentUser().getUid())) {
                        reference1.child(comment1.getUid()).child(pushkey).setValue(map);

                        reference.push().setValue(commentReplyMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Log.d(TAG, "onComplete: Completed");
                                    Toast.makeText(context, "Reply added", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Reply wasnot added", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {

                        reference.push().setValue(commentReplyMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Log.d(TAG, "onComplete: Completed");
                                    Toast.makeText(context, "Reply added", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Reply wasnot added", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } catch (Exception e) {


                    if (!comment1.getUid().equals(mAuth.getCurrentUser().getUid())) {
                        reference1.child(comment1.getUid()).child(pushkey).setValue(map);

                        reference.push().setValue(commentReplyMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Log.d(TAG, "onComplete: Completed");
                                    Toast.makeText(context, "Reply added", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Reply wasnot added", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {

                        reference.push().setValue(commentReplyMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Log.d(TAG, "onComplete: Completed");
                                    Toast.makeText(context, "Reply added", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Reply wasnot added", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }


                }

            }

        });
    }

    //--------------------------------UPDATE THE TEXT ON REPLIES----------------------------------//
    private void upDateNumberOfReplies(String commentId) {
        Log.d(TAG, "upDateNumberOfReplies: fetching count for current comment for comment" + commentId);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().
                child("EventCommentsReplies").child(commentId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "reps: Snap" + dataSnapshot);
                int count = (int) dataSnapshot.getChildrenCount();
                Log.d(TAG, "repscount: The currentCount" + count);
                if (count == 1) {
                    replyCount.setText("1 reply");
                }
                if (!dataSnapshot.exists()) {
                    replyCount.setText("");
                }
                if (count > 1) {
                    replyCount.setText(count + " Replies");
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void setCommentText(String commentText) {
        comment.setText(commentText);
    }

    public void setCommentUser(String commentUser) {

        comm.setText(commentUser);
    }

    public void setCommentUserImage(String imageUrl, Context context) {
        Glide.with(context).load(imageUrl).crossFade().bitmapTransform(new CircleTransform(context)).placeholder(R.drawable.test).into(commI);
    }

    public void setLastCommentedOn(String timestamp) {
        TextView comm = mView.findViewById(R.id.commenttime);
        comm.setText(timestamp);

    }

    public void setLikeStatus(final String commentId) {
        likesDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(commentId).hasChild(mAuth.getCurrentUser().getUid())) {
                    likeImage.setImageResource(R.drawable.ic_thumprimary);
                } else {
                    likeImage.setImageResource(R.drawable.ic_like_comment);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getCommentLikeCount(String commentId) {

        likesDb.child(commentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                if (count == 0) {
                    LikesCount.setText("");
                    likeImage.setImageResource(R.drawable.ic_likecomment);
                }
                LikesCount.setVisibility(View.VISIBLE);
                LikesCount.setText(Integer.toString(count));
                Log.d(TAG, "onDataChange: the count is " + count);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView==null){
            convertView=mLayoutInflater.inflate(layoutResource,parent,false);
            holder=new ViewHolder();

            holder.textView=convertView.findViewById(R.id.status);
            convertView.setTag(holder);
        }else{
            holder= (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(getItem(position).getStatus());
        return convertView;
    }
}
*/
