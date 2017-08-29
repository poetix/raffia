package com.codepoetics.raffia.streaming.projecting

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.operations.ProjectionResult
import com.codepoetics.raffia.java.api.Projector
import com.codepoetics.raffia.streaming.FilteringWriter
import com.codepoetics.raffia.writers.BasketWriter
import com.codepoetics.raffia.writers.writeTo

import java.math.BigDecimal

internal class PredicateMatchingProjector<T : BasketWriter<T>>(target: T, parent: FilteringWriter<T>, private val projector: Projector<Basket>) : StreamingProjector<T>(target, parent) {

    override fun beginObject(): FilteringWriter<T> {
        return WeavingProjector.weavingObject(target, this, projector)
    }

    override fun beginArray(): FilteringWriter<T> {
        return WeavingProjector.weavingArray(target, this, projector)
    }

    override fun end(): FilteringWriter<T> {
        if (parent == null) {
            throw IllegalStateException("end() called when not writing array or object")
        }

        return parent.advance(target)
    }

    override fun key(key: String): FilteringWriter<T> {
        throw IllegalStateException("key() called when not writing array or object")
    }

    private fun projected(projection: ProjectionResult<Basket>): FilteringWriter<T> {
        for (basket in projection) {
            target = basket.writeTo(target)
        }
        return this
    }

    override fun add(value: String): FilteringWriter<T> {
        return projected(projector.project(Basket.ofString(value)))
    }

    override fun add(value: BigDecimal): FilteringWriter<T> {
        return projected(projector.project(Basket.ofNumber(value)))
    }

    override fun add(value: Boolean): FilteringWriter<T> {
        return projected(projector.project(Basket.ofBoolean(value)))
    }

    override fun addNull(): FilteringWriter<T> {
        return projected(projector.project(Basket.ofNull()))
    }
}
