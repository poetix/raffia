package com.codepoetics.raffia.paths.segments

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.operations.ProjectionResult
import com.codepoetics.raffia.java.api.Projector
import com.codepoetics.raffia.java.api.Updater
import com.codepoetics.raffia.paths.PathSegmentMatchResult

internal class ArraySlicePathSegment(private val startIndex: Int, private val endIndex: Int) : BasePathSegment() {

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

                val actualStart = getActualStart(startIndex, items.size())
                val actualEnd = getActualEnd(endIndex, items.size())

                for (i in actualStart..actualEnd - 1) {
                    updated = updated.with(i, continuation.update(updated[i]))
                }

                return Basket.ofArray(updated)
            }
        }
    }

    private fun getActualStart(startIndex: Int, arraySize: Int): Int {
        return if (startIndex == PathSegments.LOWER_UNBOUNDED)
            0
        else if (startIndex < 0) arraySize + startIndex else startIndex
    }

    private fun getActualEnd(endIndex: Int, arraySize: Int): Int {
        return if (endIndex == PathSegments.UPPER_UNBOUNDED)
            arraySize
        else if (endIndex < 0) arraySize + endIndex else Math.min(endIndex, arraySize)
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

                val actualStart = getActualStart(startIndex, items.size())
                val actualEnd = getActualEnd(endIndex, items.size())

                for (i in actualStart..actualEnd - 1) {
                    result = result.add(continuation.project(items[i]))
                }
                return result
            }
        }
    }

    override fun matchesIndex(index: Int): PathSegmentMatchResult {
        return if (index >= startIndex && index < endIndex) PathSegmentMatchResult.MATCHED_BOUND else PathSegmentMatchResult.UNMATCHED
    }

    override fun matchesKey(key: String): PathSegmentMatchResult {
        return PathSegmentMatchResult.UNMATCHED
    }

    override fun representation(): String {
        val sb = StringBuilder()
        sb.append("[")
        if (startIndex != PathSegments.LOWER_UNBOUNDED) {
            sb.append(startIndex)
        }
        sb.append(":")
        if (endIndex != PathSegments.UPPER_UNBOUNDED) {
            sb.append(endIndex)
        }
        sb.append("]")
        return sb.toString()
    }

}
