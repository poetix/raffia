package com.codepoetics.raffia.streaming.projecting.inner;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.projecting.StreamingProjector;

import java.math.BigDecimal;

abstract class IndexSeekingInnerProjector<T extends BasketWriter<T>> extends InnerProjector<T> {

  static <T extends BasketWriter<T>> StreamingProjector<T> seekingArrayIndex(T target, Path path, StreamingProjector<T> parent) {
    return new ArrayIndexSeekingInnerProjector<>(target, path, parent, 0);
  }

  static <T extends BasketWriter<T>> StreamingProjector<T> seekingObjectKey(T target, Path path, StreamingProjector<T> parent) {
    return new ObjectKeySeekingInnerProjector<>(target, path, parent, null);
  }

  protected final Path path;

  protected IndexSeekingInnerProjector(T target, Path path, StreamingProjector<T> parent) {
    super(target, parent);
    this.path = path;
  }

  private FilteringWriter<T> ignoreAll(T newTarget) {
    return new IgnoreAllProjector<>(newTarget, this);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    switch (indexMatches()) {
      case UNMATCHED:
        return ignoreAll(getTarget());
      case MATCHED_BOUND:
        return startObject(getTarget(), path.tail(), this);
      case MATCHED_UNBOUND:
        return startObject(getTarget(), path, this);
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public FilteringWriter<T> beginArray() {
    switch (indexMatches()) {
      case UNMATCHED:
        return ignoreAll(getTarget());
      case MATCHED_BOUND:
        return startArray(getTarget(), path.tail(), this);
      case MATCHED_UNBOUND:
        return startArray(getTarget(), path, this);
      default:
        throw new IllegalStateException();
    }
  }

  private PathSegmentMatchResult indexMatches() {
    return indexMatches(path.head());
  }

  protected abstract PathSegmentMatchResult indexMatches(PathSegment pathHead);

  @Override
  public FilteringWriter<T> key(String key) {
    throw new IllegalStateException("key() called while writing array");
  }

  protected boolean indexIsBound() {
    return indexMatches().equals(PathSegmentMatchResult.MATCHED_BOUND);
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return indexIsBound()
      ? advance(getTarget().add(value))
      : ignore();
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return indexIsBound()
        ? advance(getTarget().add(value))
        : ignore();
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return indexIsBound()
        ? advance(getTarget().add(value))
        : ignore();
  }

  @Override
  public FilteringWriter<T> addNull() {
    return indexIsBound()
        ? advance(getTarget().addNull())
        : ignore();
  }

  private static final class ObjectKeySeekingInnerProjector<T extends BasketWriter<T>> extends IndexSeekingInnerProjector<T> {

    private final String key;

    ObjectKeySeekingInnerProjector(T target, Path path, StreamingProjector<T> parent, String key) {
      super(target, path, parent);
      this.key = key;
    }

    @Override
    public FilteringWriter<T> advance(T newTarget) {
      return new ObjectKeySeekingInnerProjector<>(newTarget, path, parent, null);
    }

    @Override
    protected PathSegmentMatchResult indexMatches(PathSegment pathHead) {
      return pathHead.matchesKey(key);
    }

    @Override
    public FilteringWriter<T> key(String newKey) {
      if (key != null) {
        throw new IllegalStateException("key() called twice");
      }
      return new ObjectKeySeekingInnerProjector<>(getTarget(), path, parent, newKey);
    }

  }

  private static final class ArrayIndexSeekingInnerProjector<T extends BasketWriter<T>> extends IndexSeekingInnerProjector<T> {

    private final int index;

    ArrayIndexSeekingInnerProjector(T target, Path path, StreamingProjector<T> parent, int index) {
      super(target, path, parent);
      this.index = index;
    }

    @Override
    public FilteringWriter<T> advance(T newTarget) {
      return new ArrayIndexSeekingInnerProjector<>(newTarget, path, parent, index + 1);
    }

    @Override
    protected PathSegmentMatchResult indexMatches(PathSegment pathHead) {
      return pathHead.matchesIndex(index);
    }

    @Override
    public FilteringWriter<T> key(String key) {
      throw new IllegalStateException("key() called while writing array");
    }

  }
}
