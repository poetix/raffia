package com.codepoetics.raffia.paths.segments

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.ObjectEntry
import com.codepoetics.raffia.baskets.PropertySet
import com.codepoetics.raffia.operations.ProjectionResult
import com.codepoetics.raffia.java.api.Projector
import com.codepoetics.raffia.java.api.Updater
import com.codepoetics.raffia.paths.PathSegmentMatchResult

import java.util.ArrayList

internal class DeepScanToObjectKeyPathSegment(private val key: String) : BasePathSegment() {

    override fun createUpdater(continuation: Updater): Updater {
        return object : StructUpdater() {
            public override fun updateArray(items: ArrayContents): Basket {
                val updated = ArrayList<Basket>()

                for (basket in items) {
                    updated.add(update(basket))
                }

                return Basket.ofArray(updated)
            }

            public override fun updateObject(properties: PropertySet): Basket {
                val entries = ArrayList<ObjectEntry>(properties.size())

                for (entry in properties) {
                    if (entry.key == key) {
                        entries.add(updateWith(entry, continuation))
                    } else {
                        entries.add(updateWith(entry, this))
                    }
                }

                return Basket.ofObject(entries)
            }

            private fun updateWith(entry: ObjectEntry, updater: Updater): ObjectEntry {
                return ObjectEntry.of(entry.key, updater.update(entry.value))
            }
        }
    }

    override fun createProjector(continuation: Projector<Basket>): Projector<Basket> {
        return object : StructProjector<Basket>() {
            public override fun projectArray(items: ArrayContents): ProjectionResult<Basket> {
                var result = ProjectionResult.empty<Basket>()
                for (basket in items) {
                    result = result.add(project(basket))
                }
                return result
            }

            public override fun projectObject(properties: PropertySet): ProjectionResult<Basket> {
                var result = ProjectionResult.empty<Basket>()
                for ((key1, value) in properties) {
                    if (key1 == key) {
                        result = result.add(continuation.project(value))
                    } else {
                        result = result.add(project(value))
                    }
                }
                return result
            }
        }
    }

    override fun matchesIndex(index: Int): PathSegmentMatchResult {
        return PathSegmentMatchResult.MATCHED_UNBOUND
    }

    override fun matchesKey(key: String): PathSegmentMatchResult {
        return if (this.key == key)
            PathSegmentMatchResult.MATCHED_BOUND
        else
            PathSegmentMatchResult.MATCHED_UNBOUND
    }

    override fun representation(): String {
        return ".." + key
    }
}
