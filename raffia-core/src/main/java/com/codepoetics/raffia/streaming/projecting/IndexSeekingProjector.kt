package com.codepoetics.raffia.streaming.projecting

import com.codepoetics.raffia.paths.Path
import com.codepoetics.raffia.paths.PathSegment
import com.codepoetics.raffia.paths.PathSegmentMatchResult
import com.codepoetics.raffia.streaming.FilteringWriter
import com.codepoetics.raffia.streaming.IgnoreAllFilter
import com.codepoetics.raffia.writers.BasketWriter

import java.math.BigDecimal

internal abstract class IndexSeekingProjector<T : BasketWriter<T>>(target: T, protected val path: Path, parent: FilteringWriter<T>) : StreamingProjector<T>(target, parent) {

    private fun ignoreAll(): FilteringWriter<T> {
        return IgnoreAllFilter(target, this)
    }

    override fun beginObject(): FilteringWriter<T> {
        when (indexMatches()) {
            PathSegmentMatchResult.UNMATCHED -> return ignoreAll()
            PathSegmentMatchResult.MATCHED_BOUND -> return StreamingProjector.startObject(target, path.tail(), this)
            PathSegmentMatchResult.MATCHED_UNBOUND -> return seekingObjectKey(target, path, this)
            else -> throw IllegalStateException()
        }
    }

    override fun beginArray(): FilteringWriter<T> {
        when (indexMatches()) {
            PathSegmentMatchResult.UNMATCHED -> return ignoreAll()
            PathSegmentMatchResult.MATCHED_BOUND -> return StreamingProjector.startArray(target, path.tail(), this)
            PathSegmentMatchResult.MATCHED_UNBOUND -> return seekingArrayIndex(target, path, this)
            else -> throw IllegalStateException()
        }
    }

    override fun end(): FilteringWriter<T> {
        if (parent == null) {
            throw IllegalStateException("end() called while not writing array or object")
        }

        return parent.advance(target)
    }

    private fun indexMatches(): PathSegmentMatchResult {
        return indexMatches(path.head())
    }

    protected abstract fun indexMatches(pathHead: PathSegment): PathSegmentMatchResult

    private val isMatchingLeaf: Boolean
        get() = indexMatches() == PathSegmentMatchResult.MATCHED_BOUND && path.tail().isEmpty

    override fun add(value: String): FilteringWriter<T> {
        return if (isMatchingLeaf)
            advance(target.add(value))
        else
            ignore()
    }

    override fun add(value: BigDecimal): FilteringWriter<T> {
        return if (isMatchingLeaf)
            advance(target.add(value))
        else
            ignore()
    }

    override fun add(value: Boolean): FilteringWriter<T> {
        return if (isMatchingLeaf)
            advance(target.add(value))
        else
            ignore()
    }

    override fun addNull(): FilteringWriter<T> {
        return if (isMatchingLeaf)
            advance(target.addNull())
        else
            ignore()
    }

    private class ObjectKeySeekingInnerProjector<T : BasketWriter<T>> internal constructor(target: T, path: Path, parent: FilteringWriter<T>, private var key: String?) : IndexSeekingProjector<T>(target, path, parent) {

        override fun advance(newTarget: T): FilteringWriter<T> {
            key = null
            return super.advance(newTarget)
        }

        override fun indexMatches(pathHead: PathSegment): PathSegmentMatchResult =
            if (key == null) PathSegmentMatchResult.UNMATCHED else pathHead.matchesKey(key!!)

        override fun key(newKey: String): FilteringWriter<T> {
            if (key != null) {
                throw IllegalStateException("key() called twice")
            }
            this.key = newKey
            return this
        }

    }

    private class ArrayIndexSeekingInnerProjector<T : BasketWriter<T>> internal constructor(target: T, path: Path, parent: FilteringWriter<T>, private var index: Int) : IndexSeekingProjector<T>(target, path, parent) {

        override fun advance(newTarget: T): FilteringWriter<T> {
            index += 1
            return super.advance(newTarget)
        }

        override fun indexMatches(pathHead: PathSegment): PathSegmentMatchResult {
            return pathHead.matchesIndex(index)
        }

        override fun key(key: String): FilteringWriter<T> {
            throw IllegalStateException("key() called while writing array")
        }

    }

    companion object {

        fun <T : BasketWriter<T>> seekingArrayIndex(target: T, path: Path, parent: FilteringWriter<T>): FilteringWriter<T> {
            return ArrayIndexSeekingInnerProjector(target, path, parent, 0)
        }

        fun <T : BasketWriter<T>> seekingObjectKey(target: T, path: Path, parent: FilteringWriter<T>): FilteringWriter<T> {
            return ObjectKeySeekingInnerProjector(target, path, parent, null)
        }
    }
}
