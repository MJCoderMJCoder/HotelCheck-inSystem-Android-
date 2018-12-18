package com.lzf.hotelcheckinsystem.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 将流转化为二进制数组
 * Created by MJCoder on 2018-04-23.
 */

public class FlowBinary {
    // 从流中读取数据
    public static byte[] binary(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        // 从输入流中读取一定数量的字节，并将其存储在缓冲区数组buffer中
        while ((len = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        inputStream.close();
        return byteArrayOutputStream.toByteArray();
    }
}
