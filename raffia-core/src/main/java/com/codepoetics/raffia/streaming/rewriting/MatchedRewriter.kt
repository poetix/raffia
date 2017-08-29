package com.codepoetics.raffia.streaming.rewriting

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.functions.Updater
import com.codepoetics.raffia.streaming.FilteringWriter
import com.codepoetics.raffia.writers.BasketWriter

import java.math.BigDecimal

internal class MatchedRewriter<T : BasketWriter<T>>(target: T, parent: FilteringWriter<T>?, updater: Updater) : StreamingRewriter<T>(target, parent, updater) {

    override fun beginObject(): FilteringWriter<T> {
        return WeavingRewriter.weavingObject(target.beginObject(), this, updater!!)
    }

    override fun beginArray(): FilteringWriter<T> {
        return WeavingRewriter.weavingObject(target.beginArray(), this, updater!!)
    }

    override fun end(): FilteringWriter<T> {
        if (parent == null) {
            throw IllegalStateException("end() called when not writing object or array")
        }

        return parent.advance(target.end())
    }

    override fun key(key: String): FilteringWriter<T> {
        target = target.key(key)
        return this
    }

    override fun add(value: String): FilteringWriter<T> {
        return updated(updater!!.update(Basket.ofString(value)))
    }

    override fun add(value: BigDecimal): FilteringWriter<T> {
        return updated(updater!!.update(Basket.ofNumber(value)))
    }

    override fun add(value: Boolean): FilteringWriter<T> {
        return updated(updater!!.update(Basket.ofBoolean(value)))
    }

    override fun addNull(): FilteringWriter<T> {
        return updated(updater!!.update(Basket.ofNull()))
    }

}
