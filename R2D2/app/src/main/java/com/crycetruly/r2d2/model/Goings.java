package com.crycetruly.r2d2.model;

/**
 * Created by Elia on 9/1/2017.
 */

public class Goings {
    public String user_id;

    public Goings() {
    }

    public Goings(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "Goings{" +
                "user_id='" + user_id + '\'' +
                '}';
    }
}
