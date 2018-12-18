package com.lzf.hotelcheckinsystem.util;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MJCoder on 2018-04-23.
 */

public class SharedPreferences {
    //定义一个保存数据的方法
    public static void save(Context context, String id, String name, String identity, String password, String type, String memo) {
        android.content.SharedPreferences sp = context.getSharedPreferences("HotelCheckinSystem", Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sp.edit();
        editor.putString("id", id);
        editor.putString("name", name);
        editor.putString("identity", identity);
        editor.putString("password", password);
        editor.putString("type", type);
        editor.putString("memo", memo);
        editor.commit();
    }

    //定义一个读取SP文件的方法
    public static Map<String, String> read(Context context) {
        Map<String, String> data = new HashMap<String, String>();
        android.content.SharedPreferences sp = context.getSharedPreferences("HotelCheckinSystem", Context.MODE_PRIVATE);
        data.put("id", sp.getString("id", ""));
        data.put("name", sp.getString("name", ""));
        data.put("identity", sp.getString("identity", ""));
        data.put("password", sp.getString("password", ""));
        data.put("type", sp.getString("type", ""));
        data.put("memo", sp.getString("memo", ""));
        return data;
    }
}
