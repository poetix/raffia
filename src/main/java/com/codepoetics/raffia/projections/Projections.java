package com.codepoetics.raffia.projections;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.Mapper;
import com.codepoetics.raffia.api.PropertySet;
import com.codepoetics.raffia.api.Visitor;
import org.pcollections.PMap;
import org.pcollections.PVector;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public final class Projections {

  private Projections() {
  }

  public static <T> Visitor<T> constant(final T constant) {
    return new Visitor<T>() {
      @Override
      public T visitString(String value) {
        return constant;
      }

      @Override
      public T visitBoolean(boolean value) {
        return constant;
      }

      @Override
      public T visitNumber(BigDecimal value) {
        return constant;
      }

      @Override
      public T visitNull() {
        return constant;
      }

      @Override
      public T visitArray(PVector<Basket> items) {
        return constant;
      }

      @Override
      public T visitObject(PropertySet properties) {
        return constant;
      }
    };
  }

  public static <T> Visitor<T> branch(final Visitor<Boolean> predicate, final Visitor<T> ifTrue, final Visitor<T> ifFalse) {
    return new Visitor<T>() {

      @Override
      public T visitString(String value) {
        return predicate.visitString(value)
            ? ifTrue.visitString(value)
            : ifFalse.visitString(value);
      }

      @Override
      public T visitBoolean(boolean value) {
        return predicate.visitBoolean(value)
            ? ifTrue.visitBoolean(value)
            : ifFalse.visitBoolean(value);
      }

      @Override
      public T visitNumber(BigDecimal value) {
        return predicate.visitNumber(value)
            ? ifTrue.visitNumber(value)
            : ifFalse.visitNumber(value);
      }

      @Override
      public T visitNull() {
        return predicate.visitNull()
            ? ifTrue.visitNull()
            : ifFalse.visitNull();
      }

      @Override
      public T visitArray(PVector<Basket> items) {
        return predicate.visitArray(items)
            ? ifTrue.visitArray(items)
            : ifFalse.visitArray(items);
      }

      @Override
      public T visitObject(PropertySet properties) {
        return predicate.visitObject(properties)
            ? ifTrue.visitObject(properties)
            : ifFalse.visitObject(properties);
      }
    };
  }

  private static abstract class VisitorProjection<T> implements Visitor<T> {

    private final String expected;

    protected VisitorProjection(String expected) {
      this.expected = expected;
    }

    private T unexpected(String actual) {
      throw new IllegalArgumentException("Tried to project " + expected + " but basket contained " + actual);
    }

    @Override
    public T visitString(String value) {
      return unexpected("a string");
    }

    @Override
    public T visitBoolean(boolean value) {
      return unexpected("a boolean");
    }

    @Override
    public T visitNumber(BigDecimal value) {
      return unexpected("a number");
    }

    @Override
    public T visitNull() {
      return unexpected("null");
    }

    @Override
    public T visitArray(PVector<Basket> items) {
      return unexpected("an array");
    }

    @Override
    public T visitObject(PropertySet properties) {
      return unexpected("an object");
    }
  }

  public static final Visitor<String> asString = new VisitorProjection<String>("a string") {
    @Override
    public String visitString(String value) {
      return value;
    }
  };

  public static final Visitor<Boolean> asBoolean = new VisitorProjection<Boolean>("a boolean") {
    @Override
    public Boolean visitBoolean(boolean value) {
      return super.visitBoolean(value);
    }
  };

  public static final Visitor<BigDecimal> asNumber = new VisitorProjection<BigDecimal>("a number") {
    @Override
    public BigDecimal visitNumber(BigDecimal value) {
      return value;
    }
  };

  public static final Visitor<Void> asNull = new VisitorProjection<Void>("null") {
    @Override
    public Void visitNull() {
      return null;
    }
  };

  public static final Visitor<PVector<Basket>> asArray = new VisitorProjection<PVector<Basket>>("an array") {
    @Override
    public PVector<Basket> visitArray(PVector<Basket> items) {
      return items;
    }
  };

  public static final Visitor<PropertySet> asObject = new VisitorProjection<PropertySet>("an object") {
    @Override
    public PropertySet visitObject(PropertySet properties) {
      return properties;
    }
  };

  public static <T> Visitor<T> atIndex(final int index, final Visitor<T> itemProjection) {
    return new VisitorProjection<T>("an array") {
      @Override
      public T visitArray(PVector<Basket> items) {
        return items.get(index).visit(itemProjection);
      }
    };
  }

  public static <T> Visitor<T> atKey(final String key, final Visitor<T> itemProjection) {
    return new VisitorProjection<T>("an object") {
      @Override
      public T visitObject(PropertySet properties) {
        return properties.get(key).visit(itemProjection);
      }
    };
  }

  public static <T, V> Visitor<V> map(final Visitor<T> visitor, final Mapper<T, V> mapper) {
    return new Visitor<V>() {
      @Override
      public V visitString(String value) {
        return mapper.map(visitor.visitString(value));
      }

      @Override
      public V visitBoolean(boolean value) {
        return mapper.map(visitor.visitBoolean(value));
      }

      @Override
      public V visitNumber(BigDecimal value) {
        return mapper.map(visitor.visitNumber(value));
      }

      @Override
      public V visitNull() {
        return mapper.map(visitor.visitNull());
      }

      @Override
      public V visitArray(PVector<Basket> items) {
        return mapper.map(visitor.visitArray(items));
      }

      @Override
      public V visitObject(PropertySet properties) {
        return mapper.map(visitor.visitObject(properties));
      }
    };
  }

  public static <T> Visitor<List<T>> listOf(Visitor<T> projection) {
    return map(projection, toListMapper);
  }

  public static <T> Visitor<T> firstOf(Visitor<List<T>> projection) {
    return map(projection, (Mapper) toFirstItemMapper);
  }

  private static final Mapper toListMapper = new Mapper() {
    @Override
    public Object map(Object input) {
      return Collections.singletonList(input);
    }
  };

  private static final Mapper<List, Object> toFirstItemMapper = new Mapper<List, Object>() {
    @Override
    public Object map(List input) {
      if (input.size() == 0) {
        throw new IllegalArgumentException("Tried to project first element of empty list");
      }
      return input.get(0);
    }
  };
}
