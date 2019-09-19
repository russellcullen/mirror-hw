// IUserService.aidl
package dev.russellcullen.mirrordemo;

import dev.russellcullen.mirrordemo.ICallback;
import dev.russellcullen.mirrordemo.IUserCallback;

interface IUserService {

    // Makes a sign up request, use `addSignupCallback` to handle response.
    void signup(String email, String name, String password, String password2);

    // Makes a log in request, use `addLoginCallback` to handle response.
    void login(String email, String password);

    // Updates the user, use `addUpdateCallback` to handle response,
    // use `addUserCallback` to handle updated user state.
    void updateUser(String name, String location, String birthdate);

    // Refreshes the user, use `addUserCallback` to handle updated user state from any source.
    // If user data is reasonably out of date (see TTLs in implementation), this method will fetch
    // data from the network.
    void refreshUser();

    // Callbacks for receiving signup success/failure.
    void addSignupCallback(ICallback callback);
    void removeSignupCallback(ICallback callback);

    // Callbacks for receiving login success/failure.
    void addLoginCallback(ICallback callback);
    void removeLoginCallback(ICallback callback);

    // Callbacks for receiving user patch success/failure.
    void addUpdateCallback(ICallback callback);
    void removeUpdateCallback(ICallback callback);

    // Callbacks for receiving all user updates.
    void addUserCallback(IUserCallback callback);
    void removeUserCallback(IUserCallback callback);
}
