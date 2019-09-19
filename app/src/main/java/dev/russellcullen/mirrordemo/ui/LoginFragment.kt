package dev.russellcullen.mirrordemo.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dev.russellcullen.mirrordemo.ICallback
import dev.russellcullen.mirrordemo.IUserService
import dev.russellcullen.mirrordemo.R
import dev.russellcullen.mirrordemo.UserService

class LoginFragment : Fragment(R.layout.fragment_login) {
  private var binder: IUserService? = null

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    val email = requireView().findViewById<EditText>(R.id.email)
    val password = requireView().findViewById<EditText>(R.id.password)

    val loginBtn = requireView().findViewById<Button>(R.id.login_btn)

    loginBtn.setOnClickListener {
      binder?.login(email.text.toString(), password.text.toString())
    }
  }

  private val loginCallback = object : ICallback.Stub() {
    override fun onComplete(success: Boolean, errorMsg: String?) {
      view?.post {
        if (success) {
          findNavController().navigate(R.id.login_to_profile)
        } else {
          Snackbar.make(
            requireView(),
            "Error logging in ${errorMsg.orEmpty()}",
            Snackbar.LENGTH_SHORT
          ).show()
        }
      }
    }
  }

  private val connection = object : ServiceConnection {
    override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
      binder = IUserService.Stub.asInterface(service)
      binder?.addLoginCallback(loginCallback)
    }

    override fun onServiceDisconnected(className: ComponentName?) {
      binder?.removeLoginCallback(loginCallback)
      binder = null
    }
  }

  override fun onStart() {
    super.onStart()
    val ctx = requireContext()
    Intent(ctx, UserService::class.java).also { intent ->
      ctx.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }
  }

  override fun onStop() {
    super.onStop()
    context?.unbindService(connection)
  }
}