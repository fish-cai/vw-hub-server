package com.fish.vwhub.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CommonUtil {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static SimpleDateFormat sdfHour = new SimpleDateFormat("yyyy-MM-dd HH:00:00");

    public static String[] formatNumber(float[] value) {
        String[] rs = new String[value.length];
        for (int i = 0, l = value.length; i < l; i++) {
            rs[i] = String.format("%.2f", value[i]);
        }
        return rs;
    }

    public static String formatIndexNum(Integer indexNum) {
        Date d = new Date();
        d.setTime(indexNum * 1000L);
        return sdf.format(d);
    }

    public static String formatIndexNumByHour(Integer indexNum) {
        Date d = new Date();
        d.setTime(indexNum * 1000L);
        return sdfHour.format(d);
    }

    public static Long parseIndexNum(String indexNumTime) {
        try {
            return sdf.parse(indexNumTime).getTime() / 1000L;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, String> invokeGetMethod(Object obj) {
        // 获取对象class
        Class<?> aClass = obj.getClass();
        // 获取对象声明字段
        Field[] declaredFields = aClass.getDeclaredFields();
        HashMap<String, String> result = new HashMap<>(127);
        for (Field declaredField : declaredFields) {
            String fieldName = declaredField.getName();
            if ("serialVersionUID".equals(fieldName)) {
                continue;
            }
            // 拼接方法名
            String firstCharUpper = fieldName.substring(0, 1).toUpperCase();
            String invokeMethodName = "get" + firstCharUpper + fieldName.substring(1);
            try {
                // 获取方法
                Method pendingInvokeMethod = aClass.getMethod(invokeMethodName);
                // 执行方法
                Object invokeResult = pendingInvokeMethod.invoke(obj);
                // 下可省略
                if (ObjectUtil.isNull(invokeResult)) {
                    result.put(fieldName, "");
                } else if (invokeResult instanceof Date) {
                    String date = sdf.format((Date) invokeResult);
                    result.put(fieldName, date);
                } else {
                    result.put(fieldName, invokeResult.toString());
                }
            } catch (Exception e) {
//                System.out.println(e.getMessage());
            }
        }
        return result;
    }

    public static void main(String[] args) {
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = sdf.parse("2023-09-07 15:13:00");
            Date end = sdf.parse("2023-12-08 16:43:47");
            System.out.println(diffHours(start,end));
        }catch (Exception e){

        }
    }

    public static String invokeGetMethod(Object obj, String fieldName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 获取对象class
        Class<?> aClass = obj.getClass();
        // 拼接方法名
        String firstCharUpper = fieldName.substring(0, 1).toUpperCase();
        String invokeMethodName = "get" + firstCharUpper + fieldName.substring(1);
        // 获取方法
        Method pendingInvokeMethod = aClass.getMethod(invokeMethodName);
        // 执行方法
        Object resObj = pendingInvokeMethod.invoke(obj);
        return resObj == null ? null : resObj.toString();
    }

    public static String getMD5String(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8位字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            //一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方）
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List deepCopy(List src) {
        List dest = null;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(byteOut);
            out.writeObject(src);
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            dest = (List) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dest;
    }

    public static boolean shellFilter(List<String> cmd) {
        for (String s : cmd) {
            if (StringUtils.contains(s, "--")) {
                return true;
            }
            if (NumberUtil.isNumber(s)) {
                return true;
            }
            if (FileUtil.isFile(s)) {
                return true;
            }
            if (StringUtils.equals(s, "python3") || StringUtils.equals(s, "python")) {
                return true;
            }
        }
        return false;
    }

    public static Long diffHours(Date startDate, Date endDate){
        if (startDate == null){
            return 0L;
        }
        LocalDateTime start = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());
        Duration duration = Duration.between(start, end);
        return duration.toHours();
    }
}
