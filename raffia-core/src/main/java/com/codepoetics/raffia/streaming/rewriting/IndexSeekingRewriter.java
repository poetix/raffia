package com.codepoetics.raffia.streaming.rewriting;

import com.codepoetics.raffia.baskets.Basket;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.paths.PathSegment;
import com.codepoetics.raffia.paths.PathSegmentMatchResult;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

abstract class IndexSeekingRewriter<T extends BasketWriter<T>> extends StreamingRewriter<T> {

  static <T extends BasketWriter<T>> StreamingRewriter<T> seekingArrayIndex(T target, Path path, FilteringWriter<T> parent, Updater updater) {
    return new ArrayIndexSeekingRewriter<>(target, path, parent, updater, 0);
  }

  static <T extends BasketWriter<T>> StreamingRewriter<T> seekingObjectKey(T target, Path path, FilteringWriter<T> parent, Updater updater) {
    return new ObjectKeySeekingRewriter<>(target, path, parent, updater, null);
  }

  protected final Path path;

  IndexSeekingRewriter(T target, Path path, FilteringWriter<T> parent, Updater updater) {
    super(target, parent, updater);
    this.path = path;
  }

  private FilteringWriter<T> passThrough(T newTarget) {
    return new PassThroughContentsRewriter<>(newTarget, this);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    switch (indexMatches()) {
      case UNMATCHED:
        return passThrough(target.beginObject());
      case MATCHED_BOUND:
        return startObject(target, path.tail(), this, updater);
      case MATCHED_UNBOUND:
        return startObject(target, path, this, updater);
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public FilteringWriter<T> beginArray() {
    switch (indexMatches()) {
      case UNMATCHED:
        return passThrough(target.beginArray());
      case MATCHED_BOUND:
        return startArray(target, path.tail(), this, updater);
      case MATCHED_UNBOUND:
        return startArray(target, path, this, updater);
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public FilteringWriter<T> end() {
    return parent.advance(target.end());
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
    return advance(updater.update(value).visit(Visitors.writingTo(target)));
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return isBoundLeaf()
      ? update(Basket.ofString(value))
      : advance(target.add(value));
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return isBoundLeaf()
        ? update(Basket.ofNumber(value))
        : advance(target.add(value));
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return isBoundLeaf()
        ? update(Basket.ofBoolean(value))
        : advance(target.add(value));
  }

  @Override
  public FilteringWriter<T> addNull() {
    return isBoundLeaf()
        ? update(Basket.ofNull())
        : advance(target.addNull());
  }

  private static final class ObjectKeySeekingRewriter<T extends BasketWriter<T>> extends IndexSeekingRewriter<T> {

    private String key;

    ObjectKeySeekingRewriter(T target, Path path, FilteringWriter<T> parent, Updater updater, String key) {
      super(target, path, parent, updater);
      this.key = key;
    }

    @Override
    public FilteringWriter<T> advance(T newTarget) {
      this.target = newTarget;
      this.key = null;
      return this;
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
      target = target.key(newKey);
      key = newKey;
      return this;
    }

  }

  private static final class ArrayIndexSeekingRewriter<T extends BasketWriter<T>> extends IndexSeekingRewriter<T> {

    private int index;

    ArrayIndexSeekingRewriter(T target, Path path, FilteringWriter<T> parent, Updater updater, int index) {
      super(target, path, parent, updater);
      this.index = index;
    }

    @Override
    public FilteringWriter<T> advance(T newTarget) {
      this.target = newTarget;
      this.index += 1;
      return this;
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
