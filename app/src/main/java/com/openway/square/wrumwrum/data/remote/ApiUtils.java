package com.openway.square.wrumwrum.data.remote;

public class ApiUtils {

    private ApiUtils() {
    }

    public static final String BASE_URL =  "http://10.101.177.18:8000/";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
