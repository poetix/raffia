package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.ProjectionResult;
import com.codepoetics.raffia.operations.Projector;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.paths.PathSegmentMatchResult;

final class ArraySlicePathSegment extends BasePathSegment {

  private final int startIndex;
  private final int endIndex;

  ArraySlicePathSegment(int startIndex, int endIndex) {
    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }

  @Override
  protected Updater createUpdater(final Updater continuation) {
    return new Updater() {
      @Override
      public Basket update(Basket basket) {
        if (basket.isArray()) {
          return updateArray(basket.asArray());
        }

        return basket;
      }

      private Basket updateArray(ArrayContents items) {
        ArrayContents updated = items;

        int actualStart = getActualStart(startIndex, items.size());
        int actualEnd = getActualEnd(endIndex, items.size());

        for (int i = actualStart; i < actualEnd; i++) {
          updated = updated.with(i, continuation.update(updated.get(i)));
        }

        return Basket.ofArray(updated);
      }
    };
  }

  private static int getActualStart(int startIndex, int arraySize) {
    return startIndex == PathSegments.LOWER_UNBOUNDED
        ? 0
        : startIndex < 0 ? arraySize + startIndex : startIndex;
  }

  private static int getActualEnd(int endIndex, int arraySize) {
    return endIndex == PathSegments.UPPER_UNBOUNDED
        ? arraySize
        : endIndex < 0 ? arraySize + endIndex : Math.min(endIndex, arraySize);
  }

  @Override
  protected Projector<Basket> createProjector(final Projector<Basket> continuation) {
    return new Projector<Basket>() {
      @Override
      public ProjectionResult<Basket> project(Basket basket) {
        if (basket.isArray()) {
          return projectArray(basket.asArray());
        }

        return ProjectionResult.empty();
      }

      private ProjectionResult<Basket> projectArray(ArrayContents items) {
        ProjectionResult<Basket> result = ProjectionResult.empty();

        int actualStart = getActualStart(startIndex, items.size());
        int actualEnd = getActualEnd(endIndex, items.size());

        for (int i = actualStart; i < actualEnd; i++) {
            result = result.add(continuation.project(items.get(i)));
        }
        return result;
      }
    };
  }

  @Override
  public PathSegmentMatchResult matchesIndex(int index) {
    return index >= startIndex && index < endIndex ? PathSegmentMatchResult.MATCHED_BOUND : PathSegmentMatchResult.UNMATCHED;
  }

  @Override
  public PathSegmentMatchResult matchesKey(String key) {
    return PathSegmentMatchResult.UNMATCHED;
  }

  @Override
  public String representation() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    if (startIndex != PathSegments.LOWER_UNBOUNDED) {
      sb.append(startIndex);
    }
    sb.append(":");
    if (endIndex != PathSegments.UPPER_UNBOUNDED) {
      sb.append(endIndex);
    }
    sb.append("]");
    return sb.toString();
  }

}
