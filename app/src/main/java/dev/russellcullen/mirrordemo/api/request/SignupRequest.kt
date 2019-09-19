package dev.russellcullen.mirrordemo.api.request

import com.squareup.moshi.JsonClass

/**
 * Request body for user sign up
 */
@JsonClass(generateAdapter = true)
data class SignupRequest(
  val name: String,
  val password: String,
  val password2: String,
  val email: String
)