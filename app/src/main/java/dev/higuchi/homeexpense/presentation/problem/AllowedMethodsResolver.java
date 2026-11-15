package dev.higuchi.homeexpense.presentation.problem;

import dev.higuchi.homeexpense.presentation.api.balance.series.BalanceSeriesApi;
import dev.higuchi.homeexpense.presentation.api.balance.statistics.BalanceStatisticsApi;
import dev.higuchi.homeexpense.presentation.api.expense.ExpenseApi;
import dev.higuchi.homeexpense.presentation.api.expense.attribute.ExpenseAttributeApi;
import dev.higuchi.homeexpense.presentation.api.income.IncomeApi;
import dev.higuchi.homeexpense.presentation.api.income.attribute.IncomeAttributeApi;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

@ApplicationScoped
@RegisterForReflection(
    targets = {
      ExpenseApi.class,
      ExpenseAttributeApi.class,
      IncomeApi.class,
      IncomeAttributeApi.class,
      BalanceStatisticsApi.class,
      BalanceSeriesApi.class
    },
    methods = true)
public class AllowedMethodsResolver {

  static final List<Class<?>> RESOURCES =
      List.of(
          ExpenseApi.class,
          ExpenseAttributeApi.class,
          IncomeApi.class,
          IncomeAttributeApi.class,
          BalanceStatisticsApi.class,
          BalanceSeriesApi.class);

  private static final Pattern TEMPLATE_VARIABLE = Pattern.compile("\\{[^}]+}");

  private final Map<Pattern, Set<String>> routes = buildRoutes(RESOURCES);

  public Optional<String> allowFor(String path) {
    String normalized = normalize(path);
    TreeSet<String> allowed = new TreeSet<>();
    routes.forEach((pattern, methods) -> {
      if (pattern.matcher(normalized).matches()) {
        allowed.addAll(methods);
      }
    });
    return allowed.isEmpty() ? Optional.empty() : Optional.of(String.join(", ", allowed));
  }

  public boolean supports(String path, String method) {
    String normalized = normalize(path);
    return routes.entrySet().stream()
        .anyMatch(route ->
            route.getKey().matcher(normalized).matches() && route.getValue().contains(method));
  }

  private static Map<Pattern, Set<String>> buildRoutes(List<Class<?>> resources) {
    Map<String, Set<String>> byTemplate = new LinkedHashMap<>();
    for (Class<?> resource : resources) {
      String base = resource.getAnnotation(Path.class).value();
      for (Method method : resource.getDeclaredMethods()) {
        httpMethodOf(method)
            .ifPresent(httpMethod -> {
              Path sub = method.getAnnotation(Path.class);
              String template = normalize(base + "/" + (sub == null ? "" : sub.value()));
              Set<String> methods = byTemplate.computeIfAbsent(template, t -> new TreeSet<>());
              methods.add(httpMethod);
              if (httpMethod.equals(HttpMethod.GET)) {
                methods.add(HttpMethod.HEAD);
              }
              methods.add(HttpMethod.OPTIONS);
            });
      }
    }
    Map<Pattern, Set<String>> routes = new LinkedHashMap<>();
    byTemplate.forEach((template, methods) ->
        routes.put(
            Pattern.compile(
                TEMPLATE_VARIABLE
                    .splitAsStream(template + "\u0000")
                    .map(part -> Pattern.quote(part.replace("\u0000", "")))
                    .reduce((a, b) -> a + "[^/]+" + b)
                    .orElse("")),
            Set.copyOf(methods)));
    return routes;
  }

  private static Optional<String> httpMethodOf(Method method) {
    for (Annotation annotation : method.getAnnotations()) {
      HttpMethod httpMethod = annotation.annotationType().getAnnotation(HttpMethod.class);
      if (httpMethod != null) {
        return Optional.of(httpMethod.value());
      }
    }
    return Optional.empty();
  }

  private static String normalize(String path) {
    String collapsed = ("/" + path).replaceAll("/+", "/");
    return collapsed.length() > 1 && collapsed.endsWith("/")
        ? collapsed.substring(0, collapsed.length() - 1)
        : collapsed;
  }
}
