package com.codepoetics.raffia.java.api

interface ValuePredicate<T> {
    fun test(value: T): Boolean
}

fun <T> valuePredicate(f: (T) -> Boolean) = object : ValuePredicate<T> {
    override fun test(value: T): Boolean = f(value)
}
