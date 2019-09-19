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

class SignupFragment : Fragment(R.layout.fragment_signup) {
  private var binder: IUserService? = null

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    val name = requireView().findViewById<EditText>(R.id.name)
    val email = requireView().findViewById<EditText>(R.id.email)
    val password = requireView().findViewById<EditText>(R.id.password)
    val password2 = requireView().findViewById<EditText>(R.id.password2)

    val signupBtn = requireView().findViewById<Button>(R.id.signup_btn)

    signupBtn.setOnClickListener {
      binder?.signup(
        email.text.toString(),
        name.text.toString(),
        password.text.toString(),
        password2.text.toString()
      )
    }
  }

  private val signupCallback = object : ICallback.Stub() {
    override fun onComplete(success: Boolean, errorMsg: String?) {
      view?.post {
        if (success) {
          findNavController().navigate(R.id.signup_to_login)
        } else {
          Snackbar.make(
            requireView(),
            "Error signing up ${errorMsg.orEmpty()}",
            Snackbar.LENGTH_SHORT
          ).show()
        }
      }
    }
  }

  private val connection = object : ServiceConnection {
    override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
      binder = IUserService.Stub.asInterface(service)
      binder?.addSignupCallback(signupCallback)
    }

    override fun onServiceDisconnected(className: ComponentName?) {
      binder?.removeSignupCallback(signupCallback)
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