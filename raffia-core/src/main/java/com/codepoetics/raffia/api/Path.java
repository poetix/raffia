package com.codepoetics.raffia.api;

public interface Path {

  boolean isEmpty();
  PathSegment head();
  Path tail();
  Path prepend(PathSegment segment);

}
