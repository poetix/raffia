package com.codepoetics.raffia.operations

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.functions.BasketPredicate
import com.codepoetics.raffia.functions.Projector
import com.codepoetics.raffia.functions.Mapper
import com.codepoetics.raffia.functions.projector

object Projectors {
    
    @JvmStatic
    val asArray: Projector<ArrayContents> = projector { basket ->
        if (basket.isArray()) ProjectionResult.ofSingle(basket.asArray()) else ProjectionResult.empty<ArrayContents>()
    }

    @JvmStatic
    val id = projector { basket -> ProjectionResult.ofSingle(basket) }

    private val ALWAYS_EMPTY = projector { ProjectionResult.empty<Any>() }

    @JvmStatic
    fun <T> constant(value: T): Projector<T> {
        return projector { ProjectionResult.ofSingle(value) }
    }

    @JvmStatic
    fun <T> alwaysEmpty(): Projector<T> = ALWAYS_EMPTY as Projector<T>

    @JvmStatic
    fun <T> branch(predicate: BasketPredicate, ifTrue: Projector<T>, ifFalse: Projector<T>): Projector<T> =
            projector { basket -> if (predicate.test(basket)) ifTrue.project(basket) else ifFalse.project(basket) }

    @JvmStatic
    fun <T> atIndex(index: Int, itemProjection: Projector<T>): Projector<T> = projector { basket ->
        if (!basket.isArray()) {
            return@projector ProjectionResult.empty<T>()
        }

        val contents = basket.asArray()
        val actual = if (index < 0) contents.size() + index else index
        if (actual < 0 || actual >= contents.size()) {
            return@projector ProjectionResult.empty<T>()
        }

        itemProjection.project(contents[actual])
    }

    @JvmStatic
    fun <T> atKey(key: String, itemProjection: Projector<T>): Projector<T> {
        return projector { basket ->
            if (basket.isObject()) {
                return@projector ProjectionResult.empty<T>()
            }

            val properties = basket.asObject()
            if (!properties.containsKey(key)) {
                return@projector ProjectionResult.empty<T>()
            }

            itemProjection.project(properties[key]!!)
        }
    }

    @JvmStatic
    fun <O> flatMap(left: Projector<Basket>, right: Projector<O>): Projector<O> {
        return projector { basket ->
            val result = ProjectionResult.empty<O>()
            for (item in left.project(basket)) {
                result.add(right.project(item))
            }
            result
        }
    }

    @JvmStatic
    fun <I, O> feedback(left: Projector<I>, right: Mapper<I, Projector<O>>): Projector<O> {
        return projector { basket ->
            var result = ProjectionResult.empty<O>()

            for (item in left.project(basket)) {
                result = result.add(right.map(item).project(basket))
            }

            result
        }
    }
}
