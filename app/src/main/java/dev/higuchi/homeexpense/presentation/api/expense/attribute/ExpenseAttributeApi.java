package dev.higuchi.homeexpense.presentation.api.expense.attribute;

import dev.higuchi.homeexpense.application.usecase.expense.ExpenseAttributeDeletionService;
import dev.higuchi.homeexpense.application.usecase.expense.ExpenseAttributeRegistrationService;
import dev.higuchi.homeexpense.application.usecase.expense.ExpenseAttributeUpdateService;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeOrder;
import dev.higuchi.homeexpense.presentation.api.IfMatchParser;
import dev.higuchi.homeexpense.presentation.validation.ExpenseCategory;
import dev.higuchi.homeexpense.presentation.validation.UuidFormat;
import dev.higuchi.homeexpense.query.expense.attribute.ExpenseAttributeDetail;
import dev.higuchi.homeexpense.query.expense.attribute.ExpenseAttributeSearchResultQuerier;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.Valid;
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

  public ExpenseAttributeApi(
      ExpenseAttributeRegistrationService expenseAttributeRegistrationService,
      ExpenseAttributeSearchResultQuerier expenseAttributeSearchResultQuerier,
      ExpenseAttributeUpdateService expenseAttributeUpdateService,
      ExpenseAttributeDeletionService expenseAttributeDeletionService) {
    this.expenseAttributeRegistrationService = expenseAttributeRegistrationService;
    this.expenseAttributeSearchResultQuerier = expenseAttributeSearchResultQuerier;
    this.expenseAttributeUpdateService = expenseAttributeUpdateService;
    this.expenseAttributeDeletionService = expenseAttributeDeletionService;
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
    Response.ResponseBuilder response = Response.ok(ExpenseAttributeGetListResponse.from(details));
    filter.ifPresent(value ->
        response.tag(
            new ExpenseAttributeOrder(
                    value,
                    details.stream()
                        .map(detail -> new ExpenseAttributeIdentifier(detail.id()))
                        .toList())
                .digest()
                .value()));
    return response.build();
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
