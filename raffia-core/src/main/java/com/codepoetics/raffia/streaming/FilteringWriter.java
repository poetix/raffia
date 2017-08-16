package com.codepoetics.raffia.streaming;

import com.codepoetics.raffia.writers.BasketWriter;

public interface FilteringWriter<T extends BasketWriter<T>> extends BasketWriter<FilteringWriter<T>> {

  FilteringWriter<T> advance(T newTarget);
  T complete();

}
