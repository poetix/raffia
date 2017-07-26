package com.codepoetics.raffia.paths;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;
import org.pcollections.PMap;

import java.util.Collections;
import java.util.List;

final class ObjectKeyPathSegment extends BasePathSegment {

  private final String key;

  ObjectKeyPathSegment(String key) {
    this.key = key;
  }

  @Override
  protected Visitor<Basket> createUpdater(Visitor<Basket> subUpdater) {
    return Projections.branch(
        Predicates.isObject,
        Projections.map(Projections.asObject, getUpdateMapper(subUpdater)),
        Visitors.copy
    );
  }

  @Override
  protected <T> Visitor<List<T>> createProjector(Visitor<List<T>> subProjector) {
    return Projections.branch(
        Predicates.isObject,
        Projections.map(Projections.asObject, getProjectionMapper(subProjector)),
        Projections.constant(Collections.<T>emptyList())
    );
  }

  private Mapper<PropertySet, Basket> getUpdateMapper(final Visitor<Basket> subUpdater) {
    return new Mapper<PropertySet, Basket>() {
      @Override
      public Basket map(PropertySet input) {
        return Baskets.ofObject(input.with(key, input.get(key).visit(subUpdater)));
      }
    };
  }

  private <V> Mapper<PropertySet, List<V>> getProjectionMapper(final Visitor<List<V>> subProjector) {
    return new Mapper<PropertySet, List<V>>() {
      @Override
      public List<V> map(PropertySet properties) {
        Basket atKey = properties.get(key);
        return atKey == null
            ? Collections.<V>emptyList()
            : atKey.visit(subProjector);
      }
    };
  }

  @Override
  public String representation() {
    return "." + key;
  }
}
