package com.lzf.hotelcheckinsystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lzf.hotelcheckinsystem.entity.Goods;
import com.lzf.hotelcheckinsystem.entity.Room;
import com.lzf.hotelcheckinsystem.util.OKHttp;
import com.lzf.hotelcheckinsystem.util.ReusableAdapter;
import com.lzf.hotelcheckinsystem.util.SharedPreferences;
import com.lzf.hotelcheckinsystem.util.UrlStr;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class RoomActivity extends Activity {
    private ListView userRoomList;
    private ListView vacantRoomList;
    private ListView goodList;
    private TextView user;
    private ReusableAdapter<Room> userRoomAdapter;
    private ReusableAdapter<Room> vacantRoomAdapter;
    private ReusableAdapter<Goods> goodAdapter;
    private List<Room> userRooms = new ArrayList<Room>();
    private List<Room> vacantRooms = new ArrayList<Room>();
    private List<Goods> goods = new ArrayList<Goods>();
    private Map<String, String> params;
    private String checkinTime = "19990909";
    private String checkoutTime = "19990909";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        userRoomList = findViewById(R.id.userRoomList);
        vacantRoomList = findViewById(R.id.vacantRoomList);
        goodList = findViewById(R.id.goodList);
        user = findViewById(R.id.user);
        params = SharedPreferences.read(RoomActivity.this);
        user.setText(params.get("name"));
        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.save(RoomActivity.this, "", "", "", "", "", "");
                startActivity(new Intent(RoomActivity.this, MainActivity.class));
                finish();
            }
        });

        vacantRoomAdapter = new ReusableAdapter<Room>(vacantRooms, R.layout.item_vacant_room_list) {
            @Override
            public void bindView(ViewHolder holder, final Room obj) {
                holder.setText(R.id.name, obj.getName());
                holder.setText(R.id.fewHuman, obj.getFewHuman() + "人间");
                holder.setText(R.id.type, obj.getType());
                holder.setText(R.id.price, obj.getPrice() + "");
                holder.setOnClickListener(R.id.book, new View.OnClickListener() { //预订入住
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(View view) {
                        //初始化Builder
                        AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this);
                        //加载自定义的那个View,同时设置下
                        final LayoutInflater inflater = RoomActivity.this.getLayoutInflater();
                        View alertView = inflater.inflate(R.layout.alert_dialog, null, false);
                        builder.setView(alertView);
                        builder.setCancelable(false);
                        final AlertDialog alert = builder.create();
                        final TextView fee = alertView.findViewById(R.id.fee);
                        DatePicker checkinDatePicker = alertView.findViewById(R.id.checkinDatePicker);
                        checkinDatePicker.setMinDate(System.currentTimeMillis());
                        Calendar calendarCheckin = Calendar.getInstance();//创建日历对象
                        checkinDatePicker.init(calendarCheckin.get(Calendar.YEAR), calendarCheckin.get(Calendar.MONTH), calendarCheckin.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                            @Override
                            public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                                checkinTime = year + "" + (monthOfYear + 1) + "" + dayOfMonth;
                                Log.v("checkoutTime", checkoutTime);
                                if (checkoutTime.equals("19990909")) {
                                    Toast.makeText(RoomActivity.this, "记得选择退房时间。", Toast.LENGTH_SHORT).show();
                                } else {
                                    if ((Integer.parseInt(checkoutTime) - Integer.parseInt(checkinTime)) > 0) {
                                        fee.setText("总费用：" + (Integer.parseInt(checkoutTime) - Integer.parseInt(checkinTime)) * obj.getPrice());
                                    } else {
                                        Toast.makeText(RoomActivity.this, "退房时间不能早于入住时间。", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                        //                        checkinDatePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                        //                            @Override
                        //                            public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        //                            }
                        //                        });
                        DatePicker checkoutDatePicker = alertView.findViewById(R.id.checkoutDatePicker);
                        checkoutDatePicker.setMinDate(System.currentTimeMillis());
                        Calendar calendarCheckout = Calendar.getInstance();//创建日历对象
                        checkoutDatePicker.init(calendarCheckout.get(Calendar.YEAR), calendarCheckout.get(Calendar.MONTH), calendarCheckout.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                            @Override
                            public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                                checkoutTime = year + "" + (monthOfYear + 1) + "" + dayOfMonth;
                                Log.v("checkoutTime", checkoutTime);
                                if (checkinTime.equals("19990909")) {
                                    Toast.makeText(RoomActivity.this, "记得选择入住时间。", Toast.LENGTH_SHORT).show();
                                } else {
                                    if ((Integer.parseInt(checkoutTime) - Integer.parseInt(checkinTime)) > 0) {
                                        fee.setText("总费用：" + (Integer.parseInt(checkoutTime) - Integer.parseInt(checkinTime)) * obj.getPrice());
                                    } else {
                                        Toast.makeText(RoomActivity.this, "退房时间不能早于入住时间。", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                        //                        checkoutDatePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                        //                            @Override
                        //                            public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        //                            }
                        //                        });
                        alertView.findViewById(R.id.pay).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (fee.getText().toString().equals("总费用：0")) {
                                    Toast.makeText(RoomActivity.this, "请先选择入住时间和退房时间（不能是同一天哦）。", Toast.LENGTH_SHORT).show();
                                } else {
                                    new Thread() {
                                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                        @Override
                                        public void run() {
                                            super.run();
                                            try {
                                                String resp = OKHttp.getData(UrlStr.USER_HANDLE_ROOM + "?price=" + obj.getPrice() + "&fewHuman=" + obj.getFewHuman() + "&status=1" + "&userId=" + params.get("id") + "&type=" + obj.getType() + "&roomId=" + obj.getId() + "&checkinTime=" + checkinTime + "&checkoutTime=" + checkoutTime);
                                                JSONObject jsonObject = new JSONObject(resp);
                                                if (jsonObject.getBoolean("success")) {
                                                    refreshRoomList();
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(RoomActivity.this, "恭喜你，预订成功。", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                                } else {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(RoomActivity.this, "预订失败，请稍后重试", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                    alert.dismiss();
                                }
                            }
                        });
                        alertView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alert.dismiss();
                            }
                        });
                        alert.show();                    //显示对话框
                    }
                });
            }
        };

        userRoomAdapter = new ReusableAdapter<Room>(userRooms, R.layout.item_user_room_list) {
            @Override
            public void bindView(ViewHolder holder, final Room obj) {
                holder.setText(R.id.name, obj.getName());
                holder.setText(R.id.fewHuman, obj.getFewHuman() + "人间");
                holder.setText(R.id.type, obj.getType());
                holder.setText(R.id.price, obj.getPrice() + "");
                holder.setOnClickListener(R.id.checkOut, new View.OnClickListener() { //退房
                    @Override
                    public void onClick(View view) {
                        new Thread() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    String resp = OKHttp.getData(UrlStr.USER_HANDLE_ROOM + "?price=" + obj.getPrice() + "&fewHuman=" + obj.getFewHuman() + "&status=0" + "&userId=" + params.get("id") + "&type=" + obj.getType() + "&roomId=" + obj.getId() + "&checkinTime=" + checkinTime + "&checkoutTime=" + checkoutTime);
                                    JSONObject jsonObject = new JSONObject(resp);
                                    if (jsonObject.getBoolean("success")) {
                                        refreshRoomList();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(RoomActivity.this, "恭喜你，退房成功。", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(RoomActivity.this, "退房失败，请稍后重试", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                });
            }
        };

        goodAdapter = new ReusableAdapter<Goods>(goods, R.layout.item_good_list) {
            @Override
            public void bindView(ViewHolder holder, final Goods obj) {
                holder.setText(R.id.type, obj.getName());
                holder.setText(R.id.price, obj.getPrice() + "");
                holder.setOnClickListener(R.id.buy, new View.OnClickListener() { //预订入住
                    @Override
                    public void onClick(View view) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog alert = null;
                                AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this);
                                alert = builder.setTitle("模拟支付").setMessage("费用：" + obj.getPrice())
                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .setPositiveButton("确认支付", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                new Thread() {
                                                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                                    @Override
                                                    public void run() {
                                                        super.run();
                                                        try {
                                                            String resp = OKHttp.getData(UrlStr.GOODS_FEE + "?fee=" + obj.getPrice() + "&memo=" + obj.getName());
                                                            JSONObject jsonObject = new JSONObject(resp);
                                                            if (jsonObject.getBoolean("success")) {
                                                                refreshRoomList();
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Toast.makeText(RoomActivity.this, "恭喜你，购买成功。", Toast.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                            } else {
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Toast.makeText(RoomActivity.this, "购买失败，请稍后重试", Toast.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }.start();
                                            }
                                        })
                                        .create();             //创建AlertDialog对象
                                alert.show();                    //显示对话框
                            }
                        });
                    }
                });
            }
        };

        userRoomList.setAdapter(userRoomAdapter);
        vacantRoomList.setAdapter(vacantRoomAdapter);
        goodList.setAdapter(goodAdapter);
        refreshRoomList();
    }

    private void refreshRoomList() {
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                super.run();
                try {
                    userRooms.clear();
                    vacantRooms.clear();
                    String resp = OKHttp.getData(UrlStr.VACANT_USER_ROOM + "?userId=" + params.get("id"));
                    JSONObject respJsonObject = new JSONObject(resp);
                    if (respJsonObject.getBoolean("success")) {
                        JSONArray dataArray = respJsonObject.getJSONArray("data");
                        JSONArray vacantRoomArray = dataArray.getJSONArray(0);
                        for (int j = 0; j < vacantRoomArray.length(); j++) {
                            JSONObject room = vacantRoomArray.getJSONObject(j);
                            vacantRooms.add(0, new Room(room.getInt("id"), room.getString("name"), Float.parseFloat(room.getString("price")), room.getInt("fewHuman"), room.getInt("status"), room.getInt("userId"), null, new Timestamp(room.getLong("checkinTime")), room.getString("type"), new Timestamp(room.getLong("checkoutTime"))));
                        }
                        JSONArray userRoomArray = dataArray.getJSONArray(1);
                        for (int j = 0; j < userRoomArray.length(); j++) {
                            JSONObject room = userRoomArray.getJSONObject(j);
                            userRooms.add(0, new Room(room.getInt("id"), room.getString("name"), Float.parseFloat(room.getString("price")), room.getInt("fewHuman"), room.getInt("status"), room.getInt("userId"), null, new Timestamp(room.getLong("checkinTime")), room.getString("type"), new Timestamp(room.getLong("checkoutTime"))));
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                userRoomAdapter.notifyDataSetChanged();
                                vacantRoomAdapter.notifyDataSetChanged();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RoomActivity.this, "房间已满，暂无空房。", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                super.run();
                try {
                    goods.clear();
                    String resp = OKHttp.getData(UrlStr.GOODS_SELECT);
                    JSONObject respJsonObject = new JSONObject(resp);
                    if (respJsonObject.getBoolean("success")) {
                        JSONArray dataArray = respJsonObject.getJSONArray("data");
                        for (int j = 0; j < dataArray.length(); j++) {
                            JSONObject room = dataArray.getJSONObject(j);
                            goods.add(0, new Goods(room.getInt("id"), room.getString("name"), Float.parseFloat(room.getString("price"))));
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                goodAdapter.notifyDataSetChanged();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RoomActivity.this, "暂无服务。", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
