package dev.higuchi.homeexpense.presentation.problem;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;

/**
 * RFC 9457 Problem Details for HTTP APIs（W3C Trace Context の trace_id 拡張付き）。
 *
 * <p>instance は「問題の発生 1 件」を識別するものだが、リクエスト URI を入れると内部ホスト名やクエリが露出し、かつ発生を識別しない。 発生の追跡は trace_id で行うため
 * instance は返さない。
 */
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
