package dev.russellcullen.mirrordemo.data

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit

/**
 * Simple data store for current user and related values.
 *
 * The underlying implementation uses `SharedPreferences`, and thus takes a `Context`,
 * but can be easily swapped out with SQLite or any other DB solution.
 */
class UserStore(context: Context) {
  private val USER_TOKEN = "user_token"
  private val USER_NAME = "user_name"
  private val USER_LOCATION = "user_location"
  private val USER_BIRTHDATE = "user_birthdate"
  private val LAST_UPDATE = "user_last_update"

  private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

  /**
   * The current logged in user
   */
  var user: User
    set(value) {
      prefs.edit {
        putString(USER_NAME, value.name)
        putString(USER_LOCATION, value.location)
        putString(USER_BIRTHDATE, value.birthdate)
      }
    }
    get() = User(
      name = prefs.getString(USER_NAME, "") ?: "",
      location = prefs.getString(USER_LOCATION, null),
      birthdate = prefs.getString(USER_BIRTHDATE, null)
    )

  /**
   * The current API token for logged in user
   */
  var token: String
    set(value) = prefs.edit { putString(USER_TOKEN, value) }
    get() = prefs.getString(USER_TOKEN, "") ?: ""

  /**
   * Latest time (in ms since epoch) the user was fetched from server
   */
  var lastUpdated: Long
    set(value) = prefs.edit { putLong(LAST_UPDATE, value) }
    get() = prefs.getLong(LAST_UPDATE, 0)

}