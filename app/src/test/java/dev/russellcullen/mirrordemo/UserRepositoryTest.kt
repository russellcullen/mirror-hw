package dev.russellcullen.mirrordemo

import android.os.IBinder
import dev.russellcullen.mirrordemo.api.MirrorApi
import dev.russellcullen.mirrordemo.api.request.LoginRequest
import dev.russellcullen.mirrordemo.api.request.SignupRequest
import dev.russellcullen.mirrordemo.api.response.LoginDataResponse
import dev.russellcullen.mirrordemo.api.response.LoginResponse
import dev.russellcullen.mirrordemo.data.UserStore
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class UserRepositoryTest {
  private val testScope = TestCoroutineScope()

  @MockK
  private lateinit var mirrorApi: MirrorApi

  @MockK
  private lateinit var userStore: UserStore

  private lateinit var userRepository: UserRepository

  @Before
  fun setup() {
    MockKAnnotations.init(this)
    userRepository = UserRepository(testScope, mirrorApi, userStore)
  }

  @After
  fun cleanUp() {
    testScope.cleanupTestCoroutines()
  }

  @Test
  fun signup() {
    // Setup
    coEvery { mirrorApi.signup(any()) } returns mockk()
    var called = false
    val callback = makeCallback { success, _ ->
      assert(success)
      called = true
    }
    userRepository.addSignupCallback(callback)

    // Test
    userRepository.signup("email", "name", "password", "password")

    // Verify
    assert(called)

    coVerify { mirrorApi.signup(SignupRequest("name", "password", "password", "email")) }
    confirmVerified(mirrorApi)
  }

  @Test
  fun signup_400() {
    // Setup
    val errorRes = Response.error<LoginResponse>(
      400,
      ResponseBody.create(
        MediaType.parse("application/json"), ""
      )
    )
    coEvery { mirrorApi.signup(any()) } throws HttpException(errorRes)
    var called = false
    val callback = makeCallback { success, _ ->
      assert(!success)
      called = true
    }
    userRepository.addSignupCallback(callback)

    // Test
    userRepository.signup("email", "name", "password", "password")

    // Verify
    assert(called)

    coVerify { mirrorApi.signup(SignupRequest("name", "password", "password", "email")) }
    confirmVerified(mirrorApi)
  }

  @Test
  fun login() {
    // Setup
    coEvery { mirrorApi.login(any()) } returns LoginResponse(LoginDataResponse("token_123"))
    every { userStore.lastUpdated = any() } just Runs
    every { userStore.token = any() } just Runs
    var called = false
    val callback = makeCallback { success, _ ->
      assert(success)
      called = true
    }
    userRepository.addLoginCallback(callback)

    // Test
    userRepository.login("email", "password")

    // Verify
    assert(called)

    coVerify { mirrorApi.login(LoginRequest("email", "password")) }
    confirmVerified(mirrorApi)

    verify { userStore.lastUpdated = 0 }
    verify { userStore.token = "token_123" }
    confirmVerified(userStore)
  }

  @Test
  fun login_400() {
    // Setup
    val errorRes = Response.error<LoginResponse>(
      400,
      ResponseBody.create(
        MediaType.parse("application/json"), ""
      )
    )
    coEvery { mirrorApi.login(any()) } throws HttpException(errorRes)
    var called = false
    val callback = makeCallback { success, _ ->
      assert(!success)
      called = true
    }
    userRepository.addLoginCallback(callback)

    // Test
    userRepository.login("email", "password")

    // Verify
    assert(called)

    coVerify { mirrorApi.login(LoginRequest("email", "password")) }
    confirmVerified(mirrorApi)

    verifyAll(inverse = true) {
      userStore.lastUpdated = any()
      userStore.token = any()
    }
    confirmVerified(userStore)
  }

  // Helper function to create an `ICallback` from kotlin lambda
  private fun makeCallback(cb: (Boolean, String?) -> Unit): ICallback = object : ICallback {
    override fun asBinder(): IBinder {
      TODO() // Should never be called in tests
    }

    override fun onComplete(success: Boolean, errorMsg: String?) {
      cb(success, errorMsg)
    }
  }
}