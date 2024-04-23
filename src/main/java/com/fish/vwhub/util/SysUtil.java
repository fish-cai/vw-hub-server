package com.fish.vwhub.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Slf4j
public class SysUtil {

    final static BigDecimal DIVISOR = BigDecimal.valueOf(1024);

    public static Long getProcessPid(Process process) {
        long pid = -1L;
        if (isWin())
            return pid;
        Class<? extends Process> aClass = process.getClass();
        try {
            if (aClass.getName().equals("java.lang.UNIXProcess") || aClass.getName().equals("java.lang.ProcessImpl")) {
                try {
                    Field f = aClass.getDeclaredField("pid");
                    f.setAccessible(true);
                    pid = f.getLong(process);
                    f.setAccessible(false);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            pid = -1L;
        }
        return pid;
    }

    public static boolean isWin() {
        String OS = System.getProperty("os.name").toLowerCase();
        return OS.contains("windows");
    }

    public static boolean isMac() {
        String OS = System.getProperty("os.name").toLowerCase();
        return OS.contains("mac");
    }

    public static String getLocalHost(String ethNum) {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    log.info("inter:{},ip:{}", netInterface, ip);
                }
            }
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                if (ethNum.equals(netInterface.getName())) {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip instanceof Inet4Address) {
                            return ip.getHostName();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "获取服务器IP错误";
    }

    public static String getGPU() throws IOException {
        Process process;
        try {
            if (isWin()) {
                process = Runtime.getRuntime().exec("nvidia-smi.exe");
            } else {
                String[] shell = {"/bin/bash", "-c", "nvidia-smi"};
                process = Runtime.getRuntime().exec(shell);
            }
            process.waitFor();
            process.getOutputStream().close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder stringBuffer = new StringBuilder();
        String line;
        while (null != (line = reader.readLine())) {
            stringBuffer.append(line).append("\n");
        }

        return stringBuffer.toString();
    }

    public static Long getUserId() {
        long userId = 2L;
        String userIdStr = System.getenv("USER_ID");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            userId = Long.parseLong(userIdStr);
        }
        log.info("userIdStr :{}", userId);
        return userId;
    }

    public static String getDateStr() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static List<String> getAllDatesInTheDateRange(String startDate, String endDate) {
        List<String> res = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            if (start.getTime() > end.getTime()) {
                return res;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            while (calendar.getTime().before(end)) {
                res.add(sdf.format(calendar.getTime()));
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            res.add(sdf.format(end));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static List<String> getAllMonthInTheDateRange(String startDate, String endDate) {
        List<String> res = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            if (start.getTime() > end.getTime()) {
                return res;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            while (calendar.getTime().before(end)) {
                res.add(sdf.format(calendar.getTime()));
                calendar.add(Calendar.MONTH, 1);
            }
            res.add(sdf.format(end));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static List<String> getAllHoursInTheDateRange(String startDate, String endDate) {
        List<String> res = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfHour = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            if (start.getTime() > end.getTime()) {
                return res;
            }
            Calendar cStart = Calendar.getInstance();
            cStart.setTime(start);
            Calendar cEnd = Calendar.getInstance();
            cEnd.setTime(end);
            cEnd.add(Calendar.DAY_OF_MONTH, 1);
            end = cEnd.getTime();
            while (cStart.getTime().before(end)) {
                res.add(sdfHour.format(cStart.getTime()));
                cStart.add(Calendar.HOUR_OF_DAY, 1);
            }
//            res.add(sdfHour.format(end));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static List<String> getAllHoursInTheDateRange2(String startDate, String endDate) {
        List<String> res = new ArrayList<>();
        SimpleDateFormat sdfHour = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
        try {
            Date start = sdfHour.parse(startDate);
            Date end = sdfHour.parse(endDate);
            if (start.getTime() > end.getTime()) {
                return res;
            }
            Calendar cStart = Calendar.getInstance();
            cStart.setTime(start);
            Calendar cEnd = Calendar.getInstance();
            cEnd.setTime(end);
            cEnd.add(Calendar.DAY_OF_MONTH, 1);
            end = cEnd.getTime();
            while (cStart.getTime().before(end)) {
                res.add(sdfHour.format(cStart.getTime()));
                cStart.add(Calendar.HOUR_OF_DAY, 1);
            }
//            res.add(sdfHour.format(end));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static TreeMap<String, Float> initHoursData2(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date current = new Date();
        List<String> hours = getAllHoursInTheDateRange2(startTime, endTime);
        TreeMap<String, Float> data = new TreeMap<>();
        for (String h : hours) {
            try {
                if (sdf.parse(h).getTime() <= current.getTime()) {
                    data.put(h, 0F);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static TreeMap<String, Float> initHoursData(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date current = new Date();
        List<String> hours = getAllHoursInTheDateRange(startTime, endTime);
        TreeMap<String, Float> data = new TreeMap<>();
        for (String h : hours) {
            try {
                if (sdf.parse(h).getTime() <= current.getTime()) {
                    data.put(h, 0F);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static TreeMap<String, Float> initDaysData(String startTime, String endTime) {
        List<String> days = SysUtil.getAllDatesInTheDateRange(startTime, endTime);
        TreeMap<String, Float> data = new TreeMap<>();
        for (String d : days) {
            data.put(d, 0F);
        }
        return data;
    }

    public static TreeMap<String, Float> initMonthsData(String startTime, String endTime) {
        List<String> months = SysUtil.getAllMonthInTheDateRange(startTime, endTime);
        TreeMap<String, Float> data = new TreeMap<>();
        for (String d : months) {
            data.put(d, 0F);
        }
        return data;
    }

    public static void main(String[] args) throws Exception {
        List<String> getAllMonthInTheDateRange = getAllMonthInTheDateRange("2022-12", "2023-11");
        System.out.println(getAllMonthInTheDateRange);
    }

    public static String getIP(HttpServletRequest request){
        // 获取客户端的源 IP 地址
        String ipAddress = request.getRemoteAddr();

        // 如果你需要考虑代理服务器的情况，可以这样获取 X-Forwarded-For 头部
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            // X-Forwarded-For 可能包含多个 IP 地址，以逗号分隔，第一个 IP 地址是客户端的真实 IP
            ipAddress = forwardedFor.split(",")[0];
        }
        return ipAddress;
    }

}
