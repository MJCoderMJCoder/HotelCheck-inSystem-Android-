package com.lzf.hotelcheckinsystem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lzf.hotelcheckinsystem.util.OKHttp;
import com.lzf.hotelcheckinsystem.util.SharedPreferences;
import com.lzf.hotelcheckinsystem.util.UrlStr;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private LinearLayout loading;
    private Button login;
    private Button register;
    private ProgressBar progressBar;
    private EditText ipAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loading = findViewById(R.id.loading);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        progressBar = findViewById(R.id.progressBar);
        ipAddress = findViewById(R.id.ipAddress);

        //请求存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //请求存储权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                //判断是否需要 向用户解释，为什么要申请该权限
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS);
            }
        }
    }

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.login:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.register:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.start:
                UrlStr.serverHost = ipAddress.getText().toString().trim();
                UrlStr.urlPrefix = UrlStr.scheme + "://" + UrlStr.serverHost + ":" + UrlStr.port + "/" + UrlStr.project;
                UrlStr.REGISTER = UrlStr.urlPrefix + "/user/register";
                UrlStr.LOGIN = UrlStr.urlPrefix + "/user/login";
                UrlStr.TEST = UrlStr.urlPrefix + "/user/test";
                UrlStr.VACANT_USER_ROOM = UrlStr.urlPrefix + "/userRoom/vacantOrUserRoom";
                UrlStr.USER_HANDLE_ROOM = UrlStr.urlPrefix + "/userRoom/userHandleRoom";
                UrlStr.GOODS_SELECT = UrlStr.urlPrefix + "/mGoods/select";
                UrlStr.GOODS_FEE = UrlStr.urlPrefix + "/mGoods/fee";
                if (UrlStr.serverHost.length() < 1) {
                    Toast.makeText(this, "请输入你的服务器地址", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    new Thread() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void run() {
                            super.run();
                            try {
                                Map<String, String> params = SharedPreferences.read(MainActivity.this);
                                Log.v("params", params + "");
                                String resp = OKHttp.submit(UrlStr.LOGIN, params, new HashMap<String, File>());
                                JSONObject respJsonObject = new JSONObject(resp);
                                if (respJsonObject.getBoolean("success")) {
                                    JSONObject data = respJsonObject.getJSONObject("data");
                                    SharedPreferences.save(MainActivity.this, data.getString("id"), data.getString("name"), data.getString("identity"), data.getString("password"), data.getString("type"), data.getString("memo"));
                                    startActivity(new Intent(MainActivity.this, RoomActivity.class));
                                    finish();
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            loading.setVisibility(View.GONE);
                                            login.setVisibility(View.VISIBLE);
                                            register.setVisibility(View.VISIBLE);
                                            //                                Toast.makeText(MainActivity.this, "登录失败；未找到该用户，请先注册", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loading.setVisibility(View.GONE);
                                        login.setVisibility(View.VISIBLE);
                                        register.setVisibility(View.VISIBLE);
                                        Toast.makeText(MainActivity.this, "连接不到服务器，请检查你的网络或稍后重试", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }.start();
                }
                break;
            default:
                break;
        }
    }
}
