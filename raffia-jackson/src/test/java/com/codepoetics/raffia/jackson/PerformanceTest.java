package com.codepoetics.raffia.jackson;

import com.codepoetics.raffia.lenses.Lens;
import com.codepoetics.raffia.mappers.Mapper;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.operations.Updaters;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static com.codepoetics.raffia.lenses.Lens.lens;
import static org.junit.Assert.assertEquals;

@Ignore
public class PerformanceTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final JsonFactory FACTORY = MAPPER.getFactory();

  private static final Lens VALUE_LENS = lens("$[*].value");

  private static final Updater TO_UPPERCASE = Updaters.ofString(new Mapper<String, String>() {
    @Override
    public String map(String input) {
      return input.toUpperCase();
    }
  });

  private static final Lens READ_IS_TRUE = lens("$[?]", lens("@.read").isTrue());

  private static final TypeReference<List<Item>> LIST_OF_ITEMS = new TypeReference<List<Item>>() {
  };

  @Rule
  public ContiPerfRule i = new ContiPerfRule();


  public static final class Item {
    @JsonProperty
    private boolean read;

    @JsonProperty
    private String value;

    public boolean isRead() {
      return read;
    }

    public void setRead(boolean read) {
      this.read = read;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }

  @Test
  public void resultsAreTheSame() throws IOException {
    assertEquals(jacksonMap(), raffiaMap());
  }

  @PerfTest(
      invocations = 100000,
      threads = 8,
      rampUp = 10000
  )
  @Test
  public void jacksonMapAndWrite() throws IOException {
    jacksonMap();
  }

  private String jacksonMap() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonGenerator generator = FACTORY.createGenerator(stringWriter);

    generator.writeStartArray();
    List<Item> items = MAPPER.readValue(getClass().getResourceAsStream("/items.json"), LIST_OF_ITEMS);

    for (Item item : items) {
      if (item.isRead()) {
        item.setValue(item.getValue().toUpperCase());
        MAPPER.writeValue(generator, item);
      }
    }

    generator.writeEndArray();
    generator.flush();

    return stringWriter.toString();
  }

  @PerfTest(
      invocations = 100000,
      threads = 8,
      rampUp = 10000
  )
  @Test
  public void raffiaMapAndWrite() throws IOException {
    raffiaMap();
  }

  private String raffiaMap() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = JsonWriter.writingTo(FACTORY, stringWriter);

    FilteringWriter<JsonWriter> rewritingWriter = FilteringWriter.rewriting(
        VALUE_LENS,
        jsonWriter,
        TO_UPPERCASE
    );

    FilteringWriter<FilteringWriter<JsonWriter>> filteringWriter = FilteringWriter.filtering(
        READ_IS_TRUE,
        rewritingWriter);

    JsonReader.readWith(getClass().getResourceAsStream("/items.json"), filteringWriter)
        .complete()
        .complete();

    return stringWriter.toString();
  }
}
