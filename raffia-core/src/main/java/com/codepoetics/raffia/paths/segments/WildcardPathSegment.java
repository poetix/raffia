package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.ObjectEntry;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.operations.ProjectionResult;
import com.codepoetics.raffia.operations.Projector;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.paths.PathSegmentMatchResult;

import java.util.ArrayList;
import java.util.List;

class WildcardPathSegment extends BasePathSegment {

  WildcardPathSegment() {
  }

  @Override
  public Updater createUpdater(final Updater continuation) {
    return new StructUpdater() {
      @Override
      public Basket updateArray(ArrayContents items) {
        List<Basket> updated = new ArrayList<>(items.size());

        for (Basket basket : items) {
          updated.add(continuation.update(basket));
        }

        return Basket.ofArray(updated);
      }

      @Override
      public Basket updateObject(PropertySet properties) {
        List<ObjectEntry> entries = new ArrayList<>(properties.size());

        for (ObjectEntry entry : properties) {
          entries.add(ObjectEntry.of(entry.getKey(), continuation.update(entry.getValue())));
        }

        return Basket.ofObject(entries);
      }
    };
  }

  @Override
  public Projector<Basket> createProjector(final Projector<Basket> continuation) {
    return new StructProjector<Basket>() {
      @Override
      public ProjectionResult<Basket> projectArray(ArrayContents items) {
        ProjectionResult<Basket> result = ProjectionResult.empty();
        for (Basket basket : items) {
          result = result.add(continuation.project(basket));
        }
        return result;
      }

      @Override
      public ProjectionResult<Basket> projectObject(PropertySet properties) {
        ProjectionResult<Basket> result = ProjectionResult.empty();
        for (ObjectEntry entry : properties) {
          result = result.add(continuation.project(entry.getValue()));
        }
        return result;
      }
    };
  }

  @Override
  public PathSegmentMatchResult matchesIndex(int index) {
    return PathSegmentMatchResult.MATCHED_BOUND;
  }

  @Override
  public PathSegmentMatchResult matchesKey(String key) {
    return PathSegmentMatchResult.MATCHED_BOUND;
  }

  @Override
  public String representation() {
    return "[*]";
  }

}
