package dev.yhiguchi.home_expense.presentation.api.balance.statistics;

import dev.yhiguchi.home_expense.query.balance.statistics.BalanceStatistics;
import dev.yhiguchi.home_expense.query.balance.statistics.BalanceStatisticsCriteria;
import dev.yhiguchi.home_expense.query.balance.statistics.BalanceStatisticsQuerier;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/balance-statistics")
@Produces(MediaType.APPLICATION_JSON)
public class BalanceStatisticsApi {

  BalanceStatisticsQuerier balanceStatisticsQuerier;

  public BalanceStatisticsApi(BalanceStatisticsQuerier balanceStatisticsQuerier) {
    this.balanceStatisticsQuerier = balanceStatisticsQuerier;
  }

  @GET
  @RunOnVirtualThread
  public Response get(
      @QueryParam("year") @NotNull(message = "yearは必須です") Integer year,
      @QueryParam("month") Integer month) {
    BalanceStatisticsCriteria criteria = new BalanceStatisticsCriteria(year, month);
    BalanceStatistics statistics = balanceStatisticsQuerier.search(criteria);
    return Response.ok(BalanceStatisticsResponse.of(statistics)).build();
  }
}
