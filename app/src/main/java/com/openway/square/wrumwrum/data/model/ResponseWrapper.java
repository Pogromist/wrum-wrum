package com.openway.square.wrumwrum.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseWrapper<T> {

    @SerializedName("data")
    @Expose
    private StandardResponse<T> data;

    public StandardResponse<T> getData() {
        return data;
    }

    public T getRawData() {
        return data.getData();
    }

    public void setData(StandardResponse<T> data) {
        this.data = data;
    }

    public ResponseWrapper(StandardResponse<T> data) {
        this.data = data;
    }
}
