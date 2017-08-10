package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Baskets;
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
  public boolean isConditional() {
    return true;
  }

  @Override
  public Visitor<Basket> createItemUpdater(Path tail, Visitor<Basket> updater) {
    return Projections.branch(
        predicate,
        tail.isEmpty() ? updater : tail.head().createUpdater(tail, updater),
        Visitors.copy
    );
  }

  @Override
  public Visitor<List<Basket>> createConditionalProjector(Path tail) {
    return Projections.branch(
        predicate,
        tail.isEmpty()
            ? Projections.listOf(Visitors.copy)
            : tail.head().createProjector(tail, Projections.listOf(Visitors.copy)),
        Visitors.constant(Collections.<Basket>emptyList())
    );
  }

  @Override
  protected Visitor<Basket> createUpdater(final Visitor<Basket> continuation) {
    return new StructUpdater() {
      @Override
      public Basket visitArray(ArrayContents items) {
        ArrayContents updated = items;
        for (int i = 0; i < items.size(); i++) {
          Basket item = items.get(i);
          if (item.visit(predicate)) {
            updated = updated.with(i, item.visit(continuation));
          }
        }
        return Baskets.ofArray(updated);
      }

      @Override
      public Basket visitObject(PropertySet properties) {
        PropertySet updated = properties;
        for (ObjectEntry entry : properties) {
          if (entry.getValue().visit(predicate)) {
            updated = updated.with(entry.getKey(), entry.getValue().visit(continuation));
          }
        }
        return Baskets.ofObject(updated);
      }
    };
  }

  @Override
  protected <T> Visitor<List<T>> createProjector(final Visitor<List<T>> continuation) {
    return new StructProjector<T>() {
      @Override
      public List<T> visitArray(ArrayContents items) {
        List<T> results = new ArrayList<>();
        for (Basket item : items) {
          if (item.visit(predicate)) {
            results.addAll(item.visit(continuation));
          }
        }
        return results;
      }

      @Override
      public List<T> visitObject(PropertySet properties) {
        List<T> results = new ArrayList<>();
        for (ObjectEntry entry : properties) {
          if (entry.getValue().visit(predicate)) {
            results.addAll(entry.getValue().visit(continuation));
          }
        }
        return results;
      }
    };
  }

  @Override
  public PathSegmentMatchResult matchesIndex(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PathSegmentMatchResult matchesKey(String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String representation() {
    return representation;
  }

}
