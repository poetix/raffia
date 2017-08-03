package com.codepoetics.raffia.predicates;

import com.codepoetics.raffia.api.ArrayContents;
import com.codepoetics.raffia.api.PropertySet;
import com.codepoetics.raffia.api.Visitor;

import java.math.BigDecimal;

public final class Predicates {

  private Predicates() {
  }

  private static abstract class TypeTestVisitor implements Visitor<Boolean> {

    @Override
    public Boolean visitString(String value) {
      return false;
    }

    @Override
    public Boolean visitBoolean(boolean value) {
      return false;
    }

    @Override
    public Boolean visitNumber(BigDecimal value) {
      return false;
    }

    @Override
    public Boolean visitNull() {
      return false;
    }

    @Override
    public Boolean visitArray(ArrayContents items) {
      return false;
    }

    @Override
    public Boolean visitObject(PropertySet properties) {
      return false;
    }

  }

  public static final Visitor<Boolean> isString = new TypeTestVisitor() {
    @Override
    public Boolean visitString(String value) {
      return true;
    }
  };

  public static final Visitor<Boolean> isBoolean = new TypeTestVisitor() {
    @Override
    public Boolean visitBoolean(boolean value) {
      return true;
    }
  };

  public static final Visitor<Boolean> isNumber = new TypeTestVisitor() {
    @Override
    public Boolean visitNumber(BigDecimal value) {
      return true;
    }
  };

  public static final Visitor<Boolean> isNull = new TypeTestVisitor() {
    @Override
    public Boolean visitNull() {
      return true;
    }
  };

  public static final Visitor<Boolean> isArray = new TypeTestVisitor() {
    @Override
    public Boolean visitArray(ArrayContents items) {
      return true;
    }
  };

  public static final Visitor<Boolean> isObject = new TypeTestVisitor() {
    @Override
    public Boolean visitObject(PropertySet properties) {
      return true;
    }
  };

  public static Visitor<Boolean> hasKey(final String key) {
    return new TypeTestVisitor() {
      @Override
      public Boolean visitObject(PropertySet properties) {
        return properties.containsKey(key);
      }
    };
  }
}
