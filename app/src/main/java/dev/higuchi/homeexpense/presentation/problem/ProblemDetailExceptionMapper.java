package dev.higuchi.homeexpense.presentation.problem;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import dev.higuchi.homeexpense.core.model.InvalidAttributeMoveException;
import dev.higuchi.homeexpense.core.model.expense.ExpenseNotFoundException;
import dev.higuchi.homeexpense.core.model.expense.InvalidExpenseAttributeReferenceException;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeAlreadyExistsException;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeConstraintException;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeLimitExceededException;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeNotFoundException;
import dev.higuchi.homeexpense.core.model.income.InvalidIncomeAttributeReferenceException;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeAlreadyExistsException;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeConstraintException;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeLimitExceededException;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeNotFoundException;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import dev.higuchi.homeexpense.infrastructure.datasource.DataAccessException;
import dev.higuchi.homeexpense.presentation.api.InvalidIfMatchHeaderException;
import dev.higuchi.homeexpense.presentation.api.InvalidQueryParameterException;
import dev.higuchi.homeexpense.presentation.api.PreconditionRequiredException;
import dev.higuchi.homeexpense.presentation.api.UnsupportedJsonPatchException;
import dev.higuchi.homeexpense.query.InvalidQueryCriteriaException;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ProblemDetailExceptionMapper {

  private static final Logger LOG = Logger.getLogger(ProblemDetailExceptionMapper.class);
  private static final String PROBLEM_JSON = "application/problem+json";

  @Inject AllowedMethodsResolver allowedMethodsResolver;

  @Context UriInfo uriInfo;

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapExpenseNotFoundException(ExpenseNotFoundException e) {
    return toResponse(Response.Status.NOT_FOUND, "経費が見つかりません");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapExpenseAttributeNotFoundException(
      ExpenseAttributeNotFoundException e) {
    return toResponse(Response.Status.NOT_FOUND, "経費属性が見つかりません");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapIncomeNotFoundException(
      dev.higuchi.homeexpense.core.model.income.IncomeNotFoundException e) {
    return toResponse(Response.Status.NOT_FOUND, "収入が見つかりません");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapIncomeAttributeNotFoundException(
      IncomeAttributeNotFoundException e) {
    return toResponse(Response.Status.NOT_FOUND, "収入属性が見つかりません");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapInvalidAttributeMoveException(
      InvalidAttributeMoveException e) {
    return toResponse(Response.Status.CONFLICT, e.getMessage());
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapUnsupportedJsonPatchException(
      UnsupportedJsonPatchException e) {
    return toResponse(422, "Unprocessable Content", e.getMessage());
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapInvalidExpenseAttributeReferenceException(
      InvalidExpenseAttributeReferenceException e) {
    return toResponse(422, "Unprocessable Content", "指定された経費属性が存在しません");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapInvalidIncomeAttributeReferenceException(
      InvalidIncomeAttributeReferenceException e) {
    return toResponse(422, "Unprocessable Content", "指定された収入属性が存在しません");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapExpenseAttributeLimitExceededException(
      ExpenseAttributeLimitExceededException e) {
    return toResponse(Response.Status.CONFLICT, e.getMessage());
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapIncomeAttributeLimitExceededException(
      IncomeAttributeLimitExceededException e) {
    return toResponse(Response.Status.CONFLICT, e.getMessage());
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapExpenseAttributeAlreadyExistsException(
      ExpenseAttributeAlreadyExistsException e) {
    return toResponse(Response.Status.CONFLICT, "既に登録されています");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapIncomeAttributeAlreadyExistsException(
      IncomeAttributeAlreadyExistsException e) {
    return toResponse(Response.Status.CONFLICT, "既に登録されています");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapExpenseAttributeConstraintException(
      ExpenseAttributeConstraintException e) {
    return toResponse(Response.Status.CONFLICT, "制約があるため削除できません");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapIncomeAttributeConstraintException(
      IncomeAttributeConstraintException e) {
    return toResponse(Response.Status.CONFLICT, "制約があるため削除できません");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapConstraintViolationException(
      ConstraintViolationException e) {
    String detail =
        e.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
    return toResponse(Response.Status.BAD_REQUEST, detail);
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapPreconditionRequiredException(
      PreconditionRequiredException e) {
    return toResponse(428, "Precondition Required", e.getMessage());
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapInvalidIfMatchHeaderException(
      InvalidIfMatchHeaderException e) {
    return toResponse(Response.Status.BAD_REQUEST, e.getMessage());
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapInvalidQueryParameterException(
      InvalidQueryParameterException e) {
    return toResponse(Response.Status.BAD_REQUEST, e.getMessage());
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapInvalidQueryCriteriaException(
      InvalidQueryCriteriaException e) {
    return toResponse(Response.Status.BAD_REQUEST, e.getMessage());
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapConcurrentUpdateException(ConcurrentUpdateException e) {
    LOG.warn("楽観ロック違反");
    return toResponse(
        Response.Status.PRECONDITION_FAILED, "指定されたバージョンは既に更新されています。最新を取得して再試行してください");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapDataAccessException(DataAccessException e) {
    LOG.error("データアクセスエラー", e);
    return toResponse(Response.Status.INTERNAL_SERVER_ERROR, "データアクセスエラーが発生しました");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapIllegalArgumentException(IllegalArgumentException e) {
    LOG.error("ドメイン不変条件違反がAPI境界を越えて到達しました（入力検証漏れの可能性）", e);
    return toResponse(Response.Status.INTERNAL_SERVER_ERROR, "サーバ内部エラーが発生しました");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapNullPointerException(NullPointerException e) {
    LOG.error("必須値の欠落がAPI境界を越えて到達しました（入力検証漏れの可能性）", e);
    return toResponse(Response.Status.INTERNAL_SERVER_ERROR, "サーバ内部エラーが発生しました");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapNoSuchElementException(NoSuchElementException e) {
    LOG.error("ドメイン語彙外の値がAPI境界を越えて到達しました（入力検証漏れの可能性）", e);
    return toResponse(Response.Status.INTERNAL_SERVER_ERROR, "サーバ内部エラーが発生しました");
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapMismatchedInputException(MismatchedInputException e) {
    String field =
        e.getPath().stream()
            .map(
                ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
            .collect(Collectors.joining("."));
    String detail =
        e instanceof UnrecognizedPropertyException unrecognized
            ? "定義されていないプロパティが含まれています: " + unrecognized.getPropertyName()
            : field.isEmpty() ? "リクエストの形式が不正です" : field + "の値の形式が不正です";
    return toResponse(Response.Status.BAD_REQUEST, detail);
  }

  @ServerExceptionMapper
  public RestResponse<ProblemDetail> mapWebApplicationException(WebApplicationException e) {
    Response original = e.getResponse();
    int status = original.getStatus();
    if (status >= 500) {
      LOG.error("HTTPエラー", e);
    }
    RestResponse.ResponseBuilder<ProblemDetail> builder =
        problemResponse(
            status,
            ProblemDetail.of(status, reasonPhrase(status), detailFor(status), currentTraceId()));
    original
        .getHeaders()
        .forEach((name, values) -> {
          if (!name.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)
              && !name.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
            values.forEach(v -> builder.header(name, v));
          }
        });
    if (status == 405 && original.getHeaderString(HttpHeaders.ALLOW) == null) {
      allowedMethodsResolver
          .allowFor(uriInfo.getPath())
          .ifPresent(allow -> builder.header(HttpHeaders.ALLOW, allow));
    }
    return builder.header(HttpHeaders.CONTENT_TYPE, PROBLEM_JSON).build();
  }

  private static String detailFor(int status) {
    return switch (status) {
      case 400 -> "リクエストの形式が不正です";
      case 404 -> "指定されたリソースは存在しません";
      case 405 -> "このリソースでは許可されていないメソッドです";
      case 406 -> "要求されたメディアタイプでは応答できません";
      case 415 -> "サポートされていないメディアタイプです";
      default -> reasonPhrase(status);
    };
  }

  private static String reasonPhrase(int status) {
    Response.StatusType type = Response.Status.fromStatusCode(status);
    return type != null ? type.getReasonPhrase() : "HTTP " + status;
  }

  private static RestResponse<ProblemDetail> toResponse(Response.Status status, String detail) {
    return toResponse(status.getStatusCode(), status.getReasonPhrase(), detail);
  }

  private static RestResponse<ProblemDetail> toResponse(
      int statusCode, String title, String detail) {
    ProblemDetail problemDetail = ProblemDetail.of(statusCode, title, detail, currentTraceId());
    return problemResponse(statusCode, problemDetail)
        .header(HttpHeaders.CONTENT_TYPE, PROBLEM_JSON)
        .build();
  }

  private static RestResponse.ResponseBuilder<ProblemDetail> problemResponse(
      int statusCode, ProblemDetail problemDetail) {
    return RestResponse.ResponseBuilder.<ProblemDetail>create(statusCode).entity(problemDetail);
  }

  private static String currentTraceId() {
    SpanContext ctx = Span.current().getSpanContext();
    return ctx.isValid() ? ctx.getTraceId() : null;
  }
}
