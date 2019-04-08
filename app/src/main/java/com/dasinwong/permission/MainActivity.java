package com.dasinwong.permission;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dasinwong.easypermission.EasyPermission;
import com.dasinwong.easypermission.PermissionListener;
import com.dasinwong.easypermission.PermissionResult;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EasyPermission.with(this).listen(new PermissionListener() {
            @Override
            public void onComplete(Map<String, PermissionResult> resultMap) {
                for (Map.Entry<String, PermissionResult> entry : resultMap.entrySet()) {
                    Log.e("EasyPermission", entry.getKey() + " " + entry.getValue());
                }
            }
        }).autoRequest();

    }
}
