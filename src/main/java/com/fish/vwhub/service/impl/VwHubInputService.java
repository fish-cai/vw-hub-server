package com.fish.vwhub.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fish.vwhub.entity.ResResult;
import com.fish.vwhub.entity.VwHubInput;
import com.fish.vwhub.entity.VwHubOutput;
import com.fish.vwhub.mapper.VwHubInputMapper;
import com.fish.vwhub.util.PyCaller;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public ResResult<String> start(Integer id) {
        VwHubInput input = this.getById(id);
        String fileName = input.getFileName();
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
        if (StringUtils.isBlank(res)){
            return ResResult.fail("500", "算法调用失败,请查看相关日志");
        }
        log.info("算法文件：{} 调用完成，结果：{}", inputFile + fileName, res);
        saveOutRes(outDir, id);
        return ResResult.success(res);
    }

    private void saveOutRes(String outputDir, Integer id) {
        List<String> strings = FileUtil.listFileNames(outputDir);
        for (String name : strings) {
            VwHubOutput out = new VwHubOutput();
            out.setGmtCreate(new Date());
            out.setInputId(id);
            out.setOutputFileName(name);
            outputService.save(out);
        }
    }
}
