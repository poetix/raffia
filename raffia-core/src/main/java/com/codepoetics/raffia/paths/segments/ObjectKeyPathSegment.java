package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.operations.ProjectionResult;
import com.codepoetics.raffia.operations.Projector;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.paths.PathSegmentMatchResult;

import java.util.Collection;

final class ObjectKeyPathSegment extends BasePathSegment {

  private final Collection<String> keys;

  ObjectKeyPathSegment(Collection<String> keys) {
    this.keys = keys;
  }

  @Override
  protected Updater createUpdater(final Updater continuation) {
    return new Updater() {

      @Override
      public Basket update(Basket basket) {
        if (!basket.isObject()) {
          return basket;
        }

        return updateProperties(basket.asObject());
      }

      private Basket updateProperties(PropertySet properties) {
        PropertySet updated = properties;
        for (String key : keys) {
          Basket atKey = updated.get(key);
          if (atKey != null) {
            updated = updated.with(key, continuation.update(atKey));
          }
        }
        return Basket.ofObject(updated);
      }
    };
  }

  @Override
  protected Projector<Basket> createProjector(final Projector<Basket> continuation) {
    return new Projector<Basket>() {

      @Override
      public ProjectionResult<Basket> project(Basket basket) {
        if (!basket.isObject()) {
          return ProjectionResult.empty();
        }

        return projectProperties(basket.asObject());
      }

      private ProjectionResult<Basket> projectProperties(PropertySet properties) {
        ProjectionResult<Basket> result = ProjectionResult.empty();
        for (String key : keys) {
          Basket atKey = properties.get(key);
          if (atKey != null) {
            result = result.add(continuation.project(atKey));
          }
        }
        return result;
      }
    };
  }

  @Override
  public PathSegmentMatchResult matchesIndex(int index) {
    return PathSegmentMatchResult.UNMATCHED;
  }

  @Override
  public PathSegmentMatchResult matchesKey(String key) {
    return keys.contains(key) ? PathSegmentMatchResult.MATCHED_BOUND : PathSegmentMatchResult.UNMATCHED;
  }

  @Override
  public String representation() {
    return keys.size() == 1
        ? "." + keys.iterator().next()
        : indexForm();
  }

  private String indexForm() {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    sb.append("[");
    for (String key : keys) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }
      sb.append("'").append(key).append("'");
    }
    sb.append("]");
    return sb.toString();
  }
}
