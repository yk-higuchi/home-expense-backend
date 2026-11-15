package dev.higuchi.homeexpense.presentation.api.expense;

import dev.higuchi.homeexpense.application.usecase.expense.ExpenseDeletionService;
import dev.higuchi.homeexpense.application.usecase.expense.ExpenseRegistrationService;
import dev.higuchi.homeexpense.application.usecase.expense.ExpenseUpdateService;
import dev.higuchi.homeexpense.core.model.expense.ExpenseIdentifier;
import dev.higuchi.homeexpense.presentation.api.IfMatchParser;
import dev.higuchi.homeexpense.presentation.api.LinkHeaderCreatable;
import dev.higuchi.homeexpense.presentation.validation.ExpenseCategory;
import dev.higuchi.homeexpense.presentation.validation.PageNumber;
import dev.higuchi.homeexpense.presentation.validation.PerPageSize;
import dev.higuchi.homeexpense.presentation.validation.UuidFormat;
import dev.higuchi.homeexpense.presentation.validation.YearRange;
import dev.higuchi.homeexpense.query.expense.ExpenseCriteriaCreator;
import dev.higuchi.homeexpense.query.expense.ExpenseDetail;
import dev.higuchi.homeexpense.query.expense.ExpenseSearchCriteria;
import dev.higuchi.homeexpense.query.expense.ExpenseSearchResult;
import dev.higuchi.homeexpense.query.expense.ExpenseSearchResultQuerier;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;

@Path("/v1/expenses")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExpenseApi {

  ExpenseRegistrationService expenseRegistrationService;
  ExpenseSearchResultQuerier expenseSearchResultQuerier;
  ExpenseUpdateService expenseUpdateService;
  ExpenseDeletionService expenseDeletionService;

  public ExpenseApi(
      ExpenseRegistrationService expenseRegistrationService,
      ExpenseSearchResultQuerier expenseSearchResultQuerier,
      ExpenseUpdateService expenseUpdateService,
      ExpenseDeletionService expenseDeletionService) {
    this.expenseRegistrationService = expenseRegistrationService;
    this.expenseSearchResultQuerier = expenseSearchResultQuerier;
    this.expenseUpdateService = expenseUpdateService;
    this.expenseDeletionService = expenseDeletionService;
  }

  @POST
  @RunOnVirtualThread
  public Response post(@Valid ExpensePostRequest request, @Context UriInfo uriInfo) {
    ExpenseIdentifier expenseIdentifier = expenseRegistrationService.register(request.toCommand());
    URI uri = uriInfo.getAbsolutePathBuilder().path(expenseIdentifier.value()).build();
    return Response.created(uri).build();
  }

  @PUT
  @Path("{id}")
  @RunOnVirtualThread
  public Response put(
      @PathParam("id") @UuidFormat String id,
      @Valid ExpensePutRequest request,
      @HeaderParam("If-Match") String ifMatch) {
    expenseUpdateService.update(request.toCommand(id, IfMatchParser.parse(ifMatch)));
    return Response.noContent().build();
  }

  @DELETE
  @Path("{id}")
  @RunOnVirtualThread
  public Response delete(
      @PathParam("id") @UuidFormat String id, @HeaderParam("If-Match") String ifMatch) {
    expenseDeletionService.delete(new ExpenseIdentifier(id), IfMatchParser.parse(ifMatch));
    return Response.noContent().build();
  }

  @GET
  @RunOnVirtualThread
  public Response get(
      @QueryParam("page") @DefaultValue("1") @PageNumber Integer page,
      @QueryParam("per_page") @DefaultValue("20") @PerPageSize Integer perPage,
      @QueryParam("year") @YearRange Integer year,
      @QueryParam("month")
          @Min(value = 1, message = "monthは1〜12を指定してください")
          @Max(value = 12, message = "monthは1〜12を指定してください")
          Integer month,
      @QueryParam("category") @ExpenseCategory String category,
      @QueryParam("attribute_id") @UuidFormat(message = "attribute_idの形式に誤りがあります")
          String attributeId,
      @Context UriInfo uriInfo) {
    ExpenseSearchCriteria criteria =
        ExpenseCriteriaCreator.create(page, perPage, year, month, category, attributeId);
    ExpenseSearchResult expenseSearchResult = expenseSearchResultQuerier.search(criteria);
    ExpenseGetListResponse response = ExpenseGetListResponse.from(expenseSearchResult, page);
    return Response.ok(response)
        .header(
            "Link",
            LinkHeaderCreatable.create(
                uriInfo, criteria.pagination(), expenseSearchResult.totalCount()))
        .build();
  }

  @GET
  @Path("{id}")
  @RunOnVirtualThread
  public Response get(@PathParam("id") @UuidFormat String id) {
    ExpenseDetail detail = expenseSearchResultQuerier.get(new ExpenseIdentifier(id));
    ExpenseGetResponse response = ExpenseGetResponse.from(detail);
    return Response.ok(response).tag(detail.digest().value()).build();
  }
}
