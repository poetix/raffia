package com.codepoetics.raffia.lenses;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.paths.Paths;
import com.codepoetics.raffia.paths.segments.PathSegments;
import com.codepoetics.raffia.predicates.NumberPredicates;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.predicates.StringPredicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class Lens {

  @SafeVarargs
  public static Lens lens(String path, Visitor<Boolean>...predicates) {
    return new Lens(PathParser.parse(path, TreePVector.from(Arrays.asList(predicates))));
  }

  public static Lens lens() {
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
    return plus(PathSegments.ofArrayIndices(first, subsequent));
  }

  public Lens toAll() {
    return plus(PathSegments.ofWildcard());
  }

  public Lens to(String objectKey) {
    return plus(PathSegments.ofObjectKey(objectKey));
  }

  public Lens to(String first, String...remaining) {
    return plus(PathSegments.ofObjectKeys(first, remaining));
  }

  public Lens toAny(String objectKey) {
    return plus(PathSegments.ofAny(objectKey));
  }

  public Lens toMatching(Visitor<Boolean> valuePredicate) {
    return toMatching("?", valuePredicate);
  }

  public Lens toMatching(String representation, Visitor<Boolean> valuePredicate) {
    return plus(PathSegments.itemMatching(representation, valuePredicate));
  }

  public Lens toHavingKey(String key) {
    return toMatching("?(@." + key + ")", Predicates.hasKey(key));
  }

  public Path getPath() {
    return Paths.create(segments);
  }

  public Visitor<Basket> updating(Visitor<Basket> updater) {
    Path path = getPath();
    return path.isEmpty()
        ? updater
        : path.head().createUpdater(path, updater);
  }

  public Basket update(Visitor<Basket> updater, Basket target) {
    return target.visit(updating(updater));
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

  public Visitor<Boolean> matching(String value) {
    return matchingString(StringPredicates.isEqualTo(value));
  }

  public Visitor<Boolean> matchingString(Mapper<String, Boolean> matcher) {
    return gettingOne(
        Projections.branch(
            Predicates.isString,
            Projections.map(Projections.asString, matcher),
            Projections.constant(false)));
  }

  public Visitor<Boolean> isTrue() {
    return gettingOne(
        Projections.branch(
            Predicates.isBoolean,
            Projections.asBoolean,
            Projections.constant(false)));
  }

  public Visitor<Boolean> matching(BigDecimal value) {
    return matchingNumber(NumberPredicates.isEqualTo(value));
  }

  public Visitor<Boolean> matchingNumber(Mapper<BigDecimal, Boolean> matcher) {
    return gettingOne(
        Projections.branch(
            Predicates.isNumber,
            Projections.map(Projections.asNumber, matcher),
            Projections.constant(false)));
  }
}
