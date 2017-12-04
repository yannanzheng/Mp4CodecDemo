package com.jephy.mp4codecdemo.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.widget.Toast;

import com.jephy.mp4codecdemo.R;


public class PermissionUtils {
    private static final int permissionRequestCode = 10086;
    private static PermissionCallback permissionRunnable;

    public interface PermissionCallback {
        void hasPermission();

    }

    /**
     * 读取存储权限
     *
     * @param permissionCallback 请求权限回调
     */
    public static void performCodeWithStoragePermission(Activity activity, PermissionUtils
            .PermissionCallback
            permissionCallback) {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission
                .READ_EXTERNAL_STORAGE};
        performCodeWithPermission(activity, permissionCallback, permissions);
    }

    public static void performAudioRecordPermission(Activity activity, PermissionUtils
            .PermissionCallback
            permissionCallback) {
        String[] permissions = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA};
        performCodeWithPermission(activity, permissionCallback, permissions);
    }

    public static void perfromShowWindowButtonPermission(Activity activity, PermissionUtils.PermissionCallback permissionCallback) {
        String[] permissions = {Manifest.permission.SYSTEM_ALERT_WINDOW};
        performCodeWithPermission(activity, permissionCallback, permissions);
    }

    /**
     * Android M运行时权限请求封装
     *
     * @param runnable    请求权限回调
     * @param permissions 请求的权限（数组类型），直接从Manifest中读取相应的值，比如Manifest.permission.WRITE_CONTACTS
     */
    private static void performCodeWithPermission(Activity activity, PermissionCallback
            runnable, @NonNull String... permissions) {
        if (permissions.length == 0) {
            return;
        }
        permissionRunnable = runnable;
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M) || checkPermissionGranted
                (activity, permissions)) {
            if (permissionRunnable != null) {
                permissionRunnable.hasPermission();
                permissionRunnable = null;
            }
        } else {
            //permission has not been granted.
            requestPermission(activity, permissionRequestCode, permissions);
        }
    }

    private static boolean checkPermissionGranted(Activity activity, String[] permissions) {
        for (String p : permissions) {
            if (!selfPermissionGranted(activity, p)) {
                return false;
            }
        }
        return true;
    }

    private static boolean selfPermissionGranted(Activity activity, String permission) {
        // For Android < Android M, self permissions are always granted.
        boolean result = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (AppInfo.targetSdkVersion(activity) >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                result = activity.checkSelfPermission(permission)
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                result = PermissionChecker.checkSelfPermission(activity, permission)
                        == PermissionChecker.PERMISSION_GRANTED;
            }
        }

        return result;
    }


    private static void requestPermission(Activity activity, int requestCode, String[]
            permissions) {
        // Contact permissions have not been granted yet. Request them directly.
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public static boolean onRequestPermissionsResult(Activity activity, int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        if (requestCode == permissionRequestCode) {
            if (verifyPermissions(grantResults)) {
                if (permissionRunnable != null) {
                    permissionRunnable.hasPermission();
                    permissionRunnable = null;
                }
            } else {
                Toast.makeText(activity, R.string.app_name, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    private static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
