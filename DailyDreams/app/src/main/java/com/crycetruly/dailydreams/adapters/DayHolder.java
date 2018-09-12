package com.crycetruly.dailydreams.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crycetruly.dailydreams.R;
import com.crycetruly.dailydreams.models.Day;
import com.squareup.picasso.Picasso;

import java.util.List;


public class DayHolder extends RecyclerView.Adapter<DayHolder.SingleDayHolder>{
private Context context;
private List<Day> daysList;

    public DayHolder(Context context, List<Day> daysList) {
        this.context = context;
        this.daysList = daysList;
    }

    @NonNull
    @Override
    public SingleDayHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
       View view= LayoutInflater.from(context).
               inflate(R.layout.single_item_view,viewGroup,false);
        return new SingleDayHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SingleDayHolder singleDayHolder, int i) { Picasso.get().load(daysList.get(i).getFeeling()).into(singleDayHolder.feeling);
    singleDayHolder.story.setText(daysList.get(i).getStory());
    singleDayHolder.date.setText(daysList.get(i).getTheDay());
    }

    @Override
    public int getItemCount() {
        return daysList.size();
    }

    public static class SingleDayHolder extends RecyclerView.ViewHolder{
        private View mView;
        private TextView story,date;
        ImageView feeling;
        public SingleDayHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;

            story=mView.findViewById(R.id.story);
            date=mView.findViewById(R.id.date);
            feeling=mView.findViewById(R.id.fellingrepimage);
        }
    }
}
