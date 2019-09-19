// IUserCallback.aidl
package dev.russellcullen.mirrordemo;

// Callback to notify of user changes
interface IUserCallback {
    // This method is called whenever the user is updated from any source.
    void onUserUpdate(String name, String birthdate, String location);
}
