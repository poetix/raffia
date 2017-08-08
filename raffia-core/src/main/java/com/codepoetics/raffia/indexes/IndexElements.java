package com.codepoetics.raffia.indexes;

import com.codepoetics.raffia.api.IndexElement;
import com.codepoetics.raffia.api.PathSegment;
import com.codepoetics.raffia.api.PathSegmentMatchResult;

final class IndexElements {

  private IndexElements() {
  }

  private static final IndexElement keyPlaceholder = new KeyPlaceholder();

  static IndexElement arrayStart() {
    return new ArrayElement(0);
  }

  static IndexElement objectStart() {
    return keyPlaceholder;
  }

  static IndexElement objectKey(String key) {
    return new KeyElement(key);
  }

  private static final class ArrayElement implements IndexElement {

    private final int index;

    private ArrayElement(int index) {
      this.index = index;
    }

    @Override
    public PathSegmentMatchResult isMatchedBy(PathSegment pathSegment) {
      return pathSegment.matchesIndex(index);
    }

    @Override
    public IndexElement advance() {
      return new ArrayElement(index + 1);
    }


    @Override
    public boolean equals(Object other) {
      return this == other
          || (other instanceof ArrayElement
          && ArrayElement.class.cast(other).index == (index));
    }

    @Override
    public int hashCode() {
      return index;
    }

    @Override
    public String toString() {
      return "[" + index + "]";
    }
  }

  private static final class KeyPlaceholder implements IndexElement {

    @Override
    public PathSegmentMatchResult isMatchedBy(PathSegment pathSegment) {
      return PathSegmentMatchResult.UNMATCHED;
    }

    @Override
    public IndexElement advance() {
      throw new IllegalStateException("Cannot advance until we have received a value");
    }


    @Override
    public boolean equals(Object other) {
      return this == other;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    @Override
    public String toString() {
      return "";
    }
  }

  private static final class KeyElement implements IndexElement {

    private final String key;

    private KeyElement(String key) {
      this.key = key;
    }

    @Override
    public PathSegmentMatchResult isMatchedBy(PathSegment pathSegment) {
      return pathSegment.matchesKey(key);
    }

    @Override
    public IndexElement advance() {
      return keyPlaceholder;
    }

    @Override
    public boolean equals(Object other) {
      return this == other
          || (other instanceof KeyElement
            && KeyElement.class.cast(other).key.equals(key));
    }

    @Override
    public int hashCode() {
      return key.hashCode();
    }

    @Override
    public String toString() {
      return "." + key;
    }
  }
 }
