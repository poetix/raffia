package com.codepoetics.raffia.streaming;

import com.codepoetics.raffia.lenses.Lens;
import com.codepoetics.raffia.operations.Updater;
import com.codepoetics.raffia.streaming.projecting.StreamingProjector;
import com.codepoetics.raffia.streaming.rewriting.StreamingRewriter;
import com.codepoetics.raffia.writers.BasketWeavingWriter;
import com.codepoetics.raffia.writers.BasketWriter;
import com.codepoetics.raffia.writers.Writers;

public final class StreamingWriters {

  private StreamingWriters() {
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> rewriting(Lens lens, T target, Updater updater) {
    return StreamingRewriter.start(target, lens.getPath(), updater);
  }

  public static <T extends BasketWriter<T>> FilteringWriter<T> filteringArray(Lens lens, T target) {
    return StreamingProjector.startArray(target, lens.getPath());
  }

  public static FilteringWriter<BasketWeavingWriter> projectingArray(Lens lens) {
    return filteringArray(lens, Writers.weaving());
  }
}
