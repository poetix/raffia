package com.codepoetics.raffia;

import com.codepoetics.raffia.api.Basket;
import com.codepoetics.raffia.api.BasketWeavingWriter;
import com.codepoetics.raffia.api.Mapper;
import com.codepoetics.raffia.api.Visitor;
import com.codepoetics.raffia.projections.Projections;
import com.codepoetics.raffia.setters.Setters;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.writers.Writers;
import org.junit.Test;

import static com.codepoetics.raffia.lenses.Lens.lens;
import static com.codepoetics.raffia.projections.Projections.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class FilteringWriterProjectingTest {

  @Test
  public void projectSingleValue() {
    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.projecting(
        lens("$"));

    Basket result = writer.add("Value").complete().weave();

    assertThat(lens("$[*]").getAll(asString, result), contains("Value"));
  }

  @Test
  public void projectMatchedKey() {
    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.projecting(
        lens("$.xyzzy.foo"));

    Basket result = writer.beginObject()
        .key("quux").beginObject()
          .add("foo").add("not the foo value you want")
        .end()
        .key("xyzzy").beginObject()
          .key("foo").add("foo value")
          .key("bar").add("bar value")
        .end()
        .end()
        .complete().weave();

    assertThat(lens("$[*]").getAll(asString, result), contains("foo value"));
  }

  @Test
  public void projectAllKeys() {
    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.projecting(
        lens("$.*"));

    Basket result = writer.beginObject()
        .key("foo").add("foo value")
        .key("bar").add("bar value")
        .end()
        .complete().weave();

    assertThat(lens("$[*]").getAll(asString, result), contains("foo value", "bar value"));
  }

  @Test
  public void projectAllNestedKeys() {
    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.projecting(
        lens("$.*.*"));

    Basket result = writer.beginObject()
        .key("ignored").add("outer value")
        .key("nested").beginObject()
          .key("foo").add("nested foo value")
          .key("bar").add("nested bar value")
        .end()
        .end()
        .complete().weave();

    assertThat(lens("$[*]").getAll(asString, result), contains("nested foo value", "nested bar value"));
  }

  @Test
  public void projectSecondItem() {
    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.projecting(
        lens("$[1]"));

    Basket result = writer.beginArray()
        .add("first")
        .add("second")
        .add("third")
        .end()
        .complete().weave();

    assertThat(lens("$[*]").getAll(asString, result), contains("second"));
  }

  @Test
  public void projectSecondAndThirdItems() {
    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.projecting(
        lens("$[1, 2]"));

    Basket result = writer.beginArray()
        .add("first")
        .add("second")
        .add("third")
        .end()
        .complete().weave();

    assertThat(lens("$[*]").getAll(asString, result), contains("second", "third"));
  }

  @Test
  public void projectAllItems() {
    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.projecting(
        lens("$[*]"));

    Basket result = writer.beginArray()
        .add("first")
        .add("second")
        .add("third")
        .end()
        .complete().weave();

    assertThat(lens("$[*]").getAll(asString, result), contains("first", "second", "third"));
  }

  @Test
  public void projectMatchingStrings() {
    Visitor<Boolean> shouldBeRewritten = Projections.map(asString, new Mapper<String, Boolean>() {
      @Override
      public Boolean map(String input) {
        return input.contains("project me");
      }
    });

    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.projecting(
        lens("$[?]", shouldBeRewritten));

    Basket result = writer.beginArray()
        .add("a (project me)")
        .add("b")
        .add("c (project me)")
        .end()
        .complete().weave();

    assertThat(lens("$[*]").getAll(asString, result), contains("a (project me)", "c (project me)"));
  }

  @Test
  public void projectDeepScannedStrings() {
    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.projecting(
        lens("$..nested.bar"));

    Basket result = writer.beginObject()
        .key("nested").beginObject()
          .key("foo").add("foo 1")
          .key("bar").add("bar 1")
        .end()
        .key("foo").beginArray()
          .beginObject()
            .key("nested").beginObject()
              .key("foo").add("foo 2")
              .key("bar").add("bar 2")
            .end()
          .end()
          .beginObject()
            .key("outer").beginObject()
              .key("nested").beginObject()
                .key("foo").add("foo 3")
                .key("bar").add("bar 3")
              .end()
            .end()
          .end()
        .end()
      .end()
      .complete().weave();

    assertThat(lens("$[*]").getAll(asString, result), contains("bar 1", "bar 2", "bar 3"));
  }

  @Test
  public void rewriteDeepScannedMatchingStrings() {
    Visitor<Boolean> isFlaggedForRewrite = lens("@.project").isTrue();

    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.projecting(
        lens("$..nested[?].value", isFlaggedForRewrite));

    Basket result = writer.beginObject()
        .key("nested").beginArray()
          .beginObject()
            .key("project").add(true)
            .key("value").add("project me")
          .end()
        .end()
        .key("foo").beginArray()
          .beginObject()
            .key("nested").beginArray()
              .beginObject()
                .key("project").add(false)
                .key("value").add("but not me")
              .end()
            .end()
          .end()
          .beginObject()
            .key("outer").beginObject()
              .key("nested").beginArray()
                .beginObject()
                  .key("project").add(true)
                  .key("value").add("and me")
                .end()
              .end()
            .end()
          .end()
        .end()
        .end()
        .complete().weave();

    assertThat(lens("$[*]").getAll(asString, result), contains("project me", "and me"));
  }

  @Test
  public void rewriteMatchingObjects() {
    Visitor<Boolean> isFlaggedForRewrite = Projections.atKey("rewrite", Projections.asBoolean);

    FilteringWriter<BasketWeavingWriter> writer = FilteringWriter.rewriting(
        lens("$[?]..value", isFlaggedForRewrite),
        Writers.weaving(),
        Setters.toString("Rewritten"));

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

    assertThat(lens("$..value").getAll(asString, result), contains("Rewritten", "Rewritten", "Unrewritten"));
  }

}
