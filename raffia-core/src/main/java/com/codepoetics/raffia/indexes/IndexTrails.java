package com.codepoetics.raffia.indexes;

import com.codepoetics.raffia.api.IndexElement;
import com.codepoetics.raffia.api.IndexTrail;
import com.codepoetics.raffia.api.Path;
import com.codepoetics.raffia.api.PathSegment;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

public final class IndexTrails {

  private IndexTrails() {
  }

  public static IndexTrail empty() {
    return new PathMatchingIndexTrail(TreePVector.<IndexElement>empty());
  }

  private static final class PathMatchingIndexTrail implements IndexTrail {

    private final PVector<IndexElement> elements;

    private PathMatchingIndexTrail(PVector<IndexElement> elements) {
      this.elements = elements;
    }

    @Override
    public IndexTrail enterArray() {
      return new PathMatchingIndexTrail(elements.plus(IndexElements.arrayStart()));
    }

    @Override
    public IndexTrail enterObject() {
      return new PathMatchingIndexTrail(elements.plus(IndexElements.objectStart()));
    }

    @Override
    public IndexTrail atKey(String key) {
      int lastIndex = elements.size() - 1;
      return new PathMatchingIndexTrail(elements.with(lastIndex, IndexElements.objectKey(key)));
    }

    @Override
    public boolean isEmpty() {
      return elements.isEmpty();
    }

    @Override
    public IndexTrail advance() {
      if (isEmpty()) {
        return this;
      }
      int lastIndex = elements.size() - 1;
      return new PathMatchingIndexTrail(elements.with(lastIndex, elements.get(lastIndex).advance()));
    }

    @Override
    public boolean isMatchedBy(Path path) {
      if (path.isEmpty()) {
        return isEmpty();
      }

      if (isEmpty()) {
        return false;
      }

      PathSegment pathSegment = path.head();
      IndexElement indexElement = head();

      switch (indexElement.isMatchedBy(pathSegment)) {
        case MATCHED_UNBOUND:
          return tail().isMatchedBy(path);
        case MATCHED_BOUND:
          return tail().isMatchedBy(path.tail());
        default:
          return false;
      }
    }

    @Override
    public IndexElement head() {
      if (elements.isEmpty()) {
        throw new IllegalStateException("Cannot call head on an empty IndexTrail");
      }
      return elements.get(0);
    }

    @Override
    public IndexTrail tail() {
      return new PathMatchingIndexTrail(elements.minus(0));
    }

    @Override
    public boolean equals(Object other) {
      return this == other
          || (other instanceof PathMatchingIndexTrail
            && PathMatchingIndexTrail.class.cast(other).elements.equals(elements));
    }

    @Override
    public int hashCode() {
      return elements.hashCode();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("$");
      for (IndexElement element : elements) {
        sb.append(element);
      }
      return sb.toString();
    }
  }
}
