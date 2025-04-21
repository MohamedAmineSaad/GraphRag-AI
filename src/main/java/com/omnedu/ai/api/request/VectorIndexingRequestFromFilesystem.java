package com.omnedu.ai.api.request;


import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record VectorIndexingRequestFromFilesystem(
        @NotBlank String path,
        List<String> keywords) {

}