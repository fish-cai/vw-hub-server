package com.fish.vwhub.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fish.vwhub.entity.ResResult;
import com.fish.vwhub.entity.VwHubOutput;
import com.fish.vwhub.mapper.VwHubOutputMapper;
import com.fish.vwhub.util.ExcelUtil;
import com.fish.vwhub.util.GeoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * @author fish
 * @description 针对表【vw_hub_output】的数据库操作Service实现
 * @createDate 2024-03-22 19:24:28
 */
@Service
@Slf4j
public class VwHubOutputService extends ServiceImpl<VwHubOutputMapper, VwHubOutput> implements IService<VwHubOutput> {

    @Value("${vw.hub.output.dir}")
    private String outDir;

    public ResResult view(Integer resultId) {
        VwHubOutput output = this.getById(resultId);
        String excelPath = outDir + "/" + output.getInputId() + "/" + output.getOutputFileName();
        List<JSONObject> jsonObjects;
        try {
            jsonObjects = ExcelUtil.readExcelData(new File(excelPath));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Excel读取失败，e:{0}", e);
            return ResResult.fail("500", "Excel文件读取失败，请重试");
        }
        JSONObject jo = parseExcelData(jsonObjects);
        return ResResult.success(jo);
    }

    public JSONObject parseExcelData(List<JSONObject> dataList) {
        JSONObject res = new JSONObject();
        JSONArray pointRes = new JSONArray();
        JSONArray lineRes = new JSONArray();
        for (JSONObject jo : dataList) {
            String from = jo.getString("中转库城市");
            String to = jo.getString("覆盖城市");
            Float[] fromPoint = GeoUtil.getGeo(from);
            Float[] toPoint = GeoUtil.getGeo(to);
            if (fromPoint == null || toPoint == null) {
                continue;
            }
            //点位集合 用于涟漪效果
            JSONObject fromPointJo = new JSONObject();
            fromPointJo.put("name", from);
            fromPointJo.put("value", fromPoint);
            JSONObject toPointJo = new JSONObject();
            toPointJo.put("name", to);
            toPointJo.put("value", toPoint);
            pointRes.add(fromPointJo);
            pointRes.add(toPointJo);

            //飞线结果集合
            JSONObject lineJo = new JSONObject();
            JSONArray coords = new JSONArray();
            coords.add(fromPoint);
            coords.add(toPoint);
            lineJo.put("coords", coords);
            lineRes.add(lineJo);
        }
        res.put("pointRes", pointRes);
        res.put("lineRes", lineRes);
        return res;
    }
}
