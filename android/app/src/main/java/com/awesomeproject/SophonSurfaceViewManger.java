package com.awesomeproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewProps;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.image.ImageResizeMode;
import com.facebook.react.views.image.ReactImageView;

import org.webrtc.sdk.SophonSurfaceView;


public class SophonSurfaceViewManger extends SimpleViewManager<SophonSurfaceView>{
    public static final String REACT_CLASS = "SophonSurfaceView";
    ReactApplicationContext mCallerContext;

    public SophonSurfaceViewManger(ReactApplicationContext reactContext) {
        mCallerContext = reactContext;
        mCallerContext.getCurrentActivity();
    }

    @ReactProp(name = "src")
    public void setSrc(ReactImageView view, @Nullable ReadableArray sources) {
        view.setSource(sources);
    }

    @ReactProp(name = "borderRadius", defaultFloat = 0f)
    public void setBorderRadius(ReactImageView view, float borderRadius) {
        view.setBorderRadius(borderRadius);
    }

    @ReactProp(name = ViewProps.RESIZE_MODE)
    public void setResizeMode(ReactImageView view, @Nullable String resizeMode) {
        view.setScaleType(ImageResizeMode.toScaleType(resizeMode));
    }


    @NonNull
    @Override
    public String getName(){
        return REACT_CLASS;
    }

    @NonNull
    @Override
    protected SophonSurfaceView createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new SophonSurfaceView(reactContext);
    }
}
