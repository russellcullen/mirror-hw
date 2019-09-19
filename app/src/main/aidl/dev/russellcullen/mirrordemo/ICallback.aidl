// ICallback.aidl
package dev.russellcullen.mirrordemo;

// Generic callback for completable calls with option error message
interface ICallback {
    // Called on completion of some task, `errorMsg` will be non null if `success` is `false`.
    void onComplete(boolean success, String errorMsg);
}
