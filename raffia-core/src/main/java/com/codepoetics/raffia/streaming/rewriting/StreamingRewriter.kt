package com.codepoetics.raffia.streaming.rewriting

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.functions.Updater
import com.codepoetics.raffia.paths.Path
import com.codepoetics.raffia.streaming.FilteringWriter
import com.codepoetics.raffia.writers.BasketWriter
import com.codepoetics.raffia.writers.writeTo

abstract class StreamingRewriter<T : BasketWriter<T>> protected constructor(protected var target: T, protected val parent: FilteringWriter<T>?, protected val updater: Updater?) : FilteringWriter<T> {

    override fun advance(newTarget: T): FilteringWriter<T> {
        target = newTarget
        return this
    }

    override fun complete(): T = target

    protected fun updated(basket: Basket): FilteringWriter<T> = advance(updater!!.update(basket).writeTo(target))

    companion object {

        fun <T : BasketWriter<T>> start(target: T, path: Path, updater: Updater): FilteringWriter<T> =
                if (path.isEmpty) MatchedRewriter<T>(target, null, updater)
                else StructStartSeekingRewriter(target, path, updater)

        private fun makeConditionalUpdater(path: Path, updater: Updater): Updater = path.head().createItemUpdater(path.tail(), updater)

        fun <T : BasketWriter<T>> startArray(target: T, path: Path, parent: FilteringWriter<T>, updater: Updater): FilteringWriter<T> =
                if (path.isEmpty) MatchedRewriter<T>(target.beginArray(), parent, updater)
                else if (path.head().isConditional) PredicateMatchingRewriter(target.beginArray(), parent, makeConditionalUpdater(path, updater))
                else IndexSeekingRewriter.seekingArrayIndex(target.beginArray(), path, parent, updater)

        fun <T : BasketWriter<T>> startObject(target: T, path: Path, parent: FilteringWriter<T>, updater: Updater): FilteringWriter<T> =
                if (path.isEmpty) MatchedRewriter(target.beginObject(), parent, updater)
                else if (path.head().isConditional) PredicateMatchingRewriter(target.beginObject(), parent, makeConditionalUpdater(path, updater))
                else IndexSeekingRewriter.seekingObjectKey(target.beginObject(), path, parent, updater)
    }

}
