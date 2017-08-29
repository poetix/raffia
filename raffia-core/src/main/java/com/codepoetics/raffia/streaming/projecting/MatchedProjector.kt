package com.codepoetics.raffia.streaming.projecting

import com.codepoetics.raffia.streaming.FilteringWriter
import com.codepoetics.raffia.writers.BasketWriter

import java.math.BigDecimal

internal class MatchedProjector<T : BasketWriter<T>>(target: T, parent: FilteringWriter<T>?) : StreamingProjector<T>(target, parent) {

    private var depth = 0

    override fun beginObject(): FilteringWriter<T> {
        target = target.beginObject()
        depth += 1
        return this
    }

    override fun beginArray(): FilteringWriter<T> {
        target = target.beginArray()
        depth += 1
        return this
    }

    override fun end(): FilteringWriter<T> {
        target = target.end()

        if (depth-- > 0) {
            return this
        }

        if (parent == null) {
            throw IllegalStateException("end() called when not writing object or array")
        }

        return parent.advance(target)
    }

    override fun key(key: String): FilteringWriter<T> {
        target = target.key(key)
        return this
    }

    override fun add(value: String): FilteringWriter<T> {
        target = target.add(value)
        return this
    }

    override fun add(value: BigDecimal): FilteringWriter<T> {
        target = target.add(value)
        return this
    }

    override fun add(value: Boolean): FilteringWriter<T> {
        target = target.add(value)
        return this
    }

    override fun addNull(): FilteringWriter<T> {
        target = target.addNull()
        return this
    }
}
