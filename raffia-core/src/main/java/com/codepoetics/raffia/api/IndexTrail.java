package com.codepoetics.raffia.api;

public interface IndexTrail {

  IndexTrail enterArray();
  IndexTrail enterObject();
  IndexTrail atKey(String key);

  boolean isEmpty();

  IndexTrail advance();

  boolean isMatchedBy(Path path);

  IndexElement head();
  IndexTrail tail();

}
