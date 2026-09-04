package com.jcloud.controller;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.utility.request.FlushReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.GetReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.QueryResp;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 向量化
 *
 * @author chujunjie
 * @date create in 9:12 2026/9/3
 */
@RestController
@RequestMapping("/embedding")
public class EmbeddingDemoController {

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private MilvusClientV2 milvusClientV2;

    @Value("${spring.ai.dashscope.embedding.options.model}")
    private String embeddingModelName;

    /**
     * 文字转向量
     *
     * @param msg
     * @return
     */
    @GetMapping("/text2Embed")
    public EmbeddingResponse text2Embed(@RequestParam("msg") String msg) {

        // EmbeddingResponse 是文本向量化接口的完整响应对象，它包含了所有返回信息（元数据、向量结果、用量等）
        EmbeddingResponse embeddingResponse = embeddingModel.call(new EmbeddingRequest(List.of(msg),
                DashScopeEmbeddingOptions.builder().withModel(embeddingModelName).build()));

        // 只提取向量（最简洁写法）
        System.out.println(Arrays.toString(embeddingResponse.getResult().getOutput()));

        return embeddingResponse;
    }

    /**
     * 创建milvus collection
     *
     * @param collectionName
     * @return
     */
    @GetMapping("/createMilvusCollection")
    public String createMilvusCollection(@RequestParam("collectionName") String collectionName) {

        createCollection(milvusClientV2, collectionName);

        List<String> collectionNames = milvusClientV2.listCollections().getCollectionNames();
        System.out.println("collectionNames:" + collectionNames);

        return StringUtils.join(collectionNames, ",");
    }

    /**
     * 插入数据
     *
     * @param collectionName
     * @return
     */
    @GetMapping("/addData")
    public Long addData(@RequestParam("collectionName") String collectionName) {

        Gson gson = new Gson();
        List<JsonObject> data = Arrays.asList(
                gson.fromJson("{\"id\": 1, \"vector\": [0.19886812562848388, 0.06023560599112088, 0.6976963061752597, 0.2614474506242501, 0.838729485096104], \"color\": \"red_7025\"}", JsonObject.class),
                gson.fromJson("{\"id\": 2, \"vector\": [0.43742130801983836, -0.5597502546264526, 0.6457887650909682, 0.7894058910881185, 0.20785793220625592], \"color\": \"orange_6781\"}", JsonObject.class),
                gson.fromJson("{\"id\": 3, \"vector\": [0.3172005263489739, 0.9719044792798428, -0.36981146090600725, -0.4860894583077995, 0.95791889146345], \"color\": \"pink_9298\"}", JsonObject.class),
                gson.fromJson("{\"id\": 4, \"vector\": [0.4452349528804562, -0.8757026943054742, 0.8220779437047674, 0.46406290649483184, 0.30337481143159106], \"color\": \"red_4794\"}", JsonObject.class),
                gson.fromJson("{\"id\": 5, \"vector\": [0.985825131989184, -0.8144651566660419, 0.6299267002202009, 0.1206906911183383, -0.1446277761879955], \"color\": \"yellow_4222\"}", JsonObject.class)
                );

        // 插入数据
        InsertResp insert = milvusClientV2.insert(
                InsertReq.builder()
                        .collectionName(collectionName)
                        .data(data)
                        .build()
        );

        // 刷新数据，否则查询不到数据
        milvusClientV2.flush(
                FlushReq.builder()
                        .collectionNames(List.of(collectionName))
                        .build()
        );

        System.out.println("插入数据成功");

        return insert.getInsertCnt();
    }

    /**
     * 获取数据
     *
     * @param collectionName
     * @return
     */
    @GetMapping("/getData")
    public List<String> getData(@RequestParam("collectionName") String collectionName) {

        GetResp getResp = milvusClientV2.get(
                GetReq.builder()
                        .collectionName(collectionName)
                        // 指定id查询
                        .ids(List.of(1, 2, 3))
                        // 过滤字段
                        .outputFields(List.of("id", "color"))
                        .build()
        );

        List<String> result = new ArrayList<>();

        for (QueryResp.QueryResult getResult : getResp.getResults) {
            String res = getResult.toString();
            System.out.println("数据：" + res);
            result.add(res);
        }

        return result;
    }

    /**
     * 删除指定数据
     *
     * @param collectionName
     * @param id
     * @return
     */
    @GetMapping("/deleteData")
    public void deleteData(@RequestParam("collectionName") String collectionName,
                           @RequestParam("id") Integer id) {


        DeleteResp delete = milvusClientV2.delete(DeleteReq.builder().collectionName(collectionName).ids(List.of(id)).build());
        System.out.println("delete:" + delete);
    }

    /**
     * 创建collection
     *
     * @param client
     * @param collectionName
     */
    private void createCollection(MilvusClientV2 client, String collectionName) {
        // 1.创建schema
        CreateCollectionReq.CollectionSchema schema = MilvusClientV2.CreateSchema()
                .addField(
                        AddFieldReq.builder()
                                .fieldName("id")
                                .dataType(DataType.Int64)
                                .isPrimaryKey(true)
                                .autoID(false)
                                .build()
                )
                .addField(
                        AddFieldReq.builder()
                                .fieldName("vector")
                                // 浮点向量
                                .dataType(DataType.FloatVector)
                                // 设置向量的维度
                                .dimension(5)
                                .build()
                )
                .addField(
                        AddFieldReq.builder()
                                .fieldName("color")
                                .dataType(DataType.VarChar)
                                .maxLength(512)
                                .build()
                );

        // 2. 构建索引
        List<IndexParam> indexParams = new ArrayList<>();

        IndexParam vector = IndexParam.builder()
                .fieldName("vector")
                // 分区，减少检索次数
                .indexType(IndexParam.IndexType.IVF_FLAT)
                // 余弦相似度
                .metricType(IndexParam.MetricType.COSINE)
                .build();
        indexParams.add(vector);

        // 3.创建collection
        client.createCollection(
                CreateCollectionReq.builder()
                        .collectionName(collectionName)
                        .collectionSchema(schema)
                        .indexParams(indexParams)
                        .build()
        );
    }
}
