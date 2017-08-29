package com.codepoetics.raffia.predicates

import com.codepoetics.raffia.functions.ValuePredicate
import com.codepoetics.raffia.functions.valuePredicate

object StringPredicates {

    @JvmStatic
    fun isEqualTo(other: String): ValuePredicate<String> = valuePredicate { input -> input == other }

}
