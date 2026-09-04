package com.jcloud.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 对话记忆
 *
 * @author chujunjie
 * @date create in 9:12 2026/9/3
 */
@RestController
@RequestMapping("/memory")
public class ChatMemoryDemoController {

    @Resource
    private ChatClient chatClient;

    /**
     * 对话记忆
     *
     * @param uid 用户id
     * @param cid 会话id
     * @param msg
     * @return
     */
    @GetMapping("/chat")
    public Flux<String> chat(@RequestParam(value = "uid") String uid,
                             @RequestParam(value = "cid") String cid,
                             @RequestParam(value = "msg", defaultValue = "你是谁") String msg) {
        // 核心：拼接用户ID和会话ID，生成全局唯一的会话标识
        // 格式：用户ID + 分隔符 + 会话ID（分隔符用下划线/竖线，避免和ID本身冲突）
        String uniqueConversationId = uid + "_" + cid;

        return chatClient.prompt()
                .user(msg)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, uniqueConversationId))
                .stream()
                .content();
    }
}
