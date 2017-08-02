package com.codepoetics.raffia;

import com.codepoetics.raffia.builders.BasketBuilder;

public final class Raffia {

  private Raffia() {
  }

  public static BasketBuilder builder() {
    return BasketBuilder.empty();
  }

}