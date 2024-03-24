package com.fish.vwhub.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fish.vwhub.entity.ResResult;
import com.fish.vwhub.entity.VwHubOutput;
import com.fish.vwhub.mapper.VwHubOutputMapper;
import com.fish.vwhub.util.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

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
        parseExcelData(jsonObjects);
        return ResResult.success(null);
    }

    public void parseExcelData(List<JSONObject> dataList) {
        JSONArray res = new JSONArray();
        for (JSONObject jo : dataList) {
            //todo 解析excel 结果
        }
    }
}
