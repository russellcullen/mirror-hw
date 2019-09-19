package dev.russellcullen.mirrordemo.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import dev.russellcullen.mirrordemo.*

class ProfileFragment : Fragment(R.layout.fragment_profile) {
  private var binder: IUserService? = null

  private lateinit var nameTxt: TextView
  private lateinit var bdayTxt: TextView
  private lateinit var locationTxt: TextView

  private lateinit var nameEdit: EditText
  private lateinit var bdayEdit: EditText
  private lateinit var locationEdit: EditText

  private lateinit var editGroup: Group
  private lateinit var staticGroup: Group

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    nameTxt = requireView().findViewById(R.id.name)
    nameEdit = requireView().findViewById(R.id.name_edit)
    bdayTxt = requireView().findViewById(R.id.birthdate)
    bdayEdit = requireView().findViewById(R.id.birthdate_edit)
    locationTxt = requireView().findViewById(R.id.location)
    locationEdit = requireView().findViewById(R.id.location_edit)

    staticGroup = requireView().findViewById(R.id.text_group)
    editGroup = requireView().findViewById(R.id.edit_group)

    val refreshBtn = requireView().findViewById<Button>(R.id.refresh_btn)
    refreshBtn.setOnClickListener {
      binder?.refreshUser()
    }

    val editBtn = requireView().findViewById<Button>(R.id.edit_btn)
    editBtn.setOnClickListener {
      setEditMode(true)
    }

    val saveBtn = requireView().findViewById<Button>(R.id.save_btn)
    saveBtn.setOnClickListener {
      binder?.updateUser(
        nameEdit.text.toString(),
        locationEdit.text.toString(),
        bdayEdit.text.toString()
      )
    }
  }

  private fun setEditMode(isEditMode: Boolean) {
    staticGroup.visibility = if (isEditMode) View.GONE else View.VISIBLE
    editGroup.visibility = if (isEditMode) View.VISIBLE else View.GONE
    if (isEditMode) {
      nameEdit.setText(nameTxt.text)
      bdayEdit.setText(bdayTxt.text)
      locationEdit.setText(locationTxt.text)
    }
  }

  private val userUpdateCallback = object : ICallback.Stub() {
    override fun onComplete(success: Boolean, errorMsg: String?) {
      if (success) {
        setEditMode(false)
      } else {
        Snackbar.make(
          requireView(),
          "Error saving profile ${errorMsg.orEmpty()}",
          Snackbar.LENGTH_SHORT
        ).show()
      }
    }
  }

  private val userCallback = object : IUserCallback.Stub() {
    override fun onUserUpdate(name: String?, birthdate: String?, location: String?) {
      nameTxt.text = name
      bdayTxt.text = birthdate
      locationTxt.text = location

      if (birthdate.isNullOrBlank() || location.isNullOrBlank()) {
        setEditMode(true)
      }
    }
  }

  private val connection = object : ServiceConnection {
    override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
      binder = IUserService.Stub.asInterface(service)
      binder?.addUserCallback(userCallback)
      binder?.addUpdateCallback(userUpdateCallback)
      binder?.refreshUser()
    }

    override fun onServiceDisconnected(className: ComponentName?) {
      binder?.removeUserCallback(userCallback)
      binder?.removeUpdateCallback(userUpdateCallback)
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