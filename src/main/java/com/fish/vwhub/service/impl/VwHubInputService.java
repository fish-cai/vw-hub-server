package com.fish.vwhub.service.impl;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fish.vwhub.entity.ResResult;
import com.fish.vwhub.entity.VwHubInput;
import com.fish.vwhub.entity.VwHubOutput;
import com.fish.vwhub.mapper.VwHubInputMapper;
import com.fish.vwhub.util.ExcelUtil;
import com.fish.vwhub.util.GeoUtil;
import com.fish.vwhub.util.PyCaller;
import com.fish.vwhub.util.SysUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author fish
 * @description 针对表【vw_hub_input】的数据库操作Service实现
 * @createDate 2024-03-22 19:23:39
 */
@Service
@Slf4j
public class VwHubInputService extends ServiceImpl<VwHubInputMapper, VwHubInput> implements IService<VwHubInput> {

    @Value("${vw.hub.input.dir}")
    private String inputDir;

    @Value("${vw.hub.output.dir}")
    private String outPutDir;

    @Value("${vw.hub.py.dir}")
    private String pyDir;
    @Resource
    VwHubInputMapper inputMapper;

    @Resource
    VwHubOutputService outputService;

    @Transactional(rollbackFor = Exception.class)
    public ResResult saveUpload(MultipartFile file, Integer fileType) {
        VwHubInput input = new VwHubInput();
        input.setFileName(file.getOriginalFilename());
        input.setFileType(fileType);
        input.setGmtCreate(new Date().getTime());
        this.save(input);
        String filePath;
        //输出文件写到输出目录下,输入和输出的子目录（in/out后面的）都是用 inputId 区分
        if (fileType == 2) {
            filePath = outPutDir + "/" + input.getId() + "/" + file.getOriginalFilename();
        } else {
            filePath = inputDir + "/" + input.getId() + "/" + file.getOriginalFilename();
        }
        try {
            FileUtil.writeBytes(file.getBytes(), filePath);
        } catch (IOException e) {
            log.error("文件写入失败，e:{}", e);
            e.printStackTrace();
            return ResResult.fail("500", "文件写入失败");
        }
        if (fileType == 2) {
            saveOutRes(outPutDir + "/" + input.getId() + "/", input.getId());
        }
        return ResResult.success(input);
    }

    public IPage<VwHubInput> selectPage(Page<VwHubInput> page) {
        return inputMapper.manualPage(page);
    }

    public ResResult<String> start(Integer id, HttpServletRequest request) {
        log.info("用户启动任务，id:{},用户ip：{}", id, SysUtil.getIP(request));
        VwHubInput input = this.getById(id);
        String fileName = input.getFileName();
        if (!StringUtils.containsIgnoreCase(fileName, "input")) {
            return ResResult.fail("500", "当前关联的文件非输入文件，请重新操作");
        }
        String inputFile = inputDir + id + "/";
        String outDir = outPutDir + id + "/";
        //执行算法文件
        List<String> params = new ArrayList<>();
        params.add("--input_dir");
        params.add(inputFile);
        params.add("--output_dir");
        params.add(outDir);
        File file = new File(outDir);
        if (!file.exists()) {
            file.mkdir();
        }
        String res;
        try {
            res = PyCaller.execPyCmd(pyDir + "main_entry.py", "main", params);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            res = null;
        }
        if (StringUtils.isBlank(res)) {
            return ResResult.fail("500", "算法调用失败,请查看相关日志");
        }
        JSONObject jo = JSONObject.parseObject(res);
        if (!jo.getBoolean("success")) {
            return ResResult.fail("500", "算法调用失败,返回信息:" + jo.getString("message"));
        }
        log.info("算法文件：{} 调用完成，结果：{},ip:{}", inputFile + fileName, res, SysUtil.getIP(request));

        saveOutRes(outDir, id);
        return ResResult.success(res);
    }

    private void saveOutRes(String outputDir, Integer id) {
        File[] files = new File(outputDir).listFiles();
        for (File file : files) {
            VwHubOutput out = new VwHubOutput();
            out.setGmtCreate(new Date());
            out.setInputId(id);
            out.setOutputFileName(file.getName());
            outputService.save(out);
        }
    }

    public void testMap() {
        try {
            List<JSONObject> list = ExcelUtil.readExcelData(new File("/Users/caiwenlin/java/vw-hub-server/output/35/Location_Output_2.xlsx"));
            for (JSONObject jo : list) {
//                String from = jo.getString("中转库城市");
                String to = jo.getString("覆盖城市");
                if (GeoUtil.getGeo(to) == null) {
                    System.out.println(to);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
