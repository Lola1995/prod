package com.crycetruly.r2d2.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


import java.util.List;

/**
 * Created by ava on 8/23/2017.
 */

public class Event implements Parcelable {
    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
    private static final String TAG = "Event";
    public String cover, name, date, time, desc, place_name, uid;
    public String searchSortString, date_comparable, fitness;
    public List<String> goers;
    private Context mCntext;


    public Event() {
    }

    public Event(String cover, String name, String date, String time, String desc, String place_name, String uid, String searchSortString, String date_comparable, String fitness, List<String> goers, Context mCntext) {
        this.cover = cover;
        this.name = name;
        this.date = date;
        this.time = time;
        this.desc = desc;
        this.place_name = place_name;
        this.uid = uid;
        this.searchSortString = searchSortString;
        this.date_comparable = date_comparable;
        this.fitness = fitness;
        this.goers = goers;
        this.mCntext = mCntext;
    }

    public Event(String cover, String name, String date, String time, String desc, String place_name, String uid, String searchSortString, String date_comparable, List<String> goers, Context mCntext) {
        this.cover = cover;
        this.name = name;
        this.date = date;
        this.time = time;
        this.desc = desc;
        this.place_name = place_name;
        this.uid = uid;
        this.searchSortString = searchSortString;
        this.date_comparable = date_comparable;
        this.goers = goers;
        this.mCntext = mCntext;
    }

    public Event(String cover, String name, String date, String time, String desc, String place_name,
                 String uid, List<String> goers, Context mCntext) {
        this.cover = cover;
        this.name = name;
        this.date = date;
        this.time = time;
        this.desc = desc;
        this.place_name = place_name;
        this.uid = uid;

        this.goers = goers;
        this.mCntext = mCntext;
        Log.d(TAG, "searchser" + searchSortString);
    }

    protected Event(Parcel in) {
        cover = in.readString();
        name = in.readString();
        date = in.readString();
        time = in.readString();
        desc = in.readString();
        place_name = in.readString();
        goers = in.createStringArrayList();
    }

    public static Creator<Event> getCREATOR() {
        return CREATOR;
    }

    public String getFitness() {
        return fitness;
    }

    public void setFitness(String fitness) {
        this.fitness = fitness;
    }

    public String getDate_comparable() {
        return date_comparable;
    }

    public void setDate_comparable(String date_comparable) {
        this.date_comparable = date_comparable;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public List<String> getGoers() {
        return goers;
    }

    public void setGoers(List<String> goers) {
        this.goers = goers;
    }

    @Override
    public String toString() {
        return "Event{" +
                "cover='" + cover + '\'' +
                ", name='" + name + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", description='" + desc + '\'' +
                ", place_name='" + place_name + '\'' +
                ", goers=" + goers +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(cover);
        parcel.writeString(name);
        parcel.writeString(date);
        parcel.writeString(time);
        parcel.writeString(desc);
        parcel.writeString(place_name);
        parcel.writeStringList(goers);
    }
}
