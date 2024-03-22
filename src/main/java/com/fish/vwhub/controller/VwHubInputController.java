package com.fish.vwhub.controller;

import com.fish.vwhub.entity.ResResult;
import com.fish.vwhub.service.impl.VwHubInputService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RequestMapping("/vwHubInput")
@RestController
public class VwHubInputController {

    @Resource
    VwHubInputService inputService;

    @PostMapping("/upload")
    public ResResult upload(@RequestParam(name = "file", required = false) MultipartFile file,
                            @RequestParam(required = false) Integer fileType) {

        return inputService.saveUpload(file, fileType);
    }
}
