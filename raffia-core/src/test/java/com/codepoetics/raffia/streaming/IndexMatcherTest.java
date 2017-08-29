package com.codepoetics.raffia.streaming;

import com.codepoetics.raffia.paths.PathSegment;
import com.codepoetics.raffia.paths.PathSegmentMatchResult;
import com.codepoetics.raffia.paths.segments.PathSegments;
import com.codepoetics.raffia.writers.BasketWriter;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IndexMatcherTest {

  public static final class Index {

    private enum IndexState {
      NOT_STARTED,
      ARRAY_INDEX,
      OBJECT_UNKEYED,
      OBJECT_KEY
    }

    private IndexState state = IndexState.NOT_STARTED;
    private int arrayIndex = -1;
    private String key = null;

    public PathSegmentMatchResult matches(PathSegment segment) {
      switch (state) {
        case ARRAY_INDEX:
          return segment.matchesIndex(arrayIndex);
        case OBJECT_KEY:
          return segment.matchesKey(key);
        default:
          return PathSegmentMatchResult.UNMATCHED;
      }
    }

    public boolean isStarted() {
      return state != IndexState.NOT_STARTED;
    }

    public boolean readyForKey() {
      return state == IndexState.OBJECT_KEY;
    }

    public boolean readyForValue() {
      return state == IndexState.ARRAY_INDEX || state == IndexState.OBJECT_KEY;
    }

    public void startArray() {
      if (state != IndexState.NOT_STARTED) {
        throw new IllegalStateException("Tried to start array while in state " + state);
      }
      arrayIndex = 0;
      state = IndexState.ARRAY_INDEX;
    }

    public void startObject() {
      if (state != IndexState.NOT_STARTED) {
        throw new IllegalStateException("Tried to start object while in state " + state);
      }
      key = "";
      state = IndexState.OBJECT_UNKEYED;
    }

    public void key(String key) {
      if (state != IndexState.OBJECT_UNKEYED) {
        throw new IllegalStateException("Tried to set key while in state " + state);
      }
      this.key = key;
      state = IndexState.OBJECT_KEY;
    }

    public void advance() {
      switch(state) {
        case ARRAY_INDEX: {
          arrayIndex += 1;
          return;
        }
        case OBJECT_KEY: {
          key = null;
          state = IndexState.OBJECT_UNKEYED;
          return;
        }
        default:
          throw new IllegalStateException("Tried to advance while in state " + state);
      }
    }

    public void end() {
      key = null;
      arrayIndex = -1;
      state = IndexState.NOT_STARTED;
    }

    @Override
    public String toString() {
      switch (state) {
        case NOT_STARTED:
          return "?";
        case OBJECT_KEY:
          return "." + key;
        case OBJECT_UNKEYED:
          return ".?";
        case ARRAY_INDEX:
          return "[" + arrayIndex + "]";
        default:
          throw new IllegalStateException();
      }
    }
  }

  public static final class IndexTrail implements BasketWriter<IndexTrail> {

    private List<Index> indices = new ArrayList<>();
    {
      indices.add(new Index());
    }

    private int ptr = 0;

    public PathSegmentMatchResult matches(PathSegment pathSegment) {
      return currentIndex().matches(pathSegment);
    }

    private Index currentIndex() {
      return indices.get(ptr);
    }

    private Index nextIndex() {
      if (ptr++ == indices.size()) {
        indices.add(new Index());
      }
      return currentIndex();
    }

    @Override
    public IndexTrail beginObject() {
      Index current = currentIndex();
      if (current.isStarted()) {
        nextIndex().startObject();
      } else {
        current.startObject();
      }
      return this;
    }

    @Override
    public IndexTrail beginArray() {
      Index current = currentIndex();
      if (current.isStarted()) {
        nextIndex().startArray();
      } else {
        current.startArray();
      }
      return this;
    }

    @Override
    public IndexTrail end() {
      currentIndex().end();
      if (ptr > 0) {
        ptr--;
        return advance();
      }

      return this;
    }

    private IndexTrail advance() {
      currentIndex().advance();
      return this;
    }

    @Override
    public IndexTrail key(String key) {
      currentIndex().key(key);
      return this;
    }

    @Override
    public IndexTrail add(String value) {
      return advance();
    }

    @Override
    public IndexTrail add(BigDecimal value) {
      return advance();
    }

    @Override
    public IndexTrail add(boolean value) {
      return advance();
    }

    @Override
    public IndexTrail addNull() {
      return advance();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i<=ptr; i++) {
        sb.append(indices.get(i));
      }
      return sb.toString();
    }
  }

  public static final class IndexMatcher implements BasketWriter<IndexMatcher> {

    public static IndexMatcher create(List<PathSegment> segments) {
      return new IndexMatcher(segments, new IndexTrail());
    }

    private final List<PathSegment> segments;
    private final IndexTrail indexTrail;
    private int segmentIndex = 0;

    private IndexMatcher(List<PathSegment> segments, IndexTrail indexTrail) {
      this.segments = segments;
      this.indexTrail = indexTrail;
    }

    public boolean matches() {
      return segments.isEmpty()
          || indexTrail.matches(segments.get(segmentIndex)) == (PathSegmentMatchResult.MATCHED_BOUND);
    }

    @Override
    public IndexMatcher beginObject() {
      indexTrail.beginObject();
      return this;
    }

    @Override
    public IndexMatcher beginArray() {
      indexTrail.beginArray();
      return this;
    }

    @Override
    public IndexMatcher end() {
      indexTrail.end();
      return this;
    }

    @Override
    public IndexMatcher key(String key) {
      indexTrail.key(key);
      return this;
    }

    @Override
    public IndexMatcher add(String value) {
      indexTrail.add(value);
      return this;
    }

    @Override
    public IndexMatcher add(BigDecimal value) {
      indexTrail.add(value);
      return this;
    }

    @Override
    public IndexMatcher add(boolean value) {
      indexTrail.add(value);
      return this;
    }

    @Override
    public IndexMatcher addNull() {
      indexTrail.addNull();
      return this;
    }
  }

  @Test
  public void emptyIndexMatcherAlwaysMatches() {
    IndexMatcher indexMatcher = IndexMatcher.create(Collections.<PathSegment>emptyList());

    assertTrue(indexMatcher.matches());
  }

  @Test
  public void indexMatcherWithWildcardPathSegmentMatchesAllArrayItems() {
    IndexMatcher indexMatcher = IndexMatcher.create(Collections.singletonList(PathSegments.INSTANCE.ofWildcard()));

    assertFalse(indexMatcher.matches());
    indexMatcher.beginArray();

    assertTrue(indexMatcher.matches());
    indexMatcher.add("first value");

    assertTrue(indexMatcher.matches());
    indexMatcher.add("second value");

    assertTrue(indexMatcher.matches());
    indexMatcher.end();

    assertFalse(indexMatcher.matches());
  }

  @Test
  public void indexMatcherWithArrayIndexPathSegmentMatchesIndexedArrayItems() {
    IndexMatcher indexMatcher = IndexMatcher.create(Collections.singletonList(PathSegments.INSTANCE.ofArrayIndices(0, 2)));

    assertFalse(indexMatcher.matches());

    indexMatcher.beginArray();

    assertTrue(indexMatcher.matches());
    indexMatcher.add("first value");

    assertFalse(indexMatcher.matches());
    indexMatcher.add("second value");

    assertTrue(indexMatcher.matches());
    indexMatcher.add("third value");

    assertFalse(indexMatcher.matches());
    indexMatcher.end();
    assertFalse(indexMatcher.matches());
  }
}
