package com.codepoetics.raffia.predicates

import com.codepoetics.raffia.java.api.ValuePredicate
import com.codepoetics.raffia.java.api.valuePredicate

object StringPredicates {

    @JvmStatic
    fun isEqualTo(other: String): ValuePredicate<String> = valuePredicate { input -> input == other }

}
