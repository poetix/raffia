package com.codepoetics.raffia.lenses

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.ObjectEntry
import com.codepoetics.raffia.baskets.PropertySet
import com.codepoetics.raffia.predicates.BasketPredicates
import com.codepoetics.raffia.predicates.NumberPredicates
import com.codepoetics.raffia.predicates.StringPredicates
import org.pcollections.TreePVector
import java.math.BigDecimal
import java.util.*

object Strands {

    @JvmStatic
    fun strand(path: String, vararg predicates: BasketPredicate): Strand = PathParser.parse(path, TreePVector.from(predicates.toList()))

}

interface Strand {
    val path: String
    operator fun get(basket: Basket): Sequence<Basket>

    fun update(basket: Basket, updater: (Basket) -> Basket): Basket

    fun set(basket: Basket, newValue: Basket): Basket = update(basket) { newValue }
    fun set(basket: Basket, newValue: String): Basket = set(basket, Basket.ofString(newValue))
    fun set(basket: Basket, newValue: BigDecimal): Basket = set(basket, Basket.ofNumber(newValue))
    fun set(basket: Basket, newValue: Boolean): Basket = set(basket, Basket.ofBoolean(newValue))
    fun setTrue(basket: Basket): Basket = set(basket, true)
    fun setFalse(basket: Basket): Basket = set(basket, false)

    fun then(next: Strand): Strand = ComposedStrand(this, next)
    fun to(vararg indices: Int): Strand = then(IndexStrand(indices))
    fun to(vararg keys: String): Strand = then(KeyStrand(keys))
    fun toDeep(key: String): Strand = then(DeepScanStrand(key))
    fun toAll(): Strand = then(WildcardStrand)
    fun toEverything(): Strand = then(DeepWildcardStrand)
    fun toMatching(predicate: (Basket) -> Boolean) = then(ConditionalStrand(predicate))

    fun getSingle(basket: Basket): Basket = get(basket).single()
    fun getString(basket: Basket): String = getSingle(basket).asString()
    fun getNumber(basket: Basket): BigDecimal = getSingle(basket).asNumber()
    fun getBoolean(basket: Basket): Boolean = getSingle(basket).asBoolean()

    fun getAll(basket: Basket): List<Basket> = get(basket).toList()
    fun getAllStrings(basket: Basket): List<String> = get(basket).map(Basket::asString).toList()
    fun getAllNumbers(basket: Basket): List<BigDecimal> = get(basket).map(Basket::asNumber).toList()
    fun getAllBooleans(basket: Basket): List<Boolean> = get(basket).map(Basket::asBoolean).toList()

    fun matches(string: String): (Basket) -> Boolean = matchesString(StringPredicates.isEqualTo(string))
    fun matches(number: BigDecimal): (Basket) -> Boolean = matchesNumber(NumberPredicates.isEqualTo(number))
    fun matches(boolean: Boolean): (Basket) -> Boolean = matchesBoolean { it == boolean }
    fun isTrue(): (Basket) -> Boolean = matches(true)
    fun isFalse(): (Basket) -> Boolean = matches(false)
    fun isNull(): (Basket) -> Boolean = { get(it).filter(Basket::isNull).any() }

    fun matchesString(predicate: (String) -> Boolean): (Basket) -> Boolean = { get(it).filter(BasketPredicates.isString(predicate)).any() }
    fun matchesNumber(predicate: (BigDecimal) -> Boolean): (Basket) -> Boolean = { get(it).filter(BasketPredicates.isNumber(predicate)).any() }
    fun matchesBoolean(predicate: (Boolean) -> Boolean): (Basket) -> Boolean = { get(it).filter(BasketPredicates.isBoolean(predicate)).any() }
}

object RootStrand : Strand {
    override val path = "\$"

    override fun get(basket: Basket): Sequence<Basket> = sequenceOf(basket)

    override fun update(basket: Basket, updater: (Basket) -> Basket): Basket = updater(basket)
}

data class ComposedStrand(val first: Strand, val second: Strand): Strand {
    override val path = "${first.path}${second.path}"

    override fun get(basket: Basket): Sequence<Basket> = first[basket].flatMap { second[it] }

    override fun update(basket: Basket, updater: (Basket) -> Basket): Basket = first.update(basket) { second.update(it, updater) }
}

data class IndexStrand(val indices: IntArray): Strand {
    override val path = indices.map(Int::toString).joinToString(",","[", "]")

    override fun get(basket: Basket): Sequence<Basket> = when(basket) {
        is Basket.ArrayBasket -> fromContents(basket.contents)
        else -> emptySequence()
    }

    private fun fromContents(contents: ArrayContents): Sequence<Basket> =
            indicesInBounds(contents.size()).map { contents[it] }

    private fun indicesInBounds(size: Int): Sequence<Int> = indices.asSequence()
            .map { relativeIndex(it, size) }
            .filter { it >= 0 && it < size }

    private fun relativeIndex(index: Int, size: Int): Int {
        val relativeIndex = if (index < 0) size + index else index
        return relativeIndex
    }

    private fun updateContents(contents: ArrayContents, updater: (Basket) -> Basket): ArrayContents =
            indicesInBounds(contents.size()).fold(contents) { c, index -> c.with(index, updater(contents[index])) }

    override fun update(basket: Basket, updater: (Basket) -> Basket): Basket = when(basket) {
        is Basket.ArrayBasket -> Basket.ofArray(updateContents(basket.contents, updater))
        else -> basket
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        return Arrays.equals(indices, (other as IndexStrand).indices)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(indices)
    }
}

data class KeyStrand(val keys: Array<out String>): Strand {
    override val path = if (keys.size == 1) ".${keys[0]}" else keys.map { "'$it'" }.joinToString(",", "[", "]")

    override fun get(basket: Basket): Sequence<Basket> = when(basket) {
        is Basket.ObjectBasket -> keysInBasket(basket).map { basket.getProperty(it)!! }
        else -> emptySequence()
    }

    override fun update(basket: Basket, updater: (Basket) -> Basket): Basket = when(basket) {
        is Basket.ObjectBasket -> Basket.ofObject(
                keysInBasket(basket).fold(basket.properties) { p, k -> p.with(k, updater(p[k]!!)) }
        )
        else -> basket
    }

    private fun keysInBasket(basket: Basket): Sequence<String> = keys.asSequence().filter(basket::containsKey)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        return Arrays.equals(keys, (other as KeyStrand).keys)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(keys)
    }
}

data class ArraySliceStrand(val start:Int, val end:Int): Strand {
    override val path = (if (start == 0) "" else start.toString()) +  ":" + (if (end == Int.MAX_VALUE) "" else end.toString())

    override fun get(basket: Basket): Sequence<Basket> = when(basket) {
        is Basket.ArrayBasket -> sliceIndices(basket.size()).map(basket::getItem)
        else -> emptySequence()
    }

    private fun sliceIndices(size: Int): Sequence<Int> {
        val relativeStart = bound(start, size)
        val relativeEnd = if (end == Int.MAX_VALUE) size else bound(end, size)
        return (relativeStart until relativeEnd).asSequence()
    }

    private fun bound(index: Int, size: Int): Int = Math.min(size - 1, Math.max(0, if (index < 0) size + index  else index))

    override fun update(basket: Basket, updater: (Basket) -> Basket) : Basket = when(basket) {
        is Basket.ArrayBasket -> Basket.ofArray(
                sliceIndices(basket.size()).fold(basket.contents) { c, i -> c.with(i, updater(c[i])) }
        )
        else -> basket
    }
}

data class ConditionalStrand(val predicate: (Basket) -> Boolean): Strand {
    override val path = "[?]"

    override fun get(basket: Basket): Sequence<Basket> = when(basket) {
        is Basket.ObjectBasket -> basket.properties.asSequence().map(ObjectEntry::value).filter(predicate)
        is Basket.ArrayBasket -> basket.contents.asSequence().filter(predicate)
        else -> if (predicate(basket)) sequenceOf(basket) else emptySequence()
    }

    override fun update(basket: Basket, updater: (Basket) -> Basket): Basket = when(basket) {
        is Basket.ObjectBasket -> Basket.ofObject(basket.properties.asSequence()
                .filter { predicate(it.value) }
                .fold(basket.properties) { p, e -> p.with(e.key, updater(e.value)) })
        is Basket.ArrayBasket -> Basket.ofArray(basket.contents.mapIndexed { index, item -> index to item }
                .filter { predicate(it.second) }
                .fold(basket.contents) { c, e -> c.with(e.first, updater(e.second)) })
        else -> if (predicate(basket)) updater(basket) else basket
    }

}

data class DeepScanStrand(val key: String): Strand {
    override val path = "..${key}"

    override fun get(basket: Basket): Sequence<Basket> = when(basket) {
        is Basket.ObjectBasket -> basket.properties.asSequence().flatMap {
            if (it.key == key) sequenceOf(it.value) else get(it.value)
        }
        is Basket.ArrayBasket -> basket.contents.asSequence().flatMap(this::get)
        else -> emptySequence()
    }

    override fun update(basket: Basket, updater: (Basket) -> Basket): Basket = when(basket) {
        is Basket.ObjectBasket -> Basket.ofObject(PropertySet.of(basket.properties.map {
            ObjectEntry.of(it.key, if (it.key == key) updater(it.value) else update(it.value, updater))
        }))
        is Basket.ArrayBasket -> Basket.ofArray(basket.contents.map { update(it, updater) })
        else -> basket
    }
}

object WildcardStrand: Strand {
    override val path = ".*"

    override fun get(basket: Basket): Sequence<Basket> = when(basket) {
        is Basket.ArrayBasket -> basket.contents.asSequence()
        is Basket.ObjectBasket -> basket.properties.asSequence().map(ObjectEntry::value)
        else -> sequenceOf(basket)
    }

    override fun update(basket: Basket, updater: (Basket) -> Basket): Basket = when(basket) {
        is Basket.ArrayBasket -> Basket.ofArray(basket.contents.map(updater))
        is Basket.ObjectBasket -> Basket.ofObject(basket.properties.mapValues(updater))
        else -> updater(basket)
    }

}

object DeepWildcardStrand: Strand {
    override val path = "..*"

    override fun get(basket: Basket): Sequence<Basket> = when(basket) {
        is Basket.ArrayBasket -> basket.contents.let { it.asSequence() + it.asSequence().flatMap(this::get) }
        is Basket.ObjectBasket -> basket.properties.let {
            it.asSequence().map(ObjectEntry::value) + it.asSequence().map(ObjectEntry::value).flatMap(this::get)
        }
        else -> sequenceOf(basket)
    }

    override fun update(basket: Basket, updater: (Basket) -> Basket): Basket = when(basket) {
        is Basket.ArrayBasket -> Basket.ofArray(basket.contents.map { updater(update(it, updater)) })
        is Basket.ObjectBasket -> Basket.ofObject(basket.properties.mapValues(updater))
        else -> updater(basket)
    }

}