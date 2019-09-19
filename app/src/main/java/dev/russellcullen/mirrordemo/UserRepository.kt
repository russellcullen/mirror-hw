package dev.russellcullen.mirrordemo

import dev.russellcullen.mirrordemo.api.MirrorApi
import dev.russellcullen.mirrordemo.api.request.LoginRequest
import dev.russellcullen.mirrordemo.api.request.SignupRequest
import dev.russellcullen.mirrordemo.api.request.UserUpdateRequest
import dev.russellcullen.mirrordemo.data.User
import dev.russellcullen.mirrordemo.data.UserStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.*

/**
 * Repository class for managing user state between remote source and persisted data.
 *
 * `MirrorApi` handles auth requests and refreshing user data.
 * `UserStore` is a persistent data store.
 */
class UserRepository(
  val coroutineScope: CoroutineScope,
  val mirrorApi: MirrorApi,
  val userStore: UserStore
) {
  private val SOFT_TTL_MS = 5 * 60 * 1000   // 5 minutes
  private val HARD_TTL_MS = 60 * 60 * 1000  // 60 minutes

  private val loginCallbacks = mutableListOf<ICallback>()
  private val signupCallbacks = mutableListOf<ICallback>()
  private val userCallbacks = mutableListOf<IUserCallback>()
  private val userUpdateCallbacks = mutableListOf<ICallback>()

  fun signup(email: String?, name: String?, password: String?, password2: String?) =
    coroutineScope.launch {
      try {
        mirrorApi.signup(
          SignupRequest(
            name = name ?: "",
            email = email ?: "",
            password = password ?: "",
            password2 = password2 ?: ""
          )
        )
        signupCallbacks.forEach { it.onComplete(true, null) }
      } catch (e: HttpException) {
        signupCallbacks.forEach { it.onComplete(false, e.message()) }
      }
    }

  fun login(email: String?, password: String?) = coroutineScope.launch {
    try {
      val body = mirrorApi.login(
        LoginRequest(
          email ?: "",
          password ?: ""
        )
      )
      // Reset TTL
      userStore.lastUpdated = 0
      userStore.token = body.data.token
      loginCallbacks.forEach { it.onComplete(true, null) }
    } catch (e: HttpException) {
      loginCallbacks.forEach { it.onComplete(false, e.message()) }
    }
  }

  fun updateUser(name: String?, location: String?, birthdate: String?) = coroutineScope.launch {
    try {
      val userName = name ?: userStore.user.name
      val userLocation = location ?: userStore.user.location ?: ""
      val userBday = birthdate ?: userStore.user.birthdate ?: ""
      mirrorApi.updateUser(
        token = "Bearer ${userStore.token}",
        update = UserUpdateRequest(userName, userLocation, userBday)
      )
      userStore.user = User(userName, userBday, userLocation)
      userCallbacks.forEach { it.onUserUpdate(name, birthdate, location) }
      userUpdateCallbacks.forEach { it.onComplete(true, null) }
    } catch (e: HttpException) {
      userUpdateCallbacks.forEach { it.onComplete(false, e.message()) }
    }
  }

  fun refreshUser() = coroutineScope.launch {
    val now = Date().time
    val diff = now - userStore.lastUpdated
    when {
      // Past hard TTL, fetch user from network
      diff >= HARD_TTL_MS -> fetchAndUpdateUser()
      // Past soft TTL, notify with cached user, and then fetch from network
      diff >= SOFT_TTL_MS -> {
        notifyUserUpdate(userStore.user)
        fetchAndUpdateUser()
      }
      // Otherwise just notify with cached user
      else -> notifyUserUpdate(userStore.user)
    }
  }

  private suspend fun fetchAndUpdateUser() {
    try {
      val userRes = mirrorApi.getUser(token = "Bearer ${userStore.token}")
      val profile = userRes.data.profile
      val user = User(userRes.data.name, profile.birthdate, profile.location)
      userStore.user = user
      userStore.lastUpdated = Date().time
      notifyUserUpdate(user)
    } catch (e: HttpException) {
      // Should probably retry here, or notify user of failure
    }
  }

  private fun notifyUserUpdate(user: User) {
    userCallbacks.forEach {
      it.onUserUpdate(
        user.name,
        user.birthdate,
        user.location
      )
    }
  }

  fun addSignupCallback(callback: ICallback?) =
    callback?.let { signupCallbacks.add(it) }

  fun removeSignupCallback(callback: ICallback?) =
    callback?.let { signupCallbacks.remove(it) }

  fun addLoginCallback(callback: ICallback?) =
    callback?.let { loginCallbacks.add(it) }

  fun removeLoginCallback(callback: ICallback?) =
    callback?.let { loginCallbacks.remove(it) }

  fun addUserCallback(callback: IUserCallback?) =
    callback?.let { userCallbacks.add(it) }

  fun removeUserCallback(callback: IUserCallback?) =
    callback?.let { userCallbacks.remove(it) }

  fun addUpdateCallback(callback: ICallback?) =
    callback?.let { userUpdateCallbacks.add(it) }

  fun removeUpdateCallback(callback: ICallback?) =
    callback?.let { userUpdateCallbacks.add(it) }
}