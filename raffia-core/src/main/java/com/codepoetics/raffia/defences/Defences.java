package com.codepoetics.raffia.defences;

public final class Defences {

  public static <T> T notNull(String name, T value) {
    if (value == null) {
      throw new NullPointerException(name + " must not be null");
    }
    return value;
  }

}
