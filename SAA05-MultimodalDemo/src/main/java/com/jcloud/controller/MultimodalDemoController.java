package com.jcloud.controller;

import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import jakarta.annotation.Resource;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多模态
 *
 * @author chujunjie
 * @date create in 9:12 2026/9/3
 */
@RestController
@RequestMapping("/multimodal")
public class MultimodalDemoController {

    @Value("${spring.ai.dashscope.image.options.model}")
    private String imageModelName;

    @Resource
    private ImageModel imageModel;

    /**
     * 文字转图片生成
     *
     * @param prompt
     * @return
     */
    @GetMapping(value = "/toimage")
    public String image(@RequestParam(name = "prompt") String prompt) {
        DashScopeImageOptions build = DashScopeImageOptions.builder()
                .withModel(imageModelName)
                .build();
        ImageResponse response = imageModel.call(new ImagePrompt(prompt, build));
        return response.getResult().getOutput().getUrl();
    }
}
