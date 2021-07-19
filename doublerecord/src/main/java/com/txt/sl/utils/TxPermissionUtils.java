package com.txt.sl.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;


import com.txt.sl.TXSdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by JustinWjq
 *
 * @date 2020/9/3.
 * description：
 */
public final class TxPermissionUtils {

    private static final List<String> PERMISSIONS = getPermissions();

    private static TxPermissionUtils sInstance;

    private OnRationaleListener mOnRationaleListener;
    private SimpleCallback mSimpleCallback;
    private FullCallback mFullCallback;
    private ThemeCallback mThemeCallback;
    private Set<String> mPermissions;
    private List<String>        mPermissionsRequest;
    private List<String>        mPermissionsGranted;
    private List<String>        mPermissionsDenied;
    private List<String>        mPermissionsDeniedForever;

    private static SimpleCallback sSimpleCallback4WriteSettings;
    private static SimpleCallback sSimpleCallback4DrawOverlays;

    /**
     * Return the permissions used in application.
     *
     * @return the permissions used in application
     */
    public static List<String> getPermissions() {
        return getPermissions(TXSdk.getInstance().application.getPackageName());
    }

    /**
     * Return the permissions used in application.
     *
     * @param packageName The name of the package.
     * @return the permissions used in application
     */
    public static List<String> getPermissions(final String packageName) {
        PackageManager pm = TXSdk.getInstance().application.getPackageManager();
        try {
            String[] permissions = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS).requestedPermissions;
            if (permissions == null) return Collections.emptyList();
            return Arrays.asList(permissions);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Return whether <em>you</em> have been granted the permissions.
     *
     * @param permissions The permissions.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isGranted(final String... permissions) {
        for (String permission : permissions) {
            if (!isGranted(permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isGranted(final String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || PackageManager.PERMISSION_GRANTED
                == ContextCompat.checkSelfPermission(TXSdk.getInstance().application, permission);
    }

    /**
     * Return whether the app can modify system settings.
     *
     * @return {@code true}: yes<br>{@code false}: no
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isGrantedWriteSettings() {
        return Settings.System.canWrite(TXSdk.getInstance().application);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestWriteSettings(final SimpleCallback callback) {
        if (isGrantedWriteSettings()) {
            if (callback != null) callback.onGranted();
            return;
        }
        sSimpleCallback4WriteSettings = callback;
        PermissionActivityImpl.start(PermissionActivityImpl.TYPE_WRITE_SETTINGS);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static void startWriteSettingsActivity(final Activity activity, final int requestCode) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + TXSdk.getInstance().application.getPackageName()));
        if (!isIntentAvailable(intent)) {
            launchAppDetailsSettings();
            return;
        }
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Return whether the app can draw on top of other apps.
     *
     * @return {@code true}: yes<br>{@code false}: no
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isGrantedDrawOverlays() {
        return Settings.canDrawOverlays(TXSdk.getInstance().application);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestDrawOverlays(final SimpleCallback callback) {
        if (isGrantedDrawOverlays()) {
            if (callback != null) callback.onGranted();
            return;
        }
        sSimpleCallback4DrawOverlays = callback;
        PermissionActivityImpl.start(PermissionActivityImpl.TYPE_DRAW_OVERLAYS);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static void startOverlayPermissionActivity(final Activity activity, final int requestCode) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + TXSdk.getInstance().application.getPackageName()));
        if (!isIntentAvailable(intent)) {
            launchAppDetailsSettings();
            return;
        }
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Launch the application's details settings.
     */
    public static void launchAppDetailsSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + TXSdk.getInstance().application.getPackageName()));
        if (!isIntentAvailable(intent)) return;
        TXSdk.getInstance().application.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /**
     * Set the permissions.
     *
     * @param permissions The permissions.
     * @return the single {@link TxPermissionUtils} instance
     */
    public static TxPermissionUtils permission(@TxPermissionConstants.Permission final String... permissions) {
        return new TxPermissionUtils(permissions);
    }

    private static boolean isIntentAvailable(final Intent intent) {
        return TXSdk.getInstance().application
                .getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .size() > 0;
    }

    private TxPermissionUtils(final String... permissions) {
        mPermissions = new LinkedHashSet<>();
        for (String permission : permissions) {
            for (String aPermission : TxPermissionConstants.getPermissions(permission)) {
                if (PERMISSIONS.contains(aPermission)) {
                    mPermissions.add(aPermission);
                }
            }
        }
        sInstance = this;
    }

    /**
     * Set rationale listener.
     *
     * @param listener The rationale listener.
     * @return the single {@link TxPermissionUtils} instance
     */
    public TxPermissionUtils rationale(final OnRationaleListener listener) {
        mOnRationaleListener = listener;
        return this;
    }

    /**
     * Set the simple call back.
     *
     * @param callback the simple call back
     * @return the single {@link TxPermissionUtils} instance
     */
    public TxPermissionUtils callback(final SimpleCallback callback) {
        mSimpleCallback = callback;
        return this;
    }

    /**
     * Set the full call back.
     *
     * @param callback the full call back
     * @return the single {@link TxPermissionUtils} instance
     */
    public TxPermissionUtils callback(final FullCallback callback) {
        mFullCallback = callback;
        return this;
    }

    /**
     * Set the theme callback.
     *
     * @param callback The theme callback.
     * @return the single {@link TxPermissionUtils} instance
     */
    public TxPermissionUtils theme(final ThemeCallback callback) {
        mThemeCallback = callback;
        return this;
    }

    /**
     * Start request.
     */
    public void request() {
        mPermissionsGranted = new ArrayList<>();
        mPermissionsRequest = new ArrayList<>();
        mPermissionsDenied = new ArrayList<>();
        mPermissionsDeniedForever = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mPermissionsGranted.addAll(mPermissions);
            requestCallback();
        } else {
            for (String permission : mPermissions) {
                if (isGranted(permission)) {
                    mPermissionsGranted.add(permission);
                } else {
                    mPermissionsRequest.add(permission);
                }
            }
            if (mPermissionsRequest.isEmpty()) {
                requestCallback();
            } else {
                startPermissionActivity();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startPermissionActivity() {
        PermissionActivityImpl.start(PermissionActivityImpl.TYPE_RUNTIME);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean rationale(final Activity activity) {
        boolean isRationale = false;
        if (mOnRationaleListener != null) {
            for (String permission : mPermissionsRequest) {
                if (activity.shouldShowRequestPermissionRationale(permission)) {
                    getPermissionsStatus(activity);
                    mOnRationaleListener.rationale(new OnRationaleListener.ShouldRequest() {
                        @Override
                        public void again(boolean again) {
                            activity.finish();
                            if (again) {
                                mPermissionsDenied = new ArrayList<>();
                                mPermissionsDeniedForever = new ArrayList<>();
                                startPermissionActivity();
                            } else {
                                requestCallback();
                            }
                        }
                    });
                    isRationale = true;
                    break;
                }
            }
            mOnRationaleListener = null;
        }
        return isRationale;
    }

    private void getPermissionsStatus(final Activity activity) {
        for (String permission : mPermissionsRequest) {
            if (isGranted(permission)) {
                mPermissionsGranted.add(permission);
            } else {
                mPermissionsDenied.add(permission);
                if (!activity.shouldShowRequestPermissionRationale(permission)) {
                    mPermissionsDeniedForever.add(permission);
                }
            }
        }
    }

    private void requestCallback() {
        if (mSimpleCallback != null) {
            if (mPermissionsRequest.size() == 0
                    || mPermissions.size() == mPermissionsGranted.size()) {
                mSimpleCallback.onGranted();
            } else {
                if (!mPermissionsDenied.isEmpty()) {
                    mSimpleCallback.onDenied();
                }
            }
            mSimpleCallback = null;
        }
        if (mFullCallback != null) {
            if (mPermissionsRequest.size() == 0
                    || mPermissionsGranted.size() > 0) {
                mFullCallback.onGranted(mPermissionsGranted);
            }
            if (!mPermissionsDenied.isEmpty()) {
                mFullCallback.onDenied(mPermissionsDeniedForever, mPermissionsDenied);
            }
            mFullCallback = null;
        }
        mOnRationaleListener = null;
        mThemeCallback = null;
    }

    private void onRequestPermissionsResult(final Activity activity) {
        getPermissionsStatus(activity);
        requestCallback();
    }
    public static final class TransActivity extends FragmentActivity {

        private static final Map<TransActivity, TransActivityDelegate> CALLBACK_MAP = new HashMap<>();
        private static TransActivityDelegate sDelegate;

        public static void start(final Func1<Void, Intent> consumer,
                                 final TransActivityDelegate delegate) {
            if (delegate == null) return;
            Intent starter = new Intent(TXSdk.getInstance().application, TransActivity.class);
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (consumer != null) {
                consumer.call(starter);
            }
            TXSdk.getInstance().application.startActivity(starter);
            sDelegate = delegate;
        }

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            overridePendingTransition(0, 0);
            if (sDelegate == null) {
                super.onCreate(savedInstanceState);
                finish();
                return;
            }
            CALLBACK_MAP.put(this, sDelegate);
            sDelegate.onCreateBefore(this, savedInstanceState);
            super.onCreate(savedInstanceState);
            sDelegate.onCreated(this, savedInstanceState);
            sDelegate = null;
        }

        @Override
        protected void onStart() {
            super.onStart();
            TransActivityDelegate callback = CALLBACK_MAP.get(this);
            if (callback == null) return;
            callback.onStarted(this);
        }

        @Override
        protected void onResume() {
            super.onResume();
            TransActivityDelegate callback = CALLBACK_MAP.get(this);
            if (callback == null) return;
            callback.onResumed(this);
        }

        @Override
        protected void onPause() {
            overridePendingTransition(0, 0);
            super.onPause();
            TransActivityDelegate callback = CALLBACK_MAP.get(this);
            if (callback == null) return;
            callback.onPaused(this);
        }

        @Override
        protected void onStop() {
            super.onStop();
            TransActivityDelegate callback = CALLBACK_MAP.get(this);
            if (callback == null) return;
            callback.onStopped(this);
        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            TransActivityDelegate callback = CALLBACK_MAP.get(this);
            if (callback == null) return;
            callback.onSaveInstanceState(this, outState);
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            TransActivityDelegate callback = CALLBACK_MAP.get(this);
            if (callback == null) return;
            callback.onDestroy(this);
            CALLBACK_MAP.remove(this);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            TransActivityDelegate callback = CALLBACK_MAP.get(this);
            if (callback == null) return;
            callback.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            TransActivityDelegate callback = CALLBACK_MAP.get(this);
            if (callback == null) return;
            callback.onActivityResult(this, requestCode, resultCode, data);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            TransActivityDelegate callback = CALLBACK_MAP.get(this);
            if (callback == null) return super.dispatchTouchEvent(ev);
            if (callback.dispatchTouchEvent(this, ev)) {
                return true;
            }
            return super.dispatchTouchEvent(ev);
        }

        public abstract static class TransActivityDelegate {
            public void onCreateBefore(Activity activity, @Nullable Bundle savedInstanceState) {/**/}

            public void onCreated(Activity activity, @Nullable Bundle savedInstanceState) {/**/}

            public void onStarted(Activity activity) {/**/}

            public void onDestroy(Activity activity) {/**/}

            public void onResumed(Activity activity) {/**/}

            public void onPaused(Activity activity) {/**/}

            public void onStopped(Activity activity) {/**/}

            public void onSaveInstanceState(Activity activity, Bundle outState) {/**/}

            public void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {/**/}

            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {/**/}

            public boolean dispatchTouchEvent(Activity activity, MotionEvent ev) {
                return false;
            }
        }
    }
    private static final Handler UTIL_HANDLER       = new Handler(Looper.getMainLooper());
    public static void runOnUiThreadDelayed(final Runnable runnable, long delayMillis) {
        UTIL_HANDLER.postDelayed(runnable, delayMillis);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    static final class PermissionActivityImpl extends TransActivity.TransActivityDelegate {

        private static final String TYPE                = "TYPE";
        private static final int    TYPE_RUNTIME        = 0x01;
        private static final int    TYPE_WRITE_SETTINGS = 0x02;
        private static final int    TYPE_DRAW_OVERLAYS  = 0x03;

        private static PermissionActivityImpl INSTANCE = new PermissionActivityImpl();

        public static void start(final int type) {
            TransActivity.start(new Func1<Void, Intent>() {
                @Override
                public Void call(Intent data) {
                    data.putExtra(TYPE, type);
                    return null;
                }
            }, INSTANCE);
        }

        @Override
        public void onCreated(Activity activity, @Nullable Bundle savedInstanceState) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
            int type = activity.getIntent().getIntExtra(TYPE, -1);
            if (type == TYPE_RUNTIME) {
                if (sInstance == null) {
                    Log.e("PermissionUtils", "request permissions failed");
                    activity.finish();
                    return;
                }
                if (sInstance.mThemeCallback != null) {
                    sInstance.mThemeCallback.onActivityCreate(activity);
                }
                if (sInstance.rationale(activity)) {
                    return;
                }
                if (sInstance.mPermissionsRequest != null) {
                    int size = sInstance.mPermissionsRequest.size();
                    if (size <= 0) {
                        activity.finish();
                        return;
                    }
                    activity.requestPermissions(sInstance.mPermissionsRequest.toArray(new String[size]), 1);
                }
            } else if (type == TYPE_WRITE_SETTINGS) {
                startWriteSettingsActivity(activity, TYPE_WRITE_SETTINGS);
            } else if (type == TYPE_DRAW_OVERLAYS) {
                startOverlayPermissionActivity(activity, TYPE_DRAW_OVERLAYS);
            } else {
                activity.finish();
                Log.e("PermissionUtils", "type is wrong.");
            }
        }

        @Override
        public void onRequestPermissionsResult(Activity activity,
                                               int requestCode,
                                               String[] permissions,
                                               int[] grantResults) {
            if (sInstance != null && sInstance.mPermissionsRequest != null) {
                sInstance.onRequestPermissionsResult(activity);
            }
            activity.finish();
        }


        @Override
        public boolean dispatchTouchEvent(Activity activity, MotionEvent ev) {
            activity.finish();
            return true;
        }

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            if (requestCode == TYPE_WRITE_SETTINGS) {
                if (sSimpleCallback4WriteSettings == null) return;
                if (isGrantedWriteSettings()) {
                    sSimpleCallback4WriteSettings.onGranted();
                } else {
                    sSimpleCallback4WriteSettings.onDenied();
                }
                sSimpleCallback4WriteSettings = null;
            } else if (requestCode == TYPE_DRAW_OVERLAYS) {
                if (sSimpleCallback4DrawOverlays == null) return;
                runOnUiThreadDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isGrantedDrawOverlays()) {
                            sSimpleCallback4DrawOverlays.onGranted();
                        } else {
                            sSimpleCallback4DrawOverlays.onDenied();
                        }
                        sSimpleCallback4DrawOverlays = null;
                    }
                }, 100);
            }
            activity.finish();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // interface
    ///////////////////////////////////////////////////////////////////////////

    public interface OnRationaleListener {

        void rationale(ShouldRequest shouldRequest);

        interface ShouldRequest {
            void again(boolean again);
        }
    }

    public interface SimpleCallback {
        void onGranted();

        void onDenied();
    }

    public interface FullCallback {
        void onGranted(List<String> permissionsGranted);

        void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied);
    }

    public interface ThemeCallback {
        void onActivityCreate(Activity activity);
    }

    public interface Func1<Ret, Par> {
        Ret call(Par param);
    }
}