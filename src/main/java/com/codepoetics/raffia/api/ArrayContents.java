package com.codepoetics.raffia.api;

import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.*;

public final class ArrayContents implements Iterable<Basket> {

  public static ArrayContents empty() {
    return new ArrayContents(TreePVector.<Basket>empty());
  }

  public static ArrayContents of(Basket...contents) {
    return of(Arrays.asList(contents));
  }

  public static ArrayContents of(Collection<Basket> contents) {
    return new ArrayContents(TreePVector.from(contents));
  }

  private final PVector<Basket> contents;

  private ArrayContents(PVector<Basket> contents) {
    this.contents = contents;
  }

  public Basket get(int index) {
    return contents.get(index);
  }

  public int size() {
    return contents.size();
  }

  public ArrayContents plus(Basket basket) {
    return new ArrayContents(contents.plus(basket));
  }

  public ArrayContents plus(int index, Basket basket) {
    return new ArrayContents(contents.plus(index, basket));
  }

  public ArrayContents with(int index, Basket basket) {
    return new ArrayContents(contents.with(index, basket));
  }

  public ArrayContents plusAll(Collection<Basket> baskets) {
    return new ArrayContents(contents.plusAll(baskets));
  }

  public ArrayContents minus(int index) {
    return new ArrayContents(contents.minus(index));
  }

  public List<Basket> toList() {
    return contents;
  }

  public <T> List<T> map(Visitor<T> visitor) {
    List<T> result = new ArrayList<>(contents.size());
    for (Basket item : contents) {
      result.add(item.visit(visitor));
    }
    return result;
  }

  public <T> List<T> flatMap(Visitor<List<T>> visitor) {
    List<T> result = new ArrayList<>();
    for (Basket item : contents) {
      result.addAll(item.visit(visitor));
    }
    return result;
  }

  @Override
  public Iterator<Basket> iterator() {
    return contents.iterator();
  }

  @Override
  public boolean equals(Object other) {
    return this == other
        || (other instanceof ArrayContents
          && (ArrayContents.class.cast(other).contents.equals(contents)));
  }

  @Override
  public int hashCode() {
    return contents.hashCode();
  }

  @Override
  public String toString() {
    return contents.toString();
  }

}
