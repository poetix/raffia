package com.codepoetics.raffia.operations

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.ObjectEntry
import com.codepoetics.raffia.baskets.PropertySet
import com.codepoetics.raffia.java.api.Updater

import java.math.BigDecimal

object Setters {

    fun toBasket(value: Basket): Updater {
        return Updaters.toConstant(value)
    }

    fun toString(value: String): Updater {
        return toBasket(Basket.ofString(value))
    }

    fun toNumber(value: BigDecimal): Updater {
        return toBasket(Basket.ofNumber(value))
    }

    fun toBoolean(value: Boolean): Updater {
        return toBasket(Basket.ofBoolean(value))
    }

    fun toNull(): Updater {
        return toBasket(Basket.ofNull())
    }

    fun toArray(contents: Collection<Basket>): Updater {
        return toBasket(Basket.ofArray(contents))
    }

    fun toArray(contents: ArrayContents): Updater {
        return toBasket(Basket.ofArray(contents))
    }

    fun toArray(vararg contents: Basket): Updater {
        return toBasket(Basket.ofArray(*contents))
    }

    fun toObject(vararg properties: ObjectEntry): Updater {
        return toBasket(Basket.ofObject(*properties))
    }

    fun toObject(properties: Map<String, Basket>): Updater {
        return toBasket(Basket.ofObject(properties))
    }

    fun toObject(properties: PropertySet): Updater {
        return toBasket(Basket.ofObject(properties))
    }

}
