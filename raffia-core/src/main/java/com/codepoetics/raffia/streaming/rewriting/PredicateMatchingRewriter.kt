package com.codepoetics.raffia.streaming.rewriting

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.java.api.Updater
import com.codepoetics.raffia.streaming.FilteringWriter
import com.codepoetics.raffia.writers.BasketWriter

import java.math.BigDecimal

internal class PredicateMatchingRewriter<T : BasketWriter<T>>(target: T, parent: FilteringWriter<T>, itemUpdater: Updater) : StreamingRewriter<T>(target, parent, itemUpdater) {

    override fun beginObject(): FilteringWriter<T> {
        return WeavingRewriter.weavingObject(target, this, updater!!)
    }

    override fun beginArray(): FilteringWriter<T> {
        return WeavingRewriter.weavingArray(target, this, updater!!)
    }

    override fun end(): FilteringWriter<T> {
        return parent!!.advance(target.end())
    }

    override fun key(key: String): FilteringWriter<T> {
        return advance(target.key(key))
    }

    override fun add(value: String): FilteringWriter<T> {
        return updated(Basket.ofString(value))
    }

    override fun add(value: BigDecimal): FilteringWriter<T> {
        return updated(Basket.ofNumber(value))
    }

    override fun add(value: Boolean): FilteringWriter<T> {
        return updated(Basket.ofBoolean(value))
    }

    override fun addNull(): FilteringWriter<T> {
        return updated(Basket.ofNull())
    }
}
