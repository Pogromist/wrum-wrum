package com.openway.square.wrumwrum.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserPlace {

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("rating_points")
    @Expose
    private Integer ratingPoints;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getRatingPoints() {
        return ratingPoints;
    }

    public void setRatingPoints(Integer ratingPoints) {
        this.ratingPoints = ratingPoints;
    }
}
