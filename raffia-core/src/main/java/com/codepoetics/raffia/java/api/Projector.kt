package com.codepoetics.raffia.java.api

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.operations.ProjectionResult

interface Projector<T> {
    fun project(basket: Basket): ProjectionResult<T>
}

fun <T> projector(f: (Basket) -> ProjectionResult<T>): Projector<T> = object : Projector<T> {
    override fun project(basket: Basket): ProjectionResult<T> = f(basket)
}