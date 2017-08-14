package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.paths.PathSegmentMatchResult;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class ObjectKeyPathSegment extends BasePathSegment {

  private final Collection<String> keys;

  ObjectKeyPathSegment(Collection<String> keys) {
    this.keys = keys;
  }

  @Override
  protected Visitor<Basket> createUpdater(Visitor<Basket> continuation) {
    return Projections.branch(
        Predicates.isObject,
        Projections.map(Projections.asObject, getUpdateMapper(continuation)),
        Visitors.copy
    );
  }

  @Override
  protected <T> Visitor<List<T>> createProjector(Visitor<List<T>> continuation) {
    return Projections.branch(
        Predicates.isObject,
        Projections.map(Projections.asObject, getProjectionMapper(continuation)),
        Projections.constant(Collections.<T>emptyList())
    );
  }

  private Mapper<PropertySet, Basket> getUpdateMapper(final Visitor<Basket> continuation) {
    return new Mapper<PropertySet, Basket>() {
      @Override
      public Basket map(PropertySet input) {
        PropertySet updated = input;
        for (String key : keys) {
          Basket atKey = updated.get(key);
          if (atKey != null) {
            updated = updated.with(key, atKey.visit(continuation));
          }
        }
        return Basket.ofObject(updated);
      }
    };
  }

  private <V> Mapper<PropertySet, List<V>> getProjectionMapper(final Visitor<List<V>> continuation) {
    return new Mapper<PropertySet, List<V>>() {
      @Override
      public List<V> map(PropertySet properties) {
        List<V> results = new ArrayList<>();
        for (String key : keys) {
          Basket atKey = properties.get(key);
          if (atKey != null) {
            results.addAll(atKey.visit(continuation));
          }
        }
        return results;
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
