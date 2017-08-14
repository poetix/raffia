package com.codepoetics.raffia.paths;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Projector;
import com.codepoetics.raffia.operations.Updater;

public interface PathSegment {

  Updater createUpdater(Path tail, Updater updater);
  Updater createItemUpdater(Path tail, Updater updater);

  Projector<Basket> createProjector(Path tail);
  Projector<Basket> createItemProjector(Path tail);

  PathSegmentMatchResult matchesIndex(int index);

  PathSegmentMatchResult matchesKey(String key);

  boolean isConditional();

  String representation();
}
