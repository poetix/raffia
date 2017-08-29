package com.codepoetics.raffia.writers

import java.math.BigDecimal

abstract class PassThroughWriter<T : BasketWriter<T>, S : PassThroughWriter<T, S>> protected constructor(protected val state: T) : BasketWriter<S> {

    protected abstract fun with(state: T): S

    override fun beginObject(): S = with(state.beginObject())

    override fun beginArray(): S = with(state.beginArray())

    override fun end(): S {
        return with(state.end())
    }

    override fun key(key: String): S = with(state.key(key))

    override fun add(value: String): S = with(state.add(value))

    override fun add(value: BigDecimal): S = with(state.add(value))

    override fun add(value: Boolean): S = with(state.add(value))

    override fun addNull(): S = with(state.addNull())

}
