package com.codepoetics.raffia.paths;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;
import org.pcollections.PVector;

import java.util.Collections;
import java.util.List;

final class ArrayIndexPathSegment extends BasePathSegment {

  private final int index;

  ArrayIndexPathSegment(int index) {
    this.index = index;
  }


  @Override
  protected Visitor<Basket> createUpdater(Visitor<Basket> subUpdater) {
    return Projections.branch(
        Predicates.isArray,
        Projections.map(Projections.asArray, getUpdateMapper(subUpdater)),
        Visitors.copy
    );
  }

  @Override
  protected <T> Visitor<List<T>> createProjector(Visitor<List<T>> subProjector) {
    return Projections.branch(
        Predicates.isArray,
        Projections.map(Projections.asArray, getProjectionMapper(subProjector)),
        Projections.constant(Collections.<T>emptyList())
    );
  }

  private Mapper<PVector<Basket>, Basket> getUpdateMapper(final Visitor<Basket> subUpdater) {
    return new Mapper<PVector<Basket>, Basket>() {
      @Override
      public Basket map(PVector<Basket> items) {
        return Baskets.ofArray(items.with(index, items.get(index).visit(subUpdater)));
      }
    };
  }

  private <V> Mapper<PVector<Basket>, List<V>> getProjectionMapper(final Visitor<List<V>> subProjector) {
    return new Mapper<PVector<Basket>, List<V>>() {
      @Override
      public List<V> map(PVector<Basket> input) {
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
