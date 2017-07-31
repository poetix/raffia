package com.codepoetics.raffia.api;

import java.util.List;

public interface PathSegment {

  Visitor<Basket> createUpdater(Path path, Visitor<Basket> updater);
  <T> Visitor<List<T>> createProjector(Path tail, Visitor<List<T>> projector);

  String representation();
}