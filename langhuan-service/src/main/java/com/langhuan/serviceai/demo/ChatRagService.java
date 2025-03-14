package com.langhuan.serviceai.demo;

import com.langhuan.advisors.MySimplelogAdvisor;
import com.langhuan.service.TPromptsService;
import com.langhuan.utils.rag.RagFileVectorUtils;
import com.langhuan.common.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
public class ChatRagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final RagFileVectorUtils ragFileVectorUtils;


    public ChatRagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, RagFileVectorUtils ragFileVectorUtils) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        // 此 advisor 使用向量存储来提供问答功能，实现 RAG（检索增强生成）模式。
                        new QuestionAnswerAdvisor(this.vectorStore, SearchRequest.builder().topK(Constant.WITHTOPK)
                                .similarityThreshold(Constant.WITHSIMILARITYTHRESHOLD).build()),
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new MySimplelogAdvisor()
                )
                .build();

        this.ragFileVectorUtils = ragFileVectorUtils;
    }

    public String chat(String q) {
        return this.chatClient.prompt()
                .advisors(advisor -> advisor.param("chat_memory_conversation_id", "678")
                        .param("chat_memory_response_size", 100))
                .user(q)
                .system(TPromptsService.getCachedTPromptsByMethodName("AIDEFAULTSYSTEMPROMPT"))
                .call()
                .content();
    }

    public Flux<String> stream(String q) {
        return chatClient.prompt()
                .advisors(advisor -> advisor.param("chat_memory_conversation_id", "678")
                        .param("chat_memory_response_size", 100))
                .user(q)
                .system(TPromptsService.getCachedTPromptsByMethodName("AIDEFAULTSYSTEMPROMPT"))
                .stream()
                .content();
    }

    public ChatResponse chatWithPrompt(String p, String q) {
        return chatClient.prompt(new Prompt(p))
                .advisors(advisor -> advisor.param("chat_memory_conversation_id", "678")
                        .param("chat_memory_response_size", 100))
                .user(q)
                .system(TPromptsService.getCachedTPromptsByMethodName("AIDEFAULTSYSTEMPROMPT"))
                .call()
                .chatResponse();
    }

    public String ragSearch(String q) {
        //元数据筛选 您可以将通用的可移植元数据过滤器与 PgVector 存储结合使用。 例如，您可以使用文本表达式语言：
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder().query(q).topK(Constant.WITHTOPK)
                        .similarityThreshold(Constant.WITHSIMILARITYTHRESHOLD)
                        .filterExpression("filetype == 'text/plain'").build()//设置过滤条件
        );
        log.debug("ragSearch: " + documents);
        StringBuilder contents = new StringBuilder();
        for (Document document : documents) {
            contents.append(document.getText());
            contents.append("-");
        }
        return contents.toString();
    }


}
