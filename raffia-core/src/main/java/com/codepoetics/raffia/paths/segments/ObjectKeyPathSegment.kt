package com.codepoetics.raffia.paths.segments

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.PropertySet
import com.codepoetics.raffia.functions.Projector
import com.codepoetics.raffia.functions.Updater
import com.codepoetics.raffia.operations.ProjectionResult
import com.codepoetics.raffia.paths.PathSegmentMatchResult

internal class ObjectKeyPathSegment(private val keys: Collection<String>) : BasePathSegment() {

    override fun createUpdater(continuation: Updater): Updater {
        return object : Updater {

            override fun update(basket: Basket): Basket {
                if (!basket.isObject()) {
                    return basket
                }

                return updateProperties(basket.asObject())
            }

            private fun updateProperties(properties: PropertySet): Basket {
                var updated = properties
                for (key in keys) {
                    val atKey = updated[key]
                    if (atKey != null) {
                        updated = updated.with(key, continuation.update(atKey))
                    }
                }
                return Basket.ofObject(updated)
            }
        }
    }

    override fun createProjector(continuation: Projector<Basket>): Projector<Basket> {
        return object : Projector<Basket> {

            override fun project(basket: Basket): ProjectionResult<Basket> {
                if (!basket.isObject()) {
                    return ProjectionResult.empty<Basket>()
                }

                return projectProperties(basket.asObject())
            }

            private fun projectProperties(properties: PropertySet): ProjectionResult<Basket> {
                var result = ProjectionResult.empty<Basket>()
                for (key in keys) {
                    val atKey = properties[key]
                    if (atKey != null) {
                        result = result.add(continuation.project(atKey))
                    }
                }
                return result
            }
        }
    }

    override fun matchesIndex(index: Int): PathSegmentMatchResult {
        return PathSegmentMatchResult.UNMATCHED
    }

    override fun matchesKey(key: String): PathSegmentMatchResult {
        return if (keys.contains(key)) PathSegmentMatchResult.MATCHED_BOUND else PathSegmentMatchResult.UNMATCHED
    }

    override fun representation(): String {
        return if (keys.size == 1)
            "." + keys.iterator().next()
        else
            indexForm()
    }

    private fun indexForm(): String {
        val sb = StringBuilder()
        var first = true
        sb.append("[")
        for (key in keys) {
            if (first) {
                first = false
            } else {
                sb.append(", ")
            }
            sb.append("'").append(key).append("'")
        }
        sb.append("]")
        return sb.toString()
    }
}
