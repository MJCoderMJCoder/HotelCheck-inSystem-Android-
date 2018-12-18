package com.lzf.hotelcheckinsystem;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lzf.hotelcheckinsystem.util.OKHttp;
import com.lzf.hotelcheckinsystem.util.SharedPreferences;
import com.lzf.hotelcheckinsystem.util.UrlStr;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText identity;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        identity = findViewById(R.id.identity);
        password = findViewById(R.id.password);
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String identityStr = identity.getText().toString();
                final String passwordStr = password.getText().toString();
                new Thread() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("identity", identityStr);
                            params.put("password", passwordStr);
                            Log.v("params", params + "");
                            String resp = OKHttp.submit(UrlStr.LOGIN, params, new HashMap<String, File>());
                            JSONObject respJsonObject = new JSONObject(resp);
                            if (respJsonObject.getBoolean("success")) {
                                JSONObject data = respJsonObject.getJSONObject("data");
                                SharedPreferences.save(LoginActivity.this, data.getString("id"), data.getString("name"), data.getString("identity"), data.getString("password"), data.getString("type"), data.getString("memo"));
                                startActivity(new Intent(LoginActivity.this, RoomActivity.class));
                                finish();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this, "登录失败；未找到该用户，请先注册", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "连接不到服务器，请检查你的网络或稍后重试", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }.start();
            }
        });
    }
}
