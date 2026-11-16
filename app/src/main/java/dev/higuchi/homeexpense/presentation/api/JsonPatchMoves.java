package dev.higuchi.homeexpense.presentation.api;

import dev.higuchi.homeexpense.core.model.AttributeMove;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonPatchMoves {

  public static final String MEDIA_TYPE = "application/json-patch+json";
  public static final int MAX_OPERATIONS = 100;

  private static final String INDEX = "(0|[1-9][0-9]{0,8})";

  private JsonPatchMoves() {}

  public static List<AttributeMove> parse(List<JsonPatchOperation> operations, String member) {
    Pattern from = Pattern.compile("/" + member + "/" + INDEX);
    Pattern to = Pattern.compile("/" + member + "/(" + INDEX + "|-)");
    List<AttributeMove> moves = new ArrayList<>(operations.size());
    for (JsonPatchOperation operation : operations) {
      if (operation == null || !"move".equals(operation.op())) {
        throw unsupported(member);
      }
      Matcher fromMatcher = from.matcher(String.valueOf(operation.from()));
      Matcher toMatcher = to.matcher(String.valueOf(operation.path()));
      if (operation.from() == null
          || operation.path() == null
          || !fromMatcher.matches()
          || !toMatcher.matches()) {
        throw unsupported(member);
      }
      int toIndex =
          "-".equals(toMatcher.group(1)) ? AttributeMove.END : Integer.parseInt(toMatcher.group(1));
      moves.add(new AttributeMove(Integer.parseInt(fromMatcher.group(1)), toIndex));
    }
    return moves;
  }

  private static UnsupportedJsonPatchException unsupported(String member) {
    return new UnsupportedJsonPatchException("JSON Patch は " + member + " の要素を移動する move だけを指定できます");
  }
}
