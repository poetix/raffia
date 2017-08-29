package com.codepoetics.raffia.writers

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.ObjectEntry
import com.codepoetics.raffia.baskets.PropertySet
import org.pcollections.PVector
import org.pcollections.TreePVector

import java.math.BigDecimal

object Writers {

    @JvmStatic
    fun weaving(): BasketWeavingWriter {
        return ValueWeaver(null)
    }

    private abstract class ScalarPromotingWeaver internal constructor() : BasketWeavingWriter {
        override fun add(value: String): BasketWeavingWriter = add(Basket.ofString(value))
        override fun add(value: BigDecimal): BasketWeavingWriter = add(Basket.ofNumber(value))
        override fun add(value: Boolean): BasketWeavingWriter = add(Basket.ofBoolean(value))
        override fun addNull(): BasketWeavingWriter = add(Basket.ofNull())
    }

    private class ValueWeaver internal constructor(private val value: Basket?) : ScalarPromotingWeaver() {

        override fun add(basket: Basket): ScalarPromotingWeaver =
            if (value == null) ValueWeaver(basket)
            else throw IllegalStateException("add() called when writing value, but value already set")

        override fun weave(): Basket =
            if (value != null) value
            else throw IllegalStateException("weave called on incomplete value")


        override fun beginObject(): BasketWeavingWriter {
            return ObjectWeaver(this, null, TreePVector.empty<ObjectEntry>())
        }

        override fun beginArray(): BasketWeavingWriter {
            return ArrayWeaver(this, ArrayContents.empty())
        }

        override fun end(): BasketWeavingWriter {
            throw IllegalStateException("end called without corresponding beginObject or beginArray")
        }

        override fun key(key: String): BasketWeavingWriter {
            throw IllegalStateException("key called, but not writing object")
        }
    }

    private class ArrayWeaver
        internal constructor(
                private val parent: ScalarPromotingWeaver,
                private val contents: ArrayContents) : ScalarPromotingWeaver() {

        override fun add(basket: Basket): ScalarPromotingWeaver = ArrayWeaver(parent, contents.plus(basket))

        override fun weave(): Basket = Basket.ofArray(contents)

        override fun beginObject(): BasketWeavingWriter = ObjectWeaver(this, null, TreePVector.empty<ObjectEntry>())

        override fun end(): BasketWeavingWriter = parent.add(weave())

        override fun beginArray(): BasketWeavingWriter = ArrayWeaver(this, ArrayContents.empty())

        override fun key(key: String): BasketWeavingWriter = throw IllegalStateException("key() called while writing array")
    }

    private class ObjectWeaver
        internal constructor(
                private val parent: ScalarPromotingWeaver,
                private val key: String?,
                private val contents: PVector<ObjectEntry>) : ScalarPromotingWeaver() {

        override fun add(basket: Basket): ScalarPromotingWeaver =
            if (key != null) ObjectWeaver(parent, null, contents.plus(ObjectEntry.of(key, basket)))
            else throw IllegalStateException("add() called while writing object, but key not given")

        override fun weave(): Basket = Basket.ofObject(PropertySet.of(contents))

        override fun beginObject(): BasketWeavingWriter = ObjectWeaver(this, null, TreePVector.empty<ObjectEntry>())

        override fun end(): BasketWeavingWriter = parent.add(weave())

        override fun beginArray(): BasketWeavingWriter =
            if (key != null) ArrayWeaver(this, ArrayContents.empty())
            else throw IllegalStateException("beginArray() called while writing object, but key not given")

        override fun key(key: String): BasketWeavingWriter =
            if (this.key == null) ObjectWeaver(parent, key, contents)
            else throw IllegalStateException("key() called, but key already set")
    }
}
