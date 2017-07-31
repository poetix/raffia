package com.codepoetics.raffia.paths;

import com.codepoetics.raffia.api.Path;
import com.codepoetics.raffia.api.PathSegment;
import org.pcollections.PVector;

public final class Paths {

  private Paths() {
  }

  private static final Path EMPTY = new EmptyPath();

  public static Path empty() {
    return EMPTY;
  }

  public static Path create(PVector<PathSegment> segments) {
    Path result = empty();
    for (int i = segments.size() - 1; i >=0; i--) {
      result = result.prepend(segments.get(i));
    }
    return result;
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
