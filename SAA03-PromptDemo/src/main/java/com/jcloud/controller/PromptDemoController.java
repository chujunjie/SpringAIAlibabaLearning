package com.jcloud.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 提示词
 *
 * @author chujunjie
 * @date create in 9:12 2026/9/3
 */
@RestController
@RequestMapping("/prompt")
public class PromptDemoController {

    @Resource
    private ChatClient chatClient;

    /**
     * 提示词
     *
     * @param question
     * @return
     */
    @GetMapping("/chat")
    public Flux<String> prompt(@RequestParam(value = "question", defaultValue = "你是谁") String question) {
        // 初始化一个提示词构建器，用于组装发给 AI 模型的完整请求。
        return chatClient.prompt(question)
                // 设置 AI 的系统指令（System Prompt），也就是给 AI 定角色、划边界
                .system("你是一个法律助手，只能回答法律相关问题")
                // 添加用户的问题 / 输入（User Prompt），也就是你想让 AI 回答的具体内容
                .user(question)
                // 指定 AI 的回复方式为流式输出（而非一次性返回完整结果），返回的是一个流式响应对
                .stream()
                .content();
    }

    /**
     * 提示词模版
     *
     * @param candidateName 候选人姓名
     * @param jobPosition   岗位名称
     * @param entryDate     入职时间
     * @param salaryRange   薪资范围
     * @param welfare       福利
     * @param companyName   企业名称
     * @param offerType     Offer类型
     * @return
     */
    @GetMapping("/template")
    public Flux<String> promptTemplate(@RequestParam("candidateName") String candidateName,
                                       @RequestParam("jobPosition") String jobPosition,
                                       @RequestParam("entryDate") String entryDate,
                                       @RequestParam("salaryRange") String salaryRange,
                                       @RequestParam("welfare") String welfare,
                                       // System模板的动态变量：企业名称、Offer类型
                                       @RequestParam("companyName") String companyName,
                                       @RequestParam("offerType") String offerType) {

        // 1. System提示词模板（java15长文本）
        String systemTemplateStr = """
                你是{companyName}的资深人力资源专员，精通{offerType}入职Offer的撰写规范。
                请根据用户提供的信息，生成一份符合{companyName}企业规范的{offerType}Offer，要求如下：
                1. 语言正式且温馨，符合{companyName}的官方文书风格；
                2. 包含核心要素：入职岗位、入职日期、薪资范围（税前）、核心福利、欢迎语；
                3. 以html格式输出
                4. 结尾必须带上{companyName}的名称和人力资源部联系方式提示。
                """;

        // 2. 创建System模板对象，填充System侧的变量
        PromptTemplate systemPromptTemplate = new PromptTemplate(systemTemplateStr);
        Map<String, Object> systemVariables = Map.of(
                "companyName", companyName,  // 企业名称（动态）
                "offerType", offerType      // Offer类型（如"正式员工"/"实习生"）
        );
        // 渲染System模板，得到填充后的完整System提示词
        String systemContent = systemPromptTemplate.render(systemVariables);
        SystemMessage systemMessage = new SystemMessage(systemContent);

        // 3. User提示词模板
        String userTemplateStr = """
                请生成一份入职Offer，具体信息如下：
                1. 候选人姓名：{candidateName}
                2. 入职岗位：{jobPosition}
                3. 入职日期：{entryDate}
                4. 税前薪资范围：{salaryRange}
                5. 核心福利：{welfare}
                """;
        PromptTemplate userPromptTemplate = new PromptTemplate(userTemplateStr);
        Map<String, Object> userVariables = Map.of(
                "candidateName", candidateName,
                "jobPosition", jobPosition,
                "entryDate", entryDate,
                "salaryRange", salaryRange,
                "welfare", welfare
        );
        String userContent = userPromptTemplate.render(userVariables);
        UserMessage userMessage = new UserMessage(userContent);

        // 4.组合消息并调用大模型
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        return chatClient.prompt(prompt)
                .stream()
                .content();
    }

    /**
     * 提示词模版
     *
     * @param candidateName 候选人姓名
     * @param jobPosition   岗位名称
     * @param entryDate     入职时间
     * @param salaryRange   薪资范围
     * @param welfare       福利
     * @param companyName   企业名称
     * @param offerType     Offer类型
     * @return
     */
    @GetMapping("/template2")
    public Flux<String> promptTemplate2(@RequestParam("candidateName") String candidateName,
                                        @RequestParam("jobPosition") String jobPosition,
                                        @RequestParam("entryDate") String entryDate,
                                        @RequestParam("salaryRange") String salaryRange,
                                        @RequestParam("welfare") String welfare,
                                        // System模板的动态变量：企业名称、Offer类型
                                        @RequestParam("companyName") String companyName,
                                        @RequestParam("offerType") String offerType) throws IOException {

        // 1. 读取System提示词模板
        ClassPathResource systemTemplateFile = new ClassPathResource("prompts/system.txt");
        String systemTemplateStr = new String(
                systemTemplateFile.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        // 2. 创建System模板对象，填充System侧的变量
        PromptTemplate systemPromptTemplate = new PromptTemplate(systemTemplateStr);
        Map<String, Object> systemVariables = Map.of(
                "companyName", companyName,  // 企业名称（动态）
                "offerType", offerType      // Offer类型（如"正式员工"/"实习生"）
        );
        // 渲染System模板，得到填充后的完整System提示词
        String systemContent = systemPromptTemplate.render(systemVariables);
        SystemMessage systemMessage = new SystemMessage(systemContent);

        // 3. 读取User提示词模板
        ClassPathResource userTemplateFile = new ClassPathResource("prompts/user.txt");
        String userTemplateStr = new String(
                userTemplateFile.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        PromptTemplate userPromptTemplate = new PromptTemplate(userTemplateStr);
        Map<String, Object> userVariables = Map.of(
                "candidateName", candidateName,
                "jobPosition", jobPosition,
                "entryDate", entryDate,
                "salaryRange", salaryRange,
                "welfare", welfare
        );
        String userContent = userPromptTemplate.render(userVariables);
        UserMessage userMessage = new UserMessage(userContent);

        // 4.组合消息并调用大模型
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        return chatClient.prompt(prompt)
                .stream()
                .content();
    }
}
