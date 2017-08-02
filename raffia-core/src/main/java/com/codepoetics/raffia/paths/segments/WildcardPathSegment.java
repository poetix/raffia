package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.Path;
import com.codepoetics.raffia.api.PathSegment;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.projections.Projections;

import java.util.List;

class WildcardPathSegment implements PathSegment {

  private final PathSegment arrayWildcard = ItemPredicatePathSegment.all;
  private final PathSegment objectWildcard = ObjectPredicatePathSegment.all;

  WildcardPathSegment() {
  }

  @Override
  public Visitor<Basket> createUpdater(Path path, Visitor<Basket> updater) {
    return Projections.branch(
        Predicates.isArray,
        arrayWildcard.createUpdater(path, updater),
        objectWildcard.createUpdater(path, updater));
  }

  @Override
  public <T> Visitor<List<T>> createProjector(Path path, Visitor<List<T>> projector) {
    return Projections.branch(
        Predicates.isArray,
        arrayWildcard.createProjector(path, projector),
        objectWildcard.createProjector(path, projector));
  }

  @Override
  public String representation() {
    return "*";
  }

}
