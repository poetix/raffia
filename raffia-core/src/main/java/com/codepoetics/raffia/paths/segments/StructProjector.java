package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.baskets.ArrayContents;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.PropertySet;
import com.codepoetics.raffia.operations.ProjectionResult;
import com.codepoetics.raffia.operations.Projector;

abstract class StructProjector<T> implements Projector<T> {

  @Override
  public ProjectionResult<T> project(Basket basket) {
    if (basket.isArray()) {
      return projectArray(basket.asArray());
    }

    if (basket.isObject()) {
      return projectObject(basket.asObject());
    }

    return ProjectionResult.empty();
  }

  protected abstract ProjectionResult<T> projectObject(PropertySet objectEntries);

  protected abstract ProjectionResult<T> projectArray(ArrayContents baskets);
}
