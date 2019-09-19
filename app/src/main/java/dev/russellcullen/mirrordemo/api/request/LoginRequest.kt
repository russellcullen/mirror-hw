package dev.russellcullen.mirrordemo.api.request

import com.squareup.moshi.JsonClass

/**
 * Request body for login
 */
@JsonClass(generateAdapter = true)
data class LoginRequest(
  val email: String,
  val password: String
)