package com.getaplot.getaplot.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Message;
import com.getaplot.getaplot.ui.EnlargeImageView;
import com.getaplot.getaplot.utils.GetCurTime;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;
    private static final String TAG = "MessagesAdapter";
    private FirebaseAuth auth;
    private Context context;
    private List<Message> mMessageList;

    public MessagesAdapter(Context context, List<Message> mMessageList) {
        this.context = context;
        this.mMessageList = mMessageList;
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_ME:
                View viewChatMine = layoutInflater.inflate(R.layout.chat_me_layout, parent, false);
                viewHolder = new MyChatViewHolder(viewChatMine);
                break;
            case VIEW_TYPE_OTHER:
                View viewChatOther = layoutInflater.inflate(R.layout.chat_other_single, parent, false);
                viewHolder = new OtherChatViewHolder(viewChatOther);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        auth = FirebaseAuth.getInstance();
        Message c = mMessageList.get(position);

        String from_user = c.getFrom();
        if (from_user.equals(auth.getCurrentUser().getUid())) {
            try {
                configureMyChatViewHolder((MyChatViewHolder) holder, position);
            } catch (Exception e) {
                Log.d(TAG, "onBindViewHolder: Error " + e.getMessage());
            }

        } else {
            configureOtherChatViewHolder((OtherChatViewHolder) holder, position);
        }

//todo getsubstrings of names

    }

    private void configureMyChatViewHolder(final MyChatViewHolder myChatViewHolder, int position) {
        final Message c = mMessageList.get(position);


        String type = c.getType();
        if (type.equals("image")) {

            myChatViewHolder.imageViewLayout.setVisibility(View.VISIBLE);
            myChatViewHolder.txtChatMessage.setVisibility(View.GONE);

            Glide.with(context)
                    .load(c.getMessage())
                    .thumbnail(0.5f)
                    .into(myChatViewHolder.imageViewLayout);

            myChatViewHolder.imageViewLayout.setOnClickListener(view -> {
                Intent i = new Intent(context, EnlargeImageView.class);

                i.putExtra("image_url", c.getMessage());
                context.startActivity(i);
            });
            Log.d(TAG, "mesonow " + c.getMessage());

        }
        if (type.equals("text")) {
            myChatViewHolder.imageViewLayout.setVisibility(View.GONE);
            myChatViewHolder.txtChatMessage.setVisibility(View.VISIBLE);
            myChatViewHolder.txtChatMessage.setText(c.getMessage());


        }


        CircleImageView user;
        //TODO IMAGES HERE
        Log.d(TAG, "configureMyChatViewHolder: " + c);
        //myChatViewHolder.txtChatMessage.setText(chat.getMessage());

        if ((c.getSendDate().substring(0, 12)).equals(GetCurTime.todayDate())) {
            myChatViewHolder.txttime.setText(String.format("today at%s", GetCurTime.toDateTime(c.getTime()).substring(10)));

        } else {
            myChatViewHolder.txttime.setText(GetCurTime.toDateTime(c.getTime()));

        }


        //DELETES----------------------------//
    }

    private void configureOtherChatViewHolder(final OtherChatViewHolder otherChatViewHolder, int position) {
        final Message chat = mMessageList.get(position);

        String from_user = chat.getFrom();
        if (!from_user.equals(auth.getCurrentUser().getUid())) {
            DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        String name = dataSnapshot.child("name").getValue().toString();
                        String image = dataSnapshot.child("image").getValue().toString();
                        Glide.with(context).load(image).into(otherChatViewHolder.user);
                    } catch (Exception e) {
                        Log.d(TAG, "onBindViewHolder: Error " + e.getMessage());
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        String type = chat.getType();
        if (type.equals("image")) {
            otherChatViewHolder.imageViewLayout.setVisibility(View.VISIBLE);
            Log.d(TAG, "configureMyChatViewHolder: imag");
            otherChatViewHolder.txtChatMessage.setVisibility(View.GONE);


            Glide.with(context)
                    .load(chat.getMessage())

                    .thumbnail(0.5f)
//                        .placeholder(R.drawable.)
                    .into(otherChatViewHolder.imageViewLayout);
            otherChatViewHolder.imageViewLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, EnlargeImageView.class);
                    i.putExtra("image_url", chat.getMessage());
                    context.startActivity(i);
                }
            });

        }
        if (type.equals("text")) {
            otherChatViewHolder.imageViewLayout.setVisibility(View.GONE);
            otherChatViewHolder.txtChatMessage.setVisibility(View.VISIBLE);
            otherChatViewHolder.txtChatMessage.setText(chat.getMessage());

            Log.d(TAG, "configureMyChatViewHolder: textmsg");
        }
        if ((chat.getSendDate().substring(0, 12)).equals(GetCurTime.todayDate())) {
            otherChatViewHolder.txttime.setText(String.format("today at%s", GetCurTime.toDateTime(chat.getTime()).substring(10)));

        } else {
            otherChatViewHolder.txttime.setText(GetCurTime.toDateTime(chat.getTime()));

        }

    }


    @Override
    public int getItemCount() {
        if (mMessageList != null) {
            return mMessageList.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        Message c = mMessageList.get(position);
        String from_user = c.getFrom();
        try {
            if (!from_user.equals("")) {
                if (from_user.equals(auth.getCurrentUser().getUid())) {
                    return VIEW_TYPE_ME;
                } else {
                    return VIEW_TYPE_OTHER;
                }
            } else {
                return 0;
            }
        } catch (NullPointerException e) {

        }
        return 0;

    }

    private static class MyChatViewHolder extends RecyclerView.ViewHolder {
        CircleImageView user;
        ImageView imageViewLayout;
        ImageView mapView;
        ProgressBar progressBar;
        private TextView txtChatMessage, txtUserAlphabet, txttime;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            txtChatMessage = itemView.findViewById(R.id.message_text_layout);
            Linkify.addLinks(txtChatMessage,Linkify.ALL);
            txttime = itemView.findViewById(R.id.message_time_layout);
            imageViewLayout = itemView.findViewById(R.id.message_text_layout2);

        }
    }

    private static class OtherChatViewHolder extends RecyclerView.ViewHolder {
        CircleImageView user;
        ImageView imageViewLayout;
        private TextView txtChatMessage;
        private TextView txttime;

        public OtherChatViewHolder(View itemView) {
            super(itemView);
            txtChatMessage = itemView.findViewById(R.id.message_text_layout);
            Linkify.addLinks(txtChatMessage,Linkify.ALL);
            txttime = itemView.findViewById(R.id.message_time_layout);
            imageViewLayout = itemView.findViewById(R.id.message_text_layout2);

        }
    }
}
