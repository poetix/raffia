package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class ArrayIndexPathSegment extends BasePathSegment {

  private final Collection<Integer> indices;

  ArrayIndexPathSegment(Collection<Integer> indices) {
    this.indices = indices;
  }

  @Override
  protected Visitor<Basket> createUpdater(final Visitor<Basket> continuation) {
    return new StructUpdater() {
      @Override
      public Basket visitArray(ArrayContents items) {
        ArrayContents updated = items;
        for (int index : indices) {
          int actual = index < 0 ? items.size() + index : index;
          if (actual < items.size()) {
            updated = updated.with(actual, updated.get(actual).visit(continuation));
          }
        }
        return Baskets.ofArray(updated);
      }

      @Override
      public Basket visitObject(PropertySet properties) {
        return Baskets.ofObject(properties);
      }
    };
  }

  @Override
  protected <T> Visitor<List<T>> createProjector(final Visitor<List<T>> continuation) {
    return new StructProjector<T>() {
      @Override
      public List<T> visitArray(ArrayContents items) {
        List<T> results = new ArrayList<>();
        for (int index : indices) {
          int actual = index < 0 ? items.size() + index : index;
          if (actual < items.size()) {
            results.addAll(items.get(actual).visit(continuation));
          }
        }
        return results;
      }

      @Override
      public List<T> visitObject(PropertySet properties) {
        return Collections.emptyList();
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
