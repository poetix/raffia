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
        return PersistentValueWeaver(null)
    }

    @JvmStatic
    fun weavingTransient(): BasketWeavingWriter {
        return TransientValueWeaver(null)
    }
}

private abstract class ScalarPromotingWeaver internal constructor() : BasketWeavingWriter {
    override fun add(value: String): BasketWeavingWriter = add(Basket.ofString(value))
    override fun add(value: BigDecimal): BasketWeavingWriter = add(Basket.ofNumber(value))
    override fun add(value: Boolean): BasketWeavingWriter = add(Basket.ofBoolean(value))
    override fun addNull(): BasketWeavingWriter = add(Basket.ofNull())
}

private class PersistentValueWeaver(private val value: Basket?) : ScalarPromotingWeaver() {

    override fun add(basket: Basket): ScalarPromotingWeaver =
        if (value == null) PersistentValueWeaver(basket)
        else throw IllegalStateException("add() called when writing value, but value already set")

    override fun weave(): Basket =
        if (value != null) value
        else throw IllegalStateException("weave called on incomplete value")


    override fun beginObject(): BasketWeavingWriter {
        return PersistentObjectWeaver(this, null, TreePVector.empty<ObjectEntry>())
    }

    override fun beginArray(): BasketWeavingWriter {
        return PersistentArrayWeaver(this, ArrayContents.empty())
    }

    override fun end(): BasketWeavingWriter {
        throw IllegalStateException("end called without corresponding beginObject or beginArray")
    }

    override fun key(key: String): BasketWeavingWriter {
        throw IllegalStateException("key called, but not writing object")
    }
}

private class TransientValueWeaver(var value: Basket?) : ScalarPromotingWeaver() {

    override fun add(basket: Basket): ScalarPromotingWeaver {
        if (value == null) {
            value = basket
        } else {
            throw IllegalStateException("add() called when writing value, but value already set")
        }
        return this
    }

    override fun weave(): Basket =
        if (value != null) value!!
        else throw IllegalStateException("weave called on incomplete value")


    override fun beginObject(): BasketWeavingWriter {
        return TransientObjectWeaver(this, null, mutableListOf())
    }

    override fun beginArray(): BasketWeavingWriter {
        return TransientArrayWeaver(this, mutableListOf())
    }

    override fun end(): BasketWeavingWriter {
        throw IllegalStateException("end called without corresponding beginObject or beginArray")
    }

    override fun key(key: String): BasketWeavingWriter {
        throw IllegalStateException("key called, but not writing object")
    }
}

private class PersistentArrayWeaver(
            val parent: ScalarPromotingWeaver,
            val contents: ArrayContents) : ScalarPromotingWeaver() {

    override fun add(basket: Basket): ScalarPromotingWeaver = PersistentArrayWeaver(parent, contents.plus(basket))

    override fun weave(): Basket = Basket.ofArray(contents)

    override fun beginObject(): BasketWeavingWriter = PersistentObjectWeaver(this, null, TreePVector.empty<ObjectEntry>())

    override fun end(): BasketWeavingWriter = parent.add(weave())

    override fun beginArray(): BasketWeavingWriter = PersistentArrayWeaver(this, ArrayContents.empty())

    override fun key(key: String): BasketWeavingWriter = throw IllegalStateException("key() called while writing array")
}

private class TransientArrayWeaver(
        val parent: ScalarPromotingWeaver,
        val contents: MutableList<Basket>) : ScalarPromotingWeaver() {

    override fun add(basket: Basket): ScalarPromotingWeaver {
        contents.add(basket)
        return this
    }

    override fun weave(): Basket = Basket.ofArray(contents)

    override fun beginObject(): BasketWeavingWriter = TransientObjectWeaver(this, null, mutableListOf())

    override fun end(): BasketWeavingWriter = parent.add(weave())

    override fun beginArray(): BasketWeavingWriter = TransientArrayWeaver(this, mutableListOf<Basket>())

    override fun key(key: String): BasketWeavingWriter = throw IllegalStateException("key() called while writing array")
}

private class PersistentObjectWeaver(
            val parent: ScalarPromotingWeaver,
            val key: String?,
            val contents: PVector<ObjectEntry>) : ScalarPromotingWeaver() {

    override fun add(basket: Basket): ScalarPromotingWeaver =
        if (key != null) PersistentObjectWeaver(parent, null, contents.plus(ObjectEntry.of(key, basket)))
        else throw IllegalStateException("add() called while writing object, but key not given")

    override fun weave(): Basket = Basket.ofObject(PropertySet.of(contents))

    override fun beginObject(): BasketWeavingWriter = PersistentObjectWeaver(this, null, TreePVector.empty<ObjectEntry>())

    override fun end(): BasketWeavingWriter = parent.add(weave())

    override fun beginArray(): BasketWeavingWriter =
        if (key != null) PersistentArrayWeaver(this, ArrayContents.empty())
        else throw IllegalStateException("beginArray() called while writing object, but key not given")

    override fun key(key: String): BasketWeavingWriter =
        if (this.key == null) PersistentObjectWeaver(parent, key, contents)
        else throw IllegalStateException("key() called, but key already set")
}

private class TransientObjectWeaver(
        val parent: ScalarPromotingWeaver,
        var key: String?,
        val contents: MutableList<ObjectEntry>) : ScalarPromotingWeaver() {

    override fun add(basket: Basket): ScalarPromotingWeaver {
        if (key == null) throw IllegalStateException("add() called while writing object, but key not given")
        contents.add(ObjectEntry.of(key!!, basket))
        key = null
        return this
    }

    override fun weave(): Basket = Basket.ofObject(PropertySet.of(contents))

    override fun beginObject(): BasketWeavingWriter = TransientObjectWeaver(this, null, mutableListOf())

    override fun end(): BasketWeavingWriter = parent.add(weave())

    override fun beginArray(): BasketWeavingWriter =
            if (key != null) TransientArrayWeaver(this, mutableListOf())
            else throw IllegalStateException("beginArray() called while writing object, but key not given")

    override fun key(key: String): BasketWeavingWriter {
        if (this.key != null) throw IllegalStateException("key() called, but key already set")
        this.key = key
        return this
    }
}

