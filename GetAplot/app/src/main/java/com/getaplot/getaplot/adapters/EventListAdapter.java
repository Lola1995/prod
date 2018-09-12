package com.getaplot.getaplot.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Event;

import java.util.List;


/**
 * Created by User on 9/17/2017.
 */

public class EventListAdapter extends ArrayAdapter<Event> {

    private static final String TAG = "EventListAdapter";


    private LayoutInflater mInflater;
    private List<Event> mEvents = null;
    private int layoutResource;
    private Context mContext;

    public EventListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Event> objects) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.mEvents = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.name = convertView.findViewById(R.id.ev_title);
            holder.description = convertView.findViewById(R.id.desc);
            holder.cover = convertView.findViewById(R.id.ev_cover);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(getItem(position).getName());
        holder.description.setText(getItem(position).getDesc());
        RequestOptions requestOptions=new RequestOptions().placeholder(R.drawable.ev_test);
        Glide.with(mContext).load(getItem(position).getCover()).apply(requestOptions).into(holder.cover);
        return convertView;
    }

    private static class ViewHolder {
        TextView name, description, date, place_name;
        ImageView cover;
    }
}

























