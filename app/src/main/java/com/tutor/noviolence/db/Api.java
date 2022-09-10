package com.tutor.noviolence.db;

import com.tutor.noviolence.models.response.CommentsResponseModel;
import com.tutor.noviolence.models.response.DefaultResponseModel;
import com.tutor.noviolence.models.response.LoginResponseModel;
import com.tutor.noviolence.models.response.PlaceIdResponseModel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface Api {

    @FormUrlEncoded
    @POST("createuser")
    Call<DefaultResponseModel> createUser(
            @Field("username") String username,
            @Field("email") String email,
            @Field("password") String password
            );

    @FormUrlEncoded
    @POST("loginuser")
    Call<LoginResponseModel> loginUser(
            @Field("email") String email,
            @Field("password") String password
    );

    @GET("getcommentsbyuser/{id}")
    Call<CommentsResponseModel> getCommentsByUserId(
            @Path("id") int id
    );

    @GET("getcommentsbyplace/{id}")
    Call<CommentsResponseModel> getCommentsByPlaceId(
            @Path("id") String id
    );

    @GET("allplaceid")
    Call<ResponseBody> getPlaceIds(
    );

    @FormUrlEncoded
    @POST("createcomment")
    Call<DefaultResponseModel> createComment(
            @Field("user_id") int user_id,
            @Field("place_id") String place_id,
            @Field("content") String content,
            @Field("rating") Float rating,
            @Field("pub_date") String pub_date
    );
}
