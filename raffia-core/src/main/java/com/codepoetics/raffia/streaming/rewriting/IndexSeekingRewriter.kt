package com.codepoetics.raffia.streaming.rewriting

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.java.api.Updater
import com.codepoetics.raffia.paths.Path
import com.codepoetics.raffia.paths.PathSegment
import com.codepoetics.raffia.paths.PathSegmentMatchResult
import com.codepoetics.raffia.streaming.FilteringWriter
import com.codepoetics.raffia.writers.BasketWriter
import com.codepoetics.raffia.writers.writeTo

import java.math.BigDecimal

internal abstract class IndexSeekingRewriter<T : BasketWriter<T>>(target: T, protected val path: Path, parent: FilteringWriter<T>, updater: Updater) : StreamingRewriter<T>(target, parent, updater) {

    private fun passThrough(newTarget: T): FilteringWriter<T> {
        return PassThroughContentsRewriter(newTarget, this)
    }

    override fun beginObject(): FilteringWriter<T> {
        when (indexMatches()) {
            PathSegmentMatchResult.UNMATCHED -> return passThrough(target.beginObject())
            PathSegmentMatchResult.MATCHED_BOUND -> return StreamingRewriter.startObject(target, path.tail(), this, updater!!)
            PathSegmentMatchResult.MATCHED_UNBOUND -> return StreamingRewriter.startObject(target, path, this, updater!!)
            else -> throw IllegalStateException()
        }
    }

    override fun beginArray(): FilteringWriter<T> {
        when (indexMatches()) {
            PathSegmentMatchResult.UNMATCHED -> return passThrough(target.beginArray())
            PathSegmentMatchResult.MATCHED_BOUND -> return StreamingRewriter.startArray(target, path.tail(), this, updater!!)
            PathSegmentMatchResult.MATCHED_UNBOUND -> return StreamingRewriter.startArray(target, path, this, updater!!)
            else -> throw IllegalStateException()
        }
    }

    override fun end(): FilteringWriter<T> {
        return parent!!.advance(target.end())
    }

    private fun indexMatches(): PathSegmentMatchResult {
        return indexMatches(path.head())
    }

    protected abstract fun indexMatches(pathHead: PathSegment): PathSegmentMatchResult

    override fun key(key: String): FilteringWriter<T> {
        throw IllegalStateException("key() called while writing array")
    }

    protected val isBoundLeaf: Boolean
        get() = indexMatches() == PathSegmentMatchResult.MATCHED_BOUND && path.tail().isEmpty

    private fun update(value: Basket): FilteringWriter<T> {
        return advance(updater!!.update(value).writeTo(target))
    }

    override fun add(value: String): FilteringWriter<T> {
        return if (isBoundLeaf)
            update(Basket.ofString(value))
        else
            advance(target.add(value))
    }

    override fun add(value: BigDecimal): FilteringWriter<T> {
        return if (isBoundLeaf)
            update(Basket.ofNumber(value))
        else
            advance(target.add(value))
    }

    override fun add(value: Boolean): FilteringWriter<T> {
        return if (isBoundLeaf)
            update(Basket.ofBoolean(value))
        else
            advance(target.add(value))
    }

    override fun addNull(): FilteringWriter<T> {
        return if (isBoundLeaf)
            update(Basket.ofNull())
        else
            advance(target.addNull())
    }

    private class ObjectKeySeekingRewriter<T : BasketWriter<T>> internal constructor(target: T, path: Path, parent: FilteringWriter<T>, updater: Updater, private var key: String?) : IndexSeekingRewriter<T>(target, path, parent, updater) {

        override fun advance(newTarget: T): FilteringWriter<T> {
            this.target = newTarget
            this.key = null
            return this
        }

        override fun indexMatches(pathHead: PathSegment): PathSegmentMatchResult {
            return pathHead.matchesKey(key!!)
        }

        override fun key(newKey: String): FilteringWriter<T> {
            if (key != null) {
                throw IllegalStateException("key() called twice")
            }
            target = target.key(newKey)
            key = newKey
            return this
        }

    }

    private class ArrayIndexSeekingRewriter<T : BasketWriter<T>> internal constructor(target: T, path: Path, parent: FilteringWriter<T>, updater: Updater, private var index: Int) : IndexSeekingRewriter<T>(target, path, parent, updater) {

        override fun advance(newTarget: T): FilteringWriter<T> {
            this.target = newTarget
            this.index += 1
            return this
        }

        override fun indexMatches(pathHead: PathSegment): PathSegmentMatchResult {
            return pathHead.matchesIndex(index)
        }

        override fun key(key: String): FilteringWriter<T> {
            throw IllegalStateException("key() called while writing array")
        }

    }

    companion object {

        fun <T : BasketWriter<T>> seekingArrayIndex(target: T, path: Path, parent: FilteringWriter<T>, updater: Updater): StreamingRewriter<T> {
            return ArrayIndexSeekingRewriter(target, path, parent, updater, 0)
        }

        fun <T : BasketWriter<T>> seekingObjectKey(target: T, path: Path, parent: FilteringWriter<T>, updater: Updater): StreamingRewriter<T> {
            return ObjectKeySeekingRewriter(target, path, parent, updater, null)
        }
    }
}
