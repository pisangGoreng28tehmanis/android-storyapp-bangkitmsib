package test.dapuk.dicodingstory.data.remote.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import test.dapuk.dicodingstory.data.remote.response.ListStoryItem
import test.dapuk.dicodingstory.data.remote.response.LoginResponse
import test.dapuk.dicodingstory.data.remote.response.RegisterResponse
import test.dapuk.dicodingstory.data.remote.response.StoryDetailResponse
import test.dapuk.dicodingstory.data.remote.response.StoryResponse

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun createUser(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse


    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") authToken: String
    ): StoryResponse

    @GET("stories")
    suspend fun getStoriesPaging(
        @Header("Authorization") authToken: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): StoryResponse
    @GET("stories")
    suspend fun getStoriesLocation(
        @Header("Authorization") authToken: String,
        @Query("location") location : Int = 1
    ): StoryResponse

    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Header("Authorization") authToken: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): RegisterResponse

    @Multipart
    @POST("stories")
    suspend fun addStoryLocation(
        @Header("Authorization") authToken: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: Double,
        @Part("lon") lon: Double
    ): RegisterResponse

    @GET("stories/{id}")
    suspend fun getStoriesDetail(
        @Header("Authorization") authToken: String,
        @Path("id") id: String
    ): StoryDetailResponse

}