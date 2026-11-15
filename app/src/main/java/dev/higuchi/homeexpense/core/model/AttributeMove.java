package dev.higuchi.homeexpense.core.model;

import java.util.ArrayList;
import java.util.List;

public record AttributeMove(int from, int to) {

  public static final int END = -1;

  public static <T> List<T> applyAll(List<T> items, List<AttributeMove> moves) {
    List<T> result = new ArrayList<>(items);
    for (AttributeMove move : moves) {
      if (move.from < 0 || move.from >= result.size()) {
        throw new InvalidAttributeMoveException("移動元の位置 " + move.from + " は範囲外です");
      }
      T item = result.remove(move.from);
      int to = move.to == END ? result.size() : move.to;
      if (to < 0 || to > result.size()) {
        throw new InvalidAttributeMoveException("移動先の位置 " + move.to + " は範囲外です");
      }
      result.add(to, item);
    }
    return List.copyOf(result);
  }
}
