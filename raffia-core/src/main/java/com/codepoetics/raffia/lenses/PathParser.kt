package com.codepoetics.raffia.lenses

import com.codepoetics.raffia.baskets.Basket
import org.pcollections.PVector
import java.util.regex.Pattern

typealias BasketPredicate = (Basket) -> Boolean

internal object PathParser {

    private val keyExpr = Pattern.compile("^\\.[a-zA-Z0-9\\-_]+")
    private val deepKeyExpr = Pattern.compile("^\\.\\.[a-zA-Z0-9\\-_]+")
    private val indexExpr = Pattern.compile("^\\[([^]]+)]")
    private val integerExpr = Pattern.compile("^-?[0-9]+$")

    fun parse(pathString: String, predicates: PVector<BasketPredicate>): Strand {
        val trimmed = pathString.trim()
        if (!trimmed.startsWith("$") and !trimmed.startsWith("@")) {
            throw IllegalArgumentException("Path string must begin with $ or @")
        }
        return parseRemaining(RootStrand, trimmed.substring(1), predicates)
    }

    private fun parseRemaining(parsed: Strand, remaining: String, predicates: PVector<BasketPredicate>): Strand {
        if (remaining.isEmpty()) {
            if (!predicates.isEmpty()) {
                throw IllegalArgumentException("Unmatched predicate")
            }
            return parsed
        }

        if (remaining.startsWith(".*")) {
            return parseRemaining(parsed.then(WildcardStrand), remaining.substring(2), predicates)
        }

        if (remaining.startsWith("[*]")) {
            return parseRemaining(parsed.then(WildcardStrand), remaining.substring(3), predicates)
        }

        if (remaining.startsWith("[?]")) {
            if (predicates.isEmpty()) {
                throw IllegalArgumentException("Predicate expression without matching predicate")
            }

            return parseRemaining(
                    parsed.then(ConditionalStrand(predicates[0])),
                    remaining.substring(3),
                    predicates.minus(0))
        }

        val keyExprMatcher = keyExpr.matcher(remaining)
        if (keyExprMatcher.find()) {
            val key = keyExprMatcher.group().substring(1)
            return parseRemaining(parsed.then(KeyStrand(arrayOf(key))), remaining.substring(key.length + 1), predicates)
        }

        val deepKeyExprMatcher = deepKeyExpr.matcher(remaining)
        if (deepKeyExprMatcher.find()) {
            val key = deepKeyExprMatcher.group().substring(2)
            return parseRemaining(parsed.then(DeepScanStrand(key)), remaining.substring(key.length + 2), predicates)
        }

        val indexExprMatcher = indexExpr.matcher(remaining)
        if (!indexExprMatcher.find()) {
            throw IllegalArgumentException("Unrecognised path segment: " + remaining)
        }
        val indexExpr = indexExprMatcher.group(1)
        return parseRemaining(parsed.then(parseIndexExpression(indexExpr.trim { it <= ' ' })), remaining.substring(indexExpr.length + 2), predicates)
    }

    private fun parseIndexExpression(expression: String): Strand {
        if (expression.contains(",")) {
            return parseMultiIndexExpression(expression)
        }

        if (expression.contains(":")) {
            return parseRangeExpression(expression)
        }

        if (expression.startsWith("'") && expression.endsWith("'")) {
            return KeyStrand(arrayOf(expression.substring(1, expression.length - 1)))
        }

        if (integerExpr.matcher(expression).matches()) {
            return IndexStrand(intArrayOf(Integer.valueOf(expression)!!))
        }

        throw IllegalArgumentException("Unrecognised index expression: " + expression)
    }

    private fun parseMultiIndexExpression(expression: String): Strand {
        val indices = expression.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (indices[0].trim { it <= ' ' }.startsWith("'")) {
            return toKeys(indices)
        }

        return toArrayIndices(indices)
    }

    private fun parseRangeExpression(expression: String): Strand {
        if (expression == ":") {
            throw IllegalArgumentException("\":\" is not a legal range expression")
        }

        if (expression.startsWith(":")) {
            return ArraySliceStrand(0, Integer.parseInt(expression.substring(1)))
        }

        if (expression.endsWith(":")) {
            return ArraySliceStrand(Integer.parseInt(expression.substring(0, expression.length - 1)), Integer.MAX_VALUE)
        }

        val bounds = expression.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (bounds.size != 2) {
            throw IllegalArgumentException("\"" + expression + "\" is not a legal range expression")
        }

        return ArraySliceStrand(Integer.parseInt(bounds[0]), Integer.parseInt(bounds[1]))
    }

    private fun toKeys(indices: Array<String>): Strand =
            KeyStrand(indices.map(String::trim).map { it.substring(1, it.length - 1).trim() }.toTypedArray())

    private fun toArrayIndices(indices: Array<String>): Strand =
        IndexStrand(indices.map(String::trim).map {
            if (!integerExpr.matcher(it).matches())
                throw IllegalArgumentException("Unrecognised index expression: $it")
            else
                Integer.valueOf(it)
        }.toIntArray())

}
