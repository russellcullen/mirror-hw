package dev.russellcullen.mirrordemo.api.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserResponse(
  val data: UserDataResponse
)

@JsonClass(generateAdapter = true)
data class UserDataResponse(
  val name: String,
  val profile: UserProfile
)

@JsonClass(generateAdapter = true)
data class UserProfile(
  val birthdate: String?,
  val location: String?
)