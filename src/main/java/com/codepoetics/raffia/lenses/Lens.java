package com.codepoetics.raffia.lenses;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.paths.Paths;
import com.codepoetics.raffia.paths.segments.PathSegments;
import com.codepoetics.raffia.predicates.IndexValuePredicates;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Lens {

  public static Lens create() {
    return new Lens(TreePVector.<PathSegment>empty());
  }

  private final PVector<PathSegment> segments;

  private Lens(PVector<PathSegment> segments) {
    this.segments = segments;
  }

  public Lens plus(PathSegment segment) {
    return new Lens(segments.plus(segment));
  }

  public Lens to(int arrayIndex) {
    return plus(PathSegments.ofArrayIndex(arrayIndex));
  }

  public Lens to(int first, int...subsequent) {
    StringBuilder representation = new StringBuilder();
    final Set<Integer> indexSet = new HashSet<>();
    indexSet.add(first);
    representation.append(first);

    for (int index : subsequent) {
      representation.append(",");
      representation.append(index);
      indexSet.add(index);
    }

    return toMatching(representation.toString(), new IndexValuePredicate() {
      @Override
      public boolean test(int index, Basket value) {
        return indexSet.contains(index);
      }
    });
  }

  public Lens toAll() {
    return plus(PathSegments.ofWildcard());
  }

  public Lens to(String objectKey) {
    return plus(PathSegments.ofObjectKey(objectKey));
  }

  public Lens toAny(String objectKey) {
    return plus(PathSegments.ofAny(objectKey));
  }

  public Lens toMatching(String representation, IndexValuePredicate predicate) {
    return plus(PathSegments.itemMatching(representation, predicate));
  }

  public Lens toMatching(String representation, IndexPredicate predicate) {
    return toMatching(representation, IndexValuePredicates.indexMatches(predicate));
  }

  public Lens toMatching(String representation, Visitor<Boolean> valuePredicate) {
    return toMatching(representation, IndexValuePredicates.valueMatches(valuePredicate));
  }

  public Lens toHavingKey(String key) {
    return toMatching("?(@." + key + ")", Predicates.isObjectWithKey(key));
  }

  public Path getPath() {
    return Paths.create(segments);
  }

  public Visitor<Basket> updating(Visitor<Basket> updater) {
    Path path = getPath();
    return path.isEmpty()
        ? updater
        : path.head().createUpdater(path.tail(), updater);
  }

  public <T> Visitor<List<T>> projecting(Visitor<List<T>> projector) {
    Path path = getPath();
    return path.isEmpty()
        ? projector
        : path.head().createProjector(path, projector);
  }

  public <T> List<T> project(Visitor<List<T>> projector, Basket basket) {
    return basket.visit(projecting(projector));
  }

  public Visitor<Basket> gettingOne() {
    return gettingOne(Visitors.copy);
  }

  public Basket getOne(Basket basket) {
    return basket.visit(gettingOne());
  }

  public <T> Visitor<T> gettingOne(Visitor<T> projector) {
    return Projections.firstOf(projecting(Projections.listOf(projector)));
  }

  public <T> T getOne(Visitor<T> projector, Basket basket) {
    return basket.visit(gettingOne(projector));
  }

  public <T> Visitor<List<T>> gettingAll(Visitor<T> projector) {
    return projecting(Projections.listOf(projector));
  }

  public <T> List<T> getAll(Visitor<T> projector, Basket basket) {
    return basket.visit(gettingAll(projector));
  }

  public Visitor<List<Basket>> gettingAll() {
    return gettingAll(Visitors.copy);
  }

  public List<Basket> getAll(Basket basket) {
    return basket.visit(gettingAll());
  }
}