package com.jephy.mp4codecdemo.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Locale;


public class AppInfo {

    public final static long ANIMATION_DURATION = 100;
    private static String appName;
    private static boolean hasFacebook = false;
    private static boolean hasYoutube = false;
    private static SoftReference<List<ApplicationInfo>> installedPackages;
    private static Context _applicationContext;

    public static SoftReference<List<ApplicationInfo>> getInstalledPackages(Context context) {
        if (installedPackages == null || installedPackages.get() == null) {
            final PackageManager packageManager = context.getPackageManager();//
            List<ApplicationInfo> packageInfoArr = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            installedPackages = new SoftReference<>(packageInfoArr);
        }
        return installedPackages;
    }

    public static AppVersion getAppVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return new AppVersion(info.versionName, info.versionCode);
    }

    public static int targetSdkVersion(Context context) {
        int targetSdkVersion = 0;
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return targetSdkVersion;
    }

    /**
     * 获得SD卡总大小
     *
     * @return
     */
    public static String getSDTotalSize(Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(context, blockSize * totalBlocks);
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return
     */
    public static long getExternalStorageAvailableSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        return stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
    }

    /**
     * 获得机身内存总大小
     *
     * @return
     */
    public static String getRomTotalSize(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(context, blockSize * totalBlocks);
    }

    /**
     * 获得机身可用内存
     *
     * @param context
     * @return
     */
    public static String getRomAvailableSize(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(context, blockSize * availableBlocks);
    }

    public static long getAvailableSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * availableBlocks;
    }

    /**
     *
     * @return 单位是字节
     */
    public static long getAvailableSizeLong() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return blockSize * availableBlocks;
    }


    public static String getPackageName() {
        return _applicationContext.getPackageName();
    }

    public static void setApplicationContext(Context applicationContext) {
        _applicationContext = applicationContext;
    }

    public static Context getApplicationContext() {
        return _applicationContext;
    }

    private static boolean isAppInstalledByPacketName(Context context, String packageName) {
        List<ApplicationInfo> packageInfoArr = getInstalledPackages(context).get();
        if (packageInfoArr != null) {
            for (int i = 0; i < packageInfoArr.size(); i++) {
                String name = packageInfoArr.get(i).packageName.toLowerCase(Locale
                        .getDefault());
                if (name.contains(packageName.toLowerCase(Locale.getDefault()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isFacebookInstalled(Context context) {
//        if (!hasFacebook) {
//            hasFacebook = isAppInstalledByPacketName(context, "com.facebook.katana");
//        }
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
        return intent != null;
    }

    public static boolean isYouTubeInstalled(Context context) {
//        if (!hasYoutube) {
//            hasYoutube = isAppInstalledByPacketName(context, "com.google.android.youtube");
//        }
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
        return intent != null;
    }



    public static boolean isWechatInstalled(Context context) {
        return isAppInstalledByPacketName(context, "com.tencent.mm");
    }

    public static boolean isWeiboInstalled(Context context) {
        return isAppInstalledByPacketName(context, "com.sina.weibo");
    }

    public static boolean isZH(Activity activity) {
        Locale[] locales = Locale.getAvailableLocales();
        String lan = Locale.getDefault().getLanguage();
        if (lan.equals(Locale.SIMPLIFIED_CHINESE.getLanguage())) {
            return true;
        }
        return false;
    }


    public static boolean mediacodecSupport() {
        return !(/*Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP
                || ActivityUtils.isFlymeStatusBar()
                ||*/
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT
                        || Build.HARDWARE.toLowerCase().contains("mt")
                        || Build.HARDWARE.toLowerCase().contains("hi3635")
                        /*|| Build.HARDWARE.toLowerCase().contains("qcon")*/
        );
    }

    /**
     * 用来判断服务是否运行.
     *
     * @param context
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context context, Class className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
        int size = serviceList.size();
        for (int i = 0; i < size; i++) {
            if (serviceList.get(i).service.getClassName().equals(className.getName())) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    public static class AppVersion {
        private String versionName;
        private String versionCode;

        public AppVersion(String versionName, int versionCode) {
            this.versionName = versionName;
            this.versionCode = versionCode + "";
        }

        public String getVersionName() {
            return versionName;
        }

        public String getVersionCode() {
            return versionCode;
        }
    }

}
