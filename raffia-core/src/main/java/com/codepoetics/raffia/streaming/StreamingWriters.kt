package com.codepoetics.raffia.streaming

import com.codepoetics.raffia.lenses.Lens
import com.codepoetics.raffia.java.api.Updater
import com.codepoetics.raffia.streaming.projecting.StreamingProjector
import com.codepoetics.raffia.streaming.rewriting.StreamingRewriter
import com.codepoetics.raffia.writers.BasketWeavingWriter
import com.codepoetics.raffia.writers.BasketWriter
import com.codepoetics.raffia.writers.Writers

object StreamingWriters {

    fun <T : BasketWriter<T>> rewriting(lens: Lens, target: T, updater: Updater): FilteringWriter<T> {
        return StreamingRewriter.start(target, lens.path, updater)
    }

    fun <T : BasketWriter<T>> filteringArray(lens: Lens, target: T): FilteringWriter<T> {
        return StreamingProjector.startArray(target, lens.path)
    }

    fun projectingArray(lens: Lens): FilteringWriter<BasketWeavingWriter> {
        return filteringArray(lens, Writers.weaving())
    }
}
