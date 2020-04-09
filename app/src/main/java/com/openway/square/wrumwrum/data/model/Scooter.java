package com.openway.square.wrumwrum.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Scooter {

    @SerializedName("id")
    @Expose
    private Integer id;

    @SerializedName("number")
    @Expose
    private Integer number;

    @SerializedName("lat")
    @Expose
    private Float lat;

    @SerializedName("lng")
    @Expose
    private Float lng;

    @SerializedName("fuel")
    @Expose
    private Float fuel;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Float getLat() {
        return lat;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public Float getLng() {
        return lng;
    }

    public void setLng(Float lng) {
        this.lng = lng;
    }

    public Float getFuel() {
        return fuel;
    }

    public void setFuel(Float fuel) {
        this.fuel = fuel;
    }

}
