package com.awesomeproject;

import android.Manifest;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//import com.awesomeproject.alirtc.AliRtc;
import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.soloader.SoLoader;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {

    private static final String TAG = MainApplication.class.getName();
    private final ReactNativeHost mReactNativeHost =
            new ReactNativeHost(this) {
                @Override
                public boolean getUseDeveloperSupport() {
                    return BuildConfig.DEBUG;
                }

                @Override
                protected List<ReactPackage> getPackages() {
                    @SuppressWarnings("UnnecessaryLocalVariable")
                    List<ReactPackage> packages = new PackageList(this).getPackages();
                    // Packages that cannot be autolinked yet can be added manually here, for example:
                    // packages.add(new MyReactNativePackage());
//                    packages.add(new AliRtcZijinPackage());
                    Log.d(TAG, "成功调用：getPackages");
                    packages.add(new CustomImageMangerPackage(MainApplication.this));
                    return packages;
                }

                @Override
                protected String getJSMainModuleName() {
                    return "index";
                }
            };

//    public AliRtc aliRtc;

    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }


    // 物理按键监听
    public void registerKeyEventListener(){
        BroadcastReceiver receiver4 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("com.passion.keyevent.keycode")) {
                    int KeyCode = intent.getIntExtra("keycode_value", 0);
                    boolean isDown = intent.getBooleanExtra("keycode_down", true);
                    Log.i("keyCode", Integer.toString(KeyCode));
                    WritableMap params = Arguments.createMap();
                    params.putInt("keyCode", KeyCode);
                    params.putBoolean("isDown", isDown);
                    ReactInstanceManager mReactInstanceManager = getReactNativeHost().getReactInstanceManager();
                    ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();
                    if (reactContext != null) {
                        sendEvent(reactContext, "KeyEvent", params);
                    }

                }
            }
        };
        IntentFilter filter4 = new IntentFilter();
        filter4.addAction("com.passion.keyevent.keycode");
        registerReceiver(receiver4, filter4);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (
                checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                        checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                        checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)
        ) {
            // start service
            startRtcEngineService();
        }

        ReactInstanceManager mReactInstanceManager = getReactNativeHost().getReactInstanceManager();
        ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();
        registerKeyEventListener();
        SoLoader.init(this, /* native exopackage */ false);
        initializeFlipper(this, mReactInstanceManager);


//        aliRtc = new AliRtc(reactContext);
    }

//    @Override
//    void onTerminate(){

//    }

    /**
     * Loads Flipper in React Native templates. Call this in the onCreate method with something like
     * initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
     *
     * @param context
     * @param reactInstanceManager
     */
    private static void initializeFlipper(
            Context context, ReactInstanceManager reactInstanceManager) {
        if (BuildConfig.DEBUG) {
            try {
        /*
         We use reflection here to pick up the class that initializes Flipper,
        since Flipper library is not available in release mode
        */
                Class<?> aClass = Class.forName("com.awesomeproject.ReactNativeFlipper");
                aClass
                        .getMethod("initializeFlipper", Context.class, ReactInstanceManager.class)
                        .invoke(null, context, reactInstanceManager);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSION_REQ_ID = 0x0002;

    private void startRtcEngineService() {
        Log.d(TAG, "成功调用：startRtcEngineService");
        Intent intent = new Intent(this, AliRtcEngineService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        AliRtcServiceConnection conn = new AliRtcServiceConnection();
        this.bindService(intent, conn, BIND_AUTO_CREATE);
        if(aliRtcEngineService == null){
            Log.d(TAG, "为空：aliRtcEngineService, " +  Thread.currentThread().getName());
        }
    }


    public AliRtcEngineService aliRtcEngineService;
    class AliRtcServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "绑定成功调用：onServiceConnected, " + Thread.currentThread().getName());
            // 获取Binder
            AliRtcEngineService.AliRtcBinder binder = (AliRtcEngineService.AliRtcBinder) service;
            aliRtcEngineService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getReactNativeHost().getReactInstanceManager().getCurrentReactContext().getCurrentActivity(), REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }
}
