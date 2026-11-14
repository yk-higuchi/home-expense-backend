package dev.yhiguchi.home_expense.presentation.api.balance.series;

import dev.yhiguchi.home_expense.query.Granularity;
import dev.yhiguchi.home_expense.query.balance.series.BalanceSeries;
import dev.yhiguchi.home_expense.query.balance.series.BalanceSeriesCriteria;
import dev.yhiguchi.home_expense.query.balance.series.BalanceSeriesQuerier;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;

@Path("/v1/balance-series")
@Produces(MediaType.APPLICATION_JSON)
public class BalanceSeriesApi {

  BalanceSeriesQuerier balanceSeriesQuerier;

  public BalanceSeriesApi(BalanceSeriesQuerier balanceSeriesQuerier) {
    this.balanceSeriesQuerier = balanceSeriesQuerier;
  }

  @GET
  @RunOnVirtualThread
  public Response get(
      @QueryParam("from") @NotNull(message = "fromは必須です") LocalDate from,
      @QueryParam("to") @NotNull(message = "toは必須です") LocalDate to,
      @QueryParam("granularity") @NotNull(message = "granularityは必須です") Granularity granularity) {
    BalanceSeriesCriteria criteria = new BalanceSeriesCriteria(from, to, granularity);
    BalanceSeries series = balanceSeriesQuerier.search(criteria);
    return Response.ok(BalanceSeriesResponse.of(series)).build();
  }
}
