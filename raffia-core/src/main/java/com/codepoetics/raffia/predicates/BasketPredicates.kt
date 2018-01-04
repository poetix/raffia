package com.codepoetics.raffia.predicates

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.PropertySet

import java.math.BigDecimal

typealias BasketPredicate = (Basket) -> Boolean
typealias ValuePredicate<T> = (T) -> Boolean

object BasketPredicates {

    @JvmStatic
    val isString: BasketPredicate =  { basket -> basket.isString() }

    @JvmStatic
    val isNumber: BasketPredicate =  { basket -> basket.isNumber() }

    @JvmStatic
    val isBoolean: BasketPredicate =  { basket -> basket.isBoolean() }

    @JvmStatic
    val isNull: BasketPredicate =  { basket -> basket.isNull() }

    @JvmStatic
    val isArray: BasketPredicate =  { basket -> basket.isArray() }

    @JvmStatic
    val isObject: BasketPredicate =  { basket -> basket.isObject() }

    @JvmStatic
    val isEmpty: BasketPredicate =  { basket -> basket.isEmpty() }

    @JvmStatic
    fun hasKey(key: String): BasketPredicate = isObject { value -> value.containsKey(key) }

    @JvmStatic
    fun hasKey(key: String, valuePredicate: BasketPredicate): BasketPredicate =
            isObject { value -> value.containsKey(key) && valuePredicate(value[key]!!) }

    @JvmStatic
    fun isString(expected: String): BasketPredicate =  { basket ->
        basket.isString() && basket.asString() == expected
    }

    @JvmStatic
    fun isNumber(expected: BigDecimal): BasketPredicate =  { basket ->
        basket.isNumber() && basket.asNumber() == expected
    }

    @JvmStatic
    fun isString(matcher: ValuePredicate<String>): BasketPredicate =  { basket ->
        basket.isString() && matcher(basket.asString())
    }

    @JvmStatic
    fun isNumber(matcher: ValuePredicate<BigDecimal>): BasketPredicate =  { basket ->
        basket.isNumber() && matcher(basket.asNumber())
    }

    @JvmStatic
    fun isBoolean(expected: Boolean): BasketPredicate =  { basket ->
        basket.isBoolean() && basket.asBoolean() == expected
    }

    @JvmStatic
    val isTrue = isBoolean(true)

    @JvmStatic
    val isFalse = isBoolean(false)

    @JvmStatic
    fun isBoolean(matcher: ValuePredicate<Boolean>): BasketPredicate =  { basket ->
        basket.isBoolean() && matcher(basket.asBoolean())
    }

    @JvmStatic
    fun isObject(matcher: ValuePredicate<PropertySet>): BasketPredicate =  { basket ->
        basket.isObject() && matcher(basket.asObject())
    }

    @JvmStatic
    fun isArray(matcher: ValuePredicate<ArrayContents>): BasketPredicate =  { basket ->
        basket.isArray() && matcher(basket.asArray())
    }
}
