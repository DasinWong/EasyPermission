package com.dasinwong.easypermission;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionFragment extends Fragment {

    private static final String TAG = "EasyPermission";
    private static final int REQUEST_CODE = 1;
    private static final int OVERLAYS_REQUEST_CODE = 2;
    private static final int INSTALLS_REQUEST_CODE = 3;

    private PermissionListener listener;
    private Map<String, PermissionResult> permissions;
    private Activity activity;

    /**
     * 构造权限请求fragment
     */
    public static PermissionFragment build(Map<String, PermissionResult> permissions, PermissionListener listener) {
        PermissionFragment fragment = new PermissionFragment();
        fragment.setPermissionListener(listener);
        fragment.setPermissions(permissions);
        return fragment;
    }

    /**
     * 设置监听
     */
    public void setPermissionListener(PermissionListener listener) {
        this.listener = listener;
    }

    /**
     * 设置数据源
     */
    public void setPermissions(Map<String, PermissionResult> permissions) {
        this.permissions = permissions;
    }

    /**
     * 开始执行请求逻辑
     */
    public void request(Activity activity) {
        //只能在主线程执行
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("请在主线程申请权限");
        }
        this.activity = activity;
        activity.getFragmentManager()
                .beginTransaction()
                .add(this, activity.getClass().getName())
                .commit();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //关联Activity后检测申请
        checkPermissions();
    }

    /**
     * 动态申请权限
     */
    @TargetApi(23)
    private void checkPermissions() {
        //遍历出为所有未允许权限
        List<String> deniedPermissions = new ArrayList<>();
        for (Map.Entry<String, PermissionResult> entry : permissions.entrySet()) {
            if (PermissionResult.DENIED == entry.getValue()) {
                deniedPermissions.add(entry.getKey());
            }
        }
        if (deniedPermissions.isEmpty()) {
            Log.i(TAG, "没有需要申请的权限");
            if (listener != null) {
                listener.onComplete(permissions);
            }
            getFragmentManager().beginTransaction().remove(this).commit();
            return;
        }
        for (String permission : deniedPermissions) {
            Log.i(TAG, permission + " 权限未允许");
        }
        //判断申请的权限是否仅为悬浮窗
        if (deniedPermissions.size() == 1 && deniedPermissions.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            requestOverlaysPermission();
            return;
        }
        //判断申请的权限是否仅为应用安装
        if (deniedPermissions.size() == 1 && deniedPermissions.contains(Manifest.permission.REQUEST_INSTALL_PACKAGES)) {
            requestInstallPermission();
            return;
        }
        //申请其他的运行时权限
        requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissionArray, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            //遍历所有结果重新赋值给集合
            for (int i = 0; i < permissionArray.length; i++) {
                PermissionResult result = (grantResults[i] == PackageManager.PERMISSION_GRANTED ? PermissionResult.GRANTED : PermissionResult.DENIED);
                Log.i(TAG, permissionArray[i] + " " + result);
                permissions.put(permissionArray[i], result);
            }
            if (listener != null) {
                listener.onComplete(permissions);
            }
            getFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 6.0 以上系统悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestCode == OVERLAYS_REQUEST_CODE) {
            PermissionResult result = (PermissionUtils.checkOverlaysPermission(activity) ? PermissionResult.GRANTED : PermissionResult.DENIED);
            permissions.put(Manifest.permission.SYSTEM_ALERT_WINDOW, result);
        }
        // 8.0 以上应用安装权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && requestCode == INSTALLS_REQUEST_CODE) {
            PermissionResult result = (PermissionUtils.checkInstallPermission(activity) ? PermissionResult.GRANTED : PermissionResult.DENIED);
            permissions.put(Manifest.permission.REQUEST_INSTALL_PACKAGES, result);
        }
        if (listener != null) {
            listener.onComplete(permissions);
        }
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    /**
     * 申请悬浮窗权限
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void requestOverlaysPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
        startActivityForResult(intent, OVERLAYS_REQUEST_CODE);
    }

    /**
     * 申请应用安装权限
     */
    @TargetApi(Build.VERSION_CODES.O)
    public void requestInstallPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + activity.getPackageName()));
        startActivityForResult(intent, INSTALLS_REQUEST_CODE);
    }
}

