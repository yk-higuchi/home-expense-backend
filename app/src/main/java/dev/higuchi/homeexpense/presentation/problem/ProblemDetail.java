package dev.higuchi.homeexpense.presentation.problem;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDetail(
    @JsonProperty("type") URI type,
    @JsonProperty("title") String title,
    @JsonProperty("status") int status,
    @JsonProperty("detail") String detail,
    @JsonProperty("trace_id") String traceId) {

  private static final URI ABOUT_BLANK = URI.create("about:blank");

  public static ProblemDetail of(int status, String title, String detail, String traceId) {
    return new ProblemDetail(ABOUT_BLANK, title, status, detail, traceId);
  }
}
