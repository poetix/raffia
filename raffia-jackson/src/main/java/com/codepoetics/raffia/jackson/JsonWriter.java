package com.codepoetics.raffia.jackson;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.BasketWriter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;

public final class JsonWriter implements BasketWriter<JsonWriter> {

  private static final JsonFactory DEFAULT = new JsonFactory();

  public static String writeBasketAsString(Basket basket, JsonFactory jsonFactory) {
    StringWriter writer = new StringWriter();
    writeBasketTo(basket, writer, jsonFactory);
    return writer.toString();
  }

  public static String writeBasketAsString(Basket basket) {
    StringWriter writer = new StringWriter();
    writeBasketTo(basket, writer);
    return writer.toString();
  }

  public static JsonWriter writeBasketTo(Basket basket, OutputStream outputStream, JsonFactory jsonFactory) {
    return basket.visit(Visitors.writingTo(writingTo(jsonFactory, outputStream)));
  }

  public static JsonWriter writeBasketTo(Basket basket, Writer writer, JsonFactory jsonFactory) {
    return basket.visit(Visitors.writingTo(writingTo(jsonFactory, writer)));
  }

  public static JsonWriter writeBasketTo(Basket basket, OutputStream outputStream) {
    return writeBasketTo(basket, outputStream, DEFAULT);
  }

  public static JsonWriter writeBasketTo(Basket basket, Writer writer) {
    return writeBasketTo(basket, writer, DEFAULT);
  }

  public static JsonWriter writingTo(OutputStream outputStream) {
      return writingTo(DEFAULT, outputStream);
  }

  public static JsonWriter writingTo(Writer writer) {
    return writingTo(DEFAULT, writer);
  }

  public static JsonWriter writingTo(JsonFactory factory, OutputStream outputStream) {
    try {
      return writingTo(factory.createGenerator(outputStream));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static JsonWriter writingTo(JsonFactory factory, Writer writer) {
    try {
      return writingTo(factory.createGenerator(writer));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static JsonWriter writingTo(JsonGenerator generator) {
    return new JsonWriter(null, StructContext.NONE, generator);
  }

  private enum StructContext {
    OBJECT,
    ARRAY,
    NONE
  }

  private final JsonWriter parent;
  private final StructContext context;
  private final JsonGenerator generator;

  public JsonWriter(JsonWriter parent, StructContext context, JsonGenerator generator) {
    this.parent = parent;
    this.context = context;
    this.generator = generator;
  }

  @Override
  public JsonWriter beginObject() {
    try {
      generator.writeStartObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new JsonWriter(this, StructContext.OBJECT, generator);
  }

  @Override
  public JsonWriter beginArray() {
    try {
      generator.writeStartArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new JsonWriter(this, StructContext.ARRAY, generator);
  }

  @Override
  public JsonWriter end() {
    try {
      switch (context) {
        case ARRAY: {
          generator.writeEndArray();
          return flushOnCompletion(parent);
        }
        case OBJECT: {
          generator.writeEndObject();
          return flushOnCompletion(parent);
        }
        default:
          throw new IllegalStateException("end called without matching startArray or startObject");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private JsonWriter flushOnCompletion(JsonWriter writer) {
    if (writer.context.equals(StructContext.NONE)) {
      try {
        generator.flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return writer;
  }

  @Override
  public JsonWriter key(String key) {
    try {
      generator.writeFieldName(key);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  @Override
  public JsonWriter add(String value) {
    try {
      generator.writeString(value);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return flushOnCompletion(this);
  }

  @Override
  public JsonWriter add(BigDecimal value) {
    try {
      generator.writeNumber(value);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return flushOnCompletion(this);
  }

  @Override
  public JsonWriter add(boolean value) {
    try {
      generator.writeBoolean(value);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return flushOnCompletion(this);
  }

  @Override
  public JsonWriter addNull() {
    try {
      generator.writeNull();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return flushOnCompletion(this);
  }
}
