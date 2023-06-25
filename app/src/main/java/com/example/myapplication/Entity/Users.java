package com.example.myapplication.Entity;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Users {
    private String name, status, image;
    public Map<String, Object> onlineStateMap = new HashMap<>();

    public Users() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
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

    public Map<String, Object> getOnlineStateMap() {
        return onlineStateMap;
    }

    public void setOnlineStateMap(Map<String, Object> onlineStateMap) {
        this.onlineStateMap = onlineStateMap;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("status", status);
        result.put("image", image);
        result.put("onlineStateMap", onlineStateMap);

        return result;
    }
}
