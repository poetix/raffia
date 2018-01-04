package com.codepoetics.raffia.operations

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.PropertySet

import java.math.BigDecimal

typealias BasketPredicate = (Basket) -> Boolean
typealias Updater = (Basket) -> Basket

object Updaters {

    val NO_OP: Updater = { basket -> basket }

    @JvmStatic
    fun branch(predicate: BasketPredicate, ifTrue: Updater, ifFalse: Updater): Updater = { basket ->
        if (predicate(basket)) ifTrue(basket)
        else ifFalse(basket)
    }

    @JvmStatic
    fun toConstant(value: Basket): Updater = { value }

    @JvmStatic
    fun ofString(stringMapper: (String) -> String): Updater = { basket ->
        if (basket.isString()) basket.mapString(stringMapper) else basket
    }

    @JvmStatic
    fun ofNumber(numberMapper: (BigDecimal) -> BigDecimal): Updater = { basket ->
        if (basket.isNumber()) basket.mapNumber(numberMapper) else basket
    }

    @JvmStatic
    fun ofBoolean(booleanMapper: (Boolean) -> Boolean): Updater = { basket ->
        if (basket.isBoolean()) basket.mapBoolean(booleanMapper) else basket
    }

    @JvmStatic
    fun ofArray(arrayContentsMapper: (ArrayContents) -> ArrayContents): Updater = { basket ->
        if (basket.isArray()) basket.mapArray(arrayContentsMapper) else basket
    }

    @JvmStatic
    fun ofObject(propertySetMapper:(PropertySet) -> PropertySet): Updater = { basket ->
        if (basket.isObject()) basket.mapObject(propertySetMapper) else basket
    }

    @JvmStatic
    fun appending(arrayItem: Basket): Updater = ofArray { input -> input.plus(arrayItem) }

    @JvmStatic
    fun inserting(index: Int, arrayItem: Basket): Updater = ofArray { input -> input.plus(index, arrayItem) }

    @JvmStatic
    fun replacing(index: Int, arrayItem: Basket): Updater = ofArray { input -> input.with(index, arrayItem) }

    @JvmStatic
    fun inserting(key: String, value: Basket): Updater = ofObject { input -> input.with(key, value) }

    @JvmStatic
    fun removing(index: Int): Updater = ofArray { input -> input.minus(index) }

    @JvmStatic
    fun removing(key: String): Updater = ofObject { input -> input.minus(key) }

    @JvmStatic
    fun updating(index: Int, itemUpdater: Updater): Updater = ofArray { input ->
        val actual = if (index < 0) input.size() + index else index
        if (actual < 0 || actual >= input.size()) {
            return@ofArray input
        }
        input.with(actual, itemUpdater(input[actual]))
    }

    @JvmStatic
    fun updating(key: String, valueUpdater: Updater): Updater = ofObject { input ->
        if (input.containsKey(key)) input.with(key, valueUpdater(input[key]!!))
        else input
    }
}
