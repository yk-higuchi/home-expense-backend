package dev.higuchi.homeexpense.core.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class AttributeMoveTest {

  List<String> items = List.of("a", "b", "c", "d");

  @Test
  void 取り出した要素を移動先の位置に入れる() {
    assertEquals(
        List.of("c", "a", "b", "d"),
        AttributeMove.applyAll(items, List.of(new AttributeMove(2, 0))));
    assertEquals(
        List.of("b", "c", "a", "d"),
        AttributeMove.applyAll(items, List.of(new AttributeMove(0, 2))));
  }

  @Test
  void 移動先の末尾指定は最後に置く() {
    assertEquals(
        List.of("b", "c", "d", "a"),
        AttributeMove.applyAll(items, List.of(new AttributeMove(0, AttributeMove.END))));
    assertEquals(
        List.of("b", "c", "d", "a"),
        AttributeMove.applyAll(items, List.of(new AttributeMove(0, 3))));
  }

  @Test
  void 移動は順に適用する() {
    assertEquals(
        List.of("d", "a", "c", "b"),
        AttributeMove.applyAll(items, List.of(new AttributeMove(1, 3), new AttributeMove(2, 0))));
  }

  @Test
  void 範囲外の位置は例外() {
    assertThrows(
        InvalidAttributeMoveException.class,
        () -> AttributeMove.applyAll(items, List.of(new AttributeMove(4, 0))));
    assertThrows(
        InvalidAttributeMoveException.class,
        () -> AttributeMove.applyAll(items, List.of(new AttributeMove(0, 4))));
    assertThrows(
        InvalidAttributeMoveException.class,
        () -> AttributeMove.applyAll(items, List.of(new AttributeMove(-1, 0))));
  }

  @Test
  void 元のリストは変更しない() {
    AttributeMove.applyAll(items, List.of(new AttributeMove(0, 3)));
    assertEquals(List.of("a", "b", "c", "d"), items);
  }
}
