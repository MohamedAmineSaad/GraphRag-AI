package com.omnedu.ai.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class RAGVectorProcessorService {

    private static final String KEY_CUSTOM_CONTEXT = "customContext";
    private static final String KEY_QUESTION = "question";
    private static final int TOP_K = 4;
    private static final double SIMILARITY_THRESHOLD = 0.7;

    private final PromptTemplate basicAugmentationTemplate;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    @Qualifier("openAIService")
    private AIService aiService;

    public RAGVectorProcessorService() {
        var ragBasicPromptTemplate = new ClassPathResource("prompts/rag-basic-template.st");
        this.basicAugmentationTemplate = new PromptTemplate(ragBasicPromptTemplate);
    }

    private String retrieveCustomContext(String userPrompt, int topK) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(userPrompt)
                .topK(topK > 0 ? topK : TOP_K)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .build();

        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);
        StringBuilder customContext = new StringBuilder();

        for (Document doc : similarDocuments) {
            customContext.append(doc.getText()).append("\n");
        }

        return customContext.toString();
    }

    private String augmentUserPrompt(String originalUserPrompt, String customContext) {
        var templateMap = new HashMap<String, Object>();
        templateMap.put(KEY_QUESTION, originalUserPrompt);
        templateMap.put(KEY_CUSTOM_CONTEXT, customContext);

        return basicAugmentationTemplate.render(templateMap);
    }

    public String generateRAGResponse(String systemPrompt, String userPrompt, int topK) {
        var customContext = retrieveCustomContext(userPrompt, topK);
        var augmentedUserPrompt = augmentUserPrompt(userPrompt, customContext);

        return aiService.generateBasicResponse(systemPrompt, augmentedUserPrompt);
    }

    public Flux<String> streamRAGResponse(String systemPrompt, String userPrompt, int topK) {
        var customContext = retrieveCustomContext(userPrompt, topK);
        var augmentedUserPrompt = augmentUserPrompt(userPrompt, customContext);

        return aiService.streamBasicResponse(systemPrompt, augmentedUserPrompt);
    }
}
