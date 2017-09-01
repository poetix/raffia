package com.codepoetics.raffia.paths.segments

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.PropertySet
import com.codepoetics.raffia.functions.Projector
import com.codepoetics.raffia.operations.ProjectionResult

internal abstract class StructProjector<T> : Projector<T> {

    override fun project(basket: Basket): ProjectionResult<T> {
        if (basket.isArray()) {
            return projectArray(basket.asArray())
        }

        if (basket.isObject()) {
            return projectObject(basket.asObject())
        }

        return ProjectionResult.empty<T>()
    }

    protected abstract fun projectObject(objectEntries: PropertySet): ProjectionResult<T>

    protected abstract fun projectArray(baskets: ArrayContents): ProjectionResult<T>
}
