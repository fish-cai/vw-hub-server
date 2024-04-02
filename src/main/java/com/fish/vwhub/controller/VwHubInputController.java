package com.fish.vwhub.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fish.vwhub.entity.ResResult;
import com.fish.vwhub.entity.VwHubInput;
import com.fish.vwhub.service.impl.VwHubInputService;
import com.fish.vwhub.service.impl.VwHubOutputService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

@RequestMapping("/vwHubInput")
@RestController
public class VwHubInputController {

    @Resource
    VwHubInputService inputService;

    @Resource
    VwHubOutputService outputService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");

    @PostMapping("/upload")
    public ResResult upload(@RequestParam(name = "file", required = false) MultipartFile file,
                            @RequestParam(required = false) Integer fileType) {

        return inputService.saveUpload(file, fileType);
    }

    @GetMapping("/page")
    public ResResult page(@RequestParam Integer current, @RequestParam Integer size) {
        IPage<VwHubInput> page = inputService.selectPage(new Page<>(current, size));
        List<VwHubInput> records = page.getRecords();
        if (CollectionUtil.isNotEmpty(records)) {
            for (VwHubInput record : records) {
                record.setTypeName(record.getFileType() == 1 ? "输入" : "输出");
                record.setCreateTime(LocalDateTime.ofEpochSecond(record.getGmtCreate() / 1000, 0, ZoneOffset.of("+8")).format(DATE_TIME_FORMATTER));
            }
        }
        return ResResult.success(page);
    }

    @GetMapping("/start")
    public ResResult<String> start(@RequestParam Integer id) {
        return inputService.start(id);
    }

    @GetMapping("/view")
    public ResResult view(Integer resultId) {
        return outputService.view(resultId);
    }

    @GetMapping("/downLoadOut")
    public void downLoadOut(HttpServletResponse response, Integer resultId) {
        outputService.downLoad(response, resultId);
    }

    @GetMapping("/test")
    public void test() {
        inputService.testMap();
    }
}

