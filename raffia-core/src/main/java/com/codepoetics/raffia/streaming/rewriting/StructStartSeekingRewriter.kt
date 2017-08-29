package com.codepoetics.raffia.streaming.rewriting

import com.codepoetics.raffia.java.api.Updater
import com.codepoetics.raffia.paths.Path
import com.codepoetics.raffia.streaming.EndOfLineFilter
import com.codepoetics.raffia.streaming.FilteringWriter
import com.codepoetics.raffia.writers.BasketWriter

import java.math.BigDecimal

class StructStartSeekingRewriter<T : BasketWriter<T>>(target: T, private val path: Path, updater: Updater) : StreamingRewriter<T>(target, null, updater) {

    override fun beginObject(): FilteringWriter<T> {
        return StreamingRewriter.startObject(target, path, this, updater!!)
    }

    override fun beginArray(): FilteringWriter<T> {
        return StreamingRewriter.startArray(target, path, this, updater!!)
    }

    override fun end(): FilteringWriter<T> {
        throw IllegalStateException("end() called when not writing object or array")
    }

    override fun key(key: String): FilteringWriter<T> {
        throw IllegalStateException("key() called when not writing object")
    }

    override fun advance(newTarget: T): FilteringWriter<T> {
        return EndOfLineFilter(newTarget)
    }

    override fun add(value: String): FilteringWriter<T> {
        return advance(target.add(value))
    }

    override fun add(value: BigDecimal): FilteringWriter<T> {
        return advance(target.add(value))
    }

    override fun add(value: Boolean): FilteringWriter<T> {
        return advance(target.add(value))
    }

    override fun addNull(): FilteringWriter<T> {
        return advance(target.addNull())
    }
}
