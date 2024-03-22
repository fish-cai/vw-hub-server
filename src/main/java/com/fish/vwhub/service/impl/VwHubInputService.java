package com.fish.vwhub.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fish.vwhub.entity.VwHubInput;
import com.fish.vwhub.mapper.VwHubInputMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Date;

/**
* @author fish
* @description 针对表【vw_hub_input】的数据库操作Service实现
* @createDate 2024-03-22 19:23:39
*/
@Service
public class VwHubInputService extends ServiceImpl<VwHubInputMapper, VwHubInput> implements IService<VwHubInput> {

    public VwHubInput saveUpload(MultipartFile file, Integer fileType) {
        VwHubInput input = new VwHubInput();
        input.setFileName(file.getOriginalFilename());
        input.setFileType(fileType);
        input.setGmtCreate(new Date());
        this.save(input);

        
        return input;
    }
}
