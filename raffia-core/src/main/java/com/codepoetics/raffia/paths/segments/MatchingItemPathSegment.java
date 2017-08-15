package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.ObjectEntry;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.operations.*;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.paths.PathSegmentMatchResult;

final class MatchingItemPathSegment extends BasePathSegment {

  private final String representation;
  private final BasketPredicate predicate;

  MatchingItemPathSegment(String representation, BasketPredicate predicate) {
    this.representation = representation;
    this.predicate = predicate;
  }

  @Override
  public boolean isConditional() {
    return true;
  }

  @Override
  public Updater createItemUpdater(Path tail, Updater updater) {
    return Updaters.branch(
        predicate,
        tail.isEmpty() ? updater : tail.head().createUpdater(tail, updater),
        Updaters.NO_OP
    );
  }

  @Override
  public Projector<Basket> createItemProjector(Path tail) {
    return Projectors.branch(
        predicate,
        tail.isEmpty()
            ? Projectors.id()
            : tail.head().createProjector(tail),
        Projectors.<Basket>alwaysEmpty());
  }

  @Override
  protected Updater createUpdater(final Updater continuation) {
    return new StructUpdater() {
      @Override
      public Basket updateArray(ArrayContents items) {
        ArrayContents updated = items;
        for (int i = 0; i < items.size(); i++) {
          Basket item = items.get(i);
          if (predicate.test(item)) {
            updated = updated.with(i, continuation.update(item));
          }
        }
        return Basket.ofArray(updated);
      }

      @Override
      public Basket updateObject(PropertySet properties) {
        PropertySet updated = properties;
        for (ObjectEntry entry : properties) {
          if (predicate.test(entry.getValue())) {
            updated = updated.with(entry.getKey(), continuation.update(entry.getValue()));
          }
        }
        return Basket.ofObject(updated);
      }
    };
  }

  @Override
  protected Projector<Basket> createProjector(final Projector<Basket> continuation) {
    return new StructProjector<Basket>() {
      @Override
      public ProjectionResult<Basket> projectArray(ArrayContents items) {
        ProjectionResult<Basket> result = ProjectionResult.empty();
        for (Basket item : items) {
          if (predicate.test(item)) {
            result = result.add(continuation.project(item));
          }
        }
        return result;
      }

      @Override
      public ProjectionResult<Basket> projectObject(PropertySet properties) {
        ProjectionResult<Basket> result = ProjectionResult.empty();
        for (ObjectEntry entry : properties) {
          if (predicate.test(entry.getValue())) {
            result = result.add(continuation.project(entry.getValue()));
          }
        }
        return result;
      }
    };
  }

  @Override
  public PathSegmentMatchResult matchesIndex(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PathSegmentMatchResult matchesKey(String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String representation() {
    return representation;
  }

}
