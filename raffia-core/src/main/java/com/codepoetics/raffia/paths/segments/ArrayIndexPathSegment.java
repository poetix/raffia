package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.ProjectionResult;
import com.codepoetics.raffia.operations.Projector;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.paths.PathSegmentMatchResult;

import java.util.Collection;

final class ArrayIndexPathSegment extends BasePathSegment {

  private final Collection<Integer> indices;

  ArrayIndexPathSegment(Collection<Integer> indices) {
    this.indices = indices;
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
        for (int index : indices) {
          int actual = index < 0 ? items.size() + index : index;
          if (actual < items.size()) {
            updated = updated.with(actual, continuation.update(updated.get(actual)));
          }
        }
        return Basket.ofArray(updated);
      }
    };
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
        for (int index : indices) {
          int actual = index < 0 ? items.size() + index : index;
          if (actual >= 0 && actual < items.size()) {
            result = result.add(continuation.project(items.get(actual)));
          }
        }
        return result;
      }
    };
  }

  @Override
  public PathSegmentMatchResult matchesIndex(int index) {
    return indices.contains(index) ? PathSegmentMatchResult.MATCHED_BOUND : PathSegmentMatchResult.UNMATCHED;
  }

  @Override
  public PathSegmentMatchResult matchesKey(String key) {
    return PathSegmentMatchResult.UNMATCHED;
  }

  @Override
  public String representation() {
    return indices.toString();
  }

}
