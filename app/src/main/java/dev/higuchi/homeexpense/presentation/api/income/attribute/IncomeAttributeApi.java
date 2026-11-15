package dev.higuchi.homeexpense.presentation.api.income.attribute;

import dev.higuchi.homeexpense.application.usecase.income.IncomeAttributeDeletionService;
import dev.higuchi.homeexpense.application.usecase.income.IncomeAttributeRegistrationService;
import dev.higuchi.homeexpense.application.usecase.income.IncomeAttributeReorderService;
import dev.higuchi.homeexpense.application.usecase.income.IncomeAttributeUpdateService;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeOrder;
import dev.higuchi.homeexpense.presentation.api.IfMatchParser;
import dev.higuchi.homeexpense.presentation.validation.UuidFormat;
import dev.higuchi.homeexpense.query.income.attribute.IncomeAttributeDetail;
import dev.higuchi.homeexpense.query.income.attribute.IncomeAttributeSearchResultQuerier;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

@Path("/v1/income-attributes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class IncomeAttributeApi {

  IncomeAttributeRegistrationService incomeAttributeRegistrationService;
  IncomeAttributeUpdateService incomeAttributeUpdateService;
  IncomeAttributeDeletionService incomeAttributeDeletionService;
  IncomeAttributeSearchResultQuerier incomeAttributeSearchResultQuerier;
  IncomeAttributeReorderService incomeAttributeReorderService;

  public IncomeAttributeApi(
      IncomeAttributeRegistrationService incomeAttributeRegistrationService,
      IncomeAttributeUpdateService incomeAttributeUpdateService,
      IncomeAttributeDeletionService incomeAttributeDeletionService,
      IncomeAttributeSearchResultQuerier incomeAttributeSearchResultQuerier,
      IncomeAttributeReorderService incomeAttributeReorderService) {
    this.incomeAttributeRegistrationService = incomeAttributeRegistrationService;
    this.incomeAttributeUpdateService = incomeAttributeUpdateService;
    this.incomeAttributeDeletionService = incomeAttributeDeletionService;
    this.incomeAttributeSearchResultQuerier = incomeAttributeSearchResultQuerier;
    this.incomeAttributeReorderService = incomeAttributeReorderService;
  }

  @PUT
  @Path("order")
  @RunOnVirtualThread
  public Response putOrder(
      @Valid IncomeAttributeOrderPutRequest request, @HeaderParam("If-Match") String ifMatch) {
    DocumentDigest digest =
        incomeAttributeReorderService.reorder(request.toCommand(IfMatchParser.parse(ifMatch)));
    return Response.noContent().tag(digest.value()).build();
  }

  @POST
  @RunOnVirtualThread
  public Response post(@Valid IncomeAttributePostRequest request, @Context UriInfo uriInfo) {
    IncomeAttributeIdentifier incomeAttributeIdentifier =
        incomeAttributeRegistrationService.register(request.toIncomeAttributeName());
    URI uri = uriInfo.getAbsolutePathBuilder().path(incomeAttributeIdentifier.value()).build();
    return Response.created(uri).build();
  }

  @PUT
  @Path("{id}")
  @RunOnVirtualThread
  public Response put(
      @PathParam("id") @UuidFormat String id,
      @Valid IncomeAttributePutRequest request,
      @HeaderParam("If-Match") String ifMatch) {
    incomeAttributeUpdateService.update(request.toCommand(id, IfMatchParser.parse(ifMatch)));
    return Response.noContent().build();
  }

  @DELETE
  @Path("{id}")
  @RunOnVirtualThread
  public Response delete(
      @PathParam("id") @UuidFormat String id, @HeaderParam("If-Match") String ifMatch) {
    incomeAttributeDeletionService.delete(
        new IncomeAttributeIdentifier(id), IfMatchParser.parse(ifMatch));
    return Response.noContent().build();
  }

  @GET
  @RunOnVirtualThread
  public Response list() {
    List<IncomeAttributeDetail> details = incomeAttributeSearchResultQuerier.list();
    String etag =
        new IncomeAttributeOrder(
                details.stream().map(detail -> new IncomeAttributeIdentifier(detail.id())).toList())
            .digest()
            .value();
    return Response.ok(IncomeAttributeGetListResponse.from(details)).tag(etag).build();
  }

  @GET
  @Path("{id}")
  @RunOnVirtualThread
  public Response get(@PathParam("id") @UuidFormat String id) {
    IncomeAttributeDetail detail =
        incomeAttributeSearchResultQuerier.get(new IncomeAttributeIdentifier(id));
    IncomeAttributeGetResponse response = IncomeAttributeGetResponse.from(detail);
    return Response.ok(response).tag(detail.digest().value()).build();
  }
}
