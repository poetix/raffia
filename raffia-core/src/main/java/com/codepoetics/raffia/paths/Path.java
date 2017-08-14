package com.codepoetics.raffia.paths;

public interface Path {

  boolean isEmpty();
  PathSegment head();
  Path tail();
  Path prepend(PathSegment segment);

}
