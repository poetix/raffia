package com.codepoetics.raffia;

import com.codepoetics.raffia.paths.segments.PathSegments;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PathSegmentsTests {

  @Test
  public void singleKeyRepresentation() {
    assertThat(PathSegments.INSTANCE.ofObjectKey("foo").representation(), equalTo(".foo"));
  }

  @Test
  public void multiKeyRepresentation() {
    assertThat(PathSegments.INSTANCE.ofObjectKeys("foo", "bar").representation(), equalTo("['foo', 'bar']"));
  }

  @Test
  public void singleIndexRepresentation() {
    assertThat(PathSegments.INSTANCE.ofArrayIndex(1).representation(), equalTo("[1]"));
  }

  @Test
  public void multiIndexRepresentation() {
    assertThat(PathSegments.INSTANCE.ofArrayIndices(1, 2).representation(), equalTo("[1, 2]"));
  }

}
