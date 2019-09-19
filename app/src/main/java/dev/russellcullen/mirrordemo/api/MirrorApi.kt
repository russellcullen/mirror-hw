package dev.russellcullen.mirrordemo.api

import dev.russellcullen.mirrordemo.api.request.LoginRequest
import dev.russellcullen.mirrordemo.api.request.SignupRequest
import dev.russellcullen.mirrordemo.api.request.UserUpdateRequest
import dev.russellcullen.mirrordemo.api.response.LoginResponse
import dev.russellcullen.mirrordemo.api.response.UserResponse
import okhttp3.ResponseBody
import retrofit2.http.*

interface MirrorApi {
  @POST("auth/signup")
  suspend fun signup(@Body body: SignupRequest): ResponseBody

  @POST("auth/login")
  suspend fun login(@Body body: LoginRequest): LoginResponse

  @GET("user/me")
  suspend fun getUser(@Header("Authorization") token: String): UserResponse

  @PATCH("user/me")
  suspend fun updateUser(
    @Header("Authorization") token: String,
    @Body update: UserUpdateRequest
  ): ResponseBody
}