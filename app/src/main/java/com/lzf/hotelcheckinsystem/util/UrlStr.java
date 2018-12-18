package com.lzf.hotelcheckinsystem.util;

/**
 * Created by MJCoder on 2018-04-23.
 */

public class UrlStr {
    //    http://localhost:8080/HotelCheck-inSystem/
    public static final String scheme = "http"; //模式/协议
    public static final String port = "8080"; //服务器提供服务的端口号
    public static final String project = "HotelCheck-inSystem"; //该服务的项目名称
    
    public static String serverHost = "192.168.17.251"; //服务器的名称或IP地址
    public static String urlPrefix = scheme + "://" + serverHost + ":" + port + "/" + project;

    public static String REGISTER = urlPrefix + "/user/register";
    public static String LOGIN = urlPrefix + "/user/login";
    public static String TEST = urlPrefix + "/user/test";
    public static String VACANT_USER_ROOM = urlPrefix + "/userRoom/vacantOrUserRoom";
    public static String USER_HANDLE_ROOM = urlPrefix + "/userRoom/userHandleRoom";
    public static String GOODS_SELECT = urlPrefix + "/mGoods/select";
    public static String GOODS_FEE = urlPrefix + "/mGoods/fee";
}
