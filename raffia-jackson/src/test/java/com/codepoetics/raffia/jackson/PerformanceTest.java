package com.codepoetics.raffia.jackson;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.lenses.Lens;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.StreamingWriters;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

import static com.codepoetics.raffia.lenses.Lens.lens;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

@Ignore
public class PerformanceTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final JsonFactory FACTORY = MAPPER.getFactory();

  private static final Lens VALUE_LENS = lens(
      "$.components[*].parts[*].qty");

  private static final Basket ONE = Basket.ofNumber(BigDecimal.ONE);

  private static final Updater REPLACE_NULL_WITH_ZERO = new Updater() {
    @Override
    public Basket update(Basket basket) {
      return basket.isNull() ? ONE : basket;
    }
  };

  @Rule
  public ContiPerfRule i = new ContiPerfRule();

  private static final Random random = new Random();
  private static String document;

  @BeforeClass
  public static void createDocument() throws JsonProcessingException {
    document = jacksonWriteModel();
    System.out.println(document);
  }

  private static String jacksonWriteModel() throws JsonProcessingException {
    return MAPPER.writeValueAsString(jacksonCreateModel());
  }

  private static Model jacksonCreateModel() {
    Model model = new Model();
    model.setId(UUID.randomUUID().toString());
    model.setComponents(new HashMap<String, Component>());

    for (int i = 0; i < 10; i++) {
      model.components.put(UUID.randomUUID().toString(), jacksonCreateComponent());
      model.components.put(UUID.randomUUID().toString(), jacksonCreateComponent());
    }

    return model;
  }

  private static Component jacksonCreateComponent() {
    Component component = new Component();
    component.setId(UUID.randomUUID().toString());

    List<Part> componentParts = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      componentParts.add(jacksonCreatePart());
    }
    component.setParts(componentParts);
    return component;
  }

  private static Part jacksonCreatePart() {
    Part part = new Part();
    part.setId(UUID.randomUUID().toString());
    int qty = random.nextInt(10);
    part.setQty(qty == 0 ? null : qty);
    return part;
  }

  public static final class Part {
    private String id;
    private Integer qty;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public Integer getQty() {
      return qty;
    }

    public void setQty(Integer qty) {
      this.qty = qty;
    }
  }

  public static final class Component {
    private String id;
    private List<Part> parts;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public List<Part> getParts() {
      return parts;
    }

    public void setParts(List<Part> parts) {
      this.parts = parts;
    }
  }

  public static final class Model {

    private String id;
    private String description;
    private Map<String, Component> components;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public Map<String, Component> getComponents() {
      return components;
    }

    public void setComponents(Map<String, Component> components) {
      this.components = components;
    }
  }

  @Test
  public void resultsAreTheSame() throws IOException {
    assertEquals(jacksonMap(), raffiaMap());

    System.out.println(raffiaMap());
  }

  @PerfTest(
      invocations = 10000,
      threads = 8,
      rampUp = 1000
  )
  @Test
  public void jacksonMapAndWrite() throws IOException {
    assertFalse(jacksonMap().isEmpty());
  }

  private String jacksonMap() throws IOException {
    Model model = MAPPER.readValue(document, Model.class);

    for (Component component : model.getComponents().values()) {
      for (Part part : component.getParts()) {
        if (part.getQty() == null) {
          part.setQty(1);
        }
      }
    }

    return MAPPER.writeValueAsString(model);
  }

  @PerfTest(
      invocations = 10000,
      threads = 8,
      rampUp = 1000
  )
  @Test
  public void raffiaMapAndWrite() throws IOException {
    assertFalse(raffiaMap().isEmpty());
  }

  private String raffiaMap() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = JsonWriter.writingTo(FACTORY, stringWriter);

    FilteringWriter<JsonWriter> nullReplacer = StreamingWriters.rewriting(
        VALUE_LENS,
        jsonWriter,
        REPLACE_NULL_WITH_ZERO
    );

    JsonReader.readWith(document, nullReplacer).complete();

    return stringWriter.toString();
  }
}
