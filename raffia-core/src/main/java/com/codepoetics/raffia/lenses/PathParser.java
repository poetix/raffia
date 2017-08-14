package com.codepoetics.raffia.lenses;

import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.paths.PathSegment;
import com.codepoetics.raffia.paths.segments.PathSegments;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PathParser {

  private PathParser() {
  }

  private static final Pattern keyExpr = Pattern.compile("^\\.[a-zA-Z0-9\\-_]+");
  private static final Pattern deepKeyExpr = Pattern.compile("^\\.\\.[a-zA-Z0-9\\-_]+");
  private static final Pattern indexExpr = Pattern.compile("^\\[([^]]+)]");
  private static final Pattern integerExpr = Pattern.compile("^-?[0-9]+$");

  static PVector<PathSegment> parse(String pathString, PVector<Visitor<Boolean>> predicates) {
    String trimmed = pathString.trim();
    if (!trimmed.startsWith("$") &! trimmed.startsWith("@")) {
      throw new IllegalArgumentException("Path string must begin with $ or @");
    }
    return parseRemaining(TreePVector.<PathSegment>empty(), trimmed.substring(1), predicates);
  }

  private static PVector<PathSegment> parseRemaining(PVector<PathSegment> parsed, String remaining, PVector<Visitor<Boolean>> predicates) {
    if (remaining.isEmpty()) {
      if (!predicates.isEmpty()) {
        throw new IllegalArgumentException("Unmatched predicate");
      }
      return parsed;
    }

    if (remaining.startsWith(".*")) {
      return parseRemaining(parsed.plus(PathSegments.ofWildcard()), remaining.substring(2), predicates);
    }

    if (remaining.startsWith("[*]")) {
      return parseRemaining(parsed.plus(PathSegments.ofWildcard()), remaining.substring(3), predicates);
    }

    if (remaining.startsWith("[?]")) {
      if (predicates.isEmpty()) {
        throw new IllegalArgumentException("Predicate expression without matching predicate");
      }

      return parseRemaining(
          parsed.plus(PathSegments.itemMatching("[?]", predicates.get(0))),
          remaining.substring(3),
          predicates.minus(0));
    }

    Matcher keyExprMatcher = keyExpr.matcher(remaining);
    if (keyExprMatcher.find()) {
      String key = keyExprMatcher.group().substring(1);
      return parseRemaining(parsed.plus(PathSegments.ofObjectKey(key)), remaining.substring(key.length() + 1), predicates);
    }

    Matcher deepKeyExprMatcher = deepKeyExpr.matcher(remaining);
    if (deepKeyExprMatcher.find()) {
      String key = deepKeyExprMatcher.group().substring(2);
      return parseRemaining(parsed.plus(PathSegments.ofAny(key)), remaining.substring(key.length() + 2), predicates);
    }

    Matcher indexExprMatcher = indexExpr.matcher(remaining);
    if (!indexExprMatcher.find()) {
      throw new IllegalArgumentException("Unrecognised path segment: " + remaining);
    }
    String indexExpr = indexExprMatcher.group(1);
    return parseRemaining(parsed.plus(parseIndexExpression(indexExpr.trim())), remaining.substring(indexExpr.length() + 2), predicates);
  }

  private static PathSegment parseIndexExpression(String expression) {
    if (expression.contains(",")) {
      return parseMultiIndexExpression(expression);
    }

    if (expression.contains(":")) {
      throw new UnsupportedOperationException("Ranges not supported yet");
    }

    if (expression.startsWith("'") && expression.endsWith("'")) {
      return PathSegments.ofObjectKey(expression.substring(1, expression.length() - 1));
    }

    if (integerExpr.matcher(expression).matches()) {
      return PathSegments.ofArrayIndex(Integer.valueOf(expression));
    }

    throw new IllegalArgumentException("Unrecognised index expression: " + expression);
  }

  private static PathSegment parseMultiIndexExpression(String expression) {
    String[] indices = expression.split(",");
    if (indices[0].trim().startsWith("'")) {
      return toKeys(indices);
    }

    return toArrayIndices(indices);
  }

  private static PathSegment toKeys(String[] indices) {
    Collection<String> keys = new ArrayList<>(indices.length);
    for (String index : indices) {
      String keyExpr = index.trim();
      keys.add(keyExpr.substring(1, keyExpr.length() - 1).trim());
    }
    return PathSegments.ofObjectKeys(keys);
  }

  private static PathSegment toArrayIndices(String[] indices) {
    List<Integer> intIndices = new ArrayList<>(indices.length);

    for (String index : indices) {
      String trimmed = index.trim();
      if (!integerExpr.matcher(trimmed).matches()) {
        throw new IllegalArgumentException("Unrecognised index expression:" + trimmed);
      }
      intIndices.add(Integer.valueOf(trimmed));
    }

    return PathSegments.ofArrayIndices(intIndices);
  }
}
