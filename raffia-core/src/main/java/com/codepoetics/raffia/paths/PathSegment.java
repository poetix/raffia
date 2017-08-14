package com.codepoetics.raffia.paths;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.Visitor;

import java.util.List;

public interface PathSegment {

  Visitor<Basket> createUpdater(Path path, Visitor<Basket> updater);
  Visitor<Basket> createItemUpdater(Path tail, Visitor<Basket> updater);

  <T> Visitor<List<T>> createProjector(Path tail, Visitor<List<T>> projector);
  Visitor<List<Basket>> createItemProjector(Path tail);

  PathSegmentMatchResult matchesIndex(int index);

  PathSegmentMatchResult matchesKey(String key);

  boolean isConditional();

  String representation();
}
