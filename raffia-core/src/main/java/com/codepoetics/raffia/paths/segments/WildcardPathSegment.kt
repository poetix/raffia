package com.codepoetics.raffia.paths.segments

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.ObjectEntry
import com.codepoetics.raffia.baskets.PropertySet
import com.codepoetics.raffia.functions.Projector
import com.codepoetics.raffia.functions.Updater
import com.codepoetics.raffia.operations.ProjectionResult
import com.codepoetics.raffia.paths.PathSegmentMatchResult
import java.util.*

internal class WildcardPathSegment : BasePathSegment() {

    public override fun createUpdater(continuation: Updater): Updater {
        return object : StructUpdater() {
            public override fun updateArray(items: ArrayContents): Basket {
                val updated = ArrayList<Basket>(items.size())

                for (basket in items) {
                    updated.add(continuation.update(basket))
                }

                return Basket.ofArray(updated)
            }

            public override fun updateObject(properties: PropertySet): Basket {
                val entries = ArrayList<ObjectEntry>(properties.size())

                for ((key, value) in properties) {
                    entries.add(ObjectEntry.of(key, continuation.update(value)))
                }

                return Basket.ofObject(entries)
            }
        }
    }

    public override fun createProjector(continuation: Projector<Basket>): Projector<Basket> {
        return object : StructProjector<Basket>() {
            public override fun projectArray(items: ArrayContents): ProjectionResult<Basket> {
                var result = ProjectionResult.empty<Basket>()
                for (basket in items) {
                    result = result.add(continuation.project(basket))
                }
                return result
            }

            public override fun projectObject(properties: PropertySet): ProjectionResult<Basket> {
                var result = ProjectionResult.empty<Basket>()
                for ((_, value) in properties) {
                    result = result.add(continuation.project(value))
                }
                return result
            }
        }
    }

    override fun matchesIndex(index: Int): PathSegmentMatchResult {
        return PathSegmentMatchResult.MATCHED_BOUND
    }

    override fun matchesKey(key: String): PathSegmentMatchResult {
        return PathSegmentMatchResult.MATCHED_BOUND
    }

    override fun representation(): String {
        return "[*]"
    }

}
