package com.lzf.hotelcheckinsystem;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lzf.hotelcheckinsystem.util.OKHttp;
import com.lzf.hotelcheckinsystem.util.SharedPreferences;
import com.lzf.hotelcheckinsystem.util.UrlStr;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private LinearLayout cameraChoose;
    private ImageView useImage;
    private EditText name;
    private EditText identity;
    private EditText password;

    public final int LOCAL_PHOTOS = 6003; //选择本地图片
    public final int PHOTOGRAPH = 6004; //用相机拍照
    private File currentImageFile = null; //拍照时的缓存文件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        cameraChoose = findViewById(R.id.cameraChoose);
        useImage = findViewById(R.id.useImage);
        name = findViewById(R.id.name);
        identity = findViewById(R.id.identity);
        password = findViewById(R.id.password);

        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                // 调用成功，返回AccessToken对象
                String token = result.getAccessToken();
            }

            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError子类SDKError对象
            }
        }, getApplicationContext(), "cXx3nfnZGVZESp1FKiLEan9x", "QD30CiV2fyCnkFCpLmFICy0nkjtTk4zH");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.useImage:
                cameraChoose.setVisibility(View.VISIBLE);
                break;
            case R.id.photograph:
                imageOperation(PHOTOGRAPH);
                break;
            case R.id.localPhotos:
                imageOperation(LOCAL_PHOTOS);
                break;
            case R.id.submit:
                final String nameStr = name.getText().toString();
                final String identityStr = identity.getText().toString();
                final String passwordStr = password.getText().toString();
                if (nameStr.length() <= 0 || identityStr.length() <= 0 || passwordStr.length() <= 0) {
                    Toast.makeText(RegisterActivity.this, "以上三项都不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("name", nameStr);
                                params.put("identity", identityStr);
                                params.put("password", passwordStr);
                                Log.v("params", params + "");
                                String resp = OKHttp.submit(UrlStr.REGISTER, params, new HashMap<String, File>());
                                JSONObject respJsonObject = new JSONObject(resp);
                                if (respJsonObject.getBoolean("success")) {
                                    JSONObject data = respJsonObject.getJSONObject("data");
                                    SharedPreferences.save(RegisterActivity.this, data.getString("id"), data.getString("name"), data.getString("identity"), data.getString("password"), data.getString("type"), data.getString("memo"));
                                    startActivity(new Intent(RegisterActivity.this, RoomActivity.class));
                                    finish();
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(RegisterActivity.this, "注册失败；如果该身份证号是初次注册，请稍后重试。否则，请直接登录", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, "连接不到服务器，请检查你的网络或稍后重试", Toast.LENGTH_SHORT).show();
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

    /**
     * 身份证识别
     */
    private void idCard() {
        // 身份证识别参数设置
        IDCardParams param = new IDCardParams();
        //detect_direction	是否检测图像朝向，默认不检测，即：false。可选值为：true - 检测图像朝向；false - 不检测图像朝向。朝向是指输入图像是正常方向、逆时针旋转90/180/270度
        param.setDetectDirection(true);
        // isFront	true：身份证正面，false：身份证背面
        param.setIdCardSide(IDCardParams.ID_CARD_SIDE_FRONT);
        param.setImageFile(currentImageFile);

        // 调用身份证识别服务
        OCR.getInstance().recognizeIDCard(param, new OnResultListener<IDCardResult>() {
            @Override
            public void onResult(IDCardResult result) {
                // 调用成功，返回IDCardResult对象
                //result: IDCardResult front{direction=0, wordsResultNumber=6, address=山西省孝义市兑镇镇水峪煤矿内1425号, idNumber=142301199304150558, birthday=19930415, name=刘鹏强, gender=男, ethnic=汉}
                //result: IDCardResult front{direction=0, wordsResultNumber=6, address=山西省柳林县薛村镇高红134, idNumber=142327199203282037, birthday=19920328, name=刘志峰, gender=男, ethnic=汉}
                Log.v("result", result + "");
                name.setText(result.getName().toString());
                identity.setText(result.getIdNumber().toString());
            }

            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError对象
                //error: com.baidu.ocr.sdk.exception.OCRError: [216633] recognize id card error
                Log.v("error", error + "");
                Toast.makeText(RegisterActivity.this, "请上传清晰的身份证正面照", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 分享时的照片选择或是拍照
     *
     * @param chooseType
     */
    private void imageOperation(int chooseType) {
        // 置入一个不设防的VmPolicy：Android 7.0 FileUriExposedException
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        switch (chooseType) {
            case PHOTOGRAPH:
                try {
                    if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                    }
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        File dir = new File(Environment.getExternalStorageDirectory(), "hotelcheckinsystem/.camera"); //hotelcheckinsystem/.camera
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        File[] files = dir.listFiles();//获取文件列表
                        for (int i = 0; i < files.length; i++) {
                            files[i].delete();//删除该文档下的所有文件
                        }
                        currentImageFile = new File(dir, "cameraCache.jpg");
                        //                currentImageFile = new File(dir, System.currentTimeMillis() + ".jpg");
                        if (!currentImageFile.exists()) {
                            try {
                                currentImageFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentImageFile));
                        startActivityForResult(it, PHOTOGRAPH);
                    } else {
                        String dirTemp = null;
                        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
                        Class<?>[] paramClasses = {};
                        Method getVolumePathsMethod;
                        try {
                            getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", paramClasses);
                            // 在反射调用之前将此对象的 accessible 标志设置为 true，以此来提升反射速度。
                            getVolumePathsMethod.setAccessible(true);
                            Object[] params = {};
                            Object invoke = getVolumePathsMethod.invoke(storageManager, params);
                            for (int i = 0; i < ((String[]) invoke).length; i++) {
                                if (!((String[]) invoke)[i].equals(Environment.getExternalStorageDirectory().toString())) {
                                    dirTemp = ((String[]) invoke)[i];
                                }
                            }
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        System.out.println("The default memory：" + dirTemp);
                        File dir = new File(dirTemp, "hotelcheckinsystem/.camera"); //hotelcheckinsystem/.camera
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        File[] files = dir.listFiles();//获取文件列表
                        for (int i = 0; i < files.length; i++) {
                            files[i].delete();//删除该文档下的所有文件
                        }
                        currentImageFile = new File(dir, "cameraCache.jpg");
                        //                currentImageFile = new File(dir, System.currentTimeMillis() + ".jpg");
                        if (!currentImageFile.exists()) {
                            try {
                                currentImageFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentImageFile));
                        startActivityForResult(it, PHOTOGRAPH);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //hyx
                //        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //        startActivityForResult(intentFromCapture, GlobalConsts.CAMERA_REQUEST_CODE);
                break;
            case LOCAL_PHOTOS:
                if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                if ((intent.resolveActivity(getPackageManager()) != null)) {
                    startActivityForResult(intent, LOCAL_PHOTOS);
                } else {
                    Toast.makeText(RegisterActivity.this, "本地暂无图片", Toast.LENGTH_SHORT).show();
                }
                //        Intent intentFromGallery = new Intent();
                //        intentFromGallery.setType("image/*"); // 设置文件类型
                //        intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
                //        startActivityForResult(intentFromGallery, IMAGE_REQUEST_CODE);
                break;
            default:
                break;
        }
    }

    /**
     * 解决小米手机上获取图片路径为null的情况
     *
     * @param intent
     * @return
     */
    private Uri getXiaoMiUri(Intent intent) {
        Uri uri = intent.getData();
        String type = intent.getType();
        if (uri.getScheme().equals("file") && (type.contains("image/"))) {
            String path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = this.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=")
                        .append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Images.ImageColumns._ID},
                        buff.toString(), null, null);
                int index = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    // set _id value
                    index = cur.getInt(index);
                }
                if (index == 0) {
                    // do nothing
                } else {
                    Uri uri_temp = Uri
                            .parse("content://media/external/images/media/"
                                    + index);
                    if (uri_temp != null) {
                        uri = uri_temp;
                    }
                }
            }
        }
        return uri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("requestCode", "" + requestCode);
        Log.v("resultCode", "" + resultCode);
        Log.v("data", "" + data);
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == Activity.RESULT_OK) {
                Log.v("currentImageFile", currentImageFile + "");
                switch (requestCode) {
                    case PHOTOGRAPH:
                        if (currentImageFile == null) { //部分机型（vivo v3）返回时会清除currentImageFile所占的内存空间。（重新走MyApplication导致）
                            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                File dir = new File(Environment.getExternalStorageDirectory(), "hotelcheckinsystem/.camera"); //hotelcheckinsystem/.camera
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                currentImageFile = new File(dir, "cameraCache.jpg");
                                if (!currentImageFile.exists()) {
                                    try {
                                        currentImageFile.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                String dirTemp = null;
                                StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
                                Class<?>[] paramClasses = {};
                                Method getVolumePathsMethod;
                                try {
                                    getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", paramClasses);
                                    // 在反射调用之前将此对象的 accessible 标志设置为 true，以此来提升反射速度。
                                    getVolumePathsMethod.setAccessible(true);
                                    Object[] params = {};
                                    Object invoke = getVolumePathsMethod.invoke(storageManager, params);
                                    for (int i = 0; i < ((String[]) invoke).length; i++) {
                                        if (!((String[]) invoke)[i].equals(Environment.getExternalStorageDirectory().toString())) {
                                            dirTemp = ((String[]) invoke)[i];
                                        }
                                    }
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                                System.out.println("The default memory：" + dirTemp);
                                File dir = new File(dirTemp, "hotelcheckinsystem/.camera"); //hotelcheckinsystem/.camera
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                currentImageFile = new File(dir, "cameraCache.jpg");
                                if (!currentImageFile.exists()) {
                                    try {
                                        currentImageFile.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        break;
                    case LOCAL_PHOTOS:
                        Uri selectedImage = data.getData(); // 获取系统返回的照片的Uri
                        String[] filePathColumn = new String[]{MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);// 从系统表中查询指定Uri对应的照片
                        if (cursor == null) {
                            selectedImage = getXiaoMiUri(data);//解决方案( 解决小米手机上获取图片路径为null的情况)
                            filePathColumn = new String[]{MediaStore.Images.Media.DATA};
                            cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);// 从系统表中查询指定Uri对应的照片
                        }
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        File tempFile = new File(cursor.getString(columnIndex)); // 获取照片路径
                        cursor.close();
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            File dir = new File(Environment.getExternalStorageDirectory(), "hotelcheckinsystem/.camera"); //hotelcheckinsystem/.camera
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            File[] files = dir.listFiles();//获取文件列表
                            for (int i = 0; i < files.length; i++) {
                                files[i].delete();//删除该文档下的所有文件
                            }
                            currentImageFile = new File(dir, "cameraCache.jpg");
                            //                currentImageFile = new File(dir, System.currentTimeMillis() + ".jpg");
                            if (!currentImageFile.exists()) {
                                try {
                                    currentImageFile.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            String dirTemp = null;
                            StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
                            Class<?>[] paramClasses = {};
                            Method getVolumePathsMethod;
                            try {
                                getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", paramClasses);
                                // 在反射调用之前将此对象的 accessible 标志设置为 true，以此来提升反射速度。
                                getVolumePathsMethod.setAccessible(true);
                                Object[] params = {};
                                Object invoke = getVolumePathsMethod.invoke(storageManager, params);
                                for (int i = 0; i < ((String[]) invoke).length; i++) {
                                    if (!((String[]) invoke)[i].equals(Environment.getExternalStorageDirectory().toString())) {
                                        dirTemp = ((String[]) invoke)[i];
                                    }
                                }
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                            System.out.println("The default memory：" + dirTemp);
                            File dir = new File(dirTemp, "hotelcheckinsystem/.camera"); //hotelcheckinsystem/.camera
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            File[] files = dir.listFiles();//获取文件列表
                            for (int i = 0; i < files.length; i++) {
                                files[i].delete();//删除该文档下的所有文件
                            }
                            currentImageFile = new File(dir, "cameraCache.jpg");
                            //                currentImageFile = new File(dir, System.currentTimeMillis() + ".jpg");
                            if (!currentImageFile.exists()) {
                                try {
                                    currentImageFile.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        FileInputStream fis = null;
                        FileOutputStream fos = null;
                        try {
                            //文件复制到sd卡中
                            fis = new FileInputStream(tempFile);
                            fos = new FileOutputStream(currentImageFile);
                            int len = 0;
                            byte[] buffer = new byte[2048];
                            while (-1 != (len = fis.read(buffer))) {
                                fos.write(buffer, 0, len);
                            }
                            fos.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            //关闭数据流
                            try {
                                if (fos != null)
                                    fos.close();
                                if (fis != null)
                                    fis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        break;
                }
                Log.v("currentImageFile", currentImageFile + "");
                Glide.with(this)
                        .load(currentImageFile.getAbsolutePath())
                        .skipMemoryCache(true) //禁止内存缓存
                        .diskCacheStrategy(DiskCacheStrategy.NONE)//图片缓存策略,这个一般必须有
                        .error(R.mipmap.ic_launcher)// 加载图片失败的时候显示的默认图
                        .into(useImage);
                idCard();
                cameraChoose.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (cameraChoose.getVisibility() != View.GONE) {
            cameraChoose.setVisibility(View.GONE);
        } else {
            finish();
        }
    }
}
