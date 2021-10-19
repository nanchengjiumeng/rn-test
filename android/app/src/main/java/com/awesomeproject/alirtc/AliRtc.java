package com.awesomeproject.alirtc;

import static com.alivc.rtc.AliRtcEngine.AliRtcRenderMode.AliRtcRenderModeAuto;
import static com.alivc.rtc.AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackBoth;
import static com.alivc.rtc.AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera;
import static com.alivc.rtc.AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackNo;
import static com.alivc.rtc.AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackScreen;

import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_ICE_CONNECTION_HEARTBEAT_TIMEOUT;
import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_SESSION_REMOVED;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alivc.rtc.AliRtcAuthInfo;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcEngineEventListener;
import com.alivc.rtc.AliRtcEngineNotify;
import com.alivc.rtc.AliRtcRemoteUserInfo;
import com.facebook.react.bridge.ReactApplicationContext;
import com.awesomeproject.adapter.ChartUserAdapter;
import com.awesomeproject.bean.ChartUserBean;
import com.facebook.react.bridge.ReactContext;

import org.webrtc.sdk.SophonSurfaceView;

public class AliRtc {

    private AliRtcEngine mAliRtcEngine;

    public static final int CAMERA = 1001;
    public static final int SCREEN = 1002;

    /**
     * 承载远程User的Adapter
     */
    private ChartUserAdapter mUserListAdapter;

    private static final int PERMISSION_REQ_ID = 0x0002;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    ReactContext mCallerContext;
    Activity currentActivity;

    public AliRtc(ReactContext reactContext) {
        mCallerContext = reactContext;
        currentActivity = reactContext.getCurrentActivity();
    }


//    public void setInitView(SophonSurfaceView view) {
//        initView(view);
//        if (
//                checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
//                        checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
//                        checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)
//        ) {
//            // 初始化引擎以及打开预览界面
//            initRTCEngineAndStartPreview(view);
//        }
//    }

    private void initView(SophonSurfaceView view) {
        // 承载远程User的Adapter
        mUserListAdapter = new ChartUserAdapter();
        mUserListAdapter.setOnSubConfigChangeListener(mOnSubConfigChangeListener);
    }

    private void showToast(final String msg) {
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mCallerContext, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 用户操作回调监听(回调接口都在子线程)
     */
    private final AliRtcEngineEventListener mEventListener = new AliRtcEngineEventListener() {

        /**
         * 加入房间的回调
         * @param result 结果码
         */
        @Override
        public void onJoinChannelResult(int result, String channel, int elapsed) {
            currentActivity.runOnUiThread(() -> {
                if (result == 0) {
                    showToast("加入频道成功");
                } else {
                    showToast("加入频道失败 错误码: " + result);
                }
            });
        }

        /**
         * 出现错误的回调
         * @param error 错误码
         */
        @Override
        public void onOccurError(int error, String message) {
            super.onOccurError(error, message);
            //错误处理
            processOccurError(error);
        }

    };

    /**
     * SDK事件通知(回调接口都在子线程)
     */
    private final AliRtcEngineNotify mEngineNotify = new AliRtcEngineNotify() {

        /**
         * 远端用户上线通知
         * @param uid userId
         */
        @Override
        public void onRemoteUserOnLineNotify(String uid, int elapsed) {
            addRemoteUser(uid);
        }

        /**
         * 远端用户下线通知
         * @param uid userId
         */
        @Override
        public void onRemoteUserOffLineNotify(String uid, AliRtcEngine.AliRtcUserOfflineReason reason) {
            removeRemoteUser(uid);
        }

        /**
         * 远端用户发布音视频流变化通知
         * @param s userid
         * @param aliRtcAudioTrack 音频流
         * @param aliRtcVideoTrack 相机流
         */
        @Override
        public void onRemoteTrackAvailableNotify(String s, AliRtcEngine.AliRtcAudioTrack aliRtcAudioTrack,
                                                 AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack) {
            updateRemoteDisplay(s, aliRtcAudioTrack, aliRtcVideoTrack);
        }
    };

    private ChartUserBean convertRemoteUserToUserData(AliRtcRemoteUserInfo remoteUserInfo) {
        String uid = remoteUserInfo.getUserID();
        ChartUserBean ret = mUserListAdapter.createDataIfNull(uid);
        ret.mUserId = uid;
        ret.mUserName = remoteUserInfo.getDisplayName();
        ret.mIsCameraFlip = false;
        ret.mIsScreenFlip = false;
        return ret;
    }


    private void addRemoteUser(String uid) {
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //判断用户是否在线
                if (null == mAliRtcEngine) {
                    return;
                }
                AliRtcRemoteUserInfo remoteUserInfo = mAliRtcEngine.getUserInfo(uid);
                if (remoteUserInfo != null) {
                    mUserListAdapter.updateData(convertRemoteUserToUserData(remoteUserInfo), true);
                }
            }
        });
    }

    private void removeRemoteUser(String uid) {
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUserListAdapter.removeData(uid, true);
            }
        });
    }

    private final ChartUserAdapter.OnSubConfigChangeListener mOnSubConfigChangeListener = new ChartUserAdapter.OnSubConfigChangeListener() {
        @Override
        public void onFlipView(String uid, int flag, boolean flip) {
            AliRtcRemoteUserInfo userInfo = mAliRtcEngine.getUserInfo(uid);
            switch (flag) {
                case CAMERA:
                    if (userInfo != null) {
                        AliRtcEngine.AliRtcVideoCanvas cameraCanvas = userInfo.getCameraCanvas();
                        if (cameraCanvas != null) {
                            cameraCanvas.mirrorMode = flip ? AliRtcEngine.AliRtcRenderMirrorMode.AliRtcRenderMirrorModeAllEnabled : AliRtcEngine.AliRtcRenderMirrorMode.AliRtcRenderMirrorModeAllDisable;
                            mAliRtcEngine.setRemoteViewConfig(cameraCanvas, uid, AliRtcVideoTrackCamera);
                        }
                    }
                    break;
                case SCREEN:
                    if (userInfo != null) {
                        AliRtcEngine.AliRtcVideoCanvas screenCanvas = userInfo.getScreenCanvas();
                        if (screenCanvas != null) {
                            screenCanvas.mirrorMode = flip ? AliRtcEngine.AliRtcRenderMirrorMode.AliRtcRenderMirrorModeAllEnabled : AliRtcEngine.AliRtcRenderMirrorMode.AliRtcRenderMirrorModeAllDisable;
                            mAliRtcEngine.setRemoteViewConfig(screenCanvas, uid, AliRtcVideoTrackScreen);
                        }
                    }
                    break;
            }
        }

        @Override
        public void onShowVideoInfo(String uid, int flag) {
        }
    };

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(currentActivity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(currentActivity, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }

    private AliRtcEngine.AliRtcVideoCanvas createCanvasIfNull(AliRtcEngine.AliRtcVideoCanvas canvas) {
        if (canvas == null || canvas.view == null) {
            //创建canvas，Canvas为SophonSurfaceView或者它的子类
            canvas = new AliRtcEngine.AliRtcVideoCanvas();
            SophonSurfaceView surfaceView = new SophonSurfaceView(currentActivity);
            surfaceView.setZOrderOnTop(true);
            surfaceView.setZOrderMediaOverlay(true);
            canvas.view = surfaceView;
            //renderMode提供四种模式：Auto、Stretch、Fill、Crop，建议使用Auto模式。
            canvas.renderMode = AliRtcRenderModeAuto;
        }
        return canvas;
    }

    private ChartUserBean convertRemoteUserInfo(AliRtcRemoteUserInfo remoteUserInfo,
                                                AliRtcEngine.AliRtcVideoCanvas cameraCanvas,
                                                AliRtcEngine.AliRtcVideoCanvas screenCanvas) {
        String uid = remoteUserInfo.getUserID();
        ChartUserBean ret = mUserListAdapter.createDataIfNull(uid);
        ret.mUserId = remoteUserInfo.getUserID();

        ret.mUserName = remoteUserInfo.getDisplayName();

        ret.mCameraSurface = cameraCanvas != null ? cameraCanvas.view : null;
        ret.mIsCameraFlip = cameraCanvas != null && cameraCanvas.mirrorMode == AliRtcEngine.AliRtcRenderMirrorMode.AliRtcRenderMirrorModeAllEnabled;

        ret.mScreenSurface = screenCanvas != null ? screenCanvas.view : null;
        ret.mIsScreenFlip = screenCanvas != null && screenCanvas.mirrorMode == AliRtcEngine.AliRtcRenderMirrorMode.AliRtcRenderMirrorModeAllEnabled;

        return ret;
    }

    private void updateRemoteDisplay(String uid, AliRtcEngine.AliRtcAudioTrack at, AliRtcEngine.AliRtcVideoTrack vt) {
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null == mAliRtcEngine) {
                    return;
                }
                AliRtcRemoteUserInfo remoteUserInfo = mAliRtcEngine.getUserInfo(uid);
                // 如果没有，说明已经退出了或者不存在。则不需要添加，并且删除
                if (remoteUserInfo == null) {
                    // remote user exit room
                    Log.e("tag", "updateRemoteDisplay remoteUserInfo = null, uid = " + uid);
                    return;
                }
                //change
                AliRtcEngine.AliRtcVideoCanvas cameraCanvas = remoteUserInfo.getCameraCanvas();
                AliRtcEngine.AliRtcVideoCanvas screenCanvas = remoteUserInfo.getScreenCanvas();
                //视频情况
                if (vt == AliRtcVideoTrackNo) {
                    //没有视频流
                    cameraCanvas = null;
                    screenCanvas = null;
                } else if (vt == AliRtcVideoTrackCamera) {
                    //相机流
                    screenCanvas = null;
                    cameraCanvas = createCanvasIfNull(cameraCanvas);
                    //SDK内部提供进行播放的view
                    mAliRtcEngine.setRemoteViewConfig(cameraCanvas, uid, AliRtcVideoTrackCamera);
                } else if (vt == AliRtcVideoTrackScreen) {
                    //屏幕流
                    cameraCanvas = null;
                    screenCanvas = createCanvasIfNull(screenCanvas);
                    //SDK内部提供进行播放的view
                    mAliRtcEngine.setRemoteViewConfig(screenCanvas, uid, AliRtcVideoTrackScreen);
                } else if (vt == AliRtcVideoTrackBoth) {
                    //多流
                    cameraCanvas = createCanvasIfNull(cameraCanvas);
                    //SDK内部提供进行播放的view
                    mAliRtcEngine.setRemoteViewConfig(cameraCanvas, uid, AliRtcVideoTrackCamera);
                    screenCanvas = createCanvasIfNull(screenCanvas);
                    //SDK内部提供进行播放的view
                    mAliRtcEngine.setRemoteViewConfig(screenCanvas, uid, AliRtcVideoTrackScreen);
                } else {
                    return;
                }
                ChartUserBean chartUserBean = convertRemoteUserInfo(remoteUserInfo, cameraCanvas, screenCanvas);
                mUserListAdapter.updateData(chartUserBean, true);

            }
        });
    }

    /**
     * 设置本地的预览视图的view
     */
    private void initLocalView(SophonSurfaceView view) {
        view.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        view.setZOrderOnTop(false);
        view.setZOrderMediaOverlay(false);
        AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas = new AliRtcEngine.AliRtcVideoCanvas();
        aliVideoCanvas.view = view;
        aliVideoCanvas.renderMode = AliRtcRenderModeAuto;
        if (mAliRtcEngine != null) {
            mAliRtcEngine.setLocalViewConfig(aliVideoCanvas, AliRtcVideoTrackCamera);
        }
    }

    private void startPreview() {
        if (mAliRtcEngine == null) {
            return;
        }
        try {
            mAliRtcEngine.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void joinChannel() {
        if (mAliRtcEngine == null) {
            return;
        }
        //从控制台生成的鉴权信息，具体内容请查阅:https://help.aliyun.com/document_detail/159037.html?spm=a2c4g.11186623.6.580.2a223b03USksim
        AliRtcAuthInfo userInfo = new AliRtcAuthInfo();
        userInfo.setAppId("0jockhhl"); //应用AppID
        userInfo.setNonce("AK-234d8234-face-4aa5-aaa3-b5e42a65cfc2"); //随机码。需要加上前缀AK-
        String[] gslb = new String[]{"https://rgslb.rtc.aliyuncs.com"};
        userInfo.setGslb(gslb);  //服务地址，当前请使用["https://rgslb.rtc.aliyuncs.com"]
        userInfo.setTimestamp(1634800869); //令牌过期时间戳
        userInfo.setToken("6c98e487357a1f9b45b879507e30976eb0943d50dc29e389f6dc9066728a46aa");     //加入频道Token
        userInfo.setChannelId("androidDemoChannel"); //频道ID
        userInfo.setUserId("androidDemoUser");  //用户ID

        // 加入频道，参数1:鉴权信息 参数2:用户名
        mAliRtcEngine.joinChannel(userInfo, "userName");
    }

    private void initRTCEngineAndStartPreview(SophonSurfaceView view) {

        //默认不开启兼容H5
        AliRtcEngine.setH5CompatibleMode(0);
        // 防止初始化过多
        if (mAliRtcEngine == null) {
            //实例化,必须在主线程进行。
            mAliRtcEngine = AliRtcEngine.getInstance(currentActivity);
            //设置事件的回调监听
            mAliRtcEngine.setRtcEngineEventListener(mEventListener);
            //设置接受通知事件的回调
            mAliRtcEngine.setRtcEngineNotify(mEngineNotify);
            // 初始化本地视图
            initLocalView(view);
            //开启预览
            startPreview();
//            //加入频道
            joinChannel();
        }
    }

    /**
     * 特殊错误码回调的处理方法
     *
     * @param error 错误码
     */
    private void processOccurError(int error) {
        switch (error) {
            case ERR_ICE_CONNECTION_HEARTBEAT_TIMEOUT:
            case ERR_SESSION_REMOVED:
                noSessionExit(error);
                break;
            default:
                break;
        }
    }

    /**
     * 错误处理
     *
     * @param error 错误码
     */
    private void noSessionExit(int error) {
        currentActivity.runOnUiThread(() -> new AlertDialog.Builder(currentActivity)
                .setTitle("ErrorCode : " + error)
                .setMessage("发生错误，请退出房间")
                .setPositiveButton("确定", (dialog, which) -> {
                    dialog.dismiss();
                    currentActivity.onBackPressed();
                })
                .create()
                .show());
    }
}
