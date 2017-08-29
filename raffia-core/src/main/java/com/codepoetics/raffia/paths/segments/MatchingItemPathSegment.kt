package com.codepoetics.raffia.paths.segments

import com.codepoetics.raffia.baskets.ArrayContents
import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.PropertySet
import com.codepoetics.raffia.functions.BasketPredicate
import com.codepoetics.raffia.functions.Projector
import com.codepoetics.raffia.functions.Updater
import com.codepoetics.raffia.operations.*
import com.codepoetics.raffia.paths.Path
import com.codepoetics.raffia.paths.PathSegmentMatchResult

internal class MatchingItemPathSegment(private val representation: String, private val predicate: BasketPredicate) : BasePathSegment() {

    override val isConditional: Boolean
        get() = true

    override fun createItemUpdater(tail: Path, updater: Updater): Updater {
        return Updaters.branch(
                predicate,
                if (tail.isEmpty) updater else tail.head().createUpdater(tail, updater),
                Updaters.NO_OP
        )
    }

    override fun createItemProjector(tail: Path): Projector<Basket> {
        return Projectors.branch(
                predicate,
                if (tail.isEmpty)
                    Projectors.id
                else
                    tail.head().createProjector(tail),
                Projectors.alwaysEmpty<Basket>())
    }

    override fun createUpdater(continuation: Updater): Updater {
        return object : StructUpdater() {
            public override fun updateArray(items: ArrayContents): Basket {
                var updated = items
                for (i in 0..items.size() - 1) {
                    val item = items[i]
                    if (predicate.test(item)) {
                        updated = updated.with(i, continuation.update(item))
                    }
                }
                return Basket.ofArray(updated)
            }

            public override fun updateObject(properties: PropertySet): Basket {
                var updated = properties
                for ((key, value) in properties) {
                    if (predicate.test(value)) {
                        updated = updated.with(key, continuation.update(value))
                    }
                }
                return Basket.ofObject(updated)
            }
        }
    }

    override fun createProjector(continuation: Projector<Basket>): Projector<Basket> {
        return object : StructProjector<Basket>() {
            public override fun projectArray(items: ArrayContents): ProjectionResult<Basket> {
                var result = ProjectionResult.empty<Basket>()
                for (item in items) {
                    if (predicate.test(item)) {
                        result = result.add(continuation.project(item))
                    }
                }
                return result
            }

            public override fun projectObject(properties: PropertySet): ProjectionResult<Basket> {
                var result = ProjectionResult.empty<Basket>()
                for ((_, value) in properties) {
                    if (predicate.test(value)) {
                        result = result.add(continuation.project(value))
                    }
                }
                return result
            }
        }
    }

    override fun matchesIndex(index: Int): PathSegmentMatchResult {
        throw UnsupportedOperationException()
    }

    override fun matchesKey(key: String): PathSegmentMatchResult {
        throw UnsupportedOperationException()
    }

    override fun representation(): String {
        return representation
    }

}
