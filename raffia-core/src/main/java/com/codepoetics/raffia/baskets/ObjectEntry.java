package com.codepoetics.raffia.baskets;

import java.util.Objects;

import static com.codepoetics.raffia.defences.Defences.notNull;

public final class ObjectEntry {

  public static ObjectEntry of(String key, Basket value) {
    return new ObjectEntry(notNull("key", key), notNull("value", value));
  }

  private final String key;
  private final Basket value;

  private ObjectEntry(String key, Basket value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public Basket getValue() {
    return value;
  }

  @Override
  public boolean equals(Object other) {
    return this == other
        || (other instanceof ObjectEntry
          && ObjectEntry.class.cast(other).key.equals(key)
          && ObjectEntry.class.cast(other).value.equals(value));
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public String toString() {
    return key + ": " + value;
  }

}
