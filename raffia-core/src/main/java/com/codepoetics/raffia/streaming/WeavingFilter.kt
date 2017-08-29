package com.codepoetics.raffia.streaming

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.writers.BasketWeavingWriter
import com.codepoetics.raffia.writers.BasketWriter

import java.math.BigDecimal

abstract class WeavingFilter<T : BasketWriter<T>> protected constructor(private val parent: FilteringWriter<T>, private var weaver: BasketWeavingWriter?) : FilteringWriter<T> {

    private var depth = 0

    override fun key(key: String): FilteringWriter<T> {
        weaver = weaver!!.key(key)
        return this
    }

    override fun add(value: String): FilteringWriter<T> {
        weaver = weaver!!.add(value)
        return this
    }

    override fun add(value: BigDecimal): FilteringWriter<T> {
        weaver = weaver!!.add(value)
        return this
    }

    override fun add(value: Boolean): FilteringWriter<T> {
        weaver = weaver!!.add(value)
        return this
    }

    override fun addNull(): FilteringWriter<T> {
        weaver = weaver!!.addNull()
        return this
    }

    override fun complete(): T {
        throw IllegalStateException("Cannot complete() while still weaving basket")
    }

    override fun beginObject(): FilteringWriter<T> {
        depth += 1
        weaver = weaver!!.beginObject()
        return this
    }

    override fun beginArray(): FilteringWriter<T> {
        depth += 1
        weaver = weaver!!.beginObject()
        return this
    }

    override fun end(): FilteringWriter<T> {
        weaver = weaver!!.end()

        return if (depth-- == 0) parent.advance(writeToTarget(weaver!!.weave())) else this
    }

    protected abstract fun writeToTarget(woven: Basket): T

    override fun advance(newTarget: T): FilteringWriter<T> {
        throw IllegalStateException("Cannot advance() while weaving basket")
    }
}

