package com.codepoetics.raffia.paths

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.java.api.Projector
import com.codepoetics.raffia.java.api.Updater

interface PathSegment {

    fun createUpdater(tail: Path, updater: Updater): Updater
    fun createItemUpdater(tail: Path, updater: Updater): Updater

    fun createProjector(tail: Path): Projector<Basket>
    fun createItemProjector(tail: Path): Projector<Basket>

    fun matchesIndex(index: Int): PathSegmentMatchResult

    fun matchesKey(key: String): PathSegmentMatchResult

    val isConditional: Boolean

    fun representation(): String
}
