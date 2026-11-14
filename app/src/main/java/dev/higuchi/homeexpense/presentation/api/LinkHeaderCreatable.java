package dev.higuchi.homeexpense.presentation.api;

import dev.higuchi.homeexpense.query.Pagination;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;

/**
 * RFC 8288 Link ヘッダーの組立ユーティリティ。
 *
 * <p>関係型は IANA 登録済みの self / first / last / prev / next を使う。リンク先はリクエスト URI を元にし、絞り込み条件などの
 * クエリパラメータを保持したまま page / per_page だけを置き換える。
 */
public final class LinkHeaderCreatable {

  private LinkHeaderCreatable() {}

  public static String create(UriInfo uriInfo, Pagination pagination, int totalCount) {
    int lastPage = pagination.lastPage(totalCount);
    ArrayList<String> linkList = new ArrayList<>();
    linkList.add(linkOf(uriInfo, "self", pagination.page(), pagination.perPage()));
    linkList.add(linkOf(uriInfo, "first", Pagination.PAGE_MIN, pagination.perPage()));
    linkList.add(linkOf(uriInfo, "last", lastPage, pagination.perPage()));
    if (!pagination.isFirstPage()) {
      int previous = Math.min(pagination.previousPage(), lastPage);
      linkList.add(linkOf(uriInfo, "prev", previous, pagination.perPage()));
    }
    if (!pagination.isLastPage(totalCount)) {
      linkList.add(linkOf(uriInfo, "next", pagination.nextPage(), pagination.perPage()));
    }
    return String.join(", ", linkList);
  }

  private static String linkOf(UriInfo uriInfo, String rel, int page, int perPage) {
    return String.format(
        "<%s>; rel=\"%s\"",
        uriInfo
            .getRequestUriBuilder()
            .replaceQueryParam("page", page)
            .replaceQueryParam("per_page", perPage)
            .build(),
        rel);
  }
}
