package dev.russellcullen.mirrordemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(R.layout.activity_main) {

  override fun onStart() {
    super.onStart()
    Intent(this, UserService::class.java).also { intent ->
      startService(intent)
    }
  }

  override fun onStop() {
    super.onStop()
    Intent(this, UserService::class.java).also { intent ->
      try {
        stopService(intent)
      } catch (e: Exception) {
        // If service is already stopped, that's ok
      }
    }
  }

}