package dev.russellcullen.mirrordemo

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dev.russellcullen.mirrordemo.api.MirrorApi
import dev.russellcullen.mirrordemo.data.UserStore
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class UserService : LifecycleService() {
  private lateinit var userRepository: UserRepository

  override fun onCreate() {
    super.onCreate()
    val retrofit = Retrofit.Builder()
      .baseUrl("https://dev.refinemirror.com/api/v1/")
      .addConverterFactory(MoshiConverterFactory.create())
      .build()

    val mirrorApi = retrofit.create<MirrorApi>()
    val userStore = UserStore(applicationContext)

    userRepository = UserRepository(lifecycleScope, mirrorApi, userStore)
  }

  private val binder = object : IUserService.Stub() {

    override fun signup(email: String?, name: String?, password: String?, password2: String?) {
      userRepository.signup(email, name, password, password2)
    }

    override fun login(email: String?, password: String?) {
      userRepository.login(email, password)
    }

    override fun updateUser(name: String?, location: String?, birthdate: String?) {
      userRepository.updateUser(name, location, birthdate)
    }

    override fun refreshUser() {
      userRepository.refreshUser()
    }

    override fun addSignupCallback(callback: ICallback?) {
      userRepository.addSignupCallback(callback)
    }

    override fun removeSignupCallback(callback: ICallback?) {
      userRepository.removeSignupCallback(callback)
    }

    override fun addLoginCallback(callback: ICallback?) {
      userRepository.addLoginCallback(callback)
    }

    override fun removeLoginCallback(callback: ICallback?) {
      userRepository.removeLoginCallback(callback)
    }

    override fun addUserCallback(callback: IUserCallback?) {
      userRepository.addUserCallback(callback)
    }

    override fun removeUserCallback(callback: IUserCallback?) {
      userRepository.removeUserCallback(callback)
    }

    override fun addUpdateCallback(callback: ICallback?) {
      userRepository.addUpdateCallback(callback)
    }

    override fun removeUpdateCallback(callback: ICallback?) {
      userRepository.removeUpdateCallback(callback)
    }
  }

  override fun onBind(intent: Intent): IBinder? {
    super.onBind(intent)
    return binder
  }

}