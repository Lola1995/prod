package com.crycetruly.r2d2.model;


public class Request {
    private static String user_id;
    private String image;
    private String sent;
    private String name;
    private String request_type;
    private String time;

    public Request(String image, String sent, String name, String request_type, String user_id, String time) {
        this.image = image;
        this.sent = sent;
        this.request_type = request_type;
        this.name = name;
        Request.user_id = user_id;
        this.time = time;
    }


    public Request() {
    }

    public static String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        Request.user_id = user_id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
