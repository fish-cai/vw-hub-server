package com.fish.vwhub.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fish.vwhub.entity.VwHubOutput;
import com.fish.vwhub.mapper.VwHubOutputMapper;
import org.springframework.stereotype.Service;

/**
* @author fish
* @description 针对表【vw_hub_output】的数据库操作Service实现
* @createDate 2024-03-22 19:24:28
*/
@Service
public class VwHubOutputService extends ServiceImpl<VwHubOutputMapper, VwHubOutput> implements IService<VwHubOutput> {

}
