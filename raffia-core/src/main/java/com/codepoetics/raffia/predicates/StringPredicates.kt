package com.codepoetics.raffia.predicates

object StringPredicates {

    @JvmStatic
    fun isEqualTo(other: String): ValuePredicate<String> = { input -> input == other }

}
