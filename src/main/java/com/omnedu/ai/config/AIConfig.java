package com.omnedu.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.ReactorNettyClientRequestFactory;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class AIConfig {

    @Bean("defaultRestClientBuilder")
    RestClient.Builder defaultRestClientBuilder() {
        // Configure HttpClient with increased timeouts
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
            .responseTimeout(Duration.ofMillis(30000))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)));
        
        return RestClient.builder()
                .requestFactory(new ReactorNettyClientRequestFactory(httpClient));
    }

    @Bean("openAIChatClient")
    ChatClient openAIChatClient(ChatClient.Builder builder) {
        return builder
        .defaultAdvisors(new SimpleLoggerAdvisor())
        .build();

    }

    @Bean
    TextSplitter textSplitter() {
        return new TokenTextSplitter();
    }

}
