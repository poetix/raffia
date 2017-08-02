package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.Path;
import com.codepoetics.raffia.api.PathSegment;
import com.codepoetics.raffia.api.Visitor;

import java.util.List;

abstract class BasePathSegment implements PathSegment {

  @Override
  public Visitor<Basket> createUpdater(Path path, Visitor<Basket> updater) {
    final Path subPath = path.tail();
    final Visitor<Basket> continuation = subPath.isEmpty()
        ? updater
        : subPath.head().createUpdater(subPath, updater);

    return createUpdater(continuation);
  }

  protected abstract Visitor<Basket> createUpdater(Visitor<Basket> continuation);

  @Override
  public <T> Visitor<List<T>> createProjector(Path path, Visitor<List<T>> projector) {
    final Path subPath = path.tail();
    final Visitor<List<T>> continuation = subPath.isEmpty()
        ? projector
        : subPath.head().createProjector(subPath, projector);

    return createProjector(continuation);
  }

  protected abstract <T> Visitor<List<T>> createProjector(Visitor<List<T>> continuation);

}
