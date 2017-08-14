package com.codepoetics.raffia.mappers;

public final class Mappers {

  private Mappers() {
  }

  private static final Mapper<Object, Object> ID = new Mapper<Object, Object>() {
    @Override
    public Object map(Object input) {
      return input;
    }
  };

  @SuppressWarnings("unchecked")
  public static <T> Mapper<T, T> id() {
    return (Mapper<T, T>) ID;
  }

  public static <I, O> Mapper<I, O> constant(final O value) {
    return new Mapper<I, O>() {
      @Override
      public O map(I input) {
        return value;
      }
    };
  }

  public static <A, B, C> Mapper<A, C> compose(final Mapper<A, B> first, final Mapper<B, C> second) {
    return new Mapper<A, C>() {
      @Override
      public C map(A input) {
        return second.map(first.map(input));
      }
    };
  }



}
