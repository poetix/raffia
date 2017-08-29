package com.codepoetics.raffia.paths.segments

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.PropertySet
import com.codepoetics.raffia.java.api.Updater

internal abstract class StructUpdater : Updater {
    override fun update(basket: Basket): Basket {
        if (basket.isArray()) {
            return updateArray(basket.asArray())
        }

        if (basket.isObject()) {
            return updateObject(basket.asObject())
        }

        return basket
    }

    protected abstract fun updateArray(contents: ArrayContents): Basket

    protected abstract fun updateObject(properties: PropertySet): Basket
}
