package com.codepoetics.raffia;

import com.codepoetics.raffia.operations.ProjectionResult;
import org.hamcrest.Matchers;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertTrue;

public class ProjectionResultsTest {

  private ProjectionResult<String> empty() {
    return ProjectionResult.Companion.empty();
  }

  private ProjectionResult<String> singleton(String value) {
    return ProjectionResult.Companion.ofSingle(value);
  }

  private ProjectionResult<String> multiple(String first, String second) {
    return singleton(first).add(singleton(second));
  }

  private ProjectionResult<String> nested(String first, String second, String third, String fourth) {
    return multiple(first, second).add(multiple(third, fourth));
  }

  @Test
  public void emptyAndEmptyIsEmpty() {
    assertTrue(empty().add(empty()).isEmpty());
  }

  @Test
  public void emptyAndSingletonIsSingleton() {
    assertThat(empty().add(singleton("a")), Matchers.contains("a"));
  }

  @Test
  public void singletonAndEmptyIsSingleton() {
    assertThat(singleton("a").add(empty()), Matchers.contains("a"));
  }

  @Test
  public void singletonAndSingletonIsMultiple() {
    assertThat(singleton("a").add(singleton("b")), Matchers.contains("a", "b"));
  }

  @Test
  public void multipleAndEmptyIsMultiple() {
    assertThat(multiple("a", "b").add(empty()), contains("a", "b"));
  }
  @Test
  public void singletonAndMultipleIsMultiple() {
    assertThat(singleton("a").add(multiple("b", "c")), Matchers.contains("a", "b", "c"));
  }

  @Test
  public void multipleAndSingletonIsMultiple() {
    assertThat(multiple("a", "b").add(singleton("c")), Matchers.contains("a", "b", "c"));
  }

  @Test
  public void associativity() {
    assertEquals(multiple("a", "b").add(singleton("c")), singleton("a").add(multiple("b","c")));
  }

  @Test
  public void multipleAndMultipleIsNested() {
    assertThat(multiple("a", "b").add(multiple("c", "d")),
        contains("a", "b", "c", "d"));
  }

  @Test
  public void emptyAndNestedIsNested() {
    assertThat(empty().add(nested("a", "b", "c", "d")),
        contains("a", "b", "c", "d"));
  }

  @Test
  public void nestedAndEmptyIsNested() {
    assertThat(nested("a", "b", "c", "d").add(empty()),
        contains("a", "b", "c", "d"));
  }

  @Test
  public void singletonAndNestedIsNested() {
    assertThat(singleton("a").add(nested("b", "c", "d", "e")),
        contains("a", "b", "c", "d", "e"));
  }

  @Test
  public void nestedAndSingletonIsNested() {
    assertThat(nested("a","b", "c", "d").add(singleton("e")),
        contains("a", "b", "c", "d", "e"));
  }

  @Test
  public void multipleAndNestedIsNested() {
    assertThat(multiple("a", "b").add(nested("c", "d", "e", "f")),
        contains("a", "b", "c", "d", "e", "f"));
  }

  @Test
  public void nestedAndMultipleIsNested() {
    assertThat(nested("a", "b", "c", "d")
            .add(multiple("e", "f")),
        contains("a", "b", "c", "d", "e", "f"));
  }

  @Test
  public void nestedAndNestedIsNested() {
    assertThat(nested("a", "b", "c", "d")
            .add(nested("e", "f", "g", "h")),
        contains("a", "b", "c", "d", "e", "f", "g", "h"));
  }


}
