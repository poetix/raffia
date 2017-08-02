package com.codepoetics.raffia.writers;

import com.codepoetics.raffia.api.BasketWriter;

import java.math.BigDecimal;

public abstract class PassThroughWriter<T extends BasketWriter<T>, S extends PassThroughWriter<T, S>> implements BasketWriter<S> {

  protected final T state;

  protected PassThroughWriter(T state) {
    this.state = state;
  }

  protected abstract S with(T state);

  @Override
  public S beginObject() {
    return with(state.beginObject());
  }

  @Override
  public S beginArray() {
    return with(state.beginArray());
  }

  @Override
  public S end() {
    return with(state.end());
  }

  @Override
  public S key(String key) {
    return with(state.key(key));
  }

  @Override
  public S add(String value) {
    return with(state.add(value));
  }

  @Override
  public S add(BigDecimal value) {
    return with(state.add(value));
  }

  @Override
  public S add(boolean value) {
    return with(state.add(value));
  }

  @Override
  public S addNull() {
    return with(state.addNull());
  }

}
