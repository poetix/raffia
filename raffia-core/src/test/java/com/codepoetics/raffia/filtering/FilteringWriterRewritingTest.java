package com.codepoetics.raffia.filtering;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.java.api.Mapper;
import com.codepoetics.raffia.java.api.BasketPredicate;
import com.codepoetics.raffia.operations.Setters;
import com.codepoetics.raffia.java.api.Updater;
import com.codepoetics.raffia.java.api.ValuePredicate;
import com.codepoetics.raffia.predicates.BasketPredicates;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.StreamingWriters;
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
    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.rewriting(
        lens("$"),
        Writers.INSTANCE.weaving(),
        Setters.INSTANCE.toString("Rewritten"));

    Basket result = writer.add("Unrewritten").complete().weave();

    assertThat(result.asString(), equalTo("Rewritten"));
  }

  @Test
  public void rewriteMatchedKey() {
    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.rewriting(
        lens("$.foo"),
        Writers.INSTANCE.weaving(),
        Setters.INSTANCE.toString("Rewritten"));

    Basket result = writer.beginObject()
          .key("foo").add("Unrewritten")
          .key("bar").add("Unrewritten")
        .end()
        .complete().weave();

    assertThat(result.getProperty("foo").asString(), equalTo("Rewritten"));
    assertThat(result.getProperty("bar").asString(), equalTo("Unrewritten"));
  }

  @Test
  public void rewriteAllKeys() {
    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.rewriting(
        lens("$.*"),
        Writers.INSTANCE.weaving(),
        Setters.INSTANCE.toString("Rewritten"));

    Basket result = writer.beginObject()
        .key("foo").add("Unrewritten")
        .key("bar").add("Unrewritten")
        .end()
        .complete().weave();

    assertThat(result.getProperty("foo").asString(), equalTo("Rewritten"));
    assertThat(result.getProperty("bar").asString(), equalTo("Rewritten"));
  }

  @Test
  public void rewriteAllNestedKeys() {
    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.rewriting(
        lens("$.*.*"),
        Writers.INSTANCE.weaving(),
        Setters.INSTANCE.toString("Rewritten"));

    Basket result = writer.beginObject()
        .key("foo").add("Unrewritten")
        .key("nested").beginObject()
        .key("foo").add("Unrewritten")
        .key("bar").add("Unrewritten")
        .end()
        .end()
        .complete().weave();

    assertThat(lens("$..foo").getAllStrings(result), contains("Unrewritten", "Rewritten"));
  }

  @Test
  public void rewriteSecondItem() {
    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.rewriting(
        lens("$[1]"),
        Writers.INSTANCE.weaving(),
        Setters.INSTANCE.toString("Rewritten"));

    Basket result = writer.beginArray()
        .add("Unrewritten")
        .add("Unrewritten")
        .add("Unrewritten")
        .end()
        .complete().weave();

    assertThat(result.asListOfString(), contains("Unrewritten", "Rewritten", "Unrewritten"));
  }

  @Test
  public void rewriteAllItems() {
    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.rewriting(
        lens("$[*]"),
        Writers.INSTANCE.weaving(),
        Setters.INSTANCE.toString("Rewritten"));

    Basket result = writer.beginArray()
        .add("Unrewritten")
        .add("Unrewritten")
        .add("Unrewritten")
        .end()
        .complete().weave();

    assertThat(result.asListOfString(), contains("Rewritten", "Rewritten", "Rewritten"));
  }

  @Test
  public void rewriteMatchingStrings() {
    BasketPredicate shouldBeRewritten = BasketPredicates.INSTANCE.isString(new ValuePredicate<String>() {
      @Override
      public boolean test(String value) {
        return value.contains("rewrite me");
      }
    });

    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.rewriting(
        lens("$[?]", shouldBeRewritten),
        Writers.INSTANCE.weaving(),
        Setters.INSTANCE.toString("Rewritten"));

    Basket result = writer.beginArray()
        .add("Unrewritten (rewrite me)")
        .add("Unrewritten")
        .add("Unrewritten (rewrite me)")
        .end()
        .complete().weave();

    assertThat(result.asListOfString(), contains("Rewritten", "Unrewritten", "Rewritten"));
  }

  @Test
  public void rewriteDeepScannedStrings() {
    BasketPredicate isFlaggedForRewrite = BasketPredicates.hasKey("rewrite", BasketPredicates.isTrue());

    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.rewriting(
        lens("$..nested.value"),
        Writers.INSTANCE.weaving(),
        Setters.INSTANCE.toString("Rewritten")
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

    assertThat(lens("$..value").getAllStrings(result), contains("Rewritten", "Rewritten", "Rewritten"));
  }

  @Test
  public void rewriteDeepScannedMatchingStrings() {
    BasketPredicate isFlaggedForRewrite = BasketPredicates.hasKey("rewrite", BasketPredicates.isTrue());

    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.rewriting(
        lens("$..nested[?].value", isFlaggedForRewrite),
        Writers.INSTANCE.weaving(),
        Setters.INSTANCE.toString("Rewritten")
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

    System.out.println(lens("$..value").getAll(result));
    assertThat(lens("$..value").getAllStrings(result), contains("Rewritten", "Unrewritten", "Rewritten"));
  }

  @Test
  public void rewriteMatchingObjects() {
    BasketPredicate isFlaggedForRewrite = lens("@.rewrite").isTrue();

    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.rewriting(
        lens("$[?]..value", isFlaggedForRewrite),
        Writers.INSTANCE.weaving(),
        Setters.INSTANCE.toString("Rewritten"));

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
        .complete().weave();

    assertThat(lens("$..value").getAllStrings(result), contains("Rewritten", "Rewritten", "Unrewritten"));
  }

  @Test
  public void urlRewritingTest() {
    Updater urlRewriter = com.codepoetics.raffia.operations.Updaters.INSTANCE.ofString(new Mapper<String, String>() {
      @Override
      public String map(String input) {
        return input.replace("test.com", "realsite.com");
      }
    });

    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.rewriting(
        lens("$..links[*].url"),
        Writers.INSTANCE.weaving(),
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

    assertThat(lens("$..url").getAllStrings(transformed), contains("http://realsite.com/", "http://realsite.com/about"));
  }

}
