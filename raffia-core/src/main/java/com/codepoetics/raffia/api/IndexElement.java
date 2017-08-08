package com.codepoetics.raffia.api;

public interface IndexElement {

  PathSegmentMatchResult isMatchedBy(PathSegment pathSegment);
  IndexElement advance();

}
