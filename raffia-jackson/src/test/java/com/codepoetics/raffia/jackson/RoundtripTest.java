package com.codepoetics.raffia.jackson;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.writers.BasketWeavingWriter;
import com.codepoetics.raffia.mappers.Mapper;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.predicates.NumberPredicates;
import com.codepoetics.raffia.writers.PassThroughWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static com.codepoetics.raffia.lenses.Lens.lens;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;

public class RoundtripTest {

  @Test
  public void readBasket() throws IOException {
    Basket basket = JsonReader.readBasket(getClass().getResourceAsStream("/store.json"));

    assertThat(lens().toAny("author").getAllStrings(basket),
        contains("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"));

    String repr = JsonWriter.writeBasketAsString(basket);

    System.out.println(repr);
  }

  @Test
  public void rewriteUrls() throws IOException {
    class UrlRewriter extends PassThroughWriter<JsonWriter, UrlRewriter> {

      protected UrlRewriter(JsonWriter state) {
        super(state);
      }

      @Override
      protected UrlRewriter with(JsonWriter state) {
        return new UrlRewriter(state);
      }

      @Override
      public UrlRewriter add(String value) {
        return super.add(value.replace("DOMAIN", "example.com"));
      }
    }

    StringWriter stringWriter = new StringWriter();
    UrlRewriter writer = new UrlRewriter(JsonWriter.writingTo(stringWriter));

    JsonReader.readWith(getClass().getResourceAsStream("/urls.json"), writer);

    assertThat(stringWriter.toString(), containsString("http://example.com/baz/xyzzy"));
  }

  @Test
  public void rewritePathMatchedStrings() throws IOException {
    Updater toUppercase = com.codepoetics.raffia.operations.Updaters.ofString(new Mapper<String, String>() {
      @Override
      public String map(String input) {
        return input.toUpperCase();
      }
    });

    Updater uppercaseTitle = com.codepoetics.raffia.operations.Updaters.updating("title", toUppercase);

    StringWriter stringWriter = new StringWriter();

    FilteringWriter<JsonWriter> transformer = FilteringWriter.rewriting(
        lens("$..book[?]", lens("@.author").matching("Nigel Rees")),
        JsonWriter.writingTo(stringWriter),
        uppercaseTitle
    );

    JsonReader.readWith(getClass().getResourceAsStream("/store.json"), transformer);

    System.out.println(stringWriter.toString());

    assertThat(stringWriter.toString(), containsString("SAYINGS OF THE CENTURY"));
    assertThat(stringWriter.toString(), containsString("The Lord of the Rings"));
  }

  @Test
  public void projectAuthorsOfCheapBooks() throws IOException {
    FilteringWriter<BasketWeavingWriter> filter = FilteringWriter.projecting(
        lens("$.store.book[?].author", lens("@.price").matchingNumber(NumberPredicates.isLessThan("10")))
    );

    Basket result = JsonReader.readWith(getClass().getResourceAsStream("/store.json"), filter)
        .complete()
        .weave();

    System.out.println(result);

    List<String> authors = result.asListOfString();

    assertThat(authors, contains("Nigel Rees", "Herman Melville"));
  }
}
