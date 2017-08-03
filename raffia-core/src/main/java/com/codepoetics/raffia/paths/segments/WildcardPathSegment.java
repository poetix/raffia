package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

class WildcardPathSegment extends BasePathSegment {


  WildcardPathSegment() {
  }

  @Override
  public Visitor<Basket> createUpdater(Visitor<Basket> continuation) {
    Visitor<Basket> arrayVisitor = Projections.map(Projections.asArray, getArrayUpdateMapper(continuation));
    Visitor<Basket> objectVisitor = Projections.map(Projections.asObject, getObjectUpdateMapper(continuation));

    return Projections.branch(
        Predicates.isArray,
        arrayVisitor,
        Projections.branch(
            Predicates.isObject,
            objectVisitor,
            Visitors.copy
        )
    );
  }

  @Override
  public <T> Visitor<List<T>> createProjector(Visitor<List<T>> continuation) {
    Visitor<List<T>> arrayVisitor = Projections.map(Projections.asArray, getArrayProjectionMapper(continuation));
    Visitor<List<T>> objectVisitor = Projections.map(Projections.asObject, getObjectProjectionMapper(continuation));

    return Projections.branch(
        Predicates.isArray,
        arrayVisitor,
        Projections.branch(
            Predicates.isObject,
            objectVisitor,
            Projections.constant(Collections.<T>emptyList())));
  }

  private Mapper<PropertySet, Basket> getObjectUpdateMapper(final Visitor<Basket> continuation) {
    return new Mapper<PropertySet, Basket>() {
      @Override
      public Basket map(PropertySet input) {
        List<ObjectEntry> entries = new ArrayList<>(input.size());

        for (ObjectEntry entry : input) {
          entries.add(ObjectEntry.of(entry.getKey(), entry.getValue().visit(continuation)));
        }

        return Baskets.ofObject(entries);
      }
    };
  }

  private Mapper<ArrayContents, Basket> getArrayUpdateMapper(final Visitor<Basket> continuation) {
    return new Mapper<ArrayContents, Basket>() {
      @Override
      public Basket map(ArrayContents input) {
        List<Basket> updated = new ArrayList<>(input.size());

        for (Basket basket : input) {
          updated.add(basket.visit(continuation));
        }

        return Baskets.ofArray(updated);
      }
    };
  }

  private <T> Mapper<ArrayContents, List<T>> getArrayProjectionMapper(final Visitor<List<T>> continuation) {
    return new Mapper<ArrayContents, List<T>>() {
      @Override
      public List<T> map(ArrayContents input) {
        List<T> results = new ArrayList<>();
        for (Basket basket : input) {
          results.addAll(basket.visit(continuation));
        }
        return results;
      }
    };
  }

  private <T> Mapper<PropertySet, List<T>> getObjectProjectionMapper(final Visitor<List<T>> continuation) {
    return new Mapper<PropertySet, List<T>>() {
      @Override
      public List<T> map(PropertySet input) {
        List<T> results = new ArrayList<>();
        for (ObjectEntry entry : input) {
            results.addAll(entry.getValue().visit(continuation));
        }
        return results;
      }
    };
  }

  @Override
  public String representation() {
    return "[*]";
  }

}
