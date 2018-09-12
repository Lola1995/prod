package com.getaplot.getaplot.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.getaplot.getaplot.R;
import com.getaplot.getaplot.model.Status;

import java.util.List;

/**
 * Created by Elia on 11/14/2017.
 */

public class StatusListAdapter extends ArrayAdapter<Status> {
    private static final String TAG = "StatusListAdapter";
    private LayoutInflater mLayoutInflater;
    private int layoutResource;
    private Context mContext;

    public StatusListAdapter(@NonNull Context context, int resource, @NonNull List<Status> objects) {
        super(context, resource, objects);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.textView = convertView.findViewById(R.id.status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(getItem(position).getStatus());
        return convertView;
    }

    private static class ViewHolder {
        TextView textView;

    }
}
