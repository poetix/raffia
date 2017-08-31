package com.codepoetics.raffia.operations

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.ObjectEntry
import com.codepoetics.raffia.baskets.PropertySet
import com.codepoetics.raffia.functions.Updater

import java.math.BigDecimal

object Setters {

    @JvmStatic
    fun toBasket(value: Basket): Updater {
        return Updaters.toConstant(value)
    }

    @JvmStatic
    fun toString(value: String): Updater {
        return toBasket(Basket.ofString(value))
    }

    @JvmStatic
    fun toNumber(value: BigDecimal): Updater {
        return toBasket(Basket.ofNumber(value))
    }

    @JvmStatic
    fun toBoolean(value: Boolean): Updater {
        return toBasket(Basket.ofBoolean(value))
    }

    @JvmStatic
    fun toNull(): Updater {
        return toBasket(Basket.ofNull())
    }

    @JvmStatic
    fun toArray(contents: Collection<Basket>): Updater {
        return toBasket(Basket.ofArray(contents))
    }

    @JvmStatic
    fun toArray(contents: ArrayContents): Updater {
        return toBasket(Basket.ofArray(contents))
    }

    @JvmStatic
    fun toArray(vararg contents: Basket): Updater {
        return toBasket(Basket.ofArray(*contents))
    }

    @JvmStatic
    fun toObject(vararg properties: ObjectEntry): Updater {
        return toBasket(Basket.ofObject(*properties))
    }

    @JvmStatic
    fun toObject(properties: Map<String, Basket>): Updater {
        return toBasket(Basket.ofObject(properties))
    }

    @JvmStatic
    fun toObject(properties: PropertySet): Updater {
        return toBasket(Basket.ofObject(properties))
    }

}
