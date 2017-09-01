package com.codepoetics.raffia.filtering;


import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.functions.BasketPredicate;
import com.codepoetics.raffia.functions.ValuePredicate;
import com.codepoetics.raffia.operations.ProjectionResult;
import com.codepoetics.raffia.predicates.BasketPredicates;
import com.codepoetics.raffia.streaming.Filter;
import com.codepoetics.raffia.streaming.Filters;
import com.codepoetics.raffia.writers.BasketWeavingWriter;
import org.junit.Test;

import java.util.List;

import static com.codepoetics.raffia.lenses.Lens.lens;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class FilteringWriterProjectingTest {

  @Test
  public void projectSingleValue() {
    Filter<ProjectionResult<Basket>> writer = Filters.projecting(lens("$"));

    ProjectionResult<Basket> result = writer.add("Value").getResult();

    assertThat(result.getSingle().asString(), equalTo("Value"));
  }

  @Test
  public void projectMatchedKey() {
    Filter<ProjectionResult<Basket>> writer = Filters.projecting(lens("$.xyzzy.foo"));

    ProjectionResult<Basket> result = writer.beginObject()
        .key("quux").beginObject()
          .add("foo").add("not the foo value you want")
        .end()
        .key("xyzzy").beginObject()
          .key("foo").add("foo value")
          .key("bar").add("bar value")
        .end()
        .end()
        .getResult();

    assertThat(result.asList(), contains(Basket.ofString("foo value")));
  }

  @Test
  public void projectAllKeys() {
    Filter<ProjectionResult<Basket>> writer = Filters.projecting(lens("$.*"));

    ProjectionResult<Basket> result = writer.beginObject()
        .key("foo").add("foo value")
        .key("bar").add("bar value")
        .end()
        .getResult();

    assertThat(result.asList(), contains(Basket.ofString("foo value"), Basket.ofString("bar value")));
  }

  @Test
  public void projectAllNestedKeys() {
    Filter<ProjectionResult<Basket>> writer = Filters.projecting(lens("$.*.*"));

    ProjectionResult<Basket> result = writer.beginObject()
        .key("ignored").add("outer value")
        .key("nested").beginObject()
          .key("foo").add("nested foo value")
          .key("bar").add("nested bar value")
        .end()
        .end()
        .getResult();

    assertThat(result.asList(), contains(Basket.ofString("nested foo value"), Basket.ofString("nested bar value")));
  }

  @Test
  public void projectSecondItem() {
    Filter<ProjectionResult<Basket>> writer = Filters.projecting(lens("$[1]"));

    ProjectionResult<Basket> result = writer.beginArray()
        .add("first")
        .add("second")
        .add("third")
        .end()
        .getResult();

    assertThat(result.getSingle().asString(), equalTo("second"));
  }

  @Test
  public void projectSecondAndThirdItems() {
    Filter<ProjectionResult<Basket>> writer = Filters.projecting(lens("$[1, 2]"));

    ProjectionResult<Basket> result = writer.beginArray()
        .add("first")
        .add("second")
        .add("third")
        .end()
        .getResult();

    assertThat(result.asList(), contains(Basket.ofString("second"), Basket.ofString("third")));
  }

  @Test
  public void projectAllItems() {
    Filter<ProjectionResult<Basket>> writer = Filters.projecting(lens("$[*]"));

    ProjectionResult<Basket> result = writer.beginArray()
        .add("first")
        .add("second")
        .add("third")
        .end()
        .getResult();

    assertThat(result.asList(), contains(Basket.ofString("first"), Basket.ofString("second"), Basket.ofString("third")));
  }

  @Test
  public void projectMatchingStrings() {
    BasketPredicate shouldBeRewritten = BasketPredicates.isString(new ValuePredicate<String>() {
      @Override
      public boolean test(String value) {
        return value.contains("project me");
      }
    });

    Filter<ProjectionResult<Basket>> writer = Filters.projecting(lens("$[?]", shouldBeRewritten));

    ProjectionResult<Basket> result = writer.beginArray()
        .add("a (project me)")
        .add("b")
        .add("c (project me)")
        .end()
        .getResult();

    assertThat(result.asList(), contains(Basket.ofString("a (project me)"), Basket.ofString("c (project me)")));
  }

  @Test
  public void projectDeepScannedStrings() {
    Filter<ProjectionResult<Basket>> writer = Filters.projecting(
        lens("$..nested.bar"));

    ProjectionResult<Basket> result = writer.beginObject()
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
      .getResult();

    assertThat(result.asList(), contains(Basket.ofString("bar 1"), Basket.ofString("bar 2"), Basket.ofString("bar 3")));
  }

  @Test
  public void projectDeepScannedMatchingStrings() {
    BasketPredicate isFlaggedForRewrite = lens("@.project").isTrue();

    Filter<ProjectionResult<Basket>> writer = Filters.projecting(
        lens("$..nested[?].value", isFlaggedForRewrite));

    ProjectionResult<Basket> result = writer.beginObject()
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
        .getResult();

    assertThat(result.asList(), contains(Basket.ofString("project me"), Basket.ofString("and me")));
  }

  @Test
  public void projectMatchingObjects() {
    BasketPredicate isFlaggedForRewrite = lens("@.match").isTrue();

    Filter<ProjectionResult<Basket>> writer = Filters.projecting(
        lens("$[?]..value", isFlaggedForRewrite));

    ProjectionResult<Basket> result = writer.beginArray()
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
        .getResult();

    assertThat(result.asList(), contains(Basket.ofString("a"), Basket.ofString("b")));
  }

}