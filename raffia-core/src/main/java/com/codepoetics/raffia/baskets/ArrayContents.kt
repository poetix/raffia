package com.codepoetics.raffia.baskets

import com.codepoetics.raffia.functions.Mapper
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

    fun toList(): List<Basket> = contents

    fun <T> map(mapper: Mapper<Basket, T>): List<T> = contents.map { item -> mapper.map(item) }

    fun <T> flatMap(itemFlatMapper: Mapper<Basket, List<T>>): List<T> = contents.flatMap { item -> itemFlatMapper.map(item) }

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

        @JvmStatic
        fun empty(): ArrayContents {
            return ArrayContents(TreePVector.empty<Basket>())
        }

        @JvmStatic
        fun of(vararg contents: Basket): ArrayContents {
            return of(Arrays.asList(*contents))
        }

        @JvmStatic
        fun of(contents: Collection<Basket>): ArrayContents {
            return ArrayContents(TreePVector.from(contents))
        }
    }

}
