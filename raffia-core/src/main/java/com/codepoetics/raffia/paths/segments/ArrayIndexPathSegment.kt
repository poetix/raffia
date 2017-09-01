package com.codepoetics.raffia.paths.segments

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.functions.Projector
import com.codepoetics.raffia.functions.Updater
import com.codepoetics.raffia.operations.ProjectionResult
import com.codepoetics.raffia.paths.PathSegmentMatchResult

internal class ArrayIndexPathSegment(private val indices: Collection<Int>) : BasePathSegment() {

    override fun createUpdater(continuation: Updater): Updater {
        return object : Updater {
            override fun update(basket: Basket): Basket {
                if (basket.isArray()) {
                    return updateArray(basket.asArray())
                }

                return basket
            }

            private fun updateArray(items: ArrayContents): Basket {
                var updated = items
                for (index in indices) {
                    val actual = if (index < 0) items.size() + index else index
                    if (actual < items.size()) {
                        updated = updated.with(actual, continuation.update(updated[actual]))
                    }
                }
                return Basket.ofArray(updated)
            }
        }
    }

    override fun createProjector(continuation: Projector<Basket>): Projector<Basket> {
        return object : Projector<Basket> {
            override fun project(basket: Basket): ProjectionResult<Basket> {
                if (basket.isArray()) {
                    return projectArray(basket.asArray())
                }

                return ProjectionResult.empty<Basket>()
            }

            private fun projectArray(items: ArrayContents): ProjectionResult<Basket> {
                var result = ProjectionResult.empty<Basket>()
                for (index in indices) {
                    val actual = if (index < 0) items.size() + index else index
                    if (actual >= 0 && actual < items.size()) {
                        result = result.add(continuation.project(items[actual]))
                    }
                }
                return result
            }
        }
    }

    override fun matchesIndex(index: Int): PathSegmentMatchResult {
        return if (indices.contains(index)) PathSegmentMatchResult.MATCHED_BOUND else PathSegmentMatchResult.UNMATCHED
    }

    override fun matchesKey(key: String): PathSegmentMatchResult {
        return PathSegmentMatchResult.UNMATCHED
    }

    override fun representation(): String {
        return indices.toString()
    }

}
