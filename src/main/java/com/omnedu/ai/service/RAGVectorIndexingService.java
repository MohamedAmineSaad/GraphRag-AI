package com.omnedu.ai.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.omnedu.ai.rag.indexing.RAGTikaDocumentReader;

import reactor.core.publisher.Mono;

@Service
public class RAGVectorIndexingService {

    private static final Logger LOG = LoggerFactory.getLogger(RAGVectorIndexingService.class);

    @Autowired
    private RAGTikaDocumentReader tikaDocumentReader;

    @Autowired
    private TextSplitter textSplitter;

    @Autowired
    private VectorStore vectorStore;

    private static final String CUSTOM_KEYWORDS_METADATA_KEY = "custom_keywords";

    private void addCustomMetadata(Document document, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return;
        }

        Assert.notNull(document, "Document must not be null");
        document.getMetadata().putAll(Map.of(CUSTOM_KEYWORDS_METADATA_KEY, keywords));
    }

    private Mono<List<Document>> processDocument(Resource resource, List<String> keywords) {
        Assert.isTrue(resource != null && resource.exists(), "Resource must not be null and must exist");

        return Mono.fromCallable(() -> tikaDocumentReader.readFrom(resource))
            .flatMap(parsedDocuments -> {
                var splittedDocuments = textSplitter.split(parsedDocuments);
                splittedDocuments.forEach(document -> addCustomMetadata(document, keywords));
                
                return Mono.fromCallable(() -> {
                    vectorStore.add(splittedDocuments);
                    LOG.info("Original document splitted into {} chunks and saved to vector store", splittedDocuments.size());
                    return splittedDocuments;
                });
            });
    }

    public Mono<List<Document>> indexDocumentFromFilesystem(String sourcePath, List<String> keywords) {
        var resource = new FileSystemResource(sourcePath);
        return processDocument(resource, keywords);
    }

    public Mono<List<Document>> indexDocumentFromURL(String sourcePath, List<String> keywords) {
        try {
            var resource = new UrlResource(sourcePath);
            return processDocument(resource, keywords);
        } catch (Exception e) {
            return Mono.error(new IllegalArgumentException("Invalid URL: " + sourcePath, e));
        }
    }
}
