package com.shuangqi.aiagent7.serviceai;

import com.shuangqi.aiagent7.advisors.MySimplelogAdvisor;
import com.shuangqi.aiagent7.common.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
@Slf4j
public class ChatService {

    private final ChatClient chatClient;
    private final InMemoryChatMemory inMemoryChatMemory;
    private final ApplicationContext applicationContext;
    private final String chatMemoryRetrieveSizeKey = "10";

    public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        InMemoryChatMemory inMemoryChatMemory = new InMemoryChatMemory();

        this.inMemoryChatMemory = inMemoryChatMemory;

        String defaultSystem = """
                用户会向你提出一个问题，你的任务是提供一个细致且准确的答案。
                如果在RAG或者外部获取方式中没有搜索到，请你自己回答这个问题。
                并附带两个简短的相关问题推荐，以JSON格式返回。
                确保你的回答遵循以下结构：
                {
                    "desc": "这里是回答的内容",
                    "recommend": [
                      "推荐问题1",
                      "推荐问题2"
                    ]
                }
                """;
        this.chatClient = chatClientBuilder.defaultSystem(defaultSystem)
                .defaultAdvisors(
                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()),
                        new MessageChatMemoryAdvisor(inMemoryChatMemory),
                        new SafeGuardAdvisor(Constant.AIDEFAULTSAFEGUARDADVISOR),
                        new MySimplelogAdvisor()
                )
//                .defaultFunctions(applicationContext.getBeanNamesForType(BiFunction.class))
                .build();
    }

    public ChatClient getRoleChatClient() {
        return this.chatClient;
    }

    public String chat(String id, String p, String q) {
        return this.getRoleChatClient().prompt(p)
                .user(q)
                .advisors(
                        a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, id)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, chatMemoryRetrieveSizeKey)
                ).call().chatResponse().getResult().getOutput().getContent();
    }


    public String clear(String id) {
        log.info("advisor-clear: {}", "用户id-" + id);
        this.inMemoryChatMemory.clear(id);
        return "清除成功";
    }

}