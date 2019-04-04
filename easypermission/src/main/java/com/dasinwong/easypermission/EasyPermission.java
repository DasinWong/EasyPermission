package com.dasinwong.easypermission;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EasyPermission {

    private static final String TAG = "EasyPermission";
    private Activity activity;
    private LinkedList<String> permissions;
    private Map<String, PermissionResult> permissionMap;
    private PermissionListener listener;

    private EasyPermission(Activity activity) {
        this.activity = activity;
        permissions = new LinkedList<>();
        permissionMap = new HashMap<>();
    }

    public static EasyPermission with(Activity activity) {
        return new EasyPermission(activity);
    }

    /**
     * 添加一个权限
     */
    public EasyPermission add(String permission) {
        if (!TextUtils.isEmpty(permission)) {
            permissions.add(permission);
        }
        return this;
    }

    /**
     * 设置监听
     */
    public EasyPermission listen(PermissionListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 请求权限
     */
    public void request() {
        //6.0以下默认返回允许
        if (!PermissionUtils.isOverMarshmallow()) {
            Log.i(TAG, "当前系统版本 " + Build.VERSION.SDK_INT);
            HashMap<String, PermissionResult> resultMap = new HashMap<>();
            for (String permission : permissions) {
                resultMap.put(permission, PermissionResult.GRANTED);
            }
            if (listener != null) {
                listener.onComplete(resultMap);
            }
            return;
        }
        //检测所有权限都在 AndroidManifest.xml 文件中配置过
        PermissionUtils.checkPermissionsInManifest(activity, permissions);
        checkPermission();
    }

    /**
     * 一键申请
     */
    public void autoRequest() {
        //获取 AndroidManifest.xml 文件中配置的动态权限集合
        List<String> permissionList = PermissionUtils.getManifestAutoPermissions(activity);
        if (permissionList == null || permissionList.isEmpty()) {
            Log.i(TAG, "没有需要申请的权限");
            return;
        }
        permissions.clear();
        permissions.addAll(permissionList);
        request();
    }

    /**
     * 检测权限并申请
     */
    private void checkPermission() {
        //是否检测完所有权限
        if (permissions.isEmpty()) {
            PermissionFragment.build(permissionMap, listener).request(activity);
            return;
        }
        //循环检测权限
        String permission = permissions.pollFirst();
        switch (permission) {
            case Manifest.permission.REQUEST_INSTALL_PACKAGES:
                if (PermissionUtils.checkInstallPermission(activity)) {
                    permissionMap.put(permission, PermissionResult.GRANTED);
                } else {
                    permissionMap.put(permission, PermissionResult.DENIED);
                }
                break;
            case Manifest.permission.SYSTEM_ALERT_WINDOW:
                if (PermissionUtils.checkOverlaysPermission(activity)) {
                    permissionMap.put(permission, PermissionResult.GRANTED);
                } else {
                    permissionMap.put(permission, PermissionResult.DENIED);
                }
                break;
            default:
                if (PermissionUtils.checkPermission(activity, permission)) {
                    permissionMap.put(permission, PermissionResult.GRANTED);
                } else {
                    permissionMap.put(permission, PermissionResult.DENIED);
                }
                break;
        }
        checkPermission();
    }
}
