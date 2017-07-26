package com.codepoetics.raffia.api;

public interface Mapper<I, O> {
  O map(I input);
}
