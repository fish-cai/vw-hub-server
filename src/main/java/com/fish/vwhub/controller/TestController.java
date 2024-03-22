package com.fish.vwhub.controller;

import com.fish.vwhub.entity.ResResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/test")
@RestController
public class TestController {
    @PostMapping("/upload")
    public ResResult upload(@RequestParam(name = "file", required = false) MultipartFile file,
                            @RequestParam(required = false) Integer fileType) {

        return ResResult.success(null);
    }
}
