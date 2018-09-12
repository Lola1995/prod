package com.crycetruly.r2d2.model;

/**
 * Created by ava on 8/26/2017.
 */

public class Comment {
    private String text, username, user_picture, uid, type, placeId,video_url;
    private long lastcommentedon;

    public Comment() {
    }

    public Comment(String text, String username, String user_picture, String uid, String placeId, long lastcommentedon) {
        this.text = text;
        this.username = username;
        this.user_picture = user_picture;
        this.uid = uid;

        this.placeId = placeId;
        this.lastcommentedon = lastcommentedon;
    }

    public Comment(String text, String username, String user_picture, String uid, String type, String placeId, long lastcommentedon) {
        this.text = text;
        this.username = username;
        this.user_picture = user_picture;
        this.uid = uid;
        this.type = type;
        this.placeId = placeId;
        this.lastcommentedon = lastcommentedon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getLastcommentedon() {
        return lastcommentedon;
    }

    public void setLastcommentedon(long lastcommentedon) {
        this.lastcommentedon = lastcommentedon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_picture() {
        return user_picture;
    }

    public void setUser_picture(String user_picture) {
        this.user_picture = user_picture;
    }
}
