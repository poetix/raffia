package com.codepoetics.raffia.predicates

import java.math.BigDecimal

object NumberPredicates {

    @JvmStatic
    fun isEqualTo(other: Int): ValuePredicate<BigDecimal> = isEqualTo(BigDecimal.valueOf(other.toLong()))

    @JvmStatic
    fun isEqualTo(other: Long): ValuePredicate<BigDecimal> = isEqualTo(BigDecimal.valueOf(other))

    @JvmStatic
    fun isEqualTo(other: Double): ValuePredicate<BigDecimal> = isEqualTo(BigDecimal.valueOf(other))

    @JvmStatic
    fun isEqualTo(other: String): ValuePredicate<BigDecimal> = isEqualTo(BigDecimal(other))

    @JvmStatic
    fun isEqualTo(other: BigDecimal): ValuePredicate<BigDecimal> = { input -> input == other }

    @JvmStatic
    fun isGreaterThan(other: Int): ValuePredicate<BigDecimal> = isGreaterThan(BigDecimal.valueOf(other.toLong()))

    @JvmStatic
    fun isGreaterThan(other: Long): ValuePredicate<BigDecimal> = isGreaterThan(BigDecimal.valueOf(other))

    @JvmStatic
    fun isGreaterThan(other: Double): ValuePredicate<BigDecimal> = isGreaterThan(BigDecimal.valueOf(other))

    @JvmStatic
    fun isGreaterThan(other: String): ValuePredicate<BigDecimal> = isGreaterThan(BigDecimal(other))

    @JvmStatic
    fun isGreaterThan(other: BigDecimal): ValuePredicate<BigDecimal> = { input -> input > other }

    @JvmStatic
    fun isLessThan(other: Int): ValuePredicate<BigDecimal> = isLessThan(BigDecimal.valueOf(other.toLong()))

    @JvmStatic
    fun isLessThan(other: Long): ValuePredicate<BigDecimal> = isLessThan(BigDecimal.valueOf(other))

    @JvmStatic
    fun isLessThan(other: Double): ValuePredicate<BigDecimal> = isLessThan(BigDecimal.valueOf(other))

    @JvmStatic
    fun isLessThan(other: String): ValuePredicate<BigDecimal> = isLessThan(BigDecimal(other))

    @JvmStatic
    fun isLessThan(other: BigDecimal): ValuePredicate<BigDecimal> = { input -> input < other }

    @JvmStatic
    fun isGreaterThanOrEqualTo(other: Int): ValuePredicate<BigDecimal> = isGreaterThanOrEqualTo(BigDecimal.valueOf(other.toLong()))

    @JvmStatic
    fun isGreaterThanOrEqualTo(other: Long): ValuePredicate<BigDecimal> = isGreaterThanOrEqualTo(BigDecimal.valueOf(other))

    @JvmStatic
    fun isGreaterThanOrEqualTo(other: Double): ValuePredicate<BigDecimal> = isGreaterThanOrEqualTo(BigDecimal.valueOf(other))

    @JvmStatic
    fun isGreaterThanOrEqualTo(other: String): ValuePredicate<BigDecimal> = isGreaterThanOrEqualTo(BigDecimal(other))

    @JvmStatic
    fun isGreaterThanOrEqualTo(other: BigDecimal): ValuePredicate<BigDecimal> = { input -> input >= other }

    @JvmStatic
    fun isLessThanOrEqualTo(other: Int): ValuePredicate<BigDecimal> = isLessThanOrEqualTo(BigDecimal.valueOf(other.toLong()))

    @JvmStatic
    fun isLessThanOrEqualTo(other: Long): ValuePredicate<BigDecimal> = isLessThanOrEqualTo(BigDecimal.valueOf(other))

    @JvmStatic
    fun isLessThanOrEqualTo(other: Double): ValuePredicate<BigDecimal> = isLessThanOrEqualTo(BigDecimal.valueOf(other))

    @JvmStatic
    fun isLessThanOrEqualTo(other: String): ValuePredicate<BigDecimal> = isLessThanOrEqualTo(BigDecimal(other))

    @JvmStatic
    fun isLessThanOrEqualTo(other: BigDecimal): ValuePredicate<BigDecimal> = { input -> input <= other }
}
