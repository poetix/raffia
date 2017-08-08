package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class MatchingItemPathSegment extends BasePathSegment {

  private final String representation;
  private final Visitor<Boolean> predicate;

  MatchingItemPathSegment(String representation, Visitor<Boolean> predicate) {
    this.representation = representation;
    this.predicate = predicate;
  }

  @Override
  protected Visitor<Basket> createUpdater(Visitor<Basket> continuation) {
    return Projections.branch(
        Predicates.isArray,
        Projections.map(Projections.asArray, getUpdateMapper(continuation)),
        Visitors.copy
    );
  }

  @Override
  protected <T> Visitor<List<T>> createProjector(Visitor<List<T>> continuation) {
    return Projections.branch(
        Predicates.isArray,
        Projections.map(Projections.asArray, getProjectionMapper(continuation)),
        Projections.constant(Collections.<T>emptyList())
    );
  }

  private Mapper<ArrayContents, Basket> getUpdateMapper(final Visitor<Basket> subUpdater) {
    return new Mapper<ArrayContents, Basket>() {
      @Override
      public Basket map(ArrayContents items) {
        ArrayContents updatedItems = items;
        for (int i = 0; i < items.size(); i++) {
          if (items.get(i).visit(predicate)) {
            updatedItems = updatedItems.with(i, updatedItems.get(i).visit(subUpdater));
          }
        }
        return Baskets.ofArray(updatedItems);
      }
    };
  }

  private <V> Mapper<ArrayContents, List<V>> getProjectionMapper(final Visitor<List<V>> subProjector) {
    return new Mapper<ArrayContents, List<V>>() {
      @Override
      public List<V> map(ArrayContents items) {
        List<V> result = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
          if (items.get(i).visit(predicate)) {
            result.addAll(items.get(i).visit(subProjector));
          }
        }
        return result;
      }
    };
  }

  @Override
  public PathSegmentMatchResult matchesIndex(int index) {
    throw new UnsupportedOperationException("Cannot apply path predicates to streaming data");
  }

  @Override
  public PathSegmentMatchResult matchesKey(String key) {
    throw new UnsupportedOperationException("Cannot apply path predicates to streaming data");
  }

  @Override
  public String representation() {
    return "[" + representation + "]";
  }

}
