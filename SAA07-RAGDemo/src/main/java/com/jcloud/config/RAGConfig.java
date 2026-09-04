package com.jcloud.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.GetCollectionStatisticsResponse;
import io.milvus.param.R;
import io.milvus.param.collection.FlushParam;
import io.milvus.param.collection.GetCollectionStatisticsParam;
import io.milvus.response.GetCollStatResponseWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

/**
 * RAG 配置
 *
 * @author chujunjie
 * @date create in 16:21 2026/9/2
 */
@Configuration
public class RAGConfig {

    @Resource
    private MilvusVectorStore vectorStore;

    @Value("${spring.ai.vectorstore.milvus.collection-name}")
    private String collectionName;

    @Bean
    public ChatClient chatClient(ChatModel dashscopeChatModel) {
        return ChatClient.builder(dashscopeChatModel).build();
    }


    /**
     * 配置RetrievalAugmentationAdvisor
     *
     * @return
     */
    @Bean
    public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor() {

        // 设置检索器
        VectorStoreDocumentRetriever retriver = VectorStoreDocumentRetriever.builder()
                // 绑定向量库
                .vectorStore(vectorStore)
                // 设置相似度阈值，越大越严格，这里的阈值很关键，需要根据实际场景调整，写的太小容易命中向量又回答不了问题，太大则会导致召回率低
                .similarityThreshold(0.6)
                // 设置返回的文档数量
                .topK(6)
                .build();

        // 查询增强器
        ContextualQueryAugmenter cqa = ContextualQueryAugmenter.builder()
                // 设置允许为空的上下文，即未检索到的时候让大模型自己答，比如创建云匠自己的官网问答ai时可以设置为false
                .allowEmptyContext(true)
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriver)
                .queryAugmenter(cqa)
                .build();
    }


    /**
     * 初始化向量数据库
     *
     * @throws TikaException
     * @throws IOException
     */
    @PostConstruct
    public void initVectorData() throws TikaException, IOException {
        System.out.println("初始化向量数据...");

        // 获取 Milvus 客户端
        MilvusServiceClient client = (MilvusServiceClient) vectorStore.getNativeClient().get();

        // 先获取 collection 数据条数，如果大于0 ，就不再写入，否则写入
        R<GetCollectionStatisticsResponse> resp = client.getCollectionStatistics(
                GetCollectionStatisticsParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build());

        long rowCount = new GetCollStatResponseWrapper(resp.getData()).getRowCount();

        System.out.println("milvus vector store 中的数量：" + rowCount);

        if (rowCount == 0) {
            System.out.println("milvus 中没有数据，写入...");

            // 加载文档数据到向量数据库
            loadAndStoreDocumentData();

            client.flush(FlushParam.newBuilder()
                    .withCollectionNames(List.of(collectionName))
                    .build());

        } else {
            System.out.println("milvus 中已有数据，无需写入...");
        }
    }

    /**
     * 读取 WORD 文档内容存入 Milvus 中
     */
    private void loadAndStoreDocumentData() throws IOException, TikaException {

        ClassPathResource resource = new ClassPathResource("doc/导游面试问答.doc");

        Tika tika = new Tika();
        String text = tika.parseToString(resource.getFile());

        // 文本拆分器
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(400)
                .withKeepSeparator(true)
                .build();

        List<Document> chunks = splitter.apply(List.of(new Document(text)));

        // 写入向量数据库
        vectorStore.add(chunks);
    }
}
