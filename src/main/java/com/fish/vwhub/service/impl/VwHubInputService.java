package com.fish.vwhub.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fish.vwhub.entity.ResResult;
import com.fish.vwhub.entity.VwHubInput;
import com.fish.vwhub.mapper.VwHubInputMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;

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

    @Value("${vw.hub.input.dir}")
    private String outPutDir;

    @Resource
    VwHubInputMapper inputMapper;
    @Transactional(rollbackFor = Exception.class)
    public ResResult saveUpload(MultipartFile file, Integer fileType) {
        VwHubInput input = new VwHubInput();
        input.setFileName(file.getOriginalFilename());
        input.setFileType(fileType);
        input.setGmtCreate(new Date().getTime());
        this.save(input);
        String inputFilePath = inputDir + "/" + input.getId() + "/" + file.getOriginalFilename();
        try {
            FileUtil.writeBytes(file.getBytes(), inputFilePath);
        } catch (IOException e) {
            log.error("文件写入失败，e:{}", e);
            e.printStackTrace();
            return ResResult.fail("500","文件写入失败");
        }
        return ResResult.success(input);
    }

    public IPage<VwHubInput> selectPage(Page<VwHubInput> page) {
        return inputMapper.manualPage(page);
    }
}
