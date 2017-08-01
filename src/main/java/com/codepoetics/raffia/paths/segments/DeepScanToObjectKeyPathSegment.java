package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

final class DeepScanToObjectKeyPathSegment extends BasePathSegment {

  private final String key;

  DeepScanToObjectKeyPathSegment(String key) {
    this.key = key;
  }

  @Override
  protected Visitor<Basket> createUpdater(Visitor<Basket> continuation) {
    final AtomicReference<Visitor<Basket>> self = new AtomicReference<>();

    Visitor<Basket> arrayVisitor = Projections.map(Projections.asArray, getArrayUpdateMapper(self));
    Visitor<Basket> objectVisitor = Projections.map(Projections.asObject, getObjectUpdateMapper(continuation, self));

    self.set(Projections.branch(
        Predicates.isArray,
        arrayVisitor,
        Projections.branch(
            Predicates.isObject,
            objectVisitor,
            Visitors.copy
        )
    ));

    return self.get();
  }

  @Override
  protected <T> Visitor<List<T>> createProjector(Visitor<List<T>> continuation) {
    final AtomicReference<Visitor<List<T>>> self = new AtomicReference<>();

    Visitor<List<T>> arrayVisitor = Projections.map(Projections.asArray, getArrayProjectionMapper(self));
    Visitor<List<T>> objectVisitor = Projections.map(Projections.asObject, getObjectProjectionMapper(continuation, self));

    self.set(Projections.branch(
        Predicates.isArray,
        arrayVisitor,
        Projections.branch(
            Predicates.isObject,
            objectVisitor,
            Projections.constant(Collections.<T>emptyList()))));

    return self.get();
  }

  private Mapper<PropertySet, Basket> getObjectUpdateMapper(final Visitor<Basket> subUpdater, final AtomicReference<Visitor<Basket>> self) {
    return new Mapper<PropertySet, Basket>() {
      @Override
      public Basket map(PropertySet input) {
        Map<String, Basket> updated = new HashMap<>();

        for (ObjectEntry entry : input) {
          if (entry.getKey().equals(key)) {
            updateWith(updated, entry, subUpdater);
          } else {
            updateWith(updated, entry, self.get());
          }
        }

        return Baskets.ofObject(updated);
      }

      private void updateWith(Map<String, Basket> updated, ObjectEntry entry, Visitor<Basket> updater) {
        updated.put(entry.getKey(), entry.getValue().visit(updater));
      }
    };
  }

  private Mapper<ArrayContents, Basket> getArrayUpdateMapper(final AtomicReference<Visitor<Basket>> self) {
    return new Mapper<ArrayContents, Basket>() {
      @Override
      public Basket map(ArrayContents input) {
        List<Basket> updated = new ArrayList<>();

        for (Basket basket : input) {
          updated.add(basket.visit(self.get()));
        }

        return Baskets.ofArray(updated);
      }
    };
  }

  private <T> Mapper<ArrayContents, List<T>> getArrayProjectionMapper(final AtomicReference<Visitor<List<T>>> self) {
    return new Mapper<ArrayContents, List<T>>() {
      @Override
      public List<T> map(ArrayContents input) {
        List<T> results = new ArrayList<>();
        for (Basket basket : input) {
          results.addAll(basket.visit(self.get()));
        }
        return results;
      }
    };
  }

  private <T> Mapper<PropertySet, List<T>> getObjectProjectionMapper(final Visitor<List<T>> subProjector, final AtomicReference<Visitor<List<T>>> self) {
    return new Mapper<PropertySet, List<T>>() {
      @Override
      public List<T> map(PropertySet input) {
        List<T> results = new ArrayList<>();
        for (ObjectEntry entry : input) {
          if (entry.getKey().equals(key)) {
            results.addAll(entry.getValue().visit(subProjector));
          } else {
            results.addAll(entry.getValue().visit(self.get()));
          }
        }
        return results;
      }
    };
  }

  @Override
  public String representation() {
    return ".." + key;
  }
}
