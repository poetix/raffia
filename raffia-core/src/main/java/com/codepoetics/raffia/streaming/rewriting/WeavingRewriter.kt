package com.codepoetics.raffia.streaming.rewriting

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.java.api.Updater
import com.codepoetics.raffia.streaming.FilteringWriter
import com.codepoetics.raffia.streaming.WeavingFilter
import com.codepoetics.raffia.writers.BasketWeavingWriter
import com.codepoetics.raffia.writers.BasketWriter
import com.codepoetics.raffia.writers.Writers
import com.codepoetics.raffia.writers.writeTo

internal class WeavingRewriter<T : BasketWriter<T>> private constructor(private val target: T, parent: FilteringWriter<T>, private val updater: Updater, weaver: BasketWeavingWriter) : WeavingFilter<T>(parent, weaver) {

    override fun writeToTarget(woven: Basket): T = updater.update(woven).writeTo(target)

    companion object {

        fun <T : BasketWriter<T>> weavingObject(
                target: T,
                parent: FilteringWriter<T>,
                updater: Updater): WeavingRewriter<T> =
                weaving(target, parent, updater, Writers.weaving().beginObject())

        fun <T : BasketWriter<T>> weavingArray(
                target: T,
                parent: FilteringWriter<T>,
                updater: Updater): WeavingRewriter<T> =
                weaving(target, parent, updater, Writers.weaving().beginArray())

        private fun <T : BasketWriter<T>> weaving(
                target: T,
                parent: FilteringWriter<T>,
                updater: Updater, weaver: BasketWeavingWriter): WeavingRewriter<T> =
                WeavingRewriter(target, parent, updater, weaver)
    }
}
