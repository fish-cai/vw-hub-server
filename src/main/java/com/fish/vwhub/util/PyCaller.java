package com.fish.vwhub.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PyCaller {

    public static String execPyCmd(String fileName, String methodName, List<String> param) throws IOException, InterruptedException {
        Process proc;
        StringBuilder stringBuilder = new StringBuilder();
        List<String> cmd = new ArrayList<>();
//        cmd.add("/usr/bin/python3");
        cmd.add("python3");
        cmd.add(fileName);
        cmd.addAll(param);
        log.info("cmd = {}", cmd);
        if (!CommonUtil.shellFilter(cmd)){
            log.error("参数存在违法字符，请排查后重试");
            return null;
        }
        proc = new ProcessBuilder(cmd).start();
        Long processPid = SysUtil.getProcessPid(proc);
        CountDownLatch cdl = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                super.run();
                String line;
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    while ((line = in.readLine()) != null) {
                        log.info("line:{}", line);
//                        if (!JSON.isValid(line)) {
//                            continue;
//                        }
                        stringBuilder.append(line);
                    }
                    in.close();
                } catch (Exception e) {
                    log.error("read stream from proc error,pid:{},error:{}", processPid, e);
                }
                cdl.countDown();
            }
        }.start();
        //接收并输出错误信息
        new Thread(() -> {
            StringBuilder errorBuilder = new StringBuilder();
            try {
                BufferedReader error = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String errorLine;
                while ((errorLine = error.readLine()) != null) {
                    errorBuilder.append(errorLine);
                }
            } catch (Exception e) {
                log.error("read err stream from proc error,pid:{},error:{}", processPid, e);
            }
            String errS = errorBuilder.toString();
            if (!StringUtils.isEmpty(errS)) {
                log.error("exec py fileName:{},methodName:{},param:{}, error::{}", fileName, methodName, param, errS);
            }
            cdl.countDown();
        }).start();
        cdl.await(10, TimeUnit.SECONDS);
        proc.waitFor();
        log.info("python return：" + stringBuilder);
        return stringBuilder.toString();
    }

    public static String execPy(String fileName, String methodName, String json) throws IOException, InterruptedException {
        Process proc;
        StringBuilder stringBuilder = new StringBuilder();
        String[] para = new String[3];
        JSONObject jsonObject = JSONUtil.parseObj(json);
        jsonObject.set("method_name", methodName);
        para[0] = "python3";
        para[1] = fileName;
        if (SysUtil.isWin())
            para[2] = JSONUtil.quote(JSONUtil.toJsonStr(jsonObject));
        else
            para[2] = JSONUtil.toJsonStr(jsonObject);
        log.info("para[2]={}", para[2]);
        proc = Runtime.getRuntime().exec(para);
        Long processPid = SysUtil.getProcessPid(proc);
        log.info("fileName:{},methodName:{} ,pid:{}", fileName, methodName, processPid);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                super.run();
                String line;
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    while ((line = in.readLine()) != null) {
                        log.info("line:{}", line);
                        if (!JSON.isValid(line)) {
                            continue;
                        }
                        stringBuilder.append(line);
                    }
                    in.close();
                } catch (Exception e) {
                    log.error("read stream from proc error,pid:{},error:{}", processPid, e);
                }
                countDownLatch.countDown();
            }
        }.start();
        //接收并输出错误信息
        new Thread(() -> {
            StringBuilder errorBuilder = new StringBuilder();
            try {
                BufferedReader error = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String errorLine;
                while ((errorLine = error.readLine()) != null) {
                    errorBuilder.append(errorLine);
                }
            } catch (Exception e) {
                log.error("read err stream from proc error,pid:{},error:{}", processPid, e);
            }
            String errS = errorBuilder.toString();
            if (!StringUtils.isEmpty(errS)) {
                log.error("exec py fileName:{},methodName:{},param:{}, error::{}", fileName, methodName, para[2], errS);
            }
        }).start();
        countDownLatch.await();
        proc.waitFor();
        log.info("python return：" + stringBuilder);
        return stringBuilder.toString();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        List<String[]> csvData = new ArrayList<>();
        List<String> params = new ArrayList<>();
        csvData.add(new String[]{"Time", "Value"});
        Float startStopThreshold = 0F;
        params.add("--predict_days");
        params.add("168");
        params.add("--threshold");
        params.add(startStopThreshold + "");
        params.add("--uptrend_threshold");
        params.add("0");
        params.add("--csv");
        params.add("/Users/java/enn/device-alarm/2023-12-11_2024-01-11_5_ES52.csv");
        execPyCmd("/Users/java/enn/device-alarm/py/forecast_model.py", "", params);
    }
}
