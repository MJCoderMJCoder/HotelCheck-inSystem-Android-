package com.lzf.hotelcheckinsystem.util;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by MJCoder on 2018-04-23.
 */

public class OKHttp {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static OkHttpClient client = new OkHttpClient();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String submit(String url, Map<String, String> params, Map<String, File> files) {
        String message = "fail"; // 连接不到服务器，请检查你的网络或稍后重试
        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 设置类型
        builder.setType(MultipartBody.FORM);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        if (files.size() > 0) {
            for (Map.Entry<String, File> entry : files.entrySet()) {
                File temp = entry.getValue();
                builder.addFormDataPart(entry.getKey(), temp.getName(), RequestBody.create(null, temp));
            }
        }
        RequestBody body = builder.build();
        Request request = new Request.Builder().url(url).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            message = response.body().string();
        } catch (IOException e1) {
            System.out.println("获取响应时异常" + e1.getMessage());
        }
        Log.v("submit", url + "：" + message);
        return message;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getData(String url) {
        String message = "fail"; // 连接不到服务器，请检查你的网络或稍后重试
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            message = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v("getData", url + "：" + message);
        return message;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static byte[] getVideo(String url) {
        byte[] bt = null; // 连接不到服务器，请检查你的网络或稍后重试
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            bt = FlowBinary.binary(response.body().byteStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bt;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String submit(String url, String json) {
        String message = "fail"; // 连接不到服务器，请检查你的网络或稍后重试
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(url).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            message = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v("submit", url + "：" + message);
        return message;
    }
}

