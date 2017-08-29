package com.codepoetics.raffia.streaming.projecting

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.functions.Projector
import com.codepoetics.raffia.paths.Path
import com.codepoetics.raffia.streaming.FilteringWriter
import com.codepoetics.raffia.writers.BasketWriter

abstract class StreamingProjector<T : BasketWriter<T>> internal constructor(protected var target: T, protected val parent: FilteringWriter<T>?) : FilteringWriter<T> {

    override fun advance(newTarget: T): FilteringWriter<T> {
        target = newTarget
        return this
    }

    internal fun ignore(): FilteringWriter<T> {
        return advance(target)
    }

    override fun complete(): T {
        return target
    }

    companion object {

        fun <T : BasketWriter<T>> startArray(target: T, path: Path): FilteringWriter<T> {
            return ArrayClosingProjector.closing(start(target.beginArray(), path))
        }

        fun <T : BasketWriter<T>> start(target: T, path: Path): FilteringWriter<T> {
            if (path.isEmpty) {
                return matched(target)
            }

            return StructStartSeekingProjector(target, path)
        }

        private fun <T : BasketWriter<T>> matched(target: T): FilteringWriter<T> {
            return MatchedProjector<T>(target, null)
        }

        private fun makeConditionalProjector(path: Path): Projector<Basket> {
            return path.head().createItemProjector(path.tail())
        }

        internal fun <T : BasketWriter<T>> startArray(target: T, path: Path, parent: FilteringWriter<T>): FilteringWriter<T> {
            if (path.isEmpty) {
                return MatchedProjector(target, parent)
            }

            if (path.head().isConditional) {
                return PredicateMatchingProjector(target, parent, makeConditionalProjector(path))
            }

            return IndexSeekingProjector.seekingArrayIndex(target, path, parent)
        }

        internal fun <T : BasketWriter<T>> startObject(target: T, path: Path, parent: FilteringWriter<T>): FilteringWriter<T> {
            if (path.isEmpty) {
                return MatchedProjector(target, parent)
            }

            if (path.head().isConditional) {
                return PredicateMatchingProjector(target, parent, makeConditionalProjector(path))
            }

            return IndexSeekingProjector.seekingObjectKey(target, path, parent)
        }
    }

}
