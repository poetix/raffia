package com.codepoetics.raffia.streaming.projecting

import com.codepoetics.raffia.streaming.FilteringWriter
import com.codepoetics.raffia.writers.BasketWriter

import java.math.BigDecimal

internal class ArrayClosingProjector<T : BasketWriter<T>> private constructor(private var inner: FilteringWriter<T>?) : FilteringWriter<T> {

    override fun beginObject(): FilteringWriter<T> {
        inner = inner!!.beginObject()
        return this
    }

    override fun beginArray(): FilteringWriter<T> {
        inner = inner!!.beginArray()
        return this
    }

    override fun end(): FilteringWriter<T> {
        inner = inner!!.end()
        return this
    }

    override fun key(key: String): FilteringWriter<T> {
        inner = inner!!.key(key)
        return this
    }

    override fun add(value: String): FilteringWriter<T> {
        inner = inner!!.add(value)
        return this
    }

    override fun add(value: BigDecimal): FilteringWriter<T> {
        inner = inner!!.add(value)
        return this
    }

    override fun add(value: Boolean): FilteringWriter<T> {
        inner = inner!!.add(value)
        return this
    }

    override fun addNull(): FilteringWriter<T> {
        inner = inner!!.addNull()
        return this
    }

    override fun advance(newTarget: T): FilteringWriter<T> {
        inner = inner!!.advance(newTarget)
        return this
    }

    override fun complete(): T {
        return inner!!.complete().end()
    }

    companion object {

        fun <T : BasketWriter<T>> closing(inner: FilteringWriter<T>): FilteringWriter<T> {
            return ArrayClosingProjector(inner)
        }
    }
}
