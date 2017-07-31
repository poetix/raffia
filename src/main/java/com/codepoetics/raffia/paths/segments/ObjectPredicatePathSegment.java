package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.predicates.KeyValuePredicates;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ObjectPredicatePathSegment extends BasePathSegment {

  public static final ObjectPredicatePathSegment all = new ObjectPredicatePathSegment("*", KeyValuePredicates.wildcard);

  private final String representation;
  private final KeyValuePredicate predicate;

  ObjectPredicatePathSegment(String representation, KeyValuePredicate predicate) {
    this.representation = representation;
    this.predicate = predicate;
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

  private Mapper<PropertySet, Basket> getUpdateMapper(final Visitor<Basket> subUpdater) {
    return new Mapper<PropertySet, Basket>() {
      @Override
      public Basket map(PropertySet input) {
        PropertySet updated = input;
        for (ObjectEntry entry : updated) {
          if (predicate.test(entry.getKey(), entry.getValue())) {
            updated = updated.with(entry.getKey(), entry.getValue().visit(subUpdater));
          }
        }
        return Baskets.ofObject(updated);
      }
    };
  }

  private <V> Mapper<PropertySet, List<V>> getProjectionMapper(final Visitor<List<V>> subProjector) {
    return new Mapper<PropertySet, List<V>>() {
      @Override
      public List<V> map(PropertySet properties) {
        List<V> result = new ArrayList<>();
        for (ObjectEntry entry : properties) {
          if (predicate.test(entry.getKey(), entry.getValue())) {
            result.addAll(entry.getValue().visit(subProjector));
          }
        }
        return result;
      }
    };
  }

  @Override
  public String representation() {
    return "." + representation;
  }
}
