package com.codepoetics.raffia.filtering;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.functions.BasketPredicate;
import com.codepoetics.raffia.functions.Mapper;
import com.codepoetics.raffia.functions.Updater;
import com.codepoetics.raffia.functions.ValuePredicate;
import com.codepoetics.raffia.operations.Setters;
import com.codepoetics.raffia.operations.Updaters;
import com.codepoetics.raffia.predicates.BasketPredicates;
import com.codepoetics.raffia.streaming.Filter;
import com.codepoetics.raffia.streaming.Filters;
import com.codepoetics.raffia.streaming.PathAwareWriterKt;
import com.codepoetics.raffia.writers.BasketWeavingWriter;
import com.codepoetics.raffia.writers.Writers;
import org.junit.Test;

import static com.codepoetics.raffia.lenses.Lens.lens;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class FilteringWriterRewritingTest {

  @Test
  public void rewriteSingleValue() {
    Filter<BasketWeavingWriter> writer = Filters.updating(
        lens("$"),
        Setters.toString("Rewritten"),
        Writers.weaving());

    Basket result = writer.add("Unrewritten").getResult().weave();

    assertThat(result.asString(), equalTo("Rewritten"));
  }

  @Test
  public void rewriteMatchedKey() {
    Filter<BasketWeavingWriter> writer = Filters.updating(
        lens("$.foo"),
        Setters.toString("Rewritten"),
        Writers.weaving());

    Basket result = writer.beginObject()
          .key("foo").add("Unrewritten")
          .key("bar").add("Unrewritten")
        .end()
        .getResult().weave();

    assertThat(result.getProperty("foo").asString(), equalTo("Rewritten"));
    assertThat(result.getProperty("bar").asString(), equalTo("Unrewritten"));
  }

  @Test
  public void rewriteAllKeys() {
    Filter<BasketWeavingWriter> writer = Filters.updating(
        lens("$.*"),
        Setters.toString("Rewritten"),
        Writers.weaving());

    Basket result = writer.beginObject()
        .key("foo").add("Unrewritten")
        .key("bar").add("Unrewritten")
        .end()
        .getResult().weave();

    assertThat(result.getProperty("foo").asString(), equalTo("Rewritten"));
    assertThat(result.getProperty("bar").asString(), equalTo("Rewritten"));
  }

  @Test
  public void rewriteAllNestedKeys() {
    Filter<BasketWeavingWriter> writer = Filters.updating(
        lens("$.*.*"),
        Setters.toString("Rewritten"),
        Writers.weaving());

    Basket result = writer.beginObject()
        .key("foo").add("Unrewritten")
        .key("nested").beginObject()
        .key("foo").add("Unrewritten")
        .key("bar").add("Unrewritten")
        .end()
        .end()
        .getResult().weave();

    assertThat(lens("$..foo").getAllStrings(result), contains("Unrewritten", "Rewritten"));
  }

  @Test
  public void rewriteSecondItem() {
    Filter<BasketWeavingWriter> writer = Filters.updating(
        lens("$[1]"),
        Setters.toString("Rewritten"),
        Writers.weaving());

    Basket result = writer.beginArray()
        .add("Unrewritten")
        .add("Unrewritten")
        .add("Unrewritten")
        .end()
        .getResult().weave();

    assertThat(result.asListOfString(), contains("Unrewritten", "Rewritten", "Unrewritten"));
  }

  @Test
  public void rewriteAllItems() {
    Filter<BasketWeavingWriter> writer = Filters.updating(
        lens("$[*]"),
        Setters.toString("Rewritten"),
        Writers.weaving());

    Basket result = writer.beginArray()
        .add("Unrewritten")
        .add("Unrewritten")
        .add("Unrewritten")
        .end()
        .getResult().weave();

    assertThat(result.asListOfString(), contains("Rewritten", "Rewritten", "Rewritten"));
  }

  @Test
  public void rewriteMatchingStrings() {
    BasketPredicate shouldBeRewritten = BasketPredicates.isString(new ValuePredicate<String>() {
      @Override
      public boolean test(String value) {
        return value.contains("rewrite me");
      }
    });

    Filter<BasketWeavingWriter> writer = Filters.updating(
        lens("$[?]", shouldBeRewritten),
        Setters.toString("Rewritten"),
        Writers.weaving());

    Basket result = writer.beginArray()
        .add("Unrewritten (rewrite me)")
        .add("Unrewritten")
        .add("Unrewritten (rewrite me)")
        .end()
        .getResult().weave();

    assertThat(result.asListOfString(), contains("Rewritten", "Unrewritten", "Rewritten"));
  }

  @Test
  public void rewriteDeepScannedStrings() {
    BasketPredicate isFlaggedForRewrite = BasketPredicates.hasKey("rewrite", BasketPredicates.isTrue());

    Filter<BasketWeavingWriter> writer = Filters.updating(
        lens("$..nested.value"),
        Setters.toString("Rewritten"),
        Writers.weaving());

    Basket result = writer.beginObject()
        .key("nested").beginObject()
          .key("rewrite").add(true)
          .key("value").add("Unrewritten")
        .end()
        .key("foo").beginArray()
          .beginObject()
            .key("nested").beginObject()
              .key("rewrite").add(false)
              .key("value").add("Unrewritten")
            .end()
          .end()
          .beginObject()
            .key("outer").beginObject()
              .key("nested").beginObject()
                .key("rewrite").add(true)
                .key("value").add("Unrewritten")
              .end()
            .end()
          .end()
        .end()
      .end()
      .getResult().weave();

    assertThat(lens("$..value").getAllStrings(result), contains("Rewritten", "Rewritten", "Rewritten"));
  }

  @Test
  public void rewriteDeepScannedMatchingStrings() {
    BasketPredicate isFlaggedForRewrite = BasketPredicates.hasKey("rewrite", BasketPredicates.isTrue());

    Filter<BasketWeavingWriter> writer = Filters.updating(
        lens("$..nested[?].value", isFlaggedForRewrite),
        Setters.toString("Rewritten"),
        Writers.weaving()
    );

    Basket result = writer.beginObject()
        .key("nested").beginArray()
          .beginObject()
            .key("rewrite").add(true)
            .key("value").add("Unrewritten")
          .end()
        .end()
        .key("foo").beginArray()
          .beginObject()
            .key("nested").beginArray()
              .beginObject()
                .key("rewrite").add(false)
                .key("value").add("Unrewritten")
              .end()
            .end()
          .end()
          .beginObject()
            .key("outer").beginObject()
              .key("nested").beginArray()
                .beginObject()
                  .key("rewrite").add(true)
                  .key("value").add("Unrewritten")
                .end()
              .end()
            .end()
          .end()
        .end()
        .end()
        .getResult().weave();

    System.out.println(lens("$..value").getAll(result));
    assertThat(lens("$..value").getAllStrings(result), contains("Rewritten", "Unrewritten", "Rewritten"));
  }

  @Test
  public void rewriteMatchingObjects() {
    BasketPredicate isFlaggedForRewrite = lens("@.rewrite").isTrue();

    Filter<BasketWeavingWriter> writer = Filters.updating(
        lens("$[?]..value", isFlaggedForRewrite),
        Setters.toString("Rewritten"),
        Writers.weaving());

    Basket result = writer.beginArray()
        .beginObject()
          .key("rewrite").add(true)
          .key("value").add("Unrewritten")
        .end()
        .beginObject()
          .key("rewrite").add(true)
          .key("nested").beginObject()
            .key("value").add("Unrewritten")
          .end()
        .end()
        .beginObject()
          .key("rewrite").add(false)
          .key("value").add("Unrewritten")
        .end()
        .end()
        .getResult().weave();

    assertThat(lens("$..value").getAllStrings(result), contains("Rewritten", "Rewritten", "Unrewritten"));
  }

  @Test
  public void urlRewritingTest() {
    Updater urlRewriter = Updaters.ofString(new Mapper<String, String>() {
      @Override
      public String map(String input) {
        return input.replace("test.com", "realsite.com");
      }
    });

    Filter<BasketWeavingWriter> writer = Filters.updating(
        lens("$..links[*].url"),
        urlRewriter,
        Writers.weaving());

    Basket transformed = writer.beginObject()
        .key("data").beginObject()
        .key("links")
          .beginArray()
            .beginObject()
              .key("rel").add("home")
              .key("url").add("http://test.com/")
            .end()
            .beginObject()
              .key("rel").add("about")
              .key("url").add("http://test.com/about")
            .end()
          .end()
        .end()
      .end()
      .getResult()
      .weave();

    System.out.println(transformed);

    assertThat(lens("$..url").getAllStrings(transformed), contains("http://realsite.com/", "http://realsite.com/about"));
  }

}
