package com.rnfullscreen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;

import com.facebook.react.modules.core.DeviceEventManagerModule;

/**
 * {@link NativeModule} that allows changing the appearance of the menu bar.
 */
public class RNFullscreenModule extends ReactContextBaseJavaModule {
  private static final String ERROR_NO_ACTIVITY = "E_NO_ACTIVITY";
  private static final String ERROR_NO_ACTIVITY_MESSAGE =
    "Tried to set fullscreen while not attached to an Activity";
  private static final int UI_FLAG_FULLSCREEN = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar

  private static RNFullscreenModule SINGLETON = null;

  private ReactContext _reactContext = null;
  private boolean _isFullscreenOn = false;

  public static RNFullscreenModule getInstance () {
    return SINGLETON;
  }

  public RNFullscreenModule(ReactApplicationContext reactContext) {
    super(reactContext);

    _reactContext = reactContext;
    SINGLETON = this;
  }

  @Override
  public void onCatalystInstanceDestroy() {
    _reactContext = null;
    SINGLETON = null;
  }

  @Override
  public String getName() {
    return "RNFullscreen";
  }

  @ReactMethod
  public void setFullscreen(final boolean isOn, final Promise res) {
    _setFullscreen(isOn, res);
  }
  @ReactMethod
  public void getFullscreen(final Promise res) {
    _getFullscreen(res);
  }
  @ReactMethod
  public void addFullscreenListener() {
    _addFullscreenListener();
  }

  public void emitFullscreenStateChangeEvent() {
    if (_reactContext != null) {
      _reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit("@@FULLSCREEN_STATE_CHANGED", null);
    }
  }

  private void _setFullscreen(final boolean isOn, final Promise res) {
    final Activity activity = getCurrentActivity();
    if (activity == null) {
      res.reject(ERROR_NO_ACTIVITY, ERROR_NO_ACTIVITY_MESSAGE);
      return;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      UiThreadUtil.runOnUiThread(new Runnable() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
          _isFullscreenOn = isOn;
          activity.getWindow().getDecorView().setSystemUiVisibility(isOn ?
            UI_FLAG_FULLSCREEN : View.SYSTEM_UI_FLAG_VISIBLE);
          res.resolve(null);
        }
      });
    }
  }

  private void _getFullscreen(final Promise res) {
    final Activity activity = getCurrentActivity();
    if (activity == null) {
      res.reject(ERROR_NO_ACTIVITY, ERROR_NO_ACTIVITY_MESSAGE);
      return;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      UiThreadUtil.runOnUiThread(new Runnable() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
          int visibility = activity.getWindow()
            .getDecorView()
            .getSystemUiVisibility();
          boolean isFullscreenOn = 0 != (visibility & UI_FLAG_FULLSCREEN);

          WritableMap map = Arguments.createMap();
          map.putBoolean("isFullscreenOn", isFullscreenOn);

          res.resolve(map);
        }
      });
    }
  }

  private void _addFullscreenListener() {
    final Activity activity = getCurrentActivity();
    if (activity == null) return;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      UiThreadUtil.runOnUiThread(new Runnable() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
          activity.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
              boolean isFullscreenOn = 0 != (visibility & UI_FLAG_FULLSCREEN);

              if (isFullscreenOn != _isFullscreenOn) {
                emitFullscreenStateChangeEvent();
              }
            }
          });
        }
      });
    }
  }
}
