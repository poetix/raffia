package com.codepoetics.raffia.lenses;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.mappers.Mapper;
import com.codepoetics.raffia.operations.*;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.paths.PathSegment;
import com.codepoetics.raffia.paths.Paths;
import com.codepoetics.raffia.paths.segments.PathSegments;
import com.codepoetics.raffia.predicates.NumberPredicates;
import com.codepoetics.raffia.predicates.BasketPredicates;
import com.codepoetics.raffia.predicates.StringPredicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.visitors.Visitors;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class Lens implements Projector<Basket> {

  public static Lens lens(String path, BasketPredicate...predicates) {
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

  public Lens toMatching(BasketPredicate valuePredicate) {
    return toMatching("?", valuePredicate);
  }

  public Lens toMatching(String representation, BasketPredicate valuePredicate) {
    return plus(PathSegments.itemMatching(representation, valuePredicate));
  }

  public Lens toHavingKey(String key) {
    return toMatching("?(@." + key + ")", BasketPredicates.hasKey(key));
  }

  public Path getPath() {
    return Paths.create(segments);
  }

  public Updater updating(Updater updater) {
    Path path = getPath();
    return path.isEmpty()
        ? updater
        : path.head().createUpdater(path, updater);
  }

  public Basket update(Updater updater, Basket target) {
    return updating(updater).update(target);
  }

  public Basket getOne(Basket basket) {
    return project(basket).getSingle();
  }

  public List<Basket> getAll(Basket basket) {
    return project(basket).toList();
  }

  public BasketPredicate matching(String value) {
    return matchingString(StringPredicates.isEqualTo(value));
  }

  public BasketPredicate matchingString(final ValuePredicate<String> matcher) {
    return matching(new BasketPredicate() {
      @Override
      public boolean test(Basket basket) {
        return basket.isString() && matcher.test(basket.asString());
      }
    });
  }

  public BasketPredicate matching(final boolean expected) {
    return matching(new BasketPredicate() {
      @Override
      public boolean test(Basket basket) {
        return basket.isBoolean() && basket.asBoolean() == expected;
      }
    });
  }

  public BasketPredicate isTrue() {
    return matching(true);
  }


  public BasketPredicate isFalse() {
    return matching(false);
  }

  public BasketPredicate matching(BigDecimal value) {
    return matchingNumber(NumberPredicates.isEqualTo(value));
  }

  public BasketPredicate matchingNumber(final ValuePredicate<BigDecimal> matcher) {
    return matching(new BasketPredicate() {
      @Override
      public boolean test(Basket basket) {
        return basket.isNumber() && matcher.test(basket.asNumber());
      }
    });
  }

  public BasketPredicate matching(final BasketPredicate itemPredicate) {
    return new BasketPredicate() {
      @Override
      public boolean test(Basket basket) {
        return project(basket).allMatch(itemPredicate);
      }
    };
  }

  @Override
  public ProjectionResult<Basket> project(Basket basket) {
    Path path = getPath();
    return path.isEmpty()
        ? ProjectionResult.ofSingle(basket)
        : path.head().createProjector(path).project(basket);
  }

  private final Mapper<Basket, String> asString = new Mapper<Basket, String>() {
    @Override
    public String map(Basket input) {
      return input.asString();
    }
  };

  public List<String> getAllStrings(Basket basket) {
    return project(basket).map(asString).toList();
  }

  private final Mapper<Basket, BigDecimal> asNumber = new Mapper<Basket, BigDecimal>() {
    @Override
    public BigDecimal map(Basket input) {
      return input.asNumber();
    }
  };

  public List<BigDecimal> getAllNumbers(Basket basket) {
    return project(basket).map(asNumber).toList();
  }
}
