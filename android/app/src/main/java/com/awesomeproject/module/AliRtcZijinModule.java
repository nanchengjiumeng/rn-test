package com.awesomeproject.module;

import android.app.Activity;
import android.content.Intent;

import com.awesomeproject.activity.AliRtcChatActivity;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class AliRtcZijinModule extends ReactContextBaseJavaModule {

    private static final int ALI_RTC_REQUEST = 202110;
    private static final String E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST";
    private static final String E_FAILED_TO_SHOW_ACTIVITY = "E_FAILED_TO_SHOW_ACTIVITY";

    private Promise mPickerPromise;
    private ReactContext mReactContext;

    AliRtcZijinModule(ReactApplicationContext reactContext) {
        super(reactContext);
        // Add the listener for `onActivityResult`
        this.mReactContext = reactContext;
    }

    @Override
    public String getName() {
        return "AliRtcZijinModule";
    }

    @ReactMethod
    public void joinChannel() {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
//            promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
            return;
        }

//        mPickerPromise = promise;
        try {
            final Intent rtcChatIntent = new Intent(currentActivity, AliRtcChatActivity.class);
            rtcChatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            this.mReactContext.startActivity(rtcChatIntent);
        } catch (Exception e) {
            e.printStackTrace();
//            mPickerPromise.reject(E_FAILED_TO_SHOW_ACTIVITY, e);
//            mPickerPromise = null;
        }
    }
}
