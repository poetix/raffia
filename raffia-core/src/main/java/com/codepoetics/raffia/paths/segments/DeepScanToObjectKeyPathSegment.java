package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Baskets;

import java.util.ArrayList;
import java.util.List;

final class DeepScanToObjectKeyPathSegment extends BasePathSegment {

  private final String key;

  DeepScanToObjectKeyPathSegment(String key) {
    this.key = key;
  }

  @Override
  protected Visitor<Basket> createUpdater(final Visitor<Basket> continuation) {
    return new StructUpdater() {
      @Override
      public Basket visitArray(ArrayContents items) {
        List<Basket> updated = new ArrayList<>();

        for (Basket basket : items) {
          updated.add(basket.visit(this));
        }

        return Baskets.ofArray(updated);
      }

      @Override
      public Basket visitObject(PropertySet properties) {
        List<ObjectEntry> entries = new ArrayList<>(properties.size());

        for (ObjectEntry entry : properties) {
          if (entry.getKey().equals(key)) {
            entries.add(updateWith(entry, continuation));
          } else {
            entries.add(updateWith(entry, this));
          }
        }

        return Baskets.ofObject(entries);
      }

      private ObjectEntry updateWith(ObjectEntry entry, Visitor<Basket> updater) {
        return ObjectEntry.of(entry.getKey(), entry.getValue().visit(updater));
      }
    };
  }

  @Override
  protected <T> Visitor<List<T>> createProjector(final Visitor<List<T>> continuation) {
    return new StructProjector<T>() {
      @Override
      public List<T> visitArray(ArrayContents items) {
        List<T> results = new ArrayList<>();
        for (Basket basket : items) {
          results.addAll(basket.visit(this));
        }
        return results;
      }

      @Override
      public List<T> visitObject(PropertySet properties) {
        List<T> results = new ArrayList<>();
        for (ObjectEntry entry : properties) {
          if (entry.getKey().equals(key)) {
            results.addAll(entry.getValue().visit(continuation));
          } else {
            results.addAll(entry.getValue().visit(this));
          }
        }
        return results;
      }
    };
  }

  @Override
  public PathSegmentMatchResult matchesIndex(int index) {
    return PathSegmentMatchResult.MATCHED_UNBOUND;
  }

  @Override
  public PathSegmentMatchResult matchesKey(String key) {
    return this.key.equals(key)
        ? PathSegmentMatchResult.MATCHED_BOUND
        : PathSegmentMatchResult.MATCHED_UNBOUND;
  }

  @Override
  public String representation() {
    return ".." + key;
  }
}
