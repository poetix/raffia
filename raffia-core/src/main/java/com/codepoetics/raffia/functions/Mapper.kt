package com.codepoetics.raffia.functions

interface Mapper<I, O> {
    fun map(input: I): O
}

fun <I, O> mapper(f: (I) -> O): Mapper<I, O> = object : Mapper<I, O> {
    override fun map(input: I): O = f(input)
}
