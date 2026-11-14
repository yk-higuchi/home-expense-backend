package dev.yhiguchi.home_expense.presentation.api;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class QueryParamConverterProviderTest {

  private final QueryParamConverterProvider sut = new QueryParamConverterProvider();

  @SuppressWarnings("unused")
  void signature(@QueryParam("year") Integer year, @PathParam("id") Integer id, Integer plain) {}

  @Test
  void クエリパラメータには変換器を返す() {
    assertNotNull(sut.getConverter(Integer.class, Integer.class, annotationsOf(0)));
  }

  @Test
  void パスパラメータは対象外() {
    assertNull(sut.getConverter(Integer.class, Integer.class, annotationsOf(1)));
  }

  @Test
  void アノテーションなしは対象外() {
    assertNull(sut.getConverter(Integer.class, Integer.class, annotationsOf(2)));
  }

  @Test
  void 対応していない型は対象外() {
    assertNull(sut.getConverter(String.class, String.class, annotationsOf(0)));
  }

  @Test
  void 変換できない値はパラメータ名入りの例外() {
    var converter = sut.getConverter(LocalDate.class, LocalDate.class, annotationsOf(0));

    var e = assertThrows(InvalidQueryParameterException.class, () -> converter.fromString("x"));
    assertEquals("yearはyyyy-MM-dd形式の日付を指定してください", e.getMessage());
  }

  @Test
  void 空文字はnull() {
    var converter = sut.getConverter(Integer.class, Integer.class, annotationsOf(0));

    assertNull(converter.fromString(""));
    assertNull(converter.fromString(" "));
  }

  private static Annotation[] annotationsOf(int index) {
    try {
      Method m =
          QueryParamConverterProviderTest.class.getDeclaredMethod(
              "signature", Integer.class, Integer.class, Integer.class);
      return m.getParameterAnnotations()[index];
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
  }
}
