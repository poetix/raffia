package com.codepoetics.raffia.filtering;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.functions.BasketPredicate;
import com.codepoetics.raffia.functions.ValuePredicate;
import com.codepoetics.raffia.predicates.BasketPredicates;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.StreamingWriters;
import com.codepoetics.raffia.writers.BasketWeavingWriter;
import org.junit.Test;

import static com.codepoetics.raffia.lenses.Lens.lens;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class FilteringWriterProjectingTest {

  @Test
  public void projectSingleValue() {
    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.projectingArray(
        lens("$"));

    Basket result = writer.add("Value").complete().weave();

    assertThat(result.asListOfString(), contains("Value"));
  }

  @Test
  public void projectMatchedKey() {
    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.projectingArray(
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

    assertThat(result.asListOfString(), contains("foo value"));
  }

  @Test
  public void projectAllKeys() {
    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.projectingArray(
        lens("$.*"));

    Basket result = writer.beginObject()
        .key("foo").add("foo value")
        .key("bar").add("bar value")
        .end()
        .complete().weave();

    assertThat(result.asListOfString(), contains("foo value", "bar value"));
  }

  @Test
  public void projectAllNestedKeys() {
    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.projectingArray(
        lens("$.*.*"));

    Basket result = writer.beginObject()
        .key("ignored").add("outer value")
        .key("nested").beginObject()
          .key("foo").add("nested foo value")
          .key("bar").add("nested bar value")
        .end()
        .end()
        .complete().weave();

    assertThat(result.asListOfString(), contains("nested foo value", "nested bar value"));
  }

  @Test
  public void projectSecondItem() {
    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.projectingArray(
        lens("$[1]"));

    Basket result = writer.beginArray()
        .add("first")
        .add("second")
        .add("third")
        .end()
        .complete().weave();

    assertThat(result.asListOfString(), contains("second"));
  }

  @Test
  public void projectSecondAndThirdItems() {
    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.projectingArray(
        lens("$[1, 2]"));

    Basket result = writer.beginArray()
        .add("first")
        .add("second")
        .add("third")
        .end()
        .complete().weave();

    assertThat(result.asListOfString(), contains("second", "third"));
  }

  @Test
  public void projectAllItems() {
    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.projectingArray(
        lens("$[*]"));

    Basket result = writer.beginArray()
        .add("first")
        .add("second")
        .add("third")
        .end()
        .complete().weave();

    assertThat(result.asListOfString(), contains("first", "second", "third"));
  }

  @Test
  public void projectMatchingStrings() {
    BasketPredicate shouldBeRewritten = BasketPredicates.INSTANCE.isString(new ValuePredicate<String>() {
      @Override
      public boolean test(String value) {
        return value.contains("project me");
      }
    });

    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.projectingArray(
        lens("$[?]", shouldBeRewritten));

    Basket result = writer.beginArray()
        .add("a (project me)")
        .add("b")
        .add("c (project me)")
        .end()
        .complete().weave();

    assertThat(result.asListOfString(), contains("a (project me)", "c (project me)"));
  }

  @Test
  public void projectDeepScannedStrings() {
    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.projectingArray(
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

    assertThat(result.asListOfString(), contains("bar 1", "bar 2", "bar 3"));
  }

  @Test
  public void projectDeepScannedMatchingStrings() {
    BasketPredicate isFlaggedForRewrite = lens("@.project").isTrue();

    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.projectingArray(
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

    assertThat(result.asListOfString(), contains("project me", "and me"));
  }

  @Test
  public void projectMatchingObjects() {
    BasketPredicate isFlaggedForRewrite = lens("@.match").isTrue();

    FilteringWriter<BasketWeavingWriter> writer = StreamingWriters.INSTANCE.projectingArray(
        lens("$[?]..value", isFlaggedForRewrite));

    Basket result = writer.beginArray()
        .beginObject()
          .key("match").add(true)
          .key("value").add("a")
        .end()
        .beginObject()
          .key("match").add(true)
          .key("nested").beginObject()
            .key("value").add("b")
          .end()
        .end()
        .beginObject()
          .key("match").add(false)
          .key("value").add("c")
        .end()
        .end()
        .complete().weave();

    assertThat(result.asListOfString(), contains("a", "b"));
  }

}
