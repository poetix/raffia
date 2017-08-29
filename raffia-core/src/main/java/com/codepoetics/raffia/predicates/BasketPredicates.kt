package com.codepoetics.raffia.predicates

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.PropertySet
import com.codepoetics.raffia.java.api.BasketPredicate
import com.codepoetics.raffia.java.api.ValuePredicate
import com.codepoetics.raffia.java.api.basketPredicate
import com.codepoetics.raffia.java.api.valuePredicate

import java.math.BigDecimal

object BasketPredicates {

    @JvmStatic
    val isString: BasketPredicate = basketPredicate { basket -> basket.isString() }

    @JvmStatic
    val isNumber: BasketPredicate = basketPredicate { basket -> basket.isNumber() }

    @JvmStatic
    val isBoolean: BasketPredicate = basketPredicate { basket -> basket.isBoolean() }

    @JvmStatic
    val isNull: BasketPredicate = basketPredicate { basket -> basket.isNull() }

    @JvmStatic
    val isArray: BasketPredicate = basketPredicate { basket -> basket.isArray() }

    @JvmStatic
    val isObject: BasketPredicate = basketPredicate { basket -> basket.isObject() }

    @JvmStatic
    val isEmpty: BasketPredicate = basketPredicate { basket -> basket.isEmpty() }

    @JvmStatic
    fun hasKey(key: String): BasketPredicate = isObject(valuePredicate<PropertySet> { value -> value.containsKey(key) })

    @JvmStatic
    fun hasKey(key: String, valuePredicate: BasketPredicate): BasketPredicate =
            isObject(valuePredicate<PropertySet> { value -> value.containsKey(key) && valuePredicate.test(value[key]!!) })

    @JvmStatic
    fun isString(expected: String): BasketPredicate = basketPredicate { basket ->
        basket.isString() && basket.asString() == expected
    }

    @JvmStatic
    fun isNumber(expected: BigDecimal): BasketPredicate = basketPredicate { basket ->
        basket.isNumber() && basket.asNumber() == expected
    }

    @JvmStatic
    fun isString(matcher: ValuePredicate<String>): BasketPredicate = basketPredicate { basket ->
        basket.isString() && matcher.test(basket.asString())
    }

    @JvmStatic
    fun isNumber(matcher: ValuePredicate<BigDecimal>): BasketPredicate = basketPredicate { basket ->
        basket.isNumber() && matcher.test(basket.asNumber())
    }

    @JvmStatic
    fun isBoolean(expected: Boolean): BasketPredicate = basketPredicate { basket ->
        basket.isBoolean() && basket.asBoolean() == expected
    }

    @JvmStatic
    val isTrue = isBoolean(true)

    @JvmStatic
    val isFalse = isBoolean(false)

    @JvmStatic
    fun isBoolean(matcher: ValuePredicate<Boolean>): BasketPredicate = basketPredicate { basket ->
        basket.isBoolean() && matcher.test(basket.asBoolean())
    }

    @JvmStatic
    fun isObject(matcher: ValuePredicate<PropertySet>): BasketPredicate = basketPredicate { basket ->
        basket.isObject() && matcher.test(basket.asObject())
    }

    @JvmStatic
    fun isArray(matcher: ValuePredicate<ArrayContents>): BasketPredicate = basketPredicate { basket ->
        basket.isArray() && matcher.test(basket.asArray())
    }
}
