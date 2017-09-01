package com.codepoetics.raffia.functions

import com.codepoetics.raffia.baskets.Basket

interface Updater {
    fun update(basket: Basket): Basket
}

fun updater(f: (Basket) -> Basket): Updater = object : Updater {
    override fun update(basket: Basket): Basket = f(basket)
}


