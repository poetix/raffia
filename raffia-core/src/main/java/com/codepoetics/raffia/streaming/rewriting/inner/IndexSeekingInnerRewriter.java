package com.codepoetics.raffia.streaming.rewriting.inner;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.baskets.Visitor;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.paths.PathSegment;
import com.codepoetics.raffia.paths.PathSegmentMatchResult;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.rewriting.StreamingRewriter;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

abstract class IndexSeekingInnerRewriter<T extends BasketWriter<T>> extends InnerRewriter<T> {

  static <T extends BasketWriter<T>> StreamingRewriter<T> seekingArrayIndex(T target, Path path, StreamingRewriter<T> parent, Visitor<Basket> updater) {
    return new ArrayIndexSeekingInnerRewriter<>(target, path, parent, updater, 0);
  }

  static <T extends BasketWriter<T>> StreamingRewriter<T> seekingObjectKey(T target, Path path, StreamingRewriter<T> parent, Visitor<Basket> updater) {
    return new ObjectKeySeekingInnerRewriter<>(target, path, parent, updater, null);
  }

  protected final Path path;
  protected final Visitor<Basket> updater;

  protected IndexSeekingInnerRewriter(T target, Path path, StreamingRewriter<T> parent, Visitor<Basket> updater) {
    super(target, parent);
    this.path = path;
    this.updater = updater;
  }

  private FilteringWriter<T> passThrough(T newTarget) {
    return new PassThroughContentsRewriter<>(newTarget, this);
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

  protected boolean isBoundLeaf() {
    return indexMatches().equals(PathSegmentMatchResult.MATCHED_BOUND) && path.tail().isEmpty();
  }

  private FilteringWriter<T> update(Basket value) {
    return advance(value.visit(updater).visit(Visitors.writingTo(getTarget())));
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return isBoundLeaf()
      ? update(updater.visitString(value))
      : advance(getTarget().add(value));
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return isBoundLeaf()
        ? update(updater.visitNumber(value))
        : advance(getTarget().add(value));
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return isBoundLeaf()
        ? update(updater.visitBoolean(value))
        : advance(getTarget().add(value));
  }

  @Override
  public FilteringWriter<T> addNull() {
    return isBoundLeaf()
        ? update(updater.visitNull())
        : advance(getTarget().addNull());
  }

  private static final class ObjectKeySeekingInnerRewriter<T extends BasketWriter<T>> extends IndexSeekingInnerRewriter<T> {

    private final String key;

    ObjectKeySeekingInnerRewriter(T target, Path path, StreamingRewriter<T> parent, Visitor<Basket> updater, String key) {
      super(target, path, parent, updater);
      this.key = key;
    }

    @Override
    public FilteringWriter<T> advance(T newTarget) {
      return new ObjectKeySeekingInnerRewriter<>(newTarget, path, parent, updater, null);
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
      return new ObjectKeySeekingInnerRewriter<>(getTarget().key(newKey), path, parent, updater, newKey);
    }

  }

  private static final class ArrayIndexSeekingInnerRewriter<T extends BasketWriter<T>> extends IndexSeekingInnerRewriter<T> {

    private final int index;

    ArrayIndexSeekingInnerRewriter(T target, Path path, StreamingRewriter<T> parent, Visitor<Basket> updater, int index) {
      super(target, path, parent, updater);
      this.index = index;
    }

    @Override
    public FilteringWriter<T> advance(T newTarget) {
      return new ArrayIndexSeekingInnerRewriter<>(newTarget, path, parent, updater, index + 1);
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
