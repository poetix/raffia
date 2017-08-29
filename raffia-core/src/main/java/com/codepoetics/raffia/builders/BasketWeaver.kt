package com.codepoetics.raffia.builders

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.writers.BasketWeavingWriter
import com.codepoetics.raffia.writers.BasketWriter
import com.codepoetics.raffia.writers.Writers

import java.math.BigDecimal

class BasketWeaver private constructor(private val writer: BasketWeavingWriter) : BasketWriter<BasketWeaver> {

    fun weave(): Basket = writer.weave()

    private fun with(writer: BasketWeavingWriter): BasketWeaver = BasketWeaver(writer)

    override fun beginObject(): BasketWeaver = with(writer.beginObject())

    override fun beginArray(): BasketWeaver = with(writer.beginArray())

    override fun end(): BasketWeaver = with(writer.end())

    override fun key(key: String): BasketWeaver = with(writer.key(key))

    override fun add(value: String): BasketWeaver = with(writer.add(value))

    override fun add(value: BigDecimal): BasketWeaver = with(writer.add(value))

    override fun add(value: Boolean): BasketWeaver = with(writer.add(value))

    override fun addNull(): BasketWeaver = with(writer.addNull())

    fun add(key: String, value: String): BasketWeaver = key(key).add(value)

    fun add(key: String, value: BigDecimal): BasketWeaver = key(key).add(value)

    fun add(key: String, value: Boolean): BasketWeaver = key(key).add(value)

    fun add(key: String, value: Basket): BasketWeaver = key(key).add(value)

    fun array(): BasketWeaver = with(writer.beginArray().end())

    fun array(firstItem: Basket, vararg subsequentItems: Basket): BasketWeaver =
            array(firstItem, subsequentItems) { it }

    fun array(firstItem: String, vararg subsequentItems: String): BasketWeaver =
            array<String>(firstItem, subsequentItems) { Basket.ofString(it) }

    fun array(firstItem: BigDecimal, vararg subsequentItems: BigDecimal): BasketWeaver =
            array<BigDecimal>(firstItem, subsequentItems) { Basket.ofNumber(it) }

    fun array(firstItem: Boolean, vararg subsequentItems: Boolean): BasketWeaver =
            array<Boolean>(firstItem, subsequentItems.toTypedArray()) { Basket.ofBoolean(it) }

    private fun <T> array(first: T, subsequent: Array<out T>, mapper: (T) -> Basket): BasketWeaver =
        with(subsequent.fold(
                writer.beginArray().add(mapper(first)),
                { state, item -> state.add(mapper(item)) }).end())

    fun array(items: Collection<Basket>): BasketWeaver =
        with(items.fold(writer.beginArray()) { state, item -> state.add(item) }.end())

    fun addArray(key: String): BasketWeaver = key(key).array()

    fun addArray(key: String, firstItem: Basket, vararg subsequent: Basket): BasketWeaver =
            key(key).array(firstItem, *subsequent)

    fun addArray(key: String, firstItem: String, vararg subsequent: String): BasketWeaver =
            key(key).array(firstItem, *subsequent)

    fun addArray(key: String, firstItem: BigDecimal, vararg subsequent: BigDecimal): BasketWeaver =
            key(key).array(firstItem, *subsequent)

    fun addArray(key: String, firstItem: Boolean, vararg subsequent: Boolean): BasketWeaver =
            key(key).array(firstItem, *subsequent)

    fun add(value: Basket): BasketWeaver {
        return with(writer.add(value))
    }

    override fun toString(): String = "Basket weaver with state: " + writer

    companion object {

        @JvmStatic
        fun create(): BasketWeaver {
            return weavingWith(Writers.weaving())
        }

        @JvmStatic
        fun weavingWith(writer: BasketWeavingWriter): BasketWeaver {
            return BasketWeaver(writer)
        }
    }

}
