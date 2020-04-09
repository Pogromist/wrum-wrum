package com.openway.square.wrumwrum.data.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StandardResponse<T> {

    @NonNull
    @SerializedName("status")
    @Expose
    String status;

    @SerializedName("message")
    @Expose
    String message;

    @SerializedName("data")
    @Expose
    private T data;

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public StandardResponse(@NonNull String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
