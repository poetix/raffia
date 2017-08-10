package com.codepoetics.raffia;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWeavingWriter;
import com.codepoetics.raffia.api.Mapper;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.indexes.FilteringWriter;
import com.codepoetics.raffia.predicates.Predicates;
import com.codepoetics.raffia.predicates.StringPredicates;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.setters.Setters;
import com.codepoetics.raffia.updaters.Updaters;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.Writers;
import org.junit.Test;

import static com.codepoetics.raffia.lenses.Lens.lens;
import static com.codepoetics.raffia.projections.Projections.asArray;
import static com.codepoetics.raffia.projections.Projections.asString;
import static com.codepoetics.raffia.projections.Projections.atKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class FilteringWriterRewritingTest {

  @Test
  public void rewriteSingleValue() {
    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.rewriting(
        lens("$"),
        Writers.weaving(),
        Setters.toString("Rewritten"));

    Basket result = writer.add("Unrewritten").complete().weave();

    assertThat(result.visit(asString), equalTo("Rewritten"));
  }

  @Test
  public void rewriteMatchedKey() {
    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.rewriting(
        lens("$.foo"),
        Writers.weaving(),
        Setters.toString("Rewritten"));

    Basket result = writer.beginObject()
          .key("foo").add("Unrewritten")
          .key("bar").add("Unrewritten")
        .end()
        .complete().weave();

    assertThat(result.visit(atKey("foo", asString)), equalTo("Rewritten"));
    assertThat(result.visit(atKey("bar", asString)), equalTo("Unrewritten"));
  }

  @Test
  public void rewriteAllKeys() {
    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.rewriting(
        lens("$.*"),
        Writers.weaving(),
        Setters.toString("Rewritten"));

    Basket result = writer.beginObject()
        .key("foo").add("Unrewritten")
        .key("bar").add("Unrewritten")
        .end()
        .complete().weave();

    assertThat(result.visit(atKey("foo", asString)), equalTo("Rewritten"));
    assertThat(result.visit(atKey("bar", asString)), equalTo("Rewritten"));
  }

  @Test
  public void rewriteSecondItem() {
    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.rewriting(
        lens("$[1]"),
        Writers.weaving(),
        Setters.toString("Rewritten"));

    Basket result = writer.beginArray()
        .add("Unrewritten")
        .add("Unrewritten")
        .add("Unrewritten")
        .end()
        .complete().weave();

    assertThat(result.visit(asArray).map(asString), contains("Unrewritten", "Rewritten", "Unrewritten"));
  }

  @Test
  public void rewriteAllItems() {
    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.rewriting(
        lens("$[*]"),
        Writers.weaving(),
        Setters.toString("Rewritten"));

    Basket result = writer.beginArray()
        .add("Unrewritten")
        .add("Unrewritten")
        .add("Unrewritten")
        .end()
        .complete().weave();

    assertThat(result.visit(asArray).map(asString), contains("Rewritten", "Rewritten", "Rewritten"));
  }

  @Test
  public void rewriteMatchingStrings() {
    Visitor<Boolean> shouldBeRewritten = Projections.map(asString, new Mapper<String, Boolean>() {
      @Override
      public Boolean map(String input) {
        return input.contains("rewrite me");
      }
    });

    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.rewriting(
        lens("$[?]", shouldBeRewritten),
        Writers.weaving(),
        Setters.toString("Rewritten"));

    Basket result = writer.beginArray()
        .add("Unrewritten (rewrite me)")
        .add("Unrewritten")
        .add("Unrewritten (rewrite me)")
        .end()
        .complete().weave();

    assertThat(result.visit(asArray).map(asString), contains("Rewritten", "Unrewritten", "Rewritten"));
  }

  @Test
  public void rewriteDeepScannedStrings() {
    Visitor<Boolean> isFlaggedForRewrite = Projections.atKey("rewrite", Projections.asBoolean);

    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.rewriting(
        lens("$..nested.value"),
        Writers.weaving(),
        Setters.toString("Rewritten")
    );

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
      .complete().weave();

    assertThat(lens("$..value").getAll(asString, result), contains("Rewritten", "Rewritten", "Rewritten"));
  }

  @Test
  public void rewriteDeepScannedMatchingStrings() {
    Visitor<Boolean> isFlaggedForRewrite = Projections.atKey("rewrite", Projections.asBoolean);

    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.rewriting(
        lens("$..nested[?].value", isFlaggedForRewrite),
        Writers.weaving(),
        Setters.toString("Rewritten")
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
        .complete().weave();

    assertThat(lens("$..value").getAll(asString, result), contains("Rewritten", "Unrewritten", "Rewritten"));
  }

  @Test
  public void rewriteMatchingObjects() {
    Visitor<Boolean> isFlaggedForRewrite = Projections.atKey("rewrite", Projections.asBoolean);

    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.rewriting(
        lens("$[?].value", isFlaggedForRewrite),
        Writers.weaving(),
        Setters.toString("Rewritten"));

    Basket result = writer.beginArray()
        .beginObject()
          .key("rewrite").add(true)
          .key("value").add("Unrewritten")
        .end()
        .beginObject()
          .key("rewrite").add(true)
          .key("value").add("Unrewritten")
        .end()
        .beginObject()
          .key("rewrite").add(false)
          .key("value").add("Unrewritten")
        .end()
        .end()
        .complete().weave();

    assertThat(lens("$..value").getAll(asString, result), contains("Rewritten", "Rewritten", "Unrewritten"));
  }

  @Test
  public void urlRewritingTest() {
    Visitor<Basket> urlRewriter = Updaters.ofString(new Mapper<String, String>() {
      @Override
      public String map(String input) {
        return input.replace("test.com", "realsite.com");
      }
    });

    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.rewriting(
        lens("$..links[*].url"),
        Writers.weaving(),
        urlRewriter
    );

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
      .complete()
      .weave();

    System.out.println(transformed);

    assertThat(lens("$..url").getAll(asString, transformed), contains("http://realsite.com/", "http://realsite.com/about"));
  }

}
