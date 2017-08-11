package com.codepoetics.raffia.api;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.*;

public class PropertySet implements Iterable<ObjectEntry> {

  public static PropertySet of(ObjectEntry...entries) {
    return of(Arrays.asList(entries));
  }

  public static PropertySet of(Collection<ObjectEntry> entries) {
    Map<String, Basket> properties = new LinkedHashMap<>();
    for (ObjectEntry entry : entries) {
      properties.put(entry.getKey(), entry.getValue());
    }
    return of(properties);
  }

  public static PropertySet of(Map<String, Basket> properties) {
    return new PropertySet(TreePVector.from(properties.keySet()), HashTreePMap.from(properties));
  }

  private final PVector<String> keys;
  private final PMap<String, Basket> properties;

  private PropertySet(PVector<String> keys, PMap<String, Basket> properties) {
    this.keys = keys;
    this.properties = properties;
  }

  public PropertySet with(String key, Basket basket) {
    return properties.containsKey(key)
      ? new PropertySet(keys, properties.plus(key, basket))
      : new PropertySet(keys.plus(key), properties.plus(key, basket));
  }

  public int size() {
    return properties.size();
  }

  public Basket get(String key) {
    return properties.get(key);
  }

  public boolean containsKey(String key) {
    return properties.containsKey(key);
  }

  public PropertySet minus(String key) {
    return new PropertySet(keys.minus(key), properties.minus(key));
  }

  public Map<String, Basket> toMap() {
    Map<String, Basket> result = new LinkedHashMap<>();
    for (String key : keys) {
      result.put(key, properties.get(key));
    }
    return result;
  }

  @Override
  public boolean equals(Object other) {
    return this == other
        || (other instanceof PropertySet
          && PropertySet.class.cast(other).keys.equals(keys)
          && PropertySet.class.cast(other).properties.equals(properties));
  }

  @Override
  public int hashCode() {
    return Objects.hash(keys.hashCode(), properties.hashCode());
  }

  @Override
  public String toString() {
    return toMap().toString();
  }

  @Override
  public Iterator<ObjectEntry> iterator() {
    final Iterator<String> keyIterator = keys.iterator();
    return new Iterator<ObjectEntry>() {
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean hasNext() {
        return keyIterator.hasNext();
      }

      @Override
      public ObjectEntry next() {
        String key = keyIterator.next();
        return ObjectEntry.of(key, properties.get(key));
      }
    };
  }
}
