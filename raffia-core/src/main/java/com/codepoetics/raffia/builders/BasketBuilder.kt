package com.codepoetics.raffia.builders

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.writers.BasketWriter
import com.codepoetics.raffia.writers.writeTo
import java.math.BigDecimal

class BasketBuilder<T : BasketWriter<T>> protected constructor(val writer: T) : BasketWriter<BasketBuilder<T>> {

    fun <T : BasketWriter<T>> writingTo(writer: T): BasketBuilder<T> {
        return BasketBuilder(writer)
    }

    protected fun with(writer: T): BasketBuilder<T> {
        return BasketBuilder(writer)
    }

    override fun beginObject(): BasketBuilder<T> {
        return with(writer.beginObject())
    }

    override fun end(): BasketBuilder<T> {
        return with(writer.end())
    }

    fun array(firstItem: Basket, vararg subsequentItems: Basket): BasketBuilder<T> {
        var state = firstItem.writeTo(writer.beginArray())
        for (item in subsequentItems) {
            state = item.writeTo(state)
        }
        return with(state.end())
    }

    fun array(firstItem: String, vararg subsequentItems: String): BasketBuilder<T> {
        var state = writer.beginArray().add(firstItem)
        for (item in subsequentItems) {
            state = state.add(item)
        }
        return with(state.end())
    }

    fun array(firstItem: BigDecimal, vararg subsequentItems: BigDecimal): BasketBuilder<T> {
        var state = writer.beginArray().add(firstItem)
        for (item in subsequentItems) {
            state = state.add(item)
        }
        return with(state.end())
    }

    fun array(firstItem: Boolean, vararg subsequentItems: Boolean): BasketBuilder<T> {
        var state = writer.beginArray().add(firstItem)
        for (item in subsequentItems) {
            state = state.add(item)
        }
        return with(state.end())
    }

    @JvmOverloads fun array(items: Collection<Basket> = emptyList<Basket>()): BasketBuilder<T> {
        var state = writer.beginArray()
        for (basket in items) {
            state = basket.writeTo(state)
        }
        return with(state.end())
    }

    override fun beginArray(): BasketBuilder<T> {
        return with(writer.beginArray())
    }

    override fun key(key: String): BasketBuilder<T> {
        return with(writer.key(key))
    }

    fun add(key: String, value: String): BasketBuilder<T> {
        return key(key).add(value)
    }

    fun add(key: String, value: BigDecimal): BasketBuilder<T> {
        return key(key).add(value)
    }

    fun add(key: String, value: Boolean): BasketBuilder<T> {
        return key(key).add(value)
    }

    fun add(key: String, value: Basket): BasketBuilder<T> {
        return key(key).add(value)
    }

    fun addArray(key: String): BasketBuilder<T> {
        return key(key).array()
    }

    fun addArray(key: String, firstItem: Basket, vararg subsequent: Basket): BasketBuilder<T> {
        return key(key).array(firstItem, *subsequent)
    }

    fun addArray(key: String, firstItem: String, vararg subsequent: String): BasketBuilder<T> {
        return key(key).array(firstItem, *subsequent)
    }

    fun addArray(key: String, firstItem: BigDecimal, vararg subsequent: BigDecimal): BasketBuilder<T> {
        return key(key).array(firstItem, *subsequent)
    }

    fun addArray(key: String, firstItem: Boolean, vararg subsequent: Boolean): BasketBuilder<T> {
        return key(key).array(firstItem, *subsequent)
    }

    fun addNull(key: String): BasketBuilder<T> {
        return key(key).addNull()
    }

    fun add(value: Basket): BasketBuilder<T> {
        return with(value.writeTo(writer))
    }

    override fun add(value: String): BasketBuilder<T> {
        return with(writer.add(value))
    }

    override fun add(value: BigDecimal): BasketBuilder<T> {
        return with(writer.add(value))
    }

    override fun add(value: Boolean): BasketBuilder<T> {
        return with(writer.add(value))
    }

    override fun addNull(): BasketBuilder<T> {
        return with(writer.addNull())
    }

    override fun toString(): String {
        return "Basket builder with state: " + writer
    }
}
