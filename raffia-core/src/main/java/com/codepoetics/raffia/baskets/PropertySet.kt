package com.codepoetics.raffia.baskets

import com.codepoetics.raffia.java.api.Mapper
import org.pcollections.HashTreePMap
import org.pcollections.PMap
import org.pcollections.PVector
import org.pcollections.TreePVector

import java.util.*

data class ObjectEntry(val key: String, val value: Basket) {
    override fun toString(): String {
        return key + ": " + value
    }

    companion object {
        @JvmStatic
        fun of(key: String, value: Basket): ObjectEntry {
            return ObjectEntry(key, value)
        }
    }
}

class PropertySet private constructor(private val keys: PVector<String>, private val properties: PMap<String, Basket>)
    : Iterable<ObjectEntry> {

    fun with(key: String, basket: Basket): PropertySet =
        if (properties.containsKey(key))
            PropertySet(keys, properties.plus(key, basket))
        else
            PropertySet(keys.plus(key), properties.plus(key, basket))


    fun size(): Int = properties.size

    operator fun get(key: String): Basket? = properties[key]

    fun containsKey(key: String): Boolean = properties.containsKey(key)

    operator fun minus(key: String): PropertySet = PropertySet(keys.minus(key), properties.minus(key))

    fun toMap(): Map<String, Basket> {
        val result = LinkedHashMap<String, Basket>()
        keys.forEach { result.put(it, properties[it]!!) }
        return result
    }

    val isEmpty: Boolean
        get() = properties.isEmpty()

    override fun equals(other: Any?): Boolean {
        return this === other || other is PropertySet
                && PropertySet::class.java.cast(other).keys == keys
                && PropertySet::class.java.cast(other).properties == properties
    }

    override fun hashCode(): Int {
        return Objects.hash(keys.hashCode(), properties.hashCode())
    }

    override fun toString(): String {
        return toMap().toString()
    }

    override fun iterator(): Iterator<ObjectEntry> {
        val keyIterator = keys.iterator()
        return object : Iterator<ObjectEntry> {
            override fun hasNext(): Boolean = keyIterator.hasNext()

            override fun next(): ObjectEntry = keyIterator.next().let { ObjectEntry.of(it, properties[it]!!) }
        }
    }

    fun mapValues(valueMapper: Mapper<Basket, Basket>): PropertySet =
        PropertySet.of(map { entry -> ObjectEntry.of(entry.key, valueMapper.map(entry.value)) })

    fun mapEntries(entryMapper: Mapper<ObjectEntry, List<ObjectEntry>>): PropertySet =
            PropertySet.of(flatMap { entry -> entryMapper.map(entry) })

    companion object {

        @JvmStatic
        fun of(vararg entries: ObjectEntry): PropertySet {
            return of(Arrays.asList(*entries))
        }

        @JvmStatic
        fun of(entries: Collection<ObjectEntry>): PropertySet {
            val properties = LinkedHashMap<String, Basket>()
            for (entry in entries) {
                properties.put(entry.key, entry.value)
            }
            return of(properties)
        }

        @JvmStatic
        fun of(properties: Map<String, Basket>): PropertySet {
            return PropertySet(TreePVector.from(properties.keys), HashTreePMap.from(properties))
        }
    }
}
