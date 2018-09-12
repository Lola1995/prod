package com.crycetruly.dailydreams.models;

public class Day {
    String _id;
    String theDay;
    String feeling;
    String story;
    String added;

    public Day() {
    }

    public Day(String _id, String theDay, String feeling, String story, String added) {
        this._id = _id;
        this.theDay = theDay;
        this.feeling = feeling;
        this.story = story;
        this.added = added;
    }

    @Override
    public String toString() {
        return "Day{" +
                "_id='" + _id + '\'' +
                ", theDay='" + theDay + '\'' +
                ", feeling='" + feeling + '\'' +
                ", story='" + story + '\'' +
                ", added='" + added + '\'' +
                '}';
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTheDay() {
        return theDay;
    }

    public void setTheDay(String theDay) {
        this.theDay = theDay;
    }

    public String getFeeling() {
        return feeling;
    }

    public void setFeeling(String feeling) {
        this.feeling = feeling;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getAdded() {
        return added;
    }

    public void setAdded(String added) {
        this.added = added;
    }
}
