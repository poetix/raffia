package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.ArrayContents;
import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.Mapper;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;

import java.util.Collections;
import java.util.List;

final class ArrayIndexPathSegment extends BasePathSegment {

  private final int index;

  ArrayIndexPathSegment(int index) {
    this.index = index;
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
        return Baskets.ofArray(items.with(index, items.get(index).visit(subUpdater)));
      }
    };
  }

  private <V> Mapper<ArrayContents, List<V>> getProjectionMapper(final Visitor<List<V>> subProjector) {
    return new Mapper<ArrayContents, List<V>>() {
      @Override
      public List<V> map(ArrayContents input) {
        return index >= 0
          ? input.size() > index
            ? input.get(index).visit(subProjector)
            : Collections.<V>emptyList()
          : input.size() > -index
            ? input.get(input.size() + index).visit(subProjector)
            : Collections.<V>emptyList();
      }
    };
  }

  @Override
  public String representation() {
    return "[" + index + "]";
  }

}
