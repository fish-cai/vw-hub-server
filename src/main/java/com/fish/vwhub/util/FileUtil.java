package com.fish.vwhub.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class FileUtil {

    public static String getSuffix(MultipartFile file) {
        return Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".") + 1);
    }

    public static boolean download(MultipartFile file, String dest, String filename) {
        File outFileDir = new File(dest);
        if (!outFileDir.exists()) {
            boolean isMakDir = outFileDir.mkdirs();
            if (isMakDir) {
                System.out.println("创建压缩目录成功");
            }
        }
        log.info("正在下载MultipartFile：{},目标：{}", file.getOriginalFilename(), dest + filename);
        InputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = file.getInputStream();
            fos = new FileOutputStream(dest + filename);
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = fis.read(bytes)) != -1) {
                fos.write(bytes, 0, len);
                fos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.info("下载MultipartFile完成：{},目标：{}", file.getOriginalFilename(), dest + filename);
        return true;
    }

    public static void download(String path, HttpServletResponse response) throws IOException {
        log.info("download start path:{}", path);
        // 读到流中
        InputStream inputStream = Files.newInputStream(Paths.get(path));// 文件的存放路径
        response.reset();
        response.setContentType("application/octet-stream");
        String filename = new File(path).getName();
        response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
        ServletOutputStream outputStream = response.getOutputStream();
        byte[] b = new byte[1024];
        int len;
        //从输入流中读取一定数量的字节，并将其存储在缓冲区字节数组中，读到末尾返回-1
        while ((len = inputStream.read(b)) > 0) {
            outputStream.write(b, 0, len);
        }
        inputStream.close();
        log.info("download end");
    }

    public static void writeFile(String path, String data) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(data);
        writer.flush();
        writer.close();
    }

    public static Map<String, String> readFile(String path) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            return null;
        }
        BufferedReader reader = new BufferedReader(new FileReader(path));
        StringBuilder sb = new StringBuilder();
        String line = null;
        int lineCount = 0;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
            lineCount++;
        }
        reader.close();
        Map<String, String> map = new HashMap<>();
        map.put("data", sb.toString());
        map.put("lineCount", lineCount + "");
        return map;
    }
}
