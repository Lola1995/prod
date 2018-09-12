package com.crycetruly.r2d2.model;

public class CommentReplies {
    private String time, commentUid, user, commentText, relyText, replyTime, event_id;

    public CommentReplies() {
    }

    public CommentReplies(String time, String commentUid, String user, String commentText,
                          String relyText, String replyTime, String event_id) {
        this.time = time;
        this.commentUid = commentUid;
        this.user = user;
        this.commentText = commentText;
        this.relyText = relyText;
        this.replyTime = replyTime;
        this.event_id = event_id;

    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCommentUid() {
        return commentUid;
    }

    public void setCommentUid(String commentUid) {
        this.commentUid = commentUid;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getRelyText() {
        return relyText;
    }

    public void setRelyText(String relyText) {
        this.relyText = relyText;
    }

    public String getReplyTime() {
        return replyTime;
    }

    public void setReplyTime(String replyTime) {
        this.replyTime = replyTime;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }
}