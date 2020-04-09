package com.openway.square.wrumwrum.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Rent implements Serializable {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("tenant")
    @Expose
    private String tenantUsername;
    @SerializedName("scooter")
    @Expose
    private Integer scooterNumber;
    @SerializedName("tariff")
    @Expose
    private Integer tariffId;
    @SerializedName("start_date")
    @Expose
    private String startDate;
    @SerializedName("end_date")
    @Expose
    private String endDate;
    @SerializedName("fuel_increase")
    @Expose
    private Float fuelIncrease;
    @SerializedName("payed_price")
    @Expose
    private Float payedPrice;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenantUsername() {
        return tenantUsername;
    }

    public void setTenantUsername(String tenantUsername) {
        this.tenantUsername = tenantUsername;
    }

    public Integer getScooterNumber() {
        return scooterNumber;
    }

    public void setScooterNumber(Integer scooterNumber) {
        this.scooterNumber = scooterNumber;
    }

    public Integer getTariffId() {
        return tariffId;
    }

    public void setTariffId(Integer tariffId) {
        this.tariffId = tariffId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Float getFuelIncrease() {
        return fuelIncrease;
    }

    public void setFuelIncrease(Float fuelIncrease) {
        this.fuelIncrease = fuelIncrease;
    }

    public Float getPayedPrice() {
        return payedPrice;
    }

    public void setPayedPrice(Float payedPrice) {
        this.payedPrice = payedPrice;
    }
}
