package com.codepoetics.raffia.streaming

import com.codepoetics.raffia.writers.BasketWriter

interface FilteringWriter<T : BasketWriter<T>> : BasketWriter<FilteringWriter<T>> {

    fun advance(newTarget: T): FilteringWriter<T>
    fun complete(): T

}
