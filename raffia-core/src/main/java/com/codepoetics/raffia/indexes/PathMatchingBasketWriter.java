package com.codepoetics.raffia.indexes;

import com.codepoetics.raffia.api.*;
import com.codepoetics.raffia.baskets.Baskets;
import com.codepoetics.raffia.visitors.Visitors;
import com.codepoetics.raffia.writers.Writers;

import java.math.BigDecimal;

public abstract class PathMatchingBasketWriter<T extends BasketWriter<T>> implements BasketWriter<PathMatchingBasketWriter<T>> {

  public static <T extends BasketWriter<T>> PathMatchingBasketWriter<T> with(Path path, T target, Visitor<Basket> transformer) {
    return new MatchSeekingBasketWriter<>(target, transformer, null, path, IndexTrails.empty());
  }

  protected final T target;

  protected PathMatchingBasketWriter(T target) {
    this.target = target;
  }

  public T getTarget() {
    return target;
  }

  protected abstract PathMatchingBasketWriter<T> withTarget(T newTarget);

  private static final class PassThroughWriter<T extends BasketWriter<T>> extends PathMatchingBasketWriter<T> {
    private final PathMatchingBasketWriter<T> parent;

    private PassThroughWriter(T target, PathMatchingBasketWriter<T> parent) {
      super(target);
      this.parent = parent;
    }

    @Override
    protected PathMatchingBasketWriter<T> withTarget(T newTarget) {
      return new PassThroughWriter<>(newTarget, parent);
    }

    @Override
    public PathMatchingBasketWriter<T> beginObject() {
      return new PassThroughWriter<T>(target.beginObject(), this);
    }

    @Override
    public PathMatchingBasketWriter<T> beginArray() {
      return new PassThroughWriter<T>(target.beginArray(), this);
    }

    @Override
    public PathMatchingBasketWriter<T> end() {
      return parent.withTarget(target.end());
    }

    @Override
    public PathMatchingBasketWriter<T> key(String key) {
      return withTarget(target.key(key));
    }

    @Override
    public PathMatchingBasketWriter<T> add(String value) {
      return withTarget(target.add(value));
    }

    @Override
    public PathMatchingBasketWriter<T> add(BigDecimal value) {
      return withTarget(target.add(value));
    }

    @Override
    public PathMatchingBasketWriter<T> add(boolean value) {
      return withTarget(target.add(value));
    }

    @Override
    public PathMatchingBasketWriter<T> addNull() {
      return withTarget(target.addNull());
    }
  }

  private static abstract class WeavingWriter<T extends BasketWriter<T>> extends PathMatchingBasketWriter<T> {

    protected WeavingWriter(T target) {
      super(target);
    }

    protected abstract PathMatchingBasketWriter<T> withWeaver(BasketWeavingWriter newWriter);

  }

  private static final class TransformingWriter<T extends BasketWriter<T>> extends WeavingWriter<T> {

    private final PathMatchingBasketWriter<T> parent;
    private final Visitor<Basket> transformer;
    private final BasketWeavingWriter weaver;

    private TransformingWriter(T target, PathMatchingBasketWriter<T> parent, Visitor<Basket> transformer, BasketWeavingWriter weaver) {
      super(target);
      this.parent = parent;
      this.transformer = transformer;
      this.weaver = weaver;
    }

    @Override
    protected PathMatchingBasketWriter<T> withTarget(T newTarget) {
      return new TransformingWriter<>(newTarget, parent, transformer, weaver);
    }

    @Override
    protected PathMatchingBasketWriter<T> withWeaver(BasketWeavingWriter newWriter) {
      return new TransformingWriter<>(target, parent, transformer, newWriter);
    }

    @Override
    public PathMatchingBasketWriter<T> beginObject() {
      return new TransformingWriter<>(target, this, transformer, weaver.beginObject());
    }

    @Override
    public PathMatchingBasketWriter<T> beginArray() {
      return new TransformingWriter<>(target, this, transformer, weaver.beginArray());
    }

    @Override
    public PathMatchingBasketWriter<T> end() {
      Basket woven = weaver.end().weave().visit(transformer);
      T newTarget = woven.visit(Visitors.writingTo(target));
      return parent.withTarget(newTarget);
    }

    @Override
    public PathMatchingBasketWriter<T> key(String key) {
      return withWeaver(weaver.key(key));
    }

    @Override
    public PathMatchingBasketWriter<T> add(String value) {
      return withWeaver(weaver.add(value));
    }

    @Override
    public PathMatchingBasketWriter<T> add(BigDecimal value) {
      return withWeaver(weaver.add(value));
    }

    @Override
    public PathMatchingBasketWriter<T> add(boolean value) {
      return withWeaver(weaver.add(value));
    }

    @Override
    public PathMatchingBasketWriter<T> addNull() {
      return withWeaver(weaver.addNull());
    }
  }

  private static final class InnerWeavingWriter<T extends BasketWriter<T>> extends WeavingWriter<T> {

    private final WeavingWriter<T> parent;
    private final BasketWeavingWriter weaver;

    private InnerWeavingWriter(T target, WeavingWriter<T> parent, BasketWeavingWriter weaver) {
      super(target);
      this.parent = parent;
      this.weaver = weaver;
    }

    @Override
    protected PathMatchingBasketWriter<T> withTarget(T newTarget) {
      return new InnerWeavingWriter<>(newTarget, parent, weaver);
    }

    @Override
    protected PathMatchingBasketWriter<T> withWeaver(BasketWeavingWriter newWeaver) {
      return new InnerWeavingWriter<>(target, parent, newWeaver);
    }

    @Override
    public PathMatchingBasketWriter<T> beginObject() {
      return new InnerWeavingWriter<>(target, this, weaver.beginObject());
    }

    @Override
    public PathMatchingBasketWriter<T> beginArray() {
      return new InnerWeavingWriter<>(target, this, weaver.beginArray());
    }

    @Override
    public PathMatchingBasketWriter<T> end() {
      return parent.withWeaver(weaver.end());
    }

    @Override
    public PathMatchingBasketWriter<T> key(String key) {
      return withWeaver(weaver.key(key));
    }

    @Override
    public PathMatchingBasketWriter<T> add(String value) {
      return withWeaver(weaver.add(value));
    }

    @Override
    public PathMatchingBasketWriter<T> add(BigDecimal value) {
      return withWeaver(weaver.add(value));
    }

    @Override
    public PathMatchingBasketWriter<T> add(boolean value) {
      return withWeaver(weaver.add(value));
    }

    @Override
    public PathMatchingBasketWriter<T> addNull() {
      return withWeaver(weaver.addNull());
    }
  }

  private static final class MatchSeekingBasketWriter<T extends BasketWriter<T>> extends PathMatchingBasketWriter<T> {

    private final Visitor<Basket> transformer;
    private final MatchSeekingBasketWriter<T> parent;
    private final Path pathToMatch;
    private final IndexTrail indexTrail;

    private MatchSeekingBasketWriter(T target, Visitor<Basket> transformer, MatchSeekingBasketWriter<T> parent, Path pathToMatch, IndexTrail indexTrail) {
      super(target);
      this.transformer = transformer;
      this.parent = parent;
      this.pathToMatch = pathToMatch;
      this.indexTrail = indexTrail;
    }

    @Override
    protected PathMatchingBasketWriter<T> withTarget(T newTarget) {
      return new MatchSeekingBasketWriter<>(newTarget, transformer, parent, pathToMatch, indexTrail);
    }

    private PathMatchingBasketWriter<T> enter(T newTarget, Path newPathToMatch, IndexTrail newTrail) {
      return new MatchSeekingBasketWriter<>(newTarget, transformer, this, newPathToMatch, newTrail);
    }

    private MatchSeekingBasketWriter<T> advance(T newTarget) {
      return new MatchSeekingBasketWriter<>(newTarget, transformer, parent, pathToMatch, indexTrail.advance());
    }

    @Override
    public PathMatchingBasketWriter<T> beginObject() {
      if (pathToMatch.isEmpty()) {
        return new TransformingWriter<T>(target, this, transformer, Writers.weaving().beginObject());
      }

      if (indexTrail.isEmpty()) {
        return enter(target.beginObject(), pathToMatch, indexTrail.enterObject());
      }

      switch (indexTrail.head().isMatchedBy(pathToMatch.head())) {
        case UNMATCHED: return new PassThroughWriter<>(target.beginObject(),this);
        case MATCHED_UNBOUND: return enter(target.beginObject(), pathToMatch, indexTrail.enterObject());
        default: return enter(target.beginObject(), pathToMatch.tail(), indexTrail.enterObject());
      }
    }

    @Override
    public PathMatchingBasketWriter<T> beginArray() {
      if (pathToMatch.isEmpty()) {
        return new TransformingWriter<T>(target, this, transformer, Writers.weaving().beginArray());
      }

      if (indexTrail.isEmpty()) {
        return enter(target.beginArray(), pathToMatch, indexTrail.enterArray());
      }

      switch (indexTrail.head().isMatchedBy(pathToMatch.head())) {
        case UNMATCHED: return new PassThroughWriter<>(target.beginArray(),this);
        case MATCHED_UNBOUND: return enter(target.beginArray(), pathToMatch, indexTrail.enterArray());
        default: return enter(target.beginArray(), pathToMatch.tail(), indexTrail.enterArray());
      }
    }

    @Override
    public PathMatchingBasketWriter<T> end() {
      if (parent == null) {
        throw new IllegalStateException("end() called without matching beginObject() or beginArray()");
      }
      return parent.advance(target.end());
    }

    @Override
    public PathMatchingBasketWriter<T> key(String key) {
      return new MatchSeekingBasketWriter<>(target.key(key), transformer, parent, pathToMatch, indexTrail.atKey(key));
    }

    private PathMatchingBasketWriter<T> writeTransformed(Basket basket) {
      return new MatchSeekingBasketWriter<>(
          basket.visit(transformer).visit(Visitors.writingTo(target)),
          transformer,
          parent,
          pathToMatch,
          indexTrail.advance());
    }

    @Override
    public PathMatchingBasketWriter<T> add(String value) {
      return indexTrail.isMatchedBy(pathToMatch)
        ? writeTransformed(Baskets.ofString(value))
        : advance(target.add(value));
    }

    @Override
    public PathMatchingBasketWriter<T> add(BigDecimal value) {
      return indexTrail.isMatchedBy(pathToMatch)
          ? writeTransformed(Baskets.ofNumber(value))
          : advance(target.add(value));
    }

    @Override
    public PathMatchingBasketWriter<T> add(boolean value) {
      return indexTrail.isMatchedBy(pathToMatch)
          ? writeTransformed(Baskets.ofBoolean(value))
          : advance(target.add(value));
    }

    @Override
    public PathMatchingBasketWriter<T> addNull() {
      return indexTrail.isMatchedBy(pathToMatch)
          ? writeTransformed(Baskets.ofNull())
          : advance(target.addNull());
    }
  }
}
