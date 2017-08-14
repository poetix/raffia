package com.codepoetics.raffia.mappers;

public interface Mapper<I, O> {
  O map(I input);
}
