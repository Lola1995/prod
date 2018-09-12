package com.getaplot.getaplot.model;

/**
 * Created by Elia on 11/14/2017.
 */

public class Status {
    private String status;
    private long updated;

    public Status() {
    }

    public Status(String status, long updated) {
        this.status = status;
        this.updated = updated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }
}
