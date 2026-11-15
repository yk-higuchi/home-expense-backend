package dev.higuchi.homeexpense.presentation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public record JsonPatchOperation(
    @JsonProperty("op") String op,
    @JsonProperty("from") String from,
    @JsonProperty("path") String path,
    @JsonProperty("value") JsonNode value) {}
