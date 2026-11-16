package dev.higuchi.homeexpense.presentation.api.income;

import dev.higuchi.homeexpense.application.usecase.income.IncomeDeletionService;
import dev.higuchi.homeexpense.application.usecase.income.IncomeRegistrationService;
import dev.higuchi.homeexpense.application.usecase.income.IncomeUpdateService;
import dev.higuchi.homeexpense.core.model.income.IncomeIdentifier;
import dev.higuchi.homeexpense.presentation.api.IfMatchParser;
import dev.higuchi.homeexpense.presentation.api.LinkHeaderCreatable;
import dev.higuchi.homeexpense.presentation.validation.PageNumber;
import dev.higuchi.homeexpense.presentation.validation.PerPageSize;
import dev.higuchi.homeexpense.presentation.validation.UuidFormat;
import dev.higuchi.homeexpense.presentation.validation.YearRange;
import dev.higuchi.homeexpense.query.Pagination;
import dev.higuchi.homeexpense.query.income.IncomeDetail;
import dev.higuchi.homeexpense.query.income.IncomeSearchCriteria;
import dev.higuchi.homeexpense.query.income.IncomeSearchResult;
import dev.higuchi.homeexpense.query.income.IncomeSearchResultQuerier;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;

@Path("/v1/incomes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class IncomeApi {

  IncomeRegistrationService incomeRegistrationService;
  IncomeUpdateService incomeUpdateService;
  IncomeDeletionService incomeDeletionService;
  IncomeSearchResultQuerier incomeSearchResultQuerier;

  public IncomeApi(
      IncomeRegistrationService incomeRegistrationService,
      IncomeUpdateService incomeUpdateService,
      IncomeDeletionService incomeDeletionService,
      IncomeSearchResultQuerier incomeSearchResultQuerier) {
    this.incomeRegistrationService = incomeRegistrationService;
    this.incomeUpdateService = incomeUpdateService;
    this.incomeDeletionService = incomeDeletionService;
    this.incomeSearchResultQuerier = incomeSearchResultQuerier;
  }

  @POST
  @RunOnVirtualThread
  public Response post(@Valid IncomePostRequest request, @Context UriInfo uriInfo) {
    IncomeIdentifier incomeIdentifier = incomeRegistrationService.register(request.toCommand());
    URI uri = uriInfo.getAbsolutePathBuilder().path(incomeIdentifier.value()).build();
    return Response.created(uri).build();
  }

  @PUT
  @Path("{id}")
  @RunOnVirtualThread
  public Response put(
      @PathParam("id") @UuidFormat String id,
      @Valid IncomePutRequest request,
      @HeaderParam("If-Match") String ifMatch) {
    incomeUpdateService.update(request.toCommand(id, IfMatchParser.parse(ifMatch)));
    return Response.noContent().build();
  }

  @DELETE
  @Path("{id}")
  @RunOnVirtualThread
  public Response delete(
      @PathParam("id") @UuidFormat String id, @HeaderParam("If-Match") String ifMatch) {
    incomeDeletionService.delete(new IncomeIdentifier(id), IfMatchParser.parse(ifMatch));
    return Response.noContent().build();
  }

  @GET
  @RunOnVirtualThread
  public Response get(
      @QueryParam("page") @DefaultValue("1") @PageNumber Integer page,
      @QueryParam("per_page") @DefaultValue("20") @PerPageSize Integer perPage,
      @QueryParam("year") @YearRange Integer year,
      @Context UriInfo uriInfo) {
    Pagination pagination = new Pagination(page, perPage);
    IncomeSearchCriteria criteria = new IncomeSearchCriteria(pagination, year);
    IncomeSearchResult incomeSearchResult = incomeSearchResultQuerier.search(criteria);
    IncomeGetListResponse response = IncomeGetListResponse.from(incomeSearchResult, page);
    return Response.ok(response)
        .header(
            "Link",
            LinkHeaderCreatable.create(
                uriInfo, criteria.pagination(), incomeSearchResult.totalCount()))
        .build();
  }

  @GET
  @Path("{id}")
  @RunOnVirtualThread
  public Response get(@PathParam("id") @UuidFormat String id) {
    IncomeDetail detail = incomeSearchResultQuerier.get(new IncomeIdentifier(id));
    IncomeGetResponse response = IncomeGetResponse.from(detail);
    return Response.ok(response).tag(detail.digest().value()).build();
  }
}
