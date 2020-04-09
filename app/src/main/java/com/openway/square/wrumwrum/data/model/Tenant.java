package com.openway.square.wrumwrum.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Tenant implements Serializable {

    @SerializedName("user")
    @Expose
    private String username;

    @SerializedName("active_rent")
    @Expose
    private Rent activeRent;

    @SerializedName("activated")
    @Expose
    private Boolean activated;

    @SerializedName("wallet_number")
    @Expose
    private String walletNumber;

    @SerializedName("available_amount")
    @Expose
    private Float availableAmount;

    @SerializedName("rating_points")
    @Expose
    private String ratingPoints;

    @SerializedName("token")
    @Expose
    private String token;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Rent getActiveRent() {
        return activeRent;
    }

    public void setActiveRent(Rent activeRent) {
        this.activeRent = activeRent;
    }

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public String getWalletNumber() {
        return walletNumber;
    }

    public void setWalletNumber(String walletNumber) {
        this.walletNumber = walletNumber;
    }

    public Float getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(Float availableAmount) {
        this.availableAmount = availableAmount;
    }

    public String getRatingPoints() {
        return ratingPoints;
    }

    public void setRatingPoints(String ratingPoints) {
        this.ratingPoints = ratingPoints;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFormattedToken() {
        return "Token " + token;
    }

}
