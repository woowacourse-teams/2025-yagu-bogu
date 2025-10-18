package com.yagubogu.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleErrorParam(
        String error,
        @JsonProperty("error_description") String errorDescription
) {
}
