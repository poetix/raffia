package com.codepoetics.raffia.paths;

import com.codepoetics.raffia.api.*;

import java.util.List;

abstract class BasePathSegment implements PathSegment {

  @Override
  public Visitor<Basket> createUpdater(Path path, Visitor<Basket> updater) {
    final Path subPath = path.tail();
    final Visitor<Basket> subUpdater = subPath.isEmpty()
        ? updater
        : subPath.head().createUpdater(subPath, updater);

    return createUpdater(subUpdater);
  }

  protected abstract Visitor<Basket> createUpdater(Visitor<Basket> subUpdater);

  @Override
  public <T> Visitor<List<T>> createProjector(Path path, Visitor<List<T>> projector) {
    final Path subPath = path.tail();
    final Visitor<List<T>> subProjector = subPath.isEmpty()
        ? projector
        : subPath.head().createProjector(subPath, projector);

    return createProjector(subProjector);
  }

  protected abstract <T> Visitor<List<T>> createProjector(Visitor<List<T>> subProjector);

}
