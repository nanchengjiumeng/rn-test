package com.awesomeproject;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alivc.rtc.device.utils.StringUtils;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import org.webrtc.sdk.SophonSurfaceView;


public class AliRtcZijinViewManager extends SimpleViewManager<SophonSurfaceView> {
    private static final String TAG = AliRtcZijinViewManager.class.getName();
    public static final String REACT_CLASS = "AliRtcZijinView";

    ReactApplicationContext mCallerContext;
    Activity currentActivity;
    AliRtcEngineService ares;
    MainApplication mainApplicationContext;

    public AliRtcZijinViewManager(ReactApplicationContext reactContext, MainApplication mainApplicationContext) {
        mCallerContext = reactContext;
        currentActivity = reactContext.getCurrentActivity();
        this.mainApplicationContext = mainApplicationContext;
//        this.ares = ares
//        reactContext.getApplicationInfo()
    }

    @NonNull
    @Override
    public String getName(){
        return REACT_CLASS;
    }

    @ReactProp(name = "channel")
    public void setChannel(SophonSurfaceView view, String channel) {
        if(!StringUtils.isEmpty(channel)){
            mainApplicationContext.aliRtcEngineService.joinChannel(channel);
        }
    }

    @NonNull
    @Override
    protected SophonSurfaceView createViewInstance(@NonNull ThemedReactContext reactContext) {
        Log.d(TAG, "成功调用：SophonSurfaceView");
        SophonSurfaceView view = new SophonSurfaceView(currentActivity);
//        setInitView(view);
        view.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        view.setZOrderOnTop(false);
        view.setZOrderMediaOverlay(false);
        mainApplicationContext.aliRtcEngineService.initLocalView(view);
        return view;
    }

}
