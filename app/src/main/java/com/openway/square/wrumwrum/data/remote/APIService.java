package com.openway.square.wrumwrum.data.remote;

import com.openway.square.wrumwrum.data.model.Operation;
import com.openway.square.wrumwrum.data.model.RatingData;
import com.openway.square.wrumwrum.data.model.ResponseWrapper;
import com.openway.square.wrumwrum.data.model.Scooter;
import com.openway.square.wrumwrum.data.model.Tariff;
import com.openway.square.wrumwrum.data.model.Tenant;

import java.util.List;

import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import rx.Observable;

public interface APIService {

    @POST("api/tenants/")
    @FormUrlEncoded
    Observable<ResponseWrapper<String>> registerTenant(@Field("username") String username,
                                                       @Field("password") String password);

    @PUT("api/tenants/")
    @FormUrlEncoded
    Observable<ResponseWrapper<Tenant>> activateTenant(@Field("code") String code,
                                                       @Header("Authorization") String authHeader);

    @GET("api/tenants/")
    Observable<ResponseWrapper<Tenant>> logInTenant(@Query("username") String username,
                                                    @Query("password") String password);

    @GET("api/tenants/")
    Observable<ResponseWrapper<Tenant>> logInTenant(@Header("Authorization") String authHeader);

    @GET("api/payment/wallet/")
    Observable<ResponseWrapper<Float>> getBalance(@Header("Authorization") String authHeader);

    @POST("api/payment/wallet/")
    @FormUrlEncoded
    Observable<ResponseWrapper<String>> topUpWallet(@Field("amount") Float amount,
                                                    @Field("card_number") String cardNumber,
                                                    @Field("due_date") String dueDate,
                                                    @Field("cvv") String cvv,
                                                    @Header("Authorization") String authHeader);

    @DELETE("api/payment/wallet/")
    Observable<ResponseWrapper<String>> withdrawalOfMoney(@Query("amount") Float amount,
                                                          @Query("card_number") String cardNumber,
                                                          @Header("Authorization") String authHeader);

    @GET("api/tariffs/")
    Observable<ResponseWrapper<List<Tariff>>> getTariffs(@Header("Authorization") String authHeader);

    @GET("api/tenants/scooters/")
    Observable<ResponseWrapper<List<Scooter>>> getScooters(@Header("Authorization") String authHeader);

    @POST("api/tenants/rents/")
    @FormUrlEncoded
    Observable<ResponseWrapper<Tenant>> chooseScooter(@Header("Authorization") String authHeader,
                                                      @Field("tariff") Integer tariffId,
                                                      @Field("scooter") Integer scooterId);

    @POST("api/arduino/")
    Observable<ResponseWrapper<String>> unlockScooter(@Header("Authorization") String authHeader);

    @DELETE("api/arduino/")
    Observable<ResponseWrapper<String>> finishRent(@Header("Authorization") String authHeader);

    @GET("api/tenants/rents/")
    Observable<ResponseWrapper<List<Operation>>> operationsHistory(@Header("Authorization") String authHeader);

    @GET("api/tenants/rating/")
    Observable<ResponseWrapper<RatingData>> getRating(@Header("Authorization") String authHeader);

}
