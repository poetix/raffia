package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.ObjectEntry;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.paths.PathSegmentMatchResult;

import java.util.ArrayList;
import java.util.List;

class WildcardPathSegment extends BasePathSegment {

  WildcardPathSegment() {
  }

  @Override
  public Visitor<Basket> createUpdater(final Visitor<Basket> continuation) {
    return new StructUpdater() {
      @Override
      public Basket visitArray(ArrayContents items) {
        List<Basket> updated = new ArrayList<>(items.size());

        for (Basket basket : items) {
          updated.add(basket.visit(continuation));
        }

        return Basket.ofArray(updated);
      }

      @Override
      public Basket visitObject(PropertySet properties) {
        List<ObjectEntry> entries = new ArrayList<>(properties.size());

        for (ObjectEntry entry : properties) {
          entries.add(ObjectEntry.of(entry.getKey(), entry.getValue().visit(continuation)));
        }

        return Basket.ofObject(entries);
      }
    };
  }

  @Override
  public <T> Visitor<List<T>> createProjector(final Visitor<List<T>> continuation) {
    return new StructProjector<T>() {
      @Override
      public List<T> visitArray(ArrayContents items) {
        List<T> results = new ArrayList<>();
        for (Basket basket : items) {
          results.addAll(basket.visit(continuation));
        }
        return results;
      }

      @Override
      public List<T> visitObject(PropertySet properties) {
        List<T> results = new ArrayList<>();
        for (ObjectEntry entry : properties) {
          results.addAll(entry.getValue().visit(continuation));
        }
        return results;
      }
    };
  }

  @Override
  public PathSegmentMatchResult matchesIndex(int index) {
    return PathSegmentMatchResult.MATCHED_BOUND;
  }

  @Override
  public PathSegmentMatchResult matchesKey(String key) {
    return PathSegmentMatchResult.MATCHED_BOUND;
  }

  @Override
  public String representation() {
    return "[*]";
  }

}
