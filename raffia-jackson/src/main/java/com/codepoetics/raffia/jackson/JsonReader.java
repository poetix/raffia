package com.codepoetics.raffia.jackson;

import com.codepoetics.raffia.Raffia;
import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.writers.BasketWriter;
import com.codepoetics.raffia.writers.Writers;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;

public final class JsonReader {

  private static final JsonFactory FACTORY = new JsonFactory();

  public static Basket readBasket(String json) {
    return readWith(json, Writers.weavingTransient()).weave();
  }

  public static Basket readBasket(InputStream inputStream) throws IOException {
    return readWith(inputStream, Writers.weavingTransient()).weave();
  }

  public static Basket readBasket(Reader reader) throws IOException {
    return readWith(reader, Writers.weavingTransient()).weave();
  }

  public static <T extends BasketWriter<T>> T readWith(String json, T writer) {
    try {
      return readWith(FACTORY.createParser(json), writer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T extends BasketWriter<T>> T readWith(InputStream inputStream, T writer) throws IOException {
    return readWith(FACTORY.createParser(inputStream), writer);
  }

  public static <T extends BasketWriter<T>> T readWith(Reader reader, T writer) throws IOException {
    return readWith(FACTORY.createParser(reader), writer);
  }


  public static <T extends BasketWriter<T>> T readWith(JsonParser parser, T writer) throws IOException {
    T result = writer;
    while(!parser.isClosed()){
      result = readToken(parser, result);
    }
    return result;
  }

  private static <T extends BasketWriter<T>> T readToken(JsonParser parser, T writer) throws IOException {
    JsonToken token = parser.nextToken();

    if (token == null) {
      return writer;
    }

    switch (token) {
      case START_ARRAY:
        return writer.beginArray();
      case START_OBJECT:
        return writer.beginObject();
      case END_ARRAY:
        return writer.end();
      case END_OBJECT:
        return writer.end();
      case FIELD_NAME:
        return writer.key(parser.getCurrentName());
      case VALUE_STRING:
        return writer.add(parser.getText());
      case VALUE_NUMBER_FLOAT:
        return writer.add(BigDecimal.valueOf(parser.getDoubleValue()));
      case VALUE_NUMBER_INT:
        return writer.add(BigDecimal.valueOf(parser.getIntValue()));
      case VALUE_TRUE:
        return writer.add(true);
      case VALUE_FALSE:
        return writer.add(false);
      case VALUE_NULL:
        return writer.addNull();
      default:
        return writer;
    }
  }
}
