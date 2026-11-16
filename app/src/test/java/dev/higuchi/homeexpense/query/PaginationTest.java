package dev.higuchi.homeexpense.query;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PaginationTest {

  @Test
  void offset_1ページ目() {
    Pagination pagination = new Pagination(1, 20);
    assertEquals(0, pagination.offset());
  }

  @Test
  void offset_2ページ目() {
    Pagination pagination = new Pagination(2, 20);
    assertEquals(20, pagination.offset());
  }

  @Test
  void offset_3ページ目() {
    Pagination pagination = new Pagination(3, 10);
    assertEquals(20, pagination.offset());
  }

  @Test
  void lastPage_割り切れる場合() {
    Pagination pagination = new Pagination(1, 10);
    assertEquals(5, pagination.lastPage(50));
  }

  @Test
  void lastPage_余りがある場合() {
    Pagination pagination = new Pagination(1, 10);
    assertEquals(6, pagination.lastPage(51));
  }

  @Test
  void lastPage_件数が0の場合は1() {
    Pagination pagination = new Pagination(1, 10);
    assertEquals(1, pagination.lastPage(0));
  }

  @Test
  void isFirstPage() {
    assertTrue(new Pagination(1, 10).isFirstPage());
    assertFalse(new Pagination(2, 10).isFirstPage());
  }

  @Test
  void isLastPage() {
    Pagination pagination = new Pagination(3, 10);
    assertTrue(pagination.isLastPage(30));
    assertFalse(pagination.isLastPage(31));
  }

  @Test
  void isLastPage_最終ページを超えたページもtrue() {
    assertTrue(new Pagination(5, 10).isLastPage(20));
  }

  @Test
  void isLastPage_件数が0なら1ページ目が最終ページ() {
    assertTrue(new Pagination(1, 10).isLastPage(0));
  }
}
