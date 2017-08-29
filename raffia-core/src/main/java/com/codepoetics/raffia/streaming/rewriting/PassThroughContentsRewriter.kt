package com.codepoetics.raffia.streaming.rewriting

import com.codepoetics.raffia.streaming.FilteringWriter
import com.codepoetics.raffia.writers.BasketWriter

import java.math.BigDecimal

internal class PassThroughContentsRewriter<T : BasketWriter<T>>(target: T, parent: StreamingRewriter<T>) : StreamingRewriter<T>(target, parent, null) {

    private var depth = 0

    override fun end(): FilteringWriter<T> {
        target = target.end()
        return if (depth-- == 0) parent!!.advance(target) else this
    }

    override fun beginObject(): FilteringWriter<T> {
        target = target.beginObject()
        depth++
        return this
    }

    override fun beginArray(): FilteringWriter<T> {
        target = target.beginArray()
        depth++
        return this
    }

    override fun key(key: String): FilteringWriter<T> {
        return advance(target.key(key))
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
