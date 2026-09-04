package com.jcloud.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chujunjie
 * @date create in 15:33 2026/9/2
 */
@RestController
public class OllamaDemoController {

    @Resource(name = "ollamaChatModel")
    private ChatModel chatModel;

    /**
     * ollama调用
     *
     * @param question
     * @return
     */
    @GetMapping("/hello/ollamaChat")
    public String ollamaChat(@RequestParam(value = "question", defaultValue = "你是谁") String question) {
        String res = chatModel.call(question);
        System.out.println("大模型的回答是：" + res);
        return res;
    }
}
