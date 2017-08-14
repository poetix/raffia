package com.codepoetics.raffia;

import com.codepoetics.raffia.builders.BasketWeaver;

public final class Raffia {

  private Raffia() {
  }

  public static BasketWeaver weaver() {
    return BasketWeaver.create();
  }

}
