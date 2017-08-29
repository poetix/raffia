package com.codepoetics.raffia.paths.segments

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.functions.Projector
import com.codepoetics.raffia.operations.Projectors
import com.codepoetics.raffia.functions.Updater
import com.codepoetics.raffia.paths.Path
import com.codepoetics.raffia.paths.PathSegment

internal abstract class BasePathSegment : PathSegment {

    override fun createUpdater(path: Path, updater: Updater): Updater {
        val subPath = path.tail()
        val continuation = if (subPath.isEmpty)
            updater
        else
            subPath.head().createUpdater(subPath, updater)

        return createUpdater(continuation)
    }

    override fun createItemUpdater(tail: Path, updater: Updater): Updater {
        throw UnsupportedOperationException("Cannot create item updater for non-conditional path segment")
    }

    override fun createItemProjector(tail: Path): Projector<Basket> {
        throw UnsupportedOperationException("Cannot create item projector for non-conditional path segment")
    }

    protected abstract fun createUpdater(continuation: Updater): Updater

    override fun createProjector(path: Path): Projector<Basket> {
        val subPath = path.tail()
        return if (subPath.isEmpty)
            createProjector(Projectors.id)
        else
            createProjector(subPath.head().createProjector(subPath))
    }

    override val isConditional: Boolean
        get() = false

    protected abstract fun createProjector(continuation: Projector<Basket>): Projector<Basket>

}
