package com.getaplot.getaplot.model;

/**
 * Created by Elia on 9/21/2017.
 */

public class ExLike {
    private String uid;
    private String user_id;
    private long time;

    public ExLike() {
    }

    public ExLike(String uid, String user_id, long time) {
        this.uid = uid;
        this.user_id = user_id;
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}