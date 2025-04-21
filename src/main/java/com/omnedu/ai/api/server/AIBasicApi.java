package com.omnedu.ai.api.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.chat.client.ChatClient;

import com.omnedu.ai.api.request.AIPromptRequest;
import com.omnedu.ai.service.AIService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.Optional;
import java.time.Duration;

@RestController
@RequestMapping("/api/ai")
@Validated
public class AIBasicApi {

    @Autowired
    @Qualifier("openAIService")
    private AIService aiService;

    @Autowired
    @Qualifier("openAIChatClient")
    private ChatClient chatClient;

    @PostMapping(path = "/v1/basic", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> basicAI(@RequestBody @Valid AIPromptRequest request) {
        return Mono.fromCallable(() -> {
            return aiService.generateBasicResponse(request.systemPrompt(), request.userPrompt());
        }).subscribeOn(Schedulers.boundedElastic())
          .timeout(Duration.ofSeconds(45))
          .onErrorResume(e -> {
            // Log the error and return a friendly message
            System.err.println("Error calling OpenAI: " + e.getMessage());
            return Mono.just("An error occurred while processing your request. Please try again later.");
        });
    }

    @PostMapping(path = "/v1/basic/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<String> basicStreamAI(@RequestBody @Valid AIPromptRequest request) {
        var response = aiService.streamBasicResponse(request.systemPrompt(), request.userPrompt());

        return response
            .onErrorResume(e -> {
                System.err.println("Error in streaming response: " + e.getMessage());
                return Flux.just("An error occurred while streaming the response. Please try again later.");
            });
    }

    @PostMapping(path = "/v1/test", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> testEndpoint() {
        return Mono.just("Test endpoint working!");
    }

    @PostMapping(path = "/v1/basic-alternative", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> basicAIAlternative(@RequestBody @Valid AIPromptRequest request) {
        return Mono.fromCallable(() -> {
            return chatClient.prompt()
                .system(Optional.ofNullable(request.systemPrompt()).orElse(""))
                .user(request.userPrompt())
                .call()
                .content();
        }).subscribeOn(Schedulers.boundedElastic())
          .onErrorResume(e -> {
              System.err.println("Error calling OpenAI alternative: " + e.getMessage());
              return Mono.just("An error occurred while processing your request. Please try again later.");
          });
    }

}
