package com.codepoetics.raffia.streaming.projecting

import com.codepoetics.raffia.paths.Path
import com.codepoetics.raffia.streaming.EndOfLineFilter
import com.codepoetics.raffia.streaming.FilteringWriter
import com.codepoetics.raffia.writers.BasketWriter

import java.math.BigDecimal

internal class StructStartSeekingProjector<T : BasketWriter<T>>(target: T, private val path: Path) : StreamingProjector<T>(target, null) {

    override fun beginObject(): FilteringWriter<T> {
        return StreamingProjector.startObject(target, path, this)
    }

    override fun beginArray(): FilteringWriter<T> {
        return StreamingProjector.startArray(target, path, this)
    }

    override fun end(): FilteringWriter<T> {
        throw IllegalStateException("end() called when not writing object or array")
    }

    override fun key(key: String): FilteringWriter<T> {
        throw IllegalStateException("key() called when not writing object")
    }

    override fun add(value: String): FilteringWriter<T> {
        return EndOfLineFilter(target)
    }

    override fun add(value: BigDecimal): FilteringWriter<T> {
        return EndOfLineFilter(target)
    }

    override fun add(value: Boolean): FilteringWriter<T> {
        return EndOfLineFilter(target)
    }

    override fun addNull(): FilteringWriter<T> {
        return EndOfLineFilter(target)
    }
}
