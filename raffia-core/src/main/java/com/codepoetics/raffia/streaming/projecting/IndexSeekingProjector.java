package com.codepoetics.raffia.streaming.projecting;

import com.codepoetics.raffia.paths.Path;
import com.codepoetics.raffia.paths.PathSegment;
import com.codepoetics.raffia.paths.PathSegmentMatchResult;
import com.codepoetics.raffia.streaming.FilteringWriter;
import com.codepoetics.raffia.streaming.IgnoreAllFilter;
import com.codepoetics.raffia.writers.BasketWriter;

import java.math.BigDecimal;

public abstract class IndexSeekingProjector<T extends BasketWriter<T>> extends StreamingProjector<T> {

  public static <T extends BasketWriter<T>> FilteringWriter<T> seekingArrayIndex(T target, Path path, FilteringWriter<T> parent) {
    return new ArrayIndexSeekingInnerProjector<>(target, path, parent, 0);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> seekingObjectKey(T target, Path path, FilteringWriter<T> parent) {
    return new ObjectKeySeekingInnerProjector<>(target, path, parent, null);
  }

  protected final Path path;

  protected IndexSeekingProjector(T target, Path path, FilteringWriter<T> parent) {
    super(target, parent);
    this.parent = parent;
    this.path = path;
  }

  private FilteringWriter<T> ignoreAll() {
    return new IgnoreAllFilter<>(target,this);
  }

  @Override
  public FilteringWriter<T> beginObject() {
    switch (indexMatches()) {
      case UNMATCHED:
        return ignoreAll();
      case MATCHED_BOUND:
        return StreamingProjector.startObject(target, path.tail(), this);
      case MATCHED_UNBOUND:
        return seekingObjectKey(target, path, this);
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public FilteringWriter<T> beginArray() {
    switch (indexMatches()) {
      case UNMATCHED:
        return ignoreAll();
      case MATCHED_BOUND:
        return StreamingProjector.startArray(target, path.tail(),this);
      case MATCHED_UNBOUND:
        return seekingArrayIndex(target, path, this);
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public FilteringWriter<T> end() {
    if (parent == null) {
      throw new IllegalStateException("end() called while not writing array or object");
    }

    return parent.advance(target);
  }

  private PathSegmentMatchResult indexMatches() {
    return indexMatches(path.head());
  }

  protected abstract PathSegmentMatchResult indexMatches(PathSegment pathHead);

  private boolean isMatchingLeaf() {
    return indexMatches().equals(PathSegmentMatchResult.MATCHED_BOUND) && path.tail().isEmpty();
  }

  @Override
  public FilteringWriter<T> add(String value) {
    return isMatchingLeaf()
      ? advance(target.add(value))
      : ignore();
  }

  @Override
  public FilteringWriter<T> add(BigDecimal value) {
    return isMatchingLeaf()
        ? advance(target.add(value))
        : ignore();
  }

  @Override
  public FilteringWriter<T> add(boolean value) {
    return isMatchingLeaf()
        ? advance(target.add(value))
        : ignore();
  }

  @Override
  public FilteringWriter<T> addNull() {
    return isMatchingLeaf()
        ? advance(target.addNull())
        : ignore();
  }

  private static final class ObjectKeySeekingInnerProjector<T extends BasketWriter<T>> extends IndexSeekingProjector<T> {

    private String key;

    ObjectKeySeekingInnerProjector(T target, Path path, FilteringWriter<T> parent, String key) {
      super(target, path, parent);
      this.key = key;
    }

    @Override
    public FilteringWriter<T> advance(T newTarget) {
      key = null;
      return super.advance(newTarget);
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
      this.key = newKey;
      return this;
    }

  }

  private static final class ArrayIndexSeekingInnerProjector<T extends BasketWriter<T>> extends IndexSeekingProjector<T> {

    private int index;

    ArrayIndexSeekingInnerProjector(T target, Path path, FilteringWriter<T> parent, int index) {
      super(target, path, parent);
      this.index = index;
    }

    @Override
    public FilteringWriter<T> advance(T newTarget) {
      index += 1;
      return super.advance(newTarget);
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
