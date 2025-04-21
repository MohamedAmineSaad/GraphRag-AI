package com.omnedu.ai.api.request;



import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VectorIndexingRequestFromURL(
        @NotBlank @Pattern(regexp = "^(?i)(http|https)://.*$") String url,
        List<String> keywords) {

}