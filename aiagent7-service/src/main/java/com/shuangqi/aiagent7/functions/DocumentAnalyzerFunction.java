package com.shuangqi.aiagent7.functions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.context.annotation.Description;
import org.springframework.core.io.FileSystemResource;

import java.util.function.BiFunction;

/**
 * 通过@Description描述函数的用途，这样ai在多个函数中可以根据描述进行选择。
 */
@Description("文档解析函数")
@Slf4j
public class DocumentAnalyzerFunction implements BiFunction<DocumentAnalyzerFunction.Request, ToolContext, DocumentAnalyzerFunction.Response> {

    @Override
    @SneakyThrows

    public Response apply(Request request, ToolContext context) {
        // ai解析用户的提问得到path参数，使用tika读取本地文件获取内容。把读取到的内容再返回给ai作为上下文去回答用户的问题。
        try {
            log.debug("DocumentAnalyzerFunction request.path: {}", request.path);
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new FileSystemResource(request.path));
            return new Response(tikaDocumentReader.read().get(0).getContent());
        } catch (Exception e) {
            return new Response("解析文件失败");
        }
    }

    /**
     * 通过@JsonProperty声明属性名称和是否必填
     * 通过@JsonPropertyDescription描述属性的用途，这样ai可以提取出符合参数描述的内容。
     */
    @Data
    public static class Request {
        @JsonProperty(required = true, value = "path")
        @JsonPropertyDescription(value = "需要解析的文档路径")
        String path;
    }

    public record Response(String result) {
    }


}

