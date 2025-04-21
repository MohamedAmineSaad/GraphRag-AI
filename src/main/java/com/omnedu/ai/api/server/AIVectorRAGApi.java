package com.omnedu.ai.api.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.omnedu.ai.api.request.AIPromptRequest;
import com.omnedu.ai.api.request.VectorIndexingRequestFromFilesystem;
import com.omnedu.ai.api.request.VectorIndexingRequestFromURL;
import com.omnedu.ai.api.response.BasicIndexingResponse;
import com.omnedu.ai.service.RAGVectorIndexingService;
import com.omnedu.ai.service.RAGVectorProcessorService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.Duration;

@RestController
@RequestMapping("/api/ai/rag/vector")
@Validated
public class AIVectorRAGApi {

    @Autowired
    private RAGVectorIndexingService ragIndexingService;

    @Autowired
    private RAGVectorProcessorService ragVectorProcessorService;

    @PostMapping(path = "/indexing/document/filesystem", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<BasicIndexingResponse>> indexDocumentFromFilesystem(
            @RequestBody @Valid VectorIndexingRequestFromFilesystem request) {
        return ragIndexingService.indexDocumentFromFilesystem(request.path(), request.keywords())
            .subscribeOn(Schedulers.boundedElastic())
            .map(indexedDocuments -> ResponseEntity.ok(
                new BasicIndexingResponse(true,
                    "Document successfully indexed as " + indexedDocuments.size() + " chunks")))
            .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError()
                .body(new BasicIndexingResponse(false, "Error indexing document: " + e.getMessage()))));
    }

    @PostMapping(path = "/indexing/document/url", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<BasicIndexingResponse>> indexDocumentFromURL(
            @RequestBody @Valid VectorIndexingRequestFromURL request) {
        return ragIndexingService.indexDocumentFromURL(request.url(), request.keywords())
            .subscribeOn(Schedulers.boundedElastic())
            .map(indexedDocuments -> ResponseEntity.ok(
                new BasicIndexingResponse(true,
                    "Document successfully indexed as " + indexedDocuments.size() + " chunks")))
            .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError()
                .body(new BasicIndexingResponse(false, "Error indexing document: " + e.getMessage()))));
    }

    @PostMapping(path = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> vectorRAG(@RequestBody @Valid AIPromptRequest request,
            @RequestParam(name = "top-k", required = false, defaultValue = "0") int topK) {
        return Mono.fromCallable(() -> {
            return ragVectorProcessorService.generateRAGResponse(request.systemPrompt(),
                request.userPrompt(), topK);
        }).subscribeOn(Schedulers.boundedElastic())
          .timeout(Duration.ofSeconds(45))
          .onErrorResume(e -> {
            System.err.println("Error in RAG response: " + e.getMessage());
            return Mono.just("An error occurred while processing your request. Please try again later.");
          });
    }

    @PostMapping(path = "/ask/stream", consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.ALL_VALUE)
    public ResponseEntity<Flux<String>> vectorStreamRAG(@RequestBody @Valid AIPromptRequest request,
            @RequestParam(name = "top-k", required = false, defaultValue = "0") int topK) {
        
        Flux<String> responseFlux = ragVectorProcessorService.streamRAGResponse(request.systemPrompt(),
                request.userPrompt(), topK)
            .onErrorResume(e -> {
                System.err.println("Error in streaming RAG response: " + e.getMessage());
                return Flux.just("An error occurred while streaming the response. Please try again later.");
            });
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(responseFlux);
    }
} 

       