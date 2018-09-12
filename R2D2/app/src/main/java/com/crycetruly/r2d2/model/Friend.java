package com.crycetruly.r2d2.model;


public class Friend {
    public String name, thumb_image, date, online;

    public Friend() {
    }

    public Friend(String name, String thumb_image, String since, String online) {
        this.name = name;
        this.online = online;
        this.thumb_image = thumb_image;
        this.date = since;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
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
}
