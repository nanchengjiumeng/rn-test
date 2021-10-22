package com.awesomeproject;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CustomImageMangerPackage implements ReactPackage {
    MainApplication context;
    public CustomImageMangerPackage(
            MainApplication context
    ){
        this.context = context;
    }

    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public List<ViewManager> createViewManagers(
            ReactApplicationContext reactContext) {

        return Arrays.<ViewManager>asList( new AliRtcZijinViewManager(reactContext, context));
    }

}