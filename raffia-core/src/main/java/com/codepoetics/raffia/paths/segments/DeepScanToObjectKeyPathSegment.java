package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.ObjectEntry;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.operations.ProjectionResult;
import com.codepoetics.raffia.operations.Projector;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.paths.PathSegmentMatchResult;

import java.util.ArrayList;
import java.util.List;

final class DeepScanToObjectKeyPathSegment extends BasePathSegment {

  private final String key;

  DeepScanToObjectKeyPathSegment(String key) {
    this.key = key;
  }

  @Override
  protected Updater createUpdater(final Updater continuation) {
    return new StructUpdater() {
      @Override
      public Basket updateArray(ArrayContents items) {
        List<Basket> updated = new ArrayList<>();

        for (Basket basket : items) {
          updated.add(update(basket));
        }

        return Basket.ofArray(updated);
      }

      @Override
      public Basket updateObject(PropertySet properties) {
        List<ObjectEntry> entries = new ArrayList<>(properties.size());

        for (ObjectEntry entry : properties) {
          if (entry.getKey().equals(key)) {
            entries.add(updateWith(entry, continuation));
          } else {
            entries.add(updateWith(entry, this));
          }
        }

        return Basket.ofObject(entries);
      }

      private ObjectEntry updateWith(ObjectEntry entry, Updater updater) {
        return ObjectEntry.of(entry.getKey(), updater.update(entry.getValue()));
      }
    };
  }

  @Override
  protected Projector<Basket> createProjector(final Projector<Basket> continuation) {
    return new StructProjector<Basket>() {
      @Override
      public ProjectionResult<Basket> projectArray(ArrayContents items) {
        ProjectionResult<Basket> result = ProjectionResult.empty();
        for (Basket basket : items) {
          result = result.add(project(basket));
        }
        return result;
      }

      @Override
      public ProjectionResult<Basket> projectObject(PropertySet properties) {
        ProjectionResult<Basket> result = ProjectionResult.empty();
        for (ObjectEntry entry : properties) {
          if (entry.getKey().equals(key)) {
            result = result.add(continuation.project(entry.getValue()));
          } else {
            result = result.add(project(entry.getValue()));
          }
        }
        return result;
      }
    };
  }

  @Override
  public PathSegmentMatchResult matchesIndex(int index) {
    return PathSegmentMatchResult.MATCHED_UNBOUND;
  }

  @Override
  public PathSegmentMatchResult matchesKey(String key) {
    return this.key.equals(key)
        ? PathSegmentMatchResult.MATCHED_BOUND
        : PathSegmentMatchResult.MATCHED_UNBOUND;
  }

  @Override
  public String representation() {
    return ".." + key;
  }
}
