package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.operations.Updater;

abstract class StructUpdater implements Updater {
  @Override
  public Basket update(Basket basket) {
    if (basket.isArray()) {
      return updateArray(basket.asArray());
    }

    if (basket.isObject()) {
      return updateObject(basket.asObject());
    }

    return basket;
  }

  protected abstract Basket updateArray(ArrayContents contents);

  protected abstract Basket updateObject(PropertySet properties);
}
