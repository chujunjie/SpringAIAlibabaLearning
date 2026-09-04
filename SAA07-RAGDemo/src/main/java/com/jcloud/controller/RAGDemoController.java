package com.jcloud.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RAG
 *
 * @author chujunjie
 * @date create in 9:12 2026/9/3
 */
@RestController
@RequestMapping("/rag")
public class RAGDemoController {

    @Resource
    private ChatClient chatClient;

    @Resource
    private RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "message") String message) {
        String resp = chatClient.prompt()
//                .system("你是一个导游面试官，只能回答导游面试相关问题")
                .user(message)
                .advisors(retrievalAugmentationAdvisor)
                .call()
                .content();

        return resp;
    }
}
