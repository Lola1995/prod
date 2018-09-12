package com.getaplot.getaplot.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.interfaces.ItemClickListener;
import com.getaplot.getaplot.model.Event;
import com.getaplot.getaplot.ui.detail.EventDetailActivity;
import com.getaplot.getaplot.utils.Handy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventHolder> implements Filterable {
    private static final String TAG = "EventsAdapter";
    public List<Event> eventsList_original;
    ItemFilter itemFilter = new ItemFilter();
    private Context mContext;
    private ProgressBar progressBar;
    private List<Event> filteredList = new ArrayList<>();

    public EventsAdapter(List<Event> eventsList_original, Context mContext) {
        this.eventsList_original = eventsList_original;
        this.mContext = mContext;
        this.filteredList = eventsList_original;

    }

    @Override
    public EventHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        try {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_ev, parent, false);

            return new EventHolder(v);
        } catch (Exception e) {
            Log.d(TAG, "onCreateViewHolder: exception in inflaing" + e.getMessage());
        }

        return null;
    }

    @Override
    public void onBindViewHolder(EventHolder holder, int position) {


        try {
            final Event event = filteredList.get(position);
            holder.ev_title.setText(Handy.getTrimmedName(event.getName()));
            RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.ev_test);
            Glide.with(mContext).load(event.getCover()).apply(requestOptions).into(holder.ev_cover);
            holder.ev_place.setText(Handy.getTrimmedName(event.getPlace_name()));

            holder.ev_time.setText(event.getDate());
            holder.ev_desc.setText(Handy.capitalize(event.getDesc()));

            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Events");
            //----------------------------HANDLES ITEM CLICKS ----------------------------------------------
            holder.setItemClickListener(new ItemClickListener() {
                @Override
                public void onItemClick(View view, final int pos) {
                    Log.d(TAG, "onItemClick: " + reference);

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
                                try {
                                    String kid = singleSnap.child("name").getValue().toString();
                                    //String place=dataSnapshot.child("place_name").getValue(String.class);
                                    //  String newkid=kid.concat(place);
                                    String kidsnow = filteredList.get(pos).getName();
                                    if (kidsnow.equals(kid)) {

                                        Log.d(TAG, "res " + "yes");
                                        Log.d(TAG, "onDataChange: kry" + singleSnap.getKey());

                                        Intent i = new Intent(mContext, EventDetailActivity.class);
                                        i.putExtra("event_id", singleSnap.getKey());
                                        try {
                                            mContext.startActivity(i);
                                        } catch (Exception e) {

                                        }


                                    } else {
                                        Log.d(TAG, "res " + "nop");
                                    }
                                } catch (NullPointerException e) {

                                }

                            }
                            Log.d(TAG, "snap: " + dataSnapshot);


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    Intent i = new Intent(mContext, EventDetailActivity.class);


                }
            });
        } catch (Exception e) {
            Log.d(TAG, "onBindViewHolder: +" + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        int co = 0;
        Log.d(TAG, "getItemCount: fltercout");
        //return eventsList_original.size();

        try {


            if (filteredList.size() < 0) {
                co = 0;
            } else {
                co = filteredList.size();
            }

        } catch (NullPointerException e) {

        }
        return co;
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    public class EventHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        TextView ev_title;
        TextView ev_place, ev_time, ev_desc;
        ItemClickListener itemClickListener;
        private ImageView ev_cover;

        public EventHolder(View itemView) {
            super(itemView);
            mView = itemView;
            ev_title = mView.findViewById(R.id.ev_title);
            ev_place = mView.findViewById(R.id.ev_place);
            ev_time = mView.findViewById(R.id.ev_time);
            ev_desc = mView.findViewById(R.id.desc);
            ev_cover = mView.findViewById(R.id.ev_cover);
            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            this.itemClickListener.onItemClick(view, getLayoutPosition());

        }

        public void setItemClickListener(ItemClickListener ic) {
            this.itemClickListener = ic;
        }

    }

    private class ItemFilter extends Filter {
        //--------------------FILTER OUT ITEMS USING THE NAME,DATE,PLACE----------------------------------//
        //RETURN THE ITEMS TO CLICK
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            Log.d(TAG, "performFiltering: ");
            String query = charSequence.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final List<Event> list = eventsList_original;
            final List<Event> reslist = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                //------------------------------CONSTRUCTS SOMETHING LIKE AND AN INDEX THAT USERS CAN SEARCG BY PLACENAME,DESCRIPTION,DATE---------------//
                String str_title = list.get(i).name.concat(list.get(i).place_name)
                        .concat(list.get(i).date).concat(list.get(i).desc);
                if (str_title.toLowerCase().contains(query)) {
                    reslist.add(list.get(i));
                }
            }
            results.values = reslist;
            results.count = reslist.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            Log.d(TAG, "publishResults: ");
            filteredList = (List<Event>) filterResults.values;

            if (filteredList.size() < 1) {
                Toast.makeText(mContext, "No matches in your search", Toast.LENGTH_SHORT).show();
//        progressBar.setVisibility(View.GONE);
            }
            notifyDataSetChanged();
        }
    }

}
