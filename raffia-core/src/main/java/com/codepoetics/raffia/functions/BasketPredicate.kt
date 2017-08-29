package com.codepoetics.raffia.functions

import com.codepoetics.raffia.baskets.Basket

interface BasketPredicate : ValuePredicate<Basket>

fun basketPredicate(f: (Basket) -> Boolean) = object : BasketPredicate {
    override fun test(value: Basket): Boolean = f(value)
}
