package com.getaplot.getaplot.model;

public class User {
    public String name, status, image, thumb_image, userName;
    public Boolean isPlace;

    public User() {

    }

    public User(String name, String status, String image, String thumb_image, Boolean isPlace) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.thumb_image = thumb_image;
        this.isPlace = isPlace;
    }

    public User(String name, String status, String image, String thumb_image, String userName, Boolean isPlace) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.thumb_image = thumb_image;
        this.userName = userName;
        this.isPlace = isPlace;
    }

    public User(String name, String status, String image, String thumb_image) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.thumb_image = thumb_image;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getPlace() {
        return isPlace;
    }

    public void setPlace(Boolean place) {
        isPlace = place;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}