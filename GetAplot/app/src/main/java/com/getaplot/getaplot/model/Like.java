package com.getaplot.getaplot.model;

/**
 * Created by Elia on 1/8/2018.
 */

public class Like {
    private String user, time;

    public Like() {
    }

    public Like(String user, String time) {
        this.user = user;
        this.time = time;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
