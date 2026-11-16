package dev.higuchi.homeexpense.presentation.api.expense.attribute;

import dev.higuchi.homeexpense.application.usecase.expense.ExpenseAttributeDeletionService;
import dev.higuchi.homeexpense.application.usecase.expense.ExpenseAttributeListPatchService;
import dev.higuchi.homeexpense.application.usecase.expense.ExpenseAttributeRegistrationService;
import dev.higuchi.homeexpense.application.usecase.expense.ExpenseAttributeUpdateService;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttribute;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeList;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeName;
import dev.higuchi.homeexpense.presentation.api.IfMatchParser;
import dev.higuchi.homeexpense.presentation.api.InvalidQueryParameterException;
import dev.higuchi.homeexpense.presentation.api.JsonPatchMoves;
import dev.higuchi.homeexpense.presentation.api.JsonPatchOperation;
import dev.higuchi.homeexpense.presentation.validation.ExpenseCategory;
import dev.higuchi.homeexpense.presentation.validation.UuidFormat;
import dev.higuchi.homeexpense.query.expense.attribute.ExpenseAttributeDetail;
import dev.higuchi.homeexpense.query.expense.attribute.ExpenseAttributeSearchResultQuerier;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@Path("/v1/expense-attributes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExpenseAttributeApi {

  ExpenseAttributeRegistrationService expenseAttributeRegistrationService;
  ExpenseAttributeSearchResultQuerier expenseAttributeSearchResultQuerier;
  ExpenseAttributeUpdateService expenseAttributeUpdateService;

  ExpenseAttributeDeletionService expenseAttributeDeletionService;
  ExpenseAttributeListPatchService expenseAttributeListPatchService;

  public ExpenseAttributeApi(
      ExpenseAttributeRegistrationService expenseAttributeRegistrationService,
      ExpenseAttributeSearchResultQuerier expenseAttributeSearchResultQuerier,
      ExpenseAttributeUpdateService expenseAttributeUpdateService,
      ExpenseAttributeDeletionService expenseAttributeDeletionService,
      ExpenseAttributeListPatchService expenseAttributeListPatchService) {
    this.expenseAttributeRegistrationService = expenseAttributeRegistrationService;
    this.expenseAttributeSearchResultQuerier = expenseAttributeSearchResultQuerier;
    this.expenseAttributeUpdateService = expenseAttributeUpdateService;
    this.expenseAttributeDeletionService = expenseAttributeDeletionService;
    this.expenseAttributeListPatchService = expenseAttributeListPatchService;
  }

  @PATCH
  @Consumes(JsonPatchMoves.MEDIA_TYPE)
  @RunOnVirtualThread
  public Response patch(
      @QueryParam("category") @ExpenseCategory String category,
      @NotNull(message = "JSON Patch の本文は必須です")
          @Size(max = JsonPatchMoves.MAX_OPERATIONS, message = "操作は{max}件以内で指定してください")
          List<JsonPatchOperation> operations,
      @HeaderParam("If-Match") String ifMatch) {
    if (category == null) {
      throw new InvalidQueryParameterException("並べ替えではcategoryを指定してください");
    }
    var expectedDigest = IfMatchParser.parse(ifMatch);
    DocumentDigest digest =
        expenseAttributeListPatchService.move(
            dev.higuchi.homeexpense.core.model.expense.ExpenseCategory.valueOf(category),
            JsonPatchMoves.parse(operations, "expense_attributes"),
            expectedDigest);
    return Response.noContent().tag(digest.value()).build();
  }

  @POST
  @RunOnVirtualThread
  public Response post(@Valid ExpenseAttributePostRequest request, @Context UriInfo uriInfo) {
    ExpenseAttributeIdentifier expenseAttributeIdentifier =
        expenseAttributeRegistrationService.register(request.toCommand());
    URI uri = uriInfo.getAbsolutePathBuilder().path(expenseAttributeIdentifier.value()).build();
    return Response.created(uri).build();
  }

  @PUT
  @Path("{id}")
  @RunOnVirtualThread
  public Response put(
      @PathParam("id") @UuidFormat String id,
      @Valid ExpenseAttributePutRequest request,
      @HeaderParam("If-Match") String ifMatch) {
    expenseAttributeUpdateService.update(request.toCommand(id, IfMatchParser.parse(ifMatch)));
    return Response.noContent().build();
  }

  @DELETE
  @Path("{id}")
  @RunOnVirtualThread
  public Response delete(
      @PathParam("id") @UuidFormat String id, @HeaderParam("If-Match") String ifMatch) {
    expenseAttributeDeletionService.delete(
        new ExpenseAttributeIdentifier(id), IfMatchParser.parse(ifMatch));
    return Response.noContent().build();
  }

  @GET
  @RunOnVirtualThread
  public Response list(@QueryParam("category") @ExpenseCategory String category) {
    Optional<dev.higuchi.homeexpense.core.model.expense.ExpenseCategory> filter =
        Optional.ofNullable(category)
            .map(dev.higuchi.homeexpense.core.model.expense.ExpenseCategory::valueOf);
    List<ExpenseAttributeDetail> details = expenseAttributeSearchResultQuerier.list(filter);
    String etag =
        ExpenseAttributeList.digestOf(
                details.stream()
                    .map(detail ->
                        new ExpenseAttribute(
                            new ExpenseAttributeIdentifier(detail.id()),
                            new ExpenseAttributeName(detail.name()),
                            dev.higuchi.homeexpense.core.model.expense.ExpenseCategory.valueOf(
                                detail.category())))
                    .toList())
            .value();
    return Response.ok(ExpenseAttributeGetListResponse.from(details)).tag(etag).build();
  }

  @GET
  @Path("{id}")
  @RunOnVirtualThread
  public Response get(@PathParam("id") @UuidFormat String id) {
    ExpenseAttributeDetail detail =
        expenseAttributeSearchResultQuerier.get(new ExpenseAttributeIdentifier(id));
    ExpenseAttributeGetResponse response = ExpenseAttributeGetResponse.from(detail);
    return Response.ok(response).tag(detail.digest().value()).build();
  }
}
