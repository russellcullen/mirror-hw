package dev.russellcullen.mirrordemo.api.request

import com.squareup.moshi.JsonClass

/**
 * Request body for user profile updates.
 */
@JsonClass(generateAdapter = true)
data class UserUpdateRequest(
  val name: String,
  val location: String,
  val birthdate: String // YYYY-MM-DD
)