package com.example.myapplication.Entity;

import com.google.type.DateTime;

public class Users {
    private String name, status, image, url;
    private DateTime time, date, state;

    public Users() {
    }

    public Users(String name, String status, String image) {
        this.name = name;
        this.status = status;
        this.image = image;
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

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public DateTime getState() {
        return state;
    }

    public void setState(DateTime state) {
        this.state = state;
    }
}
