package com.dasinwong.easypermission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionFragment extends Fragment {

    private static final String TAG = "EasyPermission";
    private static final int REQUEST_CODE = 1;

    private PermissionListener listener;
    private Map<String, PermissionResult> permissions;

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
}

