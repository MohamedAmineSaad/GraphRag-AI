package com.omnedu.ai.service;

import java.util.Optional;
import java.time.Duration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Service
public class OpenAIService implements AIService {

    private final ChatClient chatClient;

    public OpenAIService(@Qualifier("openAIChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String generateBasicResponse(String systemPrompt, String userPrompt) {
        Assert.hasText(userPrompt, "User prompt must not be empty");
        
        // Ensure system prompt is never null
        String safeSystemPrompt = Optional.ofNullable(systemPrompt).orElse("You are a helpful assistant.");

        try {
            return this.chatClient.prompt()
                .system(safeSystemPrompt)
                .user(userPrompt)
                // .advisors(new SimpleLoggerAdvisor())
                .call()
                .content();
        } catch (Exception e) {
            System.err.println("Error in OpenAI call: " + e.getMessage());
            return "Sorry, there was an error communicating with the AI service. Please try again later.";
        }
    }

    @Override
    public Flux<String> streamBasicResponse(String systemPrompt, String userPrompt) {
        Assert.hasText(userPrompt, "User prompt must not be empty");
        
        // Ensure system prompt is never null
        String safeSystemPrompt = Optional.ofNullable(systemPrompt).orElse("You are a helpful assistant.");

        return this.chatClient.prompt()
            .system(safeSystemPrompt)
            .user(userPrompt)
            // .advisors(new SimpleLoggerAdvisor())
            .stream()
            .content()
            .timeout(Duration.ofSeconds(30))
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .maxBackoff(Duration.ofSeconds(5)));
    }

}

