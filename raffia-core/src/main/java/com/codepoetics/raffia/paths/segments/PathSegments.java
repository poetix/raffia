package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.IndexValuePredicate;
import com.codepoetics.raffia.api.PathSegment;

import java.util.Arrays;
import java.util.Collections;

public final class PathSegments {

  private PathSegments() {
  }

  public static PathSegment ofArrayIndex(int arrayIndex) {
    return new ArrayIndexPathSegment(Collections.singletonList(arrayIndex));
  }

  public static PathSegment ofArrayIndices(Integer...arrayIndices) {
    return new ArrayIndexPathSegment(Arrays.asList(arrayIndices));
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

  public static PathSegment itemMatching(String representation, IndexValuePredicate predicate) {
    return new ItemPredicatePathSegment(representation, predicate);
  }

  public static PathSegment ofObjectKeys(String...keys) {
    return new ObjectKeyPathSegment(Arrays.asList(keys));
  }
}
