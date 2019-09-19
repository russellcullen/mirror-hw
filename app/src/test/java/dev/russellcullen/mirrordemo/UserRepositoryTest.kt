package dev.russellcullen.mirrordemo

import android.os.IBinder
import dev.russellcullen.mirrordemo.api.MirrorApi
import dev.russellcullen.mirrordemo.api.request.LoginRequest
import dev.russellcullen.mirrordemo.api.request.SignupRequest
import dev.russellcullen.mirrordemo.api.request.UserUpdateRequest
import dev.russellcullen.mirrordemo.api.response.*
import dev.russellcullen.mirrordemo.data.User
import dev.russellcullen.mirrordemo.data.UserStore
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.util.*

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
    val errorRes = Response.error<ResponseBody>(
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

  @Test
  fun updateUser() {
    // Setup
    coEvery { mirrorApi.updateUser(any(), any()) } returns mockk()
    every { userStore.token } returns "token_123"
    every { userStore.user = any() } just Runs
    var called = false
    val callback = makeCallback { success, _ ->
      assert(success)
      called = true
    }
    var userCalled = false
    val userCallback = makeUserCallback { name, bday, location ->
      assertEquals("name", name)
      assertEquals("location", location)
      assertEquals("bday", bday)
      userCalled = true
    }
    userRepository.addUpdateCallback(callback)
    userRepository.addUserCallback(userCallback)

    // Test
    userRepository.updateUser("name", "location", "bday")

    // Verify
    assert(called)
    assert(userCalled)

    coVerify {
      mirrorApi.updateUser(
        "Bearer token_123",
        UserUpdateRequest("name", "location", "bday")
      )
    }
    confirmVerified(mirrorApi)

    verify { userStore.token }
    verify { userStore.user = User("name", "bday", "location") }
    confirmVerified(userStore)
  }

  @Test
  fun refreshUser_hardTTL() {
    // Setup
    val oldUser = User("oldName", "oldBday", "oldLocation")
    val newUser = User("name", "bday", "location")
    every { userStore.user } returns oldUser
    every { userStore.user = any() } just Runs
    every { userStore.lastUpdated } returns 0
    every { userStore.lastUpdated = any() } just Runs
    every { userStore.token } returns "token_123"
    coEvery { mirrorApi.getUser(any()) } returns UserResponse(
      UserDataResponse(
        newUser.name, UserProfile(
          newUser.birthdate,
          newUser.location
        )
      )
    )
    var userCalled = false
    val userCallback = makeUserCallback { name, bday, location ->
      assertEquals(newUser.name, name)
      assertEquals(newUser.location, location)
      assertEquals(newUser.birthdate, bday)
      userCalled = true
    }
    userRepository.addUserCallback(userCallback)

    // Test
    userRepository.refreshUser()

    // Verify
    assert(userCalled)

    coVerify { mirrorApi.getUser("Bearer token_123") }
    confirmVerified(mirrorApi)

    verifyAll {
      userStore.token
      userStore.user = newUser
      userStore.lastUpdated
      userStore.lastUpdated = less(Date().time)
    }
    confirmVerified(userStore)
  }

  @Test
  fun refreshUser_softTTL() {
    // Setup
    val oldUser = User("oldName", "oldBday", "oldLocation")
    val newUser = User("name", "bday", "location")
    every { userStore.user } returns oldUser
    every { userStore.user = any() } just Runs
    every { userStore.lastUpdated } answers { Date().time - (15 * 60 * 1000) }
    every { userStore.lastUpdated = any() } just Runs
    every { userStore.token } returns "token_123"
    coEvery { mirrorApi.getUser(any()) } returns UserResponse(
      UserDataResponse(
        newUser.name, UserProfile(
          newUser.birthdate,
          newUser.location
        )
      )
    )
    var userCalledTimes = 0
    val userCallback = makeUserCallback { name, bday, location ->
      if (userCalledTimes == 0) {
        // Returns cached data first
        assertEquals(oldUser.name, name)
        assertEquals(oldUser.location, location)
        assertEquals(oldUser.birthdate, bday)
      } else {
        // Then returns network data
        assertEquals(newUser.name, name)
        assertEquals(newUser.location, location)
        assertEquals(newUser.birthdate, bday)
      }
      userCalledTimes++
    }
    userRepository.addUserCallback(userCallback)

    // Test
    userRepository.refreshUser()

    // Verify
    assertEquals(2, userCalledTimes)

    coVerify { mirrorApi.getUser("Bearer token_123") }
    confirmVerified(mirrorApi)

    verifyAll {
      userStore.token
      userStore.user
      userStore.user = newUser
      userStore.lastUpdated
      userStore.lastUpdated = less(Date().time)
    }
    confirmVerified(userStore)
  }

  @Test
  fun refreshUser_noTTL() {
    // Setup
    val oldUser = User("oldName", "oldBday", "oldLocation")
    every { userStore.user } returns oldUser
    every { userStore.lastUpdated } answers { Date().time }
    every { userStore.token } returns "token_123"
    var userCalled = false
    val userCallback = makeUserCallback { name, bday, location ->
      assertEquals(oldUser.name, name)
      assertEquals(oldUser.location, location)
      assertEquals(oldUser.birthdate, bday)
      userCalled = true
    }
    userRepository.addUserCallback(userCallback)

    // Test
    userRepository.refreshUser()

    // Verify
    assert(userCalled)

    coVerify(inverse = true) { mirrorApi.getUser(any()) }
    confirmVerified(mirrorApi)

    verifyAll {
      userStore.user
      userStore.lastUpdated
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

  // Helper function to create an `IUserCallback` from kotlin lambda
  private fun makeUserCallback(cb: (String?, String?, String?) -> Unit): IUserCallback =
    object : IUserCallback {
      override fun onUserUpdate(name: String?, birthdate: String?, location: String?) {
        cb(name, birthdate, location)
      }

      override fun asBinder(): IBinder {
        TODO() // Should never be called in tests
      }
    }
}