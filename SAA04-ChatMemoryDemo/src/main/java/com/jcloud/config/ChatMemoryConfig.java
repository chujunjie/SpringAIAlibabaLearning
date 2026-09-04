package com.jcloud.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chujunjie
 * @date create in 16:21 2026/9/2
 */
@Configuration
public class ChatMemoryConfig {

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository repo) {
        return MessageWindowChatMemory.builder()
                // 自动创建并管理记忆表spring_ai_chat_memory
                .chatMemoryRepository(repo)
                // 针对每个会话最大20条
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel, ChatMemory memory) {
        ChatClient client = ChatClient.builder(chatModel)
                // 自动管理chatMemory的增强器，类似spring aop
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                .build();

        return client;

    }
}
