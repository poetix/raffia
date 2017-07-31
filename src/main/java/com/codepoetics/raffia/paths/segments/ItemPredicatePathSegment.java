package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;
import org.pcollections.PVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ItemPredicatePathSegment extends BasePathSegment {

  static final PathSegment all = new ItemPredicatePathSegment("*", new IndexValuePredicate() {
    @Override
    public boolean test(int index, Basket value) {
      return true;
    }
  });

  private final String representation;
  private final IndexValuePredicate indexValuePredicate;

  ItemPredicatePathSegment(String representation, IndexValuePredicate indexValuePredicate) {
    this.representation = representation;
    this.indexValuePredicate = indexValuePredicate;
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

  private Mapper<PVector<Basket>, Basket> getUpdateMapper(final Visitor<Basket> subUpdater) {
    return new Mapper<PVector<Basket>, Basket>() {
      @Override
      public Basket map(PVector<Basket> items) {
        PVector<Basket> updatedItems = items;
        for (int i = 0; i < items.size(); i++) {
          if (indexValuePredicate.test(i, items.get(i))) {
            updatedItems = updatedItems.with(i, updatedItems.get(i).visit(subUpdater));
          }
        }
        return Baskets.ofArray(updatedItems);
      }
    };
  }

  private <V> Mapper<PVector<Basket>, List<V>> getProjectionMapper(final Visitor<List<V>> subProjector) {
    return new Mapper<PVector<Basket>, List<V>>() {
      @Override
      public List<V> map(PVector<Basket> items) {
        List<V> result = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
          if (indexValuePredicate.test(i, items.get(i))) {
            result.addAll(items.get(i).visit(subProjector));
          }
        }
        return result;
      }
    };
  }

  @Override
  public String representation() {
    return "[" + representation + "]";
  }

}
