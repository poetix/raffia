package com.codepoetics.raffia.streaming

import com.codepoetics.raffia.writers.BasketWriter

import java.math.BigDecimal

class IgnoreAllFilter<T : BasketWriter<T>>(private var target: T, private val parent: FilteringWriter<T>) : FilteringWriter<T> {
    private var depth = 0

    override fun beginObject(): FilteringWriter<T> {
        depth++
        return this
    }

    override fun beginArray(): FilteringWriter<T> {
        depth++
        return this
    }

    override fun end(): FilteringWriter<T> {
        return if (depth-- == 0) parent.advance(target) else this
    }

    override fun key(key: String): FilteringWriter<T> {
        return this
    }

    override fun add(value: String): FilteringWriter<T> {
        return this
    }

    override fun add(value: BigDecimal): FilteringWriter<T> {
        return this
    }

    override fun add(value: Boolean): FilteringWriter<T> {
        return this
    }

    override fun addNull(): FilteringWriter<T> {
        return this
    }

    override fun advance(newTarget: T): FilteringWriter<T> {
        target = newTarget
        return this
    }

    override fun complete(): T {
        throw IllegalStateException("Cannot complete while writing array or object")
    }
}
