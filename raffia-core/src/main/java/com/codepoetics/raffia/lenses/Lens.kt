package com.codepoetics.raffia.lenses

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.PropertySet
import com.codepoetics.raffia.functions.BasketPredicate
import com.codepoetics.raffia.functions.Projector
import com.codepoetics.raffia.functions.Updater
import com.codepoetics.raffia.functions.ValuePredicate
import com.codepoetics.raffia.functions.Mapper
import com.codepoetics.raffia.operations.*
import com.codepoetics.raffia.paths.Path
import com.codepoetics.raffia.paths.PathSegment
import com.codepoetics.raffia.paths.Paths
import com.codepoetics.raffia.paths.segments.PathSegments
import com.codepoetics.raffia.predicates.BasketPredicates
import com.codepoetics.raffia.predicates.NumberPredicates
import com.codepoetics.raffia.predicates.StringPredicates
import org.pcollections.PVector
import org.pcollections.TreePVector

import java.math.BigDecimal
import java.util.Arrays

class Lens private constructor(private val segments: PVector<PathSegment>, val path: Path, private val projector: Projector<Basket>) : Projector<Basket> {

    operator fun plus(segment: PathSegment): Lens = lens(segments.plus(segment))

    fun to(arrayIndex: Int): Lens = plus(PathSegments.ofArrayIndex(arrayIndex))

    fun to(first: Int, vararg subsequent: Int): Lens = plus(PathSegments.ofArrayIndices(first, *subsequent))

    fun toAll(): Lens = plus(PathSegments.ofWildcard())

    fun to(objectKey: String): Lens = plus(PathSegments.ofObjectKey(objectKey))

    fun to(first: String, vararg remaining: String): Lens = plus(PathSegments.ofObjectKeys(first, *remaining))

    fun toAny(objectKey: String): Lens = plus(PathSegments.ofAny(objectKey))

    fun toMatching(valuePredicate: BasketPredicate): Lens = toMatching("?", valuePredicate)

    fun toMatching(representation: String, valuePredicate: BasketPredicate): Lens = plus(PathSegments.itemMatching(representation, valuePredicate))

    fun toHavingKey(key: String): Lens = toMatching("?(@.$key)", BasketPredicates.hasKey(key))

    fun updating(updater: Updater): Updater =
        if (path.isEmpty) updater
        else path.head().createUpdater(path, updater)

    fun update(updater: Updater, target: Basket): Basket = updating(updater).update(target)

    fun setting(value: Basket): Updater = updating(Setters.toBasket(value))

    fun setting(value: String): Updater = updating(Setters.toString(value))

    fun setting(value: BigDecimal): Updater = updating(Setters.toNumber(value))

    fun setting(value: Boolean): Updater = updating(Setters.toBoolean(value))

    fun settingNull(): Updater = updating(Setters.toNull())

    operator fun set(value: Basket, target: Basket): Basket = setting(value).update(target)

    operator fun set(value: String, target: Basket): Basket = setting(value).update(target)

    operator fun set(value: BigDecimal, target: Basket): Basket = setting(value).update(target)

    operator fun set(value: Boolean, target: Basket): Basket = setting(value).update(target)

    fun setNull(target: Basket): Basket = settingNull().update(target)

    fun getOne(basket: Basket): Basket = project(basket).single

    fun <T> getOne(basket: Basket, mapper: Mapper<Basket, T>): T = project(basket).map(mapper).single
    fun <T> getOne(basket: Basket, mapper: (Basket) -> T): T = project(basket).map(mapper).single()

    fun getAll(basket: Basket): List<Basket> = project(basket).toList()

    fun <T> getAll(basket: Basket, mapper: Mapper<Basket, T>): List<T> = project(basket).map(mapper).toList()
    fun <T> getAll(basket: Basket, mapper: (Basket) -> T): List<T> = project(basket).map(mapper)

    fun matching(value: String): BasketPredicate = matchingString(StringPredicates.isEqualTo(value))

    fun matchingString(matcher: ValuePredicate<String>): BasketPredicate = matching { basket -> basket.isString() && matcher.test(basket.asString()) }

    fun matching(expected: Boolean): BasketPredicate = matching { basket -> basket.isBoolean() && basket.asBoolean() == expected }

    val isTrue: BasketPredicate
        get() = matching(true)

    val isFalse: BasketPredicate
        get() = matching(false)

    fun matching(value: BigDecimal): BasketPredicate = matchingNumber(NumberPredicates.isEqualTo(value))

    fun matchingNumber(matcher: ValuePredicate<BigDecimal>): BasketPredicate = matching { basket -> basket.isNumber() && matcher.test(basket.asNumber()) }

    private fun matching(predicate: (Basket) -> Boolean): BasketPredicate = matching(object : BasketPredicate {
        override fun test(value: Basket): Boolean = predicate(value)
    })

    fun matching(itemPredicate: BasketPredicate): BasketPredicate = object : BasketPredicate {
        override fun test(value: Basket): Boolean = project(value).allMatch(itemPredicate)
    }

    fun exists(): BasketPredicate = object : BasketPredicate {
        override fun test(value: Basket): Boolean = !project(value).isEmpty
    }

    override fun project(basket: Basket): ProjectionResult<Basket> {
        return projector.project(basket)
    }

    fun getAllStrings(basket: Basket): List<String> = getAll(basket, Basket::asString)
    fun getAllNumbers(basket: Basket): List<BigDecimal> = getAll(basket, Basket::asNumber)
    fun getAllBooleans(basket: Basket): List<Boolean> = getAll(basket, Basket::asBoolean)
    fun getAllArrays(basket: Basket): List<ArrayContents> = getAll(basket, Basket::asArray)
    fun getAllObjects(basket: Basket): List<PropertySet> = getAll(basket, Basket::asObject)

    fun getOneString(basket: Basket): String = getOne(basket, Basket::asString)
    fun getOneNumber(basket: Basket): BigDecimal = getOne(basket, Basket::asNumber)
    fun getOneBoolean(basket: Basket): Boolean = getOne(basket, Basket::asBoolean)
    fun getOneArray(basket: Basket): ArrayContents = getOne(basket, Basket::asArray)
    fun getOneObject(basket: Basket): PropertySet = getOne(basket, Basket::asObject)

    fun <T> flatMap(next: Projector<T>): Projector<T> = Projectors.flatMap(this, next)

    fun <T> feedback(next: Mapper<Basket, Projector<T>>): Projector<T> = Projectors.feedback(this, next)

    override fun toString(): String = path.toString()

    companion object {

        @JvmStatic
        fun lens(path: String, vararg predicates: BasketPredicate): Lens {
            return lens(PathParser.parse(path, TreePVector.from(Arrays.asList(*predicates))))
        }

        @JvmStatic
        fun lens(): Lens {
            return lens(TreePVector.empty<PathSegment>())
        }

        private fun lens(pathSegments: PVector<PathSegment>): Lens {
            val path = Paths.create(pathSegments)
            val projector = if (path.isEmpty)
                Projectors.id
            else
                path.head().createProjector(path)
            return Lens(pathSegments, path, projector)
        }
    }
}
