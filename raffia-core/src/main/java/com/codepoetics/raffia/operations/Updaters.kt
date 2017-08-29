package com.codepoetics.raffia.operations

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.PropertySet
import com.codepoetics.raffia.java.api.*

import java.math.BigDecimal

object Updaters {

    val NO_OP: Updater = updater { basket -> basket }

    @JvmStatic
    fun branch(predicate: BasketPredicate, ifTrue: Updater, ifFalse: Updater): Updater = updater { basket ->
        if (predicate.test(basket)) ifTrue.update(basket)
        else ifFalse.update(basket)
    }

    @JvmStatic
    fun toConstant(value: Basket): Updater = updater { value }

    @JvmStatic
    fun ofString(stringMapper: Mapper<String, String>): Updater = updater { basket ->
        if (basket.isString()) Basket.ofString(stringMapper.map(basket.asString())) else basket
    }

    @JvmStatic
    fun ofNumber(numberMapper: Mapper<BigDecimal, BigDecimal>): Updater = updater { basket ->
        if (basket.isNumber()) Basket.ofNumber(numberMapper.map(basket.asNumber())) else basket
    }

    @JvmStatic
    fun ofBoolean(booleanMapper: Mapper<Boolean, Boolean>): Updater = updater { basket ->
        if (basket.isBoolean()) Basket.ofBoolean(booleanMapper.map(basket.asBoolean())) else basket
    }

    @JvmStatic
    fun ofArray(arrayContentsMapper: Mapper<ArrayContents, ArrayContents>): Updater = updater { basket ->
        basket.mapArray(arrayContentsMapper)
    }

    @JvmStatic
    fun ofObject(propertySetMapper: Mapper<PropertySet, PropertySet>): Updater = updater { basket ->
        basket.mapObject(propertySetMapper)
    }

    private fun ofArray(f: (ArrayContents) -> ArrayContents): Updater = updater { basket -> basket.mapArray(mapper(f)) }
    private fun ofObject(f: (PropertySet) -> PropertySet): Updater = updater { basket -> basket.mapObject(mapper(f)) }

    @JvmStatic
    fun appending(arrayItem: Basket): Updater = ofArray { input -> input.plus(arrayItem) }

    fun inserting(index: Int, arrayItem: Basket): Updater = ofArray { input -> input.plus(index, arrayItem) }

    fun replacing(index: Int, arrayItem: Basket): Updater = ofArray { input -> input.with(index, arrayItem) }

    fun inserting(key: String, value: Basket): Updater = ofObject { input -> input.with(key, value) }

    fun removing(index: Int): Updater = ofArray { input -> input.minus(index) }

    fun removing(key: String): Updater = ofObject { input -> input.minus(key) }

    fun updating(index: Int, itemUpdater: Updater): Updater = ofArray { input ->
        val actual = if (index < 0) input.size() + index else index
        if (actual < 0 || actual >= input.size()) {
            return@ofArray input
        }
        input.with(actual, itemUpdater.update(input[actual]))
    }

    fun updating(key: String, valueUpdater: Updater): Updater = ofObject { input ->
        if (input.containsKey(key)) input.with(key, valueUpdater.update(input[key]!!))
        else input
    }
}
