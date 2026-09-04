package com.jcloud.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author chujunjie
 * @date create in 8:53 2026/9/2
 */
@RestController
public class DemoController {

    @Resource(name = "deepseek")
    private ChatModel dsChatModel;

    @Resource(name = "qwen")
    private ChatModel qwenChatModel;

    /**
     * 普通调用
     *
     * @param question
     * @return
     */
    @GetMapping("/hello/chat")
    public String doChat(@RequestParam(value = "question", defaultValue = "你是谁") String question) {
        String res = dsChatModel.call(question);
        System.out.println("大模型的回答是：" + res);
        return res;
    }

    /**
     * 流式输出
     *
     * @param question
     * @return
     */
    @GetMapping("/hello/streamChat")
    public Flux<String> streamChat(@RequestParam(value = "question", defaultValue = "你是谁") String question) {
        return qwenChatModel.stream(question);
    }
}
