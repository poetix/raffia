package com.codepoetics.raffia.writers

import com.codepoetics.raffia.baskets.Basket
import java.math.BigDecimal

interface BasketWriter<T : BasketWriter<T>> {

    fun beginObject(): T
    fun beginArray(): T

    fun end(): T

    fun key(key: String): T

    fun add(value: String): T
    fun add(value: BigDecimal): T
    fun add(value: Boolean): T
    fun addNull(): T

}

interface BasketWeavingWriter : BasketWriter<BasketWeavingWriter> {
    fun weave(): Basket
    fun add(basket: Basket): BasketWeavingWriter
}

fun <T : BasketWriter<T>> Basket.writeTo(writer: BasketWriter<T>): T = when(this) {
    is Basket.StringBasket -> writer.add(stringValue)
    is Basket.NumberBasket -> writer.add(numberValue)
    is Basket.TrueBasket -> writer.add(true)
    is Basket.FalseBasket -> writer.add(false)
    is Basket.NullBasket -> writer.addNull()
    is Basket.ArrayBasket -> contents.fold(writer.beginArray()) {
        state, item -> item.writeTo(state)
    }.end()
    is Basket.ObjectBasket -> properties.fold(writer.beginObject()) {
        state, (key, value) -> value.writeTo(state.key(key))
    }.end()
}
