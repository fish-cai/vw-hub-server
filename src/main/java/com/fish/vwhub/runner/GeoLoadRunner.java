package com.fish.vwhub.runner;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fish.vwhub.util.GeoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class GeoLoadRunner implements CommandLineRunner {

    @Value("${vw.hub.map.json}")
    private String jsonPath;

    @Override
    public void run(String... args) throws Exception {
        String s = FileUtil.readString(jsonPath, StandardCharsets.UTF_8);
        JSONArray array = JSON.parseArray(s);
        saveGeo(array);
        log.info("geo信息加载完成");
    }

    private void saveGeo(JSONArray array) {
        for (int i = 0; i < array.size(); i++) {
            JSONObject jo = array.getJSONObject(i);
            String name = jo.getString("name");
            Float log = jo.getFloat("log");
            Float lat = jo.getFloat("lat");
            JSONArray children = jo.getJSONArray("children");
            if (children != null) {
                for (int j = 0; j < children.size(); j++) {
                    saveGeo(children);
                }
            }
            GeoUtil.saveGeo(name, new Float[]{log, lat});
        }

    }
}
