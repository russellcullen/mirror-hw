package dev.russellcullen.mirrordemo.ui

import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.russellcullen.mirrordemo.R

class HomeFragment : Fragment(R.layout.fragment_home) {
  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    val loginBtn = requireView().findViewById<Button>(R.id.login_btn)
    val signupBtn = requireView().findViewById<Button>(R.id.signup_btn)

    loginBtn.setOnClickListener {
      findNavController().navigate(R.id.home_to_login)
    }

    signupBtn.setOnClickListener {
      findNavController().navigate(R.id.home_to_signup)
    }
  }
}