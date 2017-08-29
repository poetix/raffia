package com.codepoetics.raffia.streaming

import com.codepoetics.raffia.writers.BasketWriter

import java.math.BigDecimal

class EndOfLineFilter<T : BasketWriter<T>>(private val target: T) : FilteringWriter<T> {

    override fun beginObject(): FilteringWriter<T> {
        throw IllegalStateException("beginObject() called after basket complete")
    }

    override fun beginArray(): FilteringWriter<T> {
        throw IllegalStateException("beginArray() called after basket complete")
    }

    override fun end(): FilteringWriter<T> {
        throw IllegalStateException("end() called after basket complete")
    }

    override fun key(key: String): FilteringWriter<T> {
        throw IllegalStateException("key() called after basket complete")
    }

    override fun add(value: String): FilteringWriter<T> {
        throw IllegalStateException("add() called after basket complete")
    }

    override fun add(value: BigDecimal): FilteringWriter<T> {
        throw IllegalStateException("add() called after basket complete")
    }

    override fun add(value: Boolean): FilteringWriter<T> {
        throw IllegalStateException("add() called after basket complete")
    }

    override fun addNull(): FilteringWriter<T> {
        throw IllegalStateException("addNull() called after basket complete")
    }

    override fun advance(newTarget: T): FilteringWriter<T> {
        throw IllegalStateException("advance() called after basket complete")
    }

    override fun complete(): T {
        return target
    }
}
