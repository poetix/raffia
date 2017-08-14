package com.codepoetics.raffia.paths.segments;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Projector;
import com.codepoetics.raffia.operations.Projectors;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.paths.PathSegment;

abstract class BasePathSegment implements PathSegment {

  @Override
  public Updater createUpdater(Path path, Updater updater) {
    final Path subPath = path.tail();
    final Updater continuation = subPath.isEmpty()
        ? updater
        : subPath.head().createUpdater(subPath, updater);

    return createUpdater(continuation);
  }

  @Override
  public Updater createItemUpdater(Path tail, Updater updater) {
    throw new UnsupportedOperationException("Cannot create item updater for non-conditional path segment");
  }

  @Override
  public Projector<Basket> createItemProjector(Path tail) {
    throw new UnsupportedOperationException("Cannot create item projector for non-conditional path segment");
  }

  protected abstract Updater createUpdater(Updater continuation);

  @Override
  public Projector<Basket> createProjector(Path path) {
    final Path subPath = path.tail();
    return subPath.isEmpty()
        ? createProjector(Projectors.id())
        : createProjector(subPath.head().createProjector(subPath));
  }

  @Override
  public boolean isConditional() {
    return false;
  }

  protected abstract Projector<Basket> createProjector(Projector<Basket> continuation);

}
