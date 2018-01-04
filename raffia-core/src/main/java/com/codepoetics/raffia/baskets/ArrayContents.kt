package com.codepoetics.raffia.baskets

import org.pcollections.PVector
import org.pcollections.TreePVector

import java.util.*

class ArrayContents private constructor(private val contents: PVector<Basket>) : Iterable<Basket> {

    operator fun get(index: Int): Basket = contents[index]

    fun size(): Int = contents.size

    operator fun plus(basket: Basket): ArrayContents = ArrayContents(contents.plus(basket))

    fun plus(index: Int, basket: Basket): ArrayContents = ArrayContents(contents.plus(index, basket))

    fun with(index: Int, basket: Basket): ArrayContents = ArrayContents(contents.with(index, basket))

    fun plusAll(baskets: Collection<Basket>): ArrayContents  = ArrayContents(contents.plusAll(baskets))

    operator fun minus(index: Int): ArrayContents = ArrayContents(contents.minus(index))

    fun map(mapper: (Basket) -> Basket): ArrayContents = ArrayContents.of(contents.map(mapper))

    fun flatMap(itemFlatMapper: (Basket) -> Sequence<Basket>): ArrayContents = ArrayContents.of(contents.asSequence().flatMap(itemFlatMapper))

    val isEmpty: Boolean
            get() = contents.isEmpty()

    override fun iterator(): Iterator<Basket> = contents.iterator()

    override fun equals(other: Any?): Boolean {
        return this === other || other is ArrayContents && other.contents == contents
    }

    override fun hashCode(): Int {
        return contents.hashCode()
    }

    override fun toString(): String {
        return contents.toString()
    }

    companion object {

        val empty = ArrayContents(TreePVector.empty())

        @JvmStatic
        fun empty(): ArrayContents = empty

        @JvmStatic
        fun of(vararg contents: Basket): ArrayContents = of(Arrays.asList(*contents))

        @JvmStatic
        fun of(contents: Collection<Basket>): ArrayContents =ArrayContents(TreePVector.from(contents))

        fun of(contents: Sequence<Basket>): ArrayContents = ArrayContents(contents.fold(TreePVector.empty<Basket>(), TreePVector<Basket>::plus))
    }

}
