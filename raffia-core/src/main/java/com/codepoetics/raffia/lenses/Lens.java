package com.codepoetics.raffia.lenses;

import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.mappers.Mapper;
import com.codepoetics.raffia.operations.*;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.paths.PathSegment;
import com.codepoetics.raffia.paths.Paths;
import com.codepoetics.raffia.paths.segments.PathSegments;
import com.codepoetics.raffia.predicates.BasketPredicates;
import com.codepoetics.raffia.predicates.NumberPredicates;
import com.codepoetics.raffia.predicates.StringPredicates;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class Lens implements Projector<Basket> {

  public static Lens lens(String path, BasketPredicate...predicates) {
    return lens(PathParser.parse(path, TreePVector.from(Arrays.asList(predicates))));
  }

  public static Lens lens() {
    return lens(TreePVector.<PathSegment>empty());
  }

  private static Lens lens(PVector<PathSegment> pathSegments) {
    Path path = Paths.create(pathSegments);
    Projector<Basket> projector = path.isEmpty()
        ? Projectors.id()
        : path.head().createProjector(path);
    return new Lens(pathSegments, path, projector);
  }

  private final PVector<PathSegment> segments;
  private final Path path;
  private final Projector<Basket> projector;

  private Lens(PVector<PathSegment> segments, Path path, Projector<Basket> projector) {
    this.segments = segments;
    this.path = path;
    this.projector = projector;
  }

  public Lens plus(PathSegment segment) {
    return lens(segments.plus(segment));
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
    return path;
  }

  public Updater updating(Updater updater) {
    return path.isEmpty()
        ? updater
        : path.head().createUpdater(path, updater);
  }

  public Basket update(Updater updater, Basket target) {
    return updating(updater).update(target);
  }

  public Updater setting(Basket value) {
    return updating(Setters.toBasket(value));
  }

  public Updater setting(String value) {
    return updating(Setters.toString(value));
  }

  public Updater setting(BigDecimal value) {
    return updating(Setters.toNumber(value));
  }

  public Updater setting(boolean value) {
    return updating(Setters.toBoolean(value));
  }

  public Updater settingNull() {
    return updating(Setters.toNull());
  }

  public Basket set(Basket value, Basket target) {
    return setting(value).update(target);
  }

  public Basket set(String value, Basket target) {
    return setting(value).update(target);
  }

  public Basket set(BigDecimal value, Basket target) {
    return setting(value).update(target);
  }

  public Basket set(boolean value, Basket target) {
    return setting(value).update(target);
  }

  public Basket setNull(Basket target) {
    return settingNull().update(target);
  }

  public Basket getOne(Basket basket) {
    return project(basket).getSingle();
  }

  public <T> T getOne(Mapper<Basket, T> mapper, Basket basket) {
    return project(basket).map(mapper).getSingle();
  }

  public List<Basket> getAll(Basket basket) {
    return project(basket).toList();
  }

  public <T> List<T> getAll(Mapper<Basket, T> mapper, Basket basket) {
    return project(basket).map(mapper).toList();
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

  public BasketPredicate exists() {
    return new BasketPredicate() {
      @Override
      public boolean test(Basket basket) {
        return !project(basket).isEmpty();
      }
    };
  }

  @Override
  public ProjectionResult<Basket> project(Basket basket) {
    return projector.project(basket);
  }

  private final Mapper<Basket, String> asString = new Mapper<Basket, String>() {
    @Override
    public String map(Basket input) {
      return input.asString();
    }
  };

  private final Mapper<Basket, BigDecimal> asNumber = new Mapper<Basket, BigDecimal>() {
    @Override
    public BigDecimal map(Basket input) {
      return input.asNumber();
    }
  };

  private final Mapper<Basket, Boolean> asBoolean = new Mapper<Basket, Boolean>() {
    @Override
    public Boolean map(Basket input) {
      return input.asBoolean();
    }
  };

  private final Mapper<Basket, ArrayContents> asArray = new Mapper<Basket, ArrayContents>() {
    @Override
    public ArrayContents map(Basket input) {
      return input.asArray();
    }
  };

  private final Mapper<Basket, PropertySet> asObject = new Mapper<Basket, PropertySet>() {
    @Override
    public PropertySet map(Basket input) {
      return input.asObject();
    }
  };

  public List<String> getAllStrings(Basket basket) {
    return getAll(asString, basket);
  }

  public List<BigDecimal> getAllNumbers(Basket basket) {
    return getAll(asNumber, basket);
  }

  public List<Boolean> getAllBooleans(Basket basket) {
    return getAll(asBoolean, basket);
  }

  public List<ArrayContents> getAllArrays(Basket basket) {
    return getAll(asArray, basket);
  }

  public List<PropertySet> getAllObjects(Basket basket) {
    return getAll(asObject, basket);
  }

  public String getOneString(Basket basket) {
    return getOne(asString, basket);
  }

  public BigDecimal getOneNumber(Basket basket) {
    return getOne(asNumber, basket);
  }

  public Boolean getOneBoolean(Basket basket) {
    return getOne(asBoolean, basket);
  }

  public ArrayContents getOneArray(Basket basket) {
    return getOne(asArray, basket);
  }

  public PropertySet getOneObject(Basket basket) {
    return getOne(asObject, basket);
  }

  public <T> Projector<T> flatMap(Projector<T> next) {
    return Projectors.flatMap(this, next);
  }

  public<T> Projector<T> feedback(Mapper<Basket, Projector<T>> next) {
    return Projectors.feedback(this, next);
  }

  @Override
  public String toString() {
    return path.toString();
  }
}
