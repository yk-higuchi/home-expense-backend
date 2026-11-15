package dev.higuchi.homeexpense.presentation.api;

import dev.higuchi.homeexpense.presentation.problem.AllowedMethodsResolver;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AcceptPatchResponseFilter implements ContainerResponseFilter {

  static final String ACCEPT_PATCH = "Accept-Patch";

  private final AllowedMethodsResolver allowedMethodsResolver;

  public AcceptPatchResponseFilter(AllowedMethodsResolver allowedMethodsResolver) {
    this.allowedMethodsResolver = allowedMethodsResolver;
  }

  @Override
  public void filter(ContainerRequestContext request, ContainerResponseContext response) {
    boolean options = HttpMethod.OPTIONS.equals(request.getMethod());
    boolean unsupportedMediaType = response.getStatus() == 415;
    if ((options || unsupportedMediaType)
        && allowedMethodsResolver.supports(request.getUriInfo().getPath(), HttpMethod.PATCH)) {
      response.getHeaders().putSingle(ACCEPT_PATCH, JsonPatchMoves.MEDIA_TYPE);
    }
  }
}
