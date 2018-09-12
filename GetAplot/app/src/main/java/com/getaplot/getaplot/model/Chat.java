package com.getaplot.getaplot.model;

/**
 * Created by Elia on 9/15/2017.
 */

public class Chat {
    public boolean seen;
    public long timestamp;
    public String imageUrl, message, reversedTime, type, sendDate, from;


    public String sentDate;

    public Chat() {
    }

    public Chat(boolean seen, long timestamp, String imageUrl, String message, String reversedTime, String type, String sendDate, String from, String sentDate) {
        this.seen = seen;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.message = message;
        this.reversedTime = reversedTime;
        this.type = type;
        this.sendDate = sendDate;
        this.from = from;
        this.sentDate = sentDate;
    }

    public Chat(boolean seen, long timestamp, String imageUrl, String message, String reversedTime, String sentDate) {
        this.seen = seen;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.message = message;
        this.reversedTime = reversedTime;
        this.sentDate = sentDate;
    }

    public Chat(boolean seen, long timestamp, String imageUrl, String message, String sentDate) {
        this.seen = seen;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.message = message;
        this.sentDate = sentDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getReversedTime() {
        return reversedTime;
    }

    public void setReversedTime(String reversedTime) {
        this.reversedTime = reversedTime;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
