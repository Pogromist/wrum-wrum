package com.openway.square.wrumwrum.data.remote;

public class ApiUtils {

    private ApiUtils() {
    }

    public static final String BASE_URL =  "BASE_URL_HERE";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
