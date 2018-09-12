package com.getaplot.getaplot.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Likes implements Parcelable {
    public static final Creator<Likes> CREATOR = new Creator<Likes>() {
        @Override
        public Likes createFromParcel(Parcel in) {
            return new Likes(in);
        }

        @Override
        public Likes[] newArray(int size) {
            return new Likes[size];
        }
    };
    private String uid, time, event_id;
    private List<Likes> lists;

    public Likes() {
    }

    public Likes(String uid, String time, String event_id, List<Likes> lists) {
        this.uid = uid;
        this.time = time;
        this.event_id = event_id;
        this.lists = lists;
    }

    protected Likes(Parcel in) {
        uid = in.readString();
        time = in.readString();
        event_id = in.readString();
        lists = in.createTypedArrayList(Likes.CREATOR);
    }

    public static Creator<Likes> getCREATOR() {
        return CREATOR;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public List<Likes> getLists() {
        return lists;
    }

    public void setLists(List<Likes> lists) {
        this.lists = lists;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(time);
        parcel.writeString(event_id);
        parcel.writeTypedList(lists);
    }

    @Override
    public String toString() {
        return "Likes{" +
                "uid='" + uid + '\'' +
                ", time='" + time + '\'' +
                ", event_id='" + event_id + '\'' +
                ", lists=" + lists +
                '}';
    }
}
