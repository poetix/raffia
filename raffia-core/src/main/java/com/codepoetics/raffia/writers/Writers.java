package com.codepoetics.raffia.writers;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Baskets;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.math.BigDecimal;

public final class Writers {

  private Writers() {
  }

  public static BasketWeavingWriter weaving() {
    return new ValueBasketWeavingWriter(null);
  }

  private static abstract class PersistentBasketWeavingWriter implements BasketWeavingWriter {

    protected final PersistentBasketWeavingWriter parent;

    protected PersistentBasketWeavingWriter(PersistentBasketWeavingWriter parent) {
      this.parent = parent;
    }

    @Override
    public BasketWeavingWriter add(String value) {
      return add(Baskets.ofString(value));
    }

    @Override
    public BasketWeavingWriter add(BigDecimal value) {
      return add(Baskets.ofNumber(value));
    }

    @Override
    public BasketWeavingWriter add(boolean value) {
      return add(Baskets.ofBoolean(value));
    }

    @Override
    public BasketWeavingWriter addNull() {
      return add(Baskets.ofNull());
    }
  }

  private static final class ValueBasketWeavingWriter extends PersistentBasketWeavingWriter {

    private final Basket value;

    private ValueBasketWeavingWriter(Basket value) {
      super(null);
      this.value = value;
    }

    @Override
    public PersistentBasketWeavingWriter add(Basket basket) {
      if (value != null) {
        throw new IllegalStateException("plus called when writing value, but value already set");
      }
      return new ValueBasketWeavingWriter(basket);
    }

    @Override
    public Basket weave() {
      if (value == null) {
        throw new IllegalStateException("weave called on incomplete value");
      }
      return value;
    }

    @Override
    public BasketWeavingWriter beginObject() {
      return new ObjectBasketWeavingWriter(this, null, TreePVector.<ObjectEntry>empty());
    }

    @Override
    public BasketWeavingWriter beginArray() {
      return new ArrayBasketWeavingWriter(this, ArrayContents.empty());
    }

    @Override
    public BasketWeavingWriter end() {
      throw new IllegalStateException("end called without corresponding beginObject or beginArray");
    }

    @Override
    public BasketWeavingWriter key(String key) {
      throw new IllegalStateException("key called, but not writing object");
    }
  }

  private static final class ArrayBasketWeavingWriter extends PersistentBasketWeavingWriter {

    private final ArrayContents contents;

    private ArrayBasketWeavingWriter(PersistentBasketWeavingWriter parent, ArrayContents contents) {
      super(parent);
      this.contents = contents;
    }

    @Override
    public PersistentBasketWeavingWriter add(Basket basket) {
      return new ArrayBasketWeavingWriter(parent, contents.plus(basket));
    }

    @Override
    public Basket weave() {
      return Baskets.ofArray(contents);
    }

    @Override
    public BasketWeavingWriter beginObject() {
      return new ObjectBasketWeavingWriter(this, null, TreePVector.<ObjectEntry>empty());
    }

    @Override
    public BasketWeavingWriter end() {
      return parent.add(weave());
    }

    @Override
    public BasketWeavingWriter beginArray() {
      return new ArrayBasketWeavingWriter(this, ArrayContents.empty());
    }

    @Override
    public BasketWeavingWriter key(String key) {
      throw new IllegalStateException("key called while writing beginArray");
    }
  }

  private static final class ObjectBasketWeavingWriter extends PersistentBasketWeavingWriter {

    private final String key;
    private final PVector<ObjectEntry> contents;

    private ObjectBasketWeavingWriter(PersistentBasketWeavingWriter parent, String key, PVector<ObjectEntry> contents) {
      super(parent);
      this.key = key;
      this.contents = contents;
    }

    @Override
    public PersistentBasketWeavingWriter add(Basket basket) {
      if (key == null) {
        throw new IllegalStateException("plus called while writing beginObject, but key not given");
      }
      return new ObjectBasketWeavingWriter(parent, null, contents.plus(ObjectEntry.of(key, basket)));
    }

    @Override
    public Basket weave() {
      return Baskets.ofObject(PropertySet.of(contents));
    }

    @Override
    public BasketWeavingWriter beginObject() {
      return new ObjectBasketWeavingWriter(this, null, TreePVector.<ObjectEntry>empty());
    }

    @Override
    public BasketWeavingWriter end() {
      return parent.add(weave());
    }

    @Override
    public BasketWeavingWriter beginArray() {
      if (key == null) {
        throw new IllegalStateException("beginArray called while writing beginObject, but key not given");
      }
      return new ArrayBasketWeavingWriter(this, ArrayContents.empty());
    }

    @Override
    public BasketWeavingWriter key(String key) {
      if (this.key != null) {
        throw new IllegalStateException("key called, but key already set");
      }
      return new ObjectBasketWeavingWriter(parent, key, contents);
    }
  }
}
