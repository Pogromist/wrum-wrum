package com.openway.square.wrumwrum.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Operation {

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("start_date")
    @Expose
    private String startDate;

    @SerializedName("end_date")
    @Expose
    private String endDate;

    @SerializedName("amount")
    @Expose
    private Float amount;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }
}

