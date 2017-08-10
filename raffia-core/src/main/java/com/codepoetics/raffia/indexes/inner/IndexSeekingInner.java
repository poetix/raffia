package com.codepoetics.raffia.indexes.inner;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.indexes.FilteringWriter;
import com.codepoetics.raffia.indexes.MatchSeekingUpdater;
import com.codepoetics.raffia.visitors.Visitors;

import java.math.BigDecimal;

abstract class IndexSeekingInner<T extends BasketWriter<T>> extends Inner<T> {

  static <T extends BasketWriter<T>> MatchSeekingUpdater<T> seekingArrayIndex(T target, Path path, MatchSeekingUpdater<T> parent, Visitor<Basket> updater) {
    return new ArrayIndexSeekingInner<>(target, path, parent, updater, 0);
  }

  static <T extends BasketWriter<T>> MatchSeekingUpdater<T> seekingObjectKey(T target, Path path, MatchSeekingUpdater<T> parent, Visitor<Basket> updater) {
    return new ObjectKeySeekingInner<>(target, path, parent, updater, null);
  }

  protected final Path path;
  protected final Visitor<Basket> updater;

  protected IndexSeekingInner(T target, Path path, MatchSeekingUpdater<T> parent, Visitor<Basket> updater) {
    super(target, parent);
    this.path = path;
    this.updater = updater;
  }

  private FilteringWriter<T> passThrough(T newTarget) {
    return new PassThroughContentsUpdater<>(newTarget, this);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    switch (indexMatches()) {
      case UNMATCHED:
        return passThrough(getTarget().beginObject());
      case MATCHED_BOUND:
        return startObject(getTarget(), path.tail(), this, updater);
      case MATCHED_UNBOUND:
        return startObject(getTarget(), path, this, updater);
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public FilteringWriter<T> beginArray() {
    switch (indexMatches()) {
      case UNMATCHED:
        return passThrough(getTarget().beginArray());
      case MATCHED_BOUND:
        return startArray(getTarget(), path.tail(), this, updater);
      case MATCHED_UNBOUND:
        return startArray(getTarget(), path, this, updater);
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

  private FilteringWriter<T> update(Basket value) {
    return advance(value.visit(updater).visit(Visitors.writingTo(getTarget())));
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return indexIsBound()
      ? update(updater.visitString(value))
      : advance(getTarget().add(value));
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return indexIsBound()
        ? update(updater.visitNumber(value))
        : advance(getTarget().add(value));
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return indexIsBound()
        ? update(updater.visitBoolean(value))
        : advance(getTarget().add(value));
  }

  @Override
  public FilteringWriter<T> addNull() {
    return indexIsBound()
        ? update(updater.visitNull())
        : advance(getTarget().addNull());
  }

  private static final class ObjectKeySeekingInner<T extends BasketWriter<T>> extends IndexSeekingInner<T> {

    private final String key;

    ObjectKeySeekingInner(T target, Path path, MatchSeekingUpdater<T> parent, Visitor<Basket> updater, String key) {
      super(target, path, parent, updater);
      this.key = key;
    }

    @Override
    public FilteringWriter<T> advance(T newTarget) {
      return new ObjectKeySeekingInner<>(newTarget, path, parent, updater, null);
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
      return new ObjectKeySeekingInner<>(getTarget().key(newKey), path, parent, updater, newKey);
    }

  }

  private static final class ArrayIndexSeekingInner<T extends BasketWriter<T>> extends IndexSeekingInner<T> {

    private final int index;

    ArrayIndexSeekingInner(T target, Path path, MatchSeekingUpdater<T> parent, Visitor<Basket> updater, int index) {
      super(target, path, parent, updater);
      this.index = index;
    }

    @Override
    public FilteringWriter<T> advance(T newTarget) {
      return new ArrayIndexSeekingInner<>(newTarget, path, parent, updater, index + 1);
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
