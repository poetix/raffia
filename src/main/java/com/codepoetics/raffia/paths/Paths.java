package com.codepoetics.raffia.paths;

import com.codepoetics.raffia.api.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Paths {

  private Paths() {
  }

  private static final Path EMPTY = new EmptyPath();

  public static Path empty() {
    return EMPTY;
  }

  public static Path create(PathSegment...segments) {
    Path result = empty();
    for (int i = segments.length - 1; i >=0; i--) {
      result = result.prepend(segments[i]);
    }
    return result;
  }

  public static PathSegment toArrayIndex(int index) {
    return new ArrayIndexPathSegment(index);
  }

  public static PathSegment toArrayIndex(String predicateDescription, IndexValuePredicate predicate) {
    return new ItemPredicatePathSegment(predicateDescription, predicate);
  }

  public static PathSegment toDeepScanObjectProperty(String key) {
    return new DeepScanToObjectKeyPathSegment(key);
  }

  public static PathSegment toArrayWildcard() {
    return ItemPredicatePathSegment.ANY;
  }

  public static PathSegment toObjectProperty(String key) {
    return new ObjectKeyPathSegment(key);
  }

  public static Visitor<Basket> updateWith(Path path, Visitor<Basket> updater) {
    return path.isEmpty()
        ? updater
        : path.head().createUpdater(path.tail(), updater);
  }

  public static <T> Visitor<List<T>> projectWith(Path path, Visitor<List<T>> projector) {
    return path.isEmpty()
        ? projector
        : path.head().createProjector(path, projector);
  }

  public static PathSegment toObjectWildcard() {
    return ObjectPredicatePathSegment.all;
  }

  public static PathSegment toArrayIndices(int...indices) {
    StringBuilder representation = new StringBuilder();
    final Set<Integer> indexSet = new HashSet<>();
    boolean first = true;

    for (int index : indices) {
      if (first) {
        first = false;
      } else {
        representation.append(",");
      }
      representation.append(index);
      indexSet.add(index);
    }

    return toArrayIndex(representation.toString(), new IndexValuePredicate() {
      @Override
      public boolean test(int index, Basket value) {
        return indexSet.contains(index);
      }
    });
  }

  private static final class EmptyPath implements Path {

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public PathSegment head() {
      throw new UnsupportedOperationException("Called head on an empty path");
    }

    @Override
    public Path tail() {
      throw new UnsupportedOperationException("Called tail on an empty path");
    }

    @Override
    public Path prepend(PathSegment segment) {
      return new NonEmptyPath(segment, this);
    }

    @Override
    public String toString() {
      return "<empty path>";
    }

  }

  private static final class NonEmptyPath implements Path {

    private final PathSegment head;
    private final Path tail;

    private NonEmptyPath(PathSegment head, Path tail) {
      this.head = head;
      this.tail = tail;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public PathSegment head() {
      return head;
    }

    @Override
    public Path tail() {
      return tail;
    }

    @Override
    public Path prepend(PathSegment segment) {
      return new NonEmptyPath(segment, this);
    }

    @Override
    public String toString() {
      Path cursor = this;
      StringBuilder stringBuilder = new StringBuilder();
      while (!cursor.isEmpty()) {
        stringBuilder.append(cursor.head().representation());
        cursor = cursor.tail();
      }
      return stringBuilder.toString();
    }
  }
}
