package com.codepoetics.raffia;

import com.codepoetics.raffia.operations.ProjectionResult;
import org.hamcrest.Matchers;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class ProjectionResultsTest {

  @Test
  public void emptyAndEmptyIsEmpty() {
    assertThat(ProjectionResult.empty().add(ProjectionResult.empty()).toList(), Matchers.hasSize(0));
  }

  @Test
  public void emptyAndSingletonIsSingleton() {
    assertEquals("a", ProjectionResult.<String>empty().add(ProjectionResult.ofSingle("a")).getSingle());

    assertThat(ProjectionResult.<String>empty().add(ProjectionResult.ofSingle("a")), Matchers.contains("a"));
  }

  @Test
  public void singletonAndEmptyIsSingleton() {
    assertEquals("a", ProjectionResult.ofSingle("a").add(ProjectionResult.<String>empty()).getSingle());
    assertThat(ProjectionResult.ofSingle("a").add(ProjectionResult.<String>empty()), Matchers.contains("a"));
  }

  @Test
  public void singletonAndSingletonIsMultiple() {
    assertThat(ProjectionResult.ofSingle("a").add(ProjectionResult.ofSingle("b")), Matchers.contains("a", "b"));
  }

  @Test
  public void singletonAndMultipleIsMultiple() {
    assertThat(ProjectionResult.ofSingle("a").add((ProjectionResult.ofSingle("b").add(ProjectionResult.ofSingle("c")))), Matchers.contains("a", "b", "c"));
  }

  @Test
  public void multipleAndSingletonIsMultiple() {
    assertThat(ProjectionResult.ofSingle("a").add(ProjectionResult.ofSingle("b").add(ProjectionResult.ofSingle("c"))), Matchers.contains("a", "b", "c"));
  }

  @Test
  public void multipleAndMultipleIsMultiple() {
    assertThat(ProjectionResult.ofSingle("a").add(ProjectionResult.ofSingle("b")
        .add(ProjectionResult.ofSingle("c").add(ProjectionResult.ofSingle("d")))),
        contains("a", "b", "c", "d"));
  }


}
