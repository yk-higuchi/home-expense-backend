package dev.higuchi.homeexpense.query;

public record Pagination(int page, int perPage) {

  public static final int PAGE_MIN = 1;
  public static final int PAGE_MAX = 1_000_000;
  public static final int PER_PAGE_MIN = 1;
  public static final int PER_PAGE_MAX = 100;

  public Pagination {
    if (page < PAGE_MIN || page > PAGE_MAX) {
      throw new IllegalArgumentException("page must be between " + PAGE_MIN + " and " + PAGE_MAX);
    }
    if (perPage < PER_PAGE_MIN || perPage > PER_PAGE_MAX) {
      throw new IllegalArgumentException(
          "perPage must be between " + PER_PAGE_MIN + " and " + PER_PAGE_MAX);
    }
  }

  public int offset() {
    return (page - 1) * perPage;
  }

  public int previousPage() {
    return page - 1;
  }

  public int nextPage() {
    return page + 1;
  }

  /** 最終ページ番号。0 件でも 1 ページ目は存在するものとして 1 を返す */
  public int lastPage(int totalCount) {
    int division = totalCount / perPage;
    int last = totalCount % perPage > 0 ? division + 1 : division;
    return Math.max(PAGE_MIN, last);
  }

  public boolean isFirstPage() {
    return page == PAGE_MIN;
  }

  /** 最終ページ以降（最終ページを超えたページ番号を含む）なら true */
  public boolean isLastPage(int totalCount) {
    return page >= lastPage(totalCount);
  }
}
