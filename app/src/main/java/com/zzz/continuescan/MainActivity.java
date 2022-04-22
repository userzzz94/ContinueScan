package com.zzz.continuescan;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.zzz.zxinglibrary.android.CaptureActivity;
import com.zzz.zxinglibrary.bean.ZxingConfig;
import com.zzz.zxinglibrary.common.Constant;

public class MainActivity extends AppCompatActivity {

    private TextView mTv1;
    private Button mBtn1;
    private Button mBtn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //注册广播
        registerReceiver(scanDataReceiver, new IntentFilter(Constant.CAMERA_SCAN_ACTION));
        initView();
    }

    private void initView() {
        mTv1 = findViewById(R.id.tv1);
        mBtn1 = findViewById(R.id.btn1);
        mBtn2 = findViewById(R.id.btn2);

        mBtn1.setOnClickListener(view -> {
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
                    .onGranted(data -> {
                        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                        startActivityForResult(intent, 0);
                    })
                    .onDenied(data -> {
                        Uri packageURI = Uri.parse("package:" + getPackageName());
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                        Toast.makeText(MainActivity.this, "没有权限无法扫描呦", Toast.LENGTH_LONG).show();
                    })
                    .start();
        });

        mBtn2.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
            ZxingConfig config = new ZxingConfig();
            config.setContinueScan(true);
            intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
            startActivityForResult(intent, 0);
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消注册
        unregisterReceiver(scanDataReceiver);
    }

    private final BroadcastReceiver scanDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.CAMERA_SCAN_ACTION)) {
                try {
                    String message = intent.getStringExtra(Constant.CAMERA_SCAN_RESULT).trim();
                    mTv1.setText(message);
                    Intent intentWarn = new Intent();
                    intentWarn.setAction(Constant.WARN_SCAN_ACTION);
                    intentWarn.putExtra(Constant.WARN_SCAN_RESULT, "这是错误信息");
                    sendBroadcast(intentWarn);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 0) {
                if (data != null) {
                    String result = data.getStringExtra(Constant.CODED_CONTENT);
                    mTv1.setText(result);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("提示：")
                            .setCancelable(false)
                            .setMessage("请重新扫码!")
                            .setPositiveButton("确定", (dialog, which) -> dialog.cancel()).create().show();
                }

            }
        }
    }
}