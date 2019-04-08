package com.dasinwong.easypermission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionUtils {

    /**
     * 系统版本是否在 6.0 以上
     */
    public static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 系统版本是否在 8.0 以上
     */
    public static boolean isOverOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * 是否具有安装权限
     */
    public static boolean checkInstallPermission(Context context) {
        if (isOverOreo()) {
            return context.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

    /**
     * 申请应用安装权限
     */
    @TargetApi(Build.VERSION_CODES.O)
    public static void requestInstallPermission(Activity activity, int requestCode) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 是否具有悬浮窗权限
     */
    public static boolean checkOverlaysPermission(Context context) {
        if (isOverMarshmallow()) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    /**
     * 申请悬浮窗权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static void requestOverlaysPermission(Activity activity, int requestCode) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 获取 AndroidManifest.xml 文件中添加的所有权限集合
     */
    public static List<String> getManifestPermissions(Context context) {
        try {
            String[] permissions = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
            if (permissions != null && permissions.length > 0) {
                return Arrays.asList(permissions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 AndroidManifest.xml 文件中的运行时权限集合
     */
    public static List<String> getManifestAutoPermissions(Context context) {
        List<String> manifestPermissions = getManifestPermissions(context);
        if (manifestPermissions == null || manifestPermissions.isEmpty()) {
            return null;
        }
        List<String> runTimePermissions = Arrays.asList(Permission.RUNTIME_PERMISSIONS);
        List<String> permissions = new ArrayList<>();
        for (String permission : manifestPermissions) {
            if (runTimePermissions.contains(permission)) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    /**
     * 检查权限是否都添加在 AndroidManifest.xml 文件中
     */
    public static void checkPermissionsInManifest(Context context, List<String> permissions) {
        List<String> manifestPermissions = getManifestPermissions(context);
        if (manifestPermissions != null && !manifestPermissions.isEmpty()) {
            for (String permission : permissions) {
                if (!manifestPermissions.contains(permission)) {
                    throw new RuntimeException(permission + " 权限未注册");
                }
            }
        } else {
            throw new RuntimeException("AndroidManifest.xml 文件中未配置任何权限");
        }
    }

    /**
     * 是否具有否个权限
     */
    public static boolean checkPermission(Context context, String permission) {
        return context.checkPermission(permission, Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED;
    }
}
