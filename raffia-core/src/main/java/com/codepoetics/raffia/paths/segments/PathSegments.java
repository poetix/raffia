package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.IndexValuePredicate;
import com.codepoetics.raffia.api.PathSegment;

public final class PathSegments {

  private PathSegments() {
  }

  public static PathSegment ofArrayIndex(int arrayIndex) {
    return new ArrayIndexPathSegment(arrayIndex);
  }

  public static PathSegment ofWildcard() {
    return new WildcardPathSegment();
  }

  public static PathSegment ofObjectKey(String objectKey) {
    return new ObjectKeyPathSegment(objectKey);
  }

  public static PathSegment ofAny(String objectKey) {
    return new DeepScanToObjectKeyPathSegment(objectKey);
  }

  public static PathSegment itemMatching(String representation, IndexValuePredicate predicate) {
    return new ItemPredicatePathSegment(representation, predicate);
  }
}
