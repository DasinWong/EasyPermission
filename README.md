# EasyPermission
Android 运行时权限申请框架（自动化检测申请）
- 支持4.4以后版本（95%的设备）
- 添加异常判断，便于调试
- 可按需添加权限检测申请
- 6.0悬浮窗与8.0安装应用权限只能分别单独申请
- 可自动化检测权限并申请（上述两个权限默认DENIED）
## 1.如何接入
Project层级下的build.gradle文件
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Module层级下的build.gradle文件
```
dependencies {
    ...
    implementation 'com.github.dasinwong:EasyPermission:1.1'
}
```
## 2.类及其方法介绍
### EasyPermission
权限申请核心类

| 方法 | 描述 |
| :-------------: | :-------------: |
| with | 创建EasyPermission对象的静态方法 |
| add | 添加权限，可连续调用 |
| listen | 添加申请回调监听（选用） |
| request | 申请权限 |
| autoRequest | 自动化检测权限并申请 |
### PermissionListener
抽象类，权限请求后返回请求结果集合
### PermissionResult
| 枚举值 | 描述 |
| :-------------: | :-------------: |
| GRANTED | 允许权限 |
| DENIED | 拒绝申请 |
## 3.使用方法
#### 3.1 添加权限并申请
```
EasyPermission.with(this)
        .add(Manifest.permission.CAMERA)
        .add(Manifest.permission.READ_EXTERNAL_STORAGE)
        .add(Manifest.permission.ACCESS_COARSE_LOCATION)
        .listen(new PermissionListener() {
            @Override
            public void onComplete(Map<String, PermissionResult> resultMap) {
                for (Map.Entry<String, PermissionResult> entry : resultMap.entrySet()) {
                    Log.e("EasyPermission", entry.getKey() + " " + entry.getValue());
                }
            }
        }).request();
```
#### 3.2 自动化申请
```
EasyPermission.with(this).listen(new PermissionListener() {
    @Override
    public void onComplete(Map<String, PermissionResult> resultMap) {
        for (Map.Entry<String, PermissionResult> entry : resultMap.entrySet()) {
            Log.e("EasyPermission", entry.getKey() + " " + entry.getValue());
        }
    }
}).autoRequest();
```
#### 3.3 申请悬浮窗权限
部分手机重启后生效
```
EasyPermission.with(this)
        .add(Manifest.permission.SYSTEM_ALERT_WINDOW)
        .listen(new PermissionListener() {
            @Override
            public void onComplete(Map<String, PermissionResult> resultMap) {
                for (Map.Entry<String, PermissionResult> entry : resultMap.entrySet()) {
                    Log.e("EasyPermission", entry.getKey() + " " + entry.getValue());
                }
            }
        }).request();
```
#### 3.4 申请应用安装权限
```
EasyPermission.with(this)
        .add(Manifest.permission.REQUEST_INSTALL_PACKAGES)
        .listen(new PermissionListener() {
            @Override
            public void onComplete(Map<String, PermissionResult> resultMap) {
                for (Map.Entry<String, PermissionResult> entry : resultMap.entrySet()) {
                    Log.e("EasyPermission", entry.getKey() + " " + entry.getValue());
                }
            }
        }).request();
```
