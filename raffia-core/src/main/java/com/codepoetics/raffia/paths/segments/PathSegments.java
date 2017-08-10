package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.PathSegment;
import com.codepoetics.raffia.api.Visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class PathSegments {

  private PathSegments() {
  }

  public static PathSegment ofArrayIndex(int arrayIndex) {
    return new ArrayIndexPathSegment(Collections.singletonList(arrayIndex));
  }

  public static PathSegment ofArrayIndices(int first, int...remaining) {
    List<Integer> indices = new ArrayList<>(remaining.length + 1);
    indices.add(first);
    for (int i : remaining) {
      indices.add(i);
    }
    return ofArrayIndices(indices);
  }

  public static PathSegment ofArrayIndices(Collection<Integer> arrayIndices) {
    return new ArrayIndexPathSegment(arrayIndices);
  }

  public static PathSegment ofWildcard() {
    return new WildcardPathSegment();
  }

  public static PathSegment ofObjectKey(String objectKey) {
    return new ObjectKeyPathSegment(Collections.singletonList(objectKey));
  }

  public static PathSegment ofAny(String objectKey) {
    return new DeepScanToObjectKeyPathSegment(objectKey);
  }

  public static PathSegment itemMatching(String representation, Visitor<Boolean> predicate) {
    return new MatchingItemPathSegment(representation, predicate);
  }

  public static PathSegment ofObjectKeys(String first, String...remaining) {
    List<String> keys = new ArrayList<>(remaining.length + 1);
    keys.add(first);
    for (String key : remaining) {
      keys.add(key);
    }
    return ofObjectKeys(keys);
  }

  public static PathSegment ofObjectKeys(Collection<String> keys) {
    return new ObjectKeyPathSegment(keys);
  }
}
