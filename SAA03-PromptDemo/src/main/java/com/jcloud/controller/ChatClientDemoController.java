package com.jcloud.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chujunjie
 * @date create in 15:33 2026/9/2
 */
@RestController
public class ChatClientDemoController {

    @Resource
    private ChatModel chatModel;

    @Resource
    private ChatClient chatClient;

    /**
     * chatModel
     *
     * @param question
     * @return
     */
    @GetMapping("/chatClient/chat1")
    public String chat1(@RequestParam(value = "question", defaultValue = "你是谁") String question) {
        String res = chatModel.call(question);
        System.out.println("大模型的回答是：" + res);
        return res;
    }

    /**
     * chatClient
     *
     * @param question
     * @return
     */
    @GetMapping("/chatClient/chat2")
    public String chat2(@RequestParam(value = "question", defaultValue = "你是谁") String question) {
        String res = chatClient.prompt().user(question).call().content();
        System.out.println("大模型的回答是：" + res);
        return res;
    }
}
