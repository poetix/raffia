package com.codepoetics.raffia.writers;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWeavingWriter;
import com.codepoetics.raffia.api.ObjectEntry;
import com.codepoetics.raffia.api.PropertySet;
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
    public BasketWeavingWriter writeString(String value) {
      return writeBasket(Baskets.ofString(value));
    }

    @Override
    public BasketWeavingWriter writeNumber(BigDecimal value) {
      return writeBasket(Baskets.ofNumber(value));
    }

    @Override
    public BasketWeavingWriter writeBoolean(boolean value) {
      return writeBasket(Baskets.ofBoolean(value));
    }

    @Override
    public BasketWeavingWriter writeNull() {
      return writeBasket(Baskets.ofNull());
    }
  }

  private static final class ValueBasketWeavingWriter extends PersistentBasketWeavingWriter {

    private final Basket value;

    private ValueBasketWeavingWriter(Basket value) {
      super(null);
      this.value = value;
    }

    @Override
    public PersistentBasketWeavingWriter writeBasket(Basket basket) {
      if (value != null) {
        throw new IllegalStateException("accept called when writing value, but value already set");
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
    public BasketWeavingWriter writeStartObject() {
      return new ObjectBasketWeavingWriter(this, null, TreePVector.<ObjectEntry>empty());
    }

    @Override
    public BasketWeavingWriter writeEndObject() {
      throw new IllegalStateException("writeEndObject called without corresponding writeStartObject");
    }

    @Override
    public BasketWeavingWriter writeStartArray() {
      return new ArrayBasketWeavingWriter(this, TreePVector.<Basket>empty());
    }

    @Override
    public BasketWeavingWriter writeEndArray() {
      throw new IllegalStateException("writeEndArray called without corresponding writeStartArray");
    }

    @Override
    public BasketWeavingWriter writeKey(String key) {
      throw new IllegalStateException("writeKey called, but not writing object");
    }
  }

  private static final class ArrayBasketWeavingWriter extends PersistentBasketWeavingWriter {

    private final PVector<Basket> contents;

    private ArrayBasketWeavingWriter(PersistentBasketWeavingWriter parent, PVector<Basket> contents) {
      super(parent);
      this.contents = contents;
    }

    @Override
    public PersistentBasketWeavingWriter writeBasket(Basket basket) {
      return new ArrayBasketWeavingWriter(parent, contents.plus(basket));
    }

    @Override
    public Basket weave() {
      return Baskets.ofArray(contents);
    }

    @Override
    public BasketWeavingWriter writeStartObject() {
      return new ObjectBasketWeavingWriter(this, null, TreePVector.<ObjectEntry>empty());
    }

    @Override
    public BasketWeavingWriter writeEndObject() {
      throw new IllegalStateException("writeEndObject called while writing array");
    }

    @Override
    public BasketWeavingWriter writeStartArray() {
      return new ArrayBasketWeavingWriter(this, TreePVector.<Basket>empty());
    }

    @Override
    public BasketWeavingWriter writeEndArray() {
      return parent.writeBasket(weave());
    }

    @Override
    public BasketWeavingWriter writeKey(String key) {
      throw new IllegalStateException("writeKey called while writing array");
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
    public PersistentBasketWeavingWriter writeBasket(Basket basket) {
      if (key == null) {
        throw new IllegalStateException("accept called while writing object, but key not given");
      }
      return new ObjectBasketWeavingWriter(parent, null, contents.plus(ObjectEntry.of(key, basket)));
    }

    @Override
    public Basket weave() {
      return Baskets.ofObject(PropertySet.of(contents));
    }

    @Override
    public BasketWeavingWriter writeStartObject() {
      return new ObjectBasketWeavingWriter(this, null, TreePVector.<ObjectEntry>empty());
    }

    @Override
    public BasketWeavingWriter writeEndObject() {
      return parent.writeBasket(weave());
    }

    @Override
    public BasketWeavingWriter writeStartArray() {
      if (key == null) {
        throw new IllegalStateException("writeStartArray called while writing object, but key not given");
      }
      return new ArrayBasketWeavingWriter(this, TreePVector.<Basket>empty());
    }

    @Override
    public BasketWeavingWriter writeEndArray() {
      throw new IllegalStateException("writeEndArray called while writing object");
    }

    @Override
    public BasketWeavingWriter writeKey(String key) {
      if (this.key != null) {
        throw new IllegalStateException("writeKey called, but key already set");
      }
      return new ObjectBasketWeavingWriter(parent, key, contents);
    }
  }
}
