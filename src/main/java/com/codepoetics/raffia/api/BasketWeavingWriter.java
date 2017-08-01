package com.codepoetics.raffia.api;

public interface BasketWeavingWriter extends BasketWriter<BasketWeavingWriter> {
  Basket weave();
  BasketWeavingWriter add(Basket basket);
}
