package com.crycetruly.r2d2.model;

/**
 * Created by Elia on 1/6/2018.
 */

public class UserSearchNode {
    public String name, status, image;

    public UserSearchNode() {
    }

    public UserSearchNode(String name, String status, String image) {
        this.name = name;
        this.status = status;
        this.image = image;
    }

    @Override
    public String toString() {
        return "UserSearchNode{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", image='" + image + '\'' +
                '}';
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
}
