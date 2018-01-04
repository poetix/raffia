package com.codepoetics.raffia.baskets

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

    fun mapString(mapper: (String) -> String): Basket = ofString(mapper(asString()))

    fun flatMapString(mapper: (String) -> Basket): Basket = mapper(asString())

    fun mapNumber(mapper:(BigDecimal) -> BigDecimal): Basket = ofNumber(mapper(asNumber()))

    fun flatMapNumber(mapper: (BigDecimal) -> Basket): Basket = mapper(asNumber())

    fun mapBoolean(mapper: (Boolean) -> Boolean): Basket = ofBoolean(mapper(asBoolean()))

    fun flatMapBoolean(mapper: (Boolean) -> Basket): Basket = mapper(asBoolean())

    fun mapObject(mapper: (PropertySet) -> PropertySet): Basket = ofObject(mapper(asObject()))

    fun flatMapObject(mapper: (PropertySet) -> Basket): Basket = mapper(asObject())

    fun mapArray(mapper: (ArrayContents) -> ArrayContents): Basket = ofArray(mapper(asArray()))

    fun flatMapArray(mapper: (ArrayContents) -> Basket): Basket = mapper(asArray())

    fun asListOfString(): List<String> = asArray().map(Basket::asString).toList()

    fun asListOfNumber(): List<BigDecimal> = asArray().map(Basket::asNumber).toList()

    fun withProperty(key: String, value: Basket): Basket = ofObject(asObject().with(key, value))

    fun withoutProperty(key: String): Basket = ofObject(asObject().minus(key))

    fun withArrayItem(item: Basket): Basket = ofArray(asArray().plus(item))

    fun withArrayItem(index: Int, item: Basket): Basket = ofArray(asArray().with(index, item))

    fun withoutArrayItem(index: Int): Basket = ofArray(asArray().minus(index))

    fun getProperty(key: String): Basket? = asObject().get(key)

    fun entries(): List<ObjectEntry> = asObject().toList()

    fun getItem(index: Int): Basket = asArray().get(index)

    fun items(): List<Basket> = asArray().toList()

    fun values(): List<Basket> = when(this) {
        is Basket.ObjectBasket -> properties.map(ObjectEntry::value)
        is Basket.ArrayBasket -> items()
        is Basket.NullBasket -> emptyList()
        else -> listOf(this)
    }

    fun flatMapItems(itemFlatMapper: (Basket) -> Sequence<Basket>): Basket = ofArray(asArray().flatMap(itemFlatMapper))

    fun mapValues(valueMapper: (Basket) -> Basket): Basket = ofObject(asObject().mapValues(valueMapper))

    fun mapEntries(entryMapper: (ObjectEntry) -> Sequence<ObjectEntry>): Basket = ofObject(asObject().mapEntries(entryMapper))

    fun containsKey(key: String): Boolean = when(this) {
        is Basket.ObjectBasket -> properties.containsKey(key)
        else -> false
    }

     fun size(): Int = when(this) {
         is Basket.ObjectBasket -> properties.size()
         is Basket.ArrayBasket -> contents.size()
         is Basket.NullBasket -> 0
         else -> 1
     }

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