package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.ArrayContents;
import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.Mapper;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ArrayIndexPathSegment extends BasePathSegment {

  private final List<Integer> indices;

  ArrayIndexPathSegment(List<Integer> indices) {
    this.indices = indices;
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

  private Mapper<ArrayContents, Basket> getUpdateMapper(final Visitor<Basket> continuation) {
    return new Mapper<ArrayContents, Basket>() {
      @Override
      public Basket map(ArrayContents items) {
        ArrayContents updated = items;
        for (int index : indices) {
          int actual = index < 0 ? items.size() + index : index;
          if (actual < items.size()) {
            updated = updated.with(actual, updated.get(actual).visit(continuation));
          }
        }
        return Baskets.ofArray(updated);
      }
    };
  }

  private <V> Mapper<ArrayContents, List<V>> getProjectionMapper(final Visitor<List<V>> continuation) {
    return new Mapper<ArrayContents, List<V>>() {
      @Override
      public List<V> map(ArrayContents input) {
        List<V> results = new ArrayList<>();
        for (int index : indices) {
          int actual = index < 0 ? input.size() + index : index;
          if (actual < input.size()) {
            results.addAll(input.get(actual).visit(continuation));
          }
        }
        return results;
      }
    };
  }

  @Override
  public String representation() {
    return indices.toString();
  }

}
