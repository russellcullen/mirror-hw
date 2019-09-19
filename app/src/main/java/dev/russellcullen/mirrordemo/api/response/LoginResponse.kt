package dev.russellcullen.mirrordemo.api.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
  val data: LoginDataResponse
)

@JsonClass(generateAdapter = true)
data class LoginDataResponse(
  @Json(name = "api_token") val token: String
)