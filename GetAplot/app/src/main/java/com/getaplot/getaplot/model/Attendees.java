package com.getaplot.getaplot.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Elia on 9/21/2017.
 */

public class Attendees implements Parcelable {
    public static final Creator<Attendees> CREATOR = new Creator<Attendees>() {
        @Override
        public Attendees createFromParcel(Parcel in) {
            return new Attendees(in);
        }

        @Override
        public Attendees[] newArray(int size) {
            return new Attendees[size];
        }
    };
    private String uid;
    private String time;
    private String user_id;

    public Attendees() {
    }

    public Attendees(String uid, String user_id, String time) {
        this.uid = uid;
        this.time = time;
        this.user_id = user_id;
    }

    protected Attendees(Parcel in) {
    }

    public static Creator<Attendees> getCREATOR() {
        return CREATOR;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Attendees{" +
                "uid='" + uid + '\'' +
                '}';
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
