package com.dasinwong.easypermission;

import java.util.Map;

public abstract class PermissionListener {
    public abstract void onComplete(Map<String, PermissionResult> resultMap);
}
