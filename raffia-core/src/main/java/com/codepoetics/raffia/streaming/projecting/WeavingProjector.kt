package com.codepoetics.raffia.streaming.projecting

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.java.api.Projector
import com.codepoetics.raffia.streaming.FilteringWriter
import com.codepoetics.raffia.streaming.WeavingFilter
import com.codepoetics.raffia.writers.BasketWeavingWriter
import com.codepoetics.raffia.writers.BasketWriter
import com.codepoetics.raffia.writers.Writers
import com.codepoetics.raffia.writers.writeTo

internal class WeavingProjector<T : BasketWriter<T>>(private val target: T, parent: FilteringWriter<T>, private val projector: Projector<Basket>, weaver: BasketWeavingWriter) : WeavingFilter<T>(parent, weaver) {

    override fun writeToTarget(woven: Basket): T =
        projector.project(woven).fold(target) {
            state, basket -> basket.writeTo(state)
        }

    companion object {

        fun <T : BasketWriter<T>> weavingObject(target: T, parent: FilteringWriter<T>, projector: Projector<Basket>): WeavingFilter<T> {
            return weaving(target, parent, projector, Writers.weaving().beginObject())
        }

        fun <T : BasketWriter<T>> weavingArray(target: T, parent: FilteringWriter<T>, projector: Projector<Basket>): WeavingFilter<T> {
            return weaving(target, parent, projector, Writers.weaving().beginArray())
        }

        private fun <T : BasketWriter<T>> weaving(target: T, parent: FilteringWriter<T>, projector: Projector<Basket>, weaver: BasketWeavingWriter): WeavingFilter<T> {
            return WeavingProjector(target, parent, projector, weaver)
        }
    }
}
