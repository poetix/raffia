package com.codepoetics.raffia.writers;

import com.codepoetics.raffia.baskets.Basket;

public interface BasketWeavingWriter extends BasketWriter<BasketWeavingWriter> {
  Basket weave();
  BasketWeavingWriter add(Basket basket);
}
