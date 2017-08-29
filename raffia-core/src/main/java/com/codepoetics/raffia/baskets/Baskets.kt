package com.codepoetics.raffia.baskets

import com.codepoetics.raffia.functions.Mapper
import java.math.BigDecimal
import java.util.*

enum class BasketType {
    STRING,
    NUMBER,
    BOOLEAN,
    NULL,
    ARRAY,
    OBJECT
}

sealed class Basket {

    fun isString() = this is StringBasket
    fun asString() = if (this is StringBasket) stringValue else wrongType(BasketType.STRING)

    fun isNumber() = this is NumberBasket
    fun asNumber() = if (this is NumberBasket) numberValue else wrongType(BasketType.NUMBER)

    fun isBoolean() = this == TrueBasket || this == FalseBasket
    fun asBoolean() = when(this) {
        TrueBasket -> true
        FalseBasket -> false
        else -> wrongType(BasketType.BOOLEAN)
    }

    fun isNull() = this is NullBasket
    fun asNull() = if (this is NullBasket) null else wrongType(BasketType.NULL)

    fun isArray() = this is ArrayBasket
    fun asArray() = if (this is ArrayBasket) contents else wrongType(BasketType.ARRAY)

    fun isObject() = this is ObjectBasket
    fun asObject() = if (this is ObjectBasket) properties else wrongType(BasketType.OBJECT)

    @Suppress("UNCHECKED_CAST")
    fun <T> getValue() : T? = when(this) {
        is StringBasket -> stringValue as T
        is NumberBasket -> numberValue as T
        is TrueBasket -> true as T
        is FalseBasket -> false as T
        is NullBasket -> null
        is ObjectBasket -> properties as T
        is ArrayBasket -> contents as T
    }

    fun isEmpty() = when(this) {
        is ArrayBasket -> contents.isEmpty
        is ObjectBasket -> properties.isEmpty
        is NullBasket -> true
        else -> false
    }

    fun getType(): BasketType = when(this) {
        is ArrayBasket -> BasketType.ARRAY
        is ObjectBasket -> BasketType.OBJECT
        is StringBasket -> BasketType.STRING
        is NumberBasket -> BasketType.NUMBER
        is TrueBasket -> BasketType.BOOLEAN
        is FalseBasket -> BasketType.BOOLEAN
        is NullBasket -> BasketType.NULL
    }

    internal object TrueBasket : Basket() {
        override fun toString() = "<true>"
    }

    internal object FalseBasket : Basket() {
        override fun toString() = "<false>"
    }

    internal object NullBasket : Basket() {
        override fun toString() = "<null>"
    }

    internal data class StringBasket(val stringValue: String) : Basket() {
        override fun toString() = "\"$stringValue\""
    }

    internal data class NumberBasket(val numberValue: BigDecimal) : Basket() {
        override fun toString() = "<$numberValue>"
    }

    internal data class ObjectBasket(val properties: PropertySet) : Basket() {
        override fun toString() = properties.toString()
    }

    internal data class ArrayBasket(val contents: ArrayContents) : Basket() {
        override fun toString() = contents.toString()
    }

    private fun <T> wrongType(requested: BasketType): T {
        throw UnsupportedOperationException("Cannot get $requested value from basket of type ${getType()}")
    }

    fun mapString(mapper: Mapper<String, String>): Basket = ofString(mapper.map(asString()))

    fun flatMapString(mapper: Mapper<String, Basket>): Basket = mapper.map(asString())

    fun mapNumber(mapper: Mapper<BigDecimal, BigDecimal>): Basket = ofNumber(mapper.map(asNumber()))

    fun flatMapNumber(mapper: Mapper<BigDecimal, Basket>): Basket = mapper.map(asNumber())

    fun mapBoolean(mapper: Mapper<Boolean, Boolean>): Basket = ofBoolean(mapper.map(asBoolean()))

    fun flatMapBoolean(mapper: Mapper<Boolean, Basket>): Basket = mapper.map(asBoolean())

    fun mapObject(mapper: Mapper<PropertySet, PropertySet>): Basket = ofObject(mapper.map(asObject()))

    fun flatMapObject(mapper: Mapper<PropertySet, Basket>): Basket = mapper.map(asObject())

    fun mapArray(mapper: Mapper<ArrayContents, ArrayContents>): Basket = ofArray(mapper.map(asArray()))

    fun flatMapArray(mapper: Mapper<ArrayContents, Basket>): Basket = mapper.map(asArray())

    fun asListOfString(): List<String> = asArray().map { input -> input.asString() }

    fun asListOfNumber(): List<BigDecimal> = asArray().map { input -> input.asNumber() }

    fun withProperty(key: String, value: Basket): Basket = ofObject(asObject().with(key, value))

    fun withoutProperty(key: String): Basket = ofObject(asObject().minus(key))

    fun withArrayItem(item: Basket): Basket = ofArray(asArray().plus(item))

    fun withArrayItem(index: Int, item: Basket): Basket = ofArray(asArray().with(index, item))

    fun withoutArrayItem(index: Int): Basket = ofArray(asArray().minus(index))

    fun getProperty(key: String): Basket? = asObject().get(key)

    fun entries(): Iterable<ObjectEntry> = asObject()

    fun getItem(index: Int): Basket = asArray().get(index)

    fun items(): Iterable<Basket> = asArray()

    fun mapItems(itemMapper: Mapper<Basket, Basket>): Basket = ofArray(asArray().map(itemMapper))

    fun flatMapItems(itemFlatMapper: Mapper<Basket, List<Basket>>): Basket = ofArray(asArray().flatMap(itemFlatMapper))

    fun mapValues(valueMapper: Mapper<Basket, Basket>): Basket = ofObject(asObject().mapValues(valueMapper))

    fun mapEntries(entryMapper: Mapper<ObjectEntry, List<ObjectEntry>>): Basket = ofObject(asObject().mapEntries(entryMapper))

    companion object {

        @JvmStatic
        fun ofString(value: String): Basket = StringBasket(value)

        @JvmStatic
        fun ofNumber(value: BigDecimal): Basket = NumberBasket(value)

        @JvmStatic
        fun ofBoolean(value: Boolean): Basket = if (value) TrueBasket else FalseBasket

        @JvmStatic
        fun ofNull(): Basket = NullBasket

        @JvmStatic
        fun ofArray(vararg entries: Basket): Basket = ofArray(Arrays.asList(*entries))

        @JvmStatic
        fun ofArray(entries: Collection<Basket>): Basket = ofArray(ArrayContents.of(entries))

        @JvmStatic
        fun ofArray(entries: ArrayContents): Basket = ArrayBasket(entries)

        @JvmStatic
        fun ofObject(vararg entries: ObjectEntry): Basket = ofObject(PropertySet.of(*entries))

        @JvmStatic
        fun ofObject(entries: Collection<ObjectEntry>): Basket = ofObject(PropertySet.of(entries))

        @JvmStatic
        fun ofObject(properties: Map<String, Basket>): Basket = ofObject(PropertySet.of(properties))

        @JvmStatic
        fun ofObject(properties: PropertySet): Basket = ObjectBasket(properties)

    }
}