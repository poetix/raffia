package com.codepoetics.raffia.lenses

import com.codepoetics.raffia.java.api.BasketPredicate
import com.codepoetics.raffia.paths.PathSegment
import com.codepoetics.raffia.paths.segments.PathSegments
import org.pcollections.PVector
import org.pcollections.TreePVector

import java.util.ArrayList
import java.util.regex.Pattern

internal object PathParser {

    private val keyExpr = Pattern.compile("^\\.[a-zA-Z0-9\\-_]+")
    private val deepKeyExpr = Pattern.compile("^\\.\\.[a-zA-Z0-9\\-_]+")
    private val indexExpr = Pattern.compile("^\\[([^]]+)]")
    private val integerExpr = Pattern.compile("^-?[0-9]+$")

    fun parse(pathString: String, predicates: PVector<BasketPredicate>): PVector<PathSegment> {
        val trimmed = pathString.trim { it <= ' ' }
        if (!trimmed.startsWith("$") and !trimmed.startsWith("@")) {
            throw IllegalArgumentException("Path string must begin with $ or @")
        }
        return parseRemaining(TreePVector.empty<PathSegment>(), trimmed.substring(1), predicates)
    }

    private fun parseRemaining(parsed: PVector<PathSegment>, remaining: String, predicates: PVector<BasketPredicate>): PVector<PathSegment> {
        if (remaining.isEmpty()) {
            if (!predicates.isEmpty()) {
                throw IllegalArgumentException("Unmatched predicate")
            }
            return parsed
        }

        if (remaining.startsWith(".*")) {
            return parseRemaining(parsed.plus(PathSegments.ofWildcard()), remaining.substring(2), predicates)
        }

        if (remaining.startsWith("[*]")) {
            return parseRemaining(parsed.plus(PathSegments.ofWildcard()), remaining.substring(3), predicates)
        }

        if (remaining.startsWith("[?]")) {
            if (predicates.isEmpty()) {
                throw IllegalArgumentException("Predicate expression without matching predicate")
            }

            return parseRemaining(
                    parsed.plus(PathSegments.itemMatching("[?]", predicates[0])),
                    remaining.substring(3),
                    predicates.minus(0))
        }

        val keyExprMatcher = keyExpr.matcher(remaining)
        if (keyExprMatcher.find()) {
            val key = keyExprMatcher.group().substring(1)
            return parseRemaining(parsed.plus(PathSegments.ofObjectKey(key)), remaining.substring(key.length + 1), predicates)
        }

        val deepKeyExprMatcher = deepKeyExpr.matcher(remaining)
        if (deepKeyExprMatcher.find()) {
            val key = deepKeyExprMatcher.group().substring(2)
            return parseRemaining(parsed.plus(PathSegments.ofAny(key)), remaining.substring(key.length + 2), predicates)
        }

        val indexExprMatcher = indexExpr.matcher(remaining)
        if (!indexExprMatcher.find()) {
            throw IllegalArgumentException("Unrecognised path segment: " + remaining)
        }
        val indexExpr = indexExprMatcher.group(1)
        return parseRemaining(parsed.plus(parseIndexExpression(indexExpr.trim { it <= ' ' })), remaining.substring(indexExpr.length + 2), predicates)
    }

    private fun parseIndexExpression(expression: String): PathSegment {
        if (expression.contains(",")) {
            return parseMultiIndexExpression(expression)
        }

        if (expression.contains(":")) {
            return parseRangeExpression(expression)
        }

        if (expression.startsWith("'") && expression.endsWith("'")) {
            return PathSegments.ofObjectKey(expression.substring(1, expression.length - 1))
        }

        if (integerExpr.matcher(expression).matches()) {
            return PathSegments.ofArrayIndex(Integer.valueOf(expression)!!)
        }

        throw IllegalArgumentException("Unrecognised index expression: " + expression)
    }

    private fun parseMultiIndexExpression(expression: String): PathSegment {
        val indices = expression.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (indices[0].trim { it <= ' ' }.startsWith("'")) {
            return toKeys(indices)
        }

        return toArrayIndices(indices)
    }

    private fun parseRangeExpression(expression: String): PathSegment {
        if (expression == ":") {
            throw IllegalArgumentException("\":\" is not a legal range expression")
        }

        if (expression.startsWith(":")) {
            return PathSegments.ofArraySlice(PathSegments.LOWER_UNBOUNDED, Integer.parseInt(expression.substring(1)))
        }

        if (expression.endsWith(":")) {
            return PathSegments.ofArraySlice(Integer.parseInt(expression.substring(0, expression.length - 1)), PathSegments.UPPER_UNBOUNDED)
        }

        val bounds = expression.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (bounds.size != 2) {
            throw IllegalArgumentException("\"" + expression + "\" is not a legal range expression")
        }

        return PathSegments.ofArraySlice(Integer.parseInt(bounds[0]), Integer.parseInt(bounds[1]))
    }

    private fun toKeys(indices: Array<String>): PathSegment {
        val keys = ArrayList<String>(indices.size)
        for (index in indices) {
            val keyExpr = index.trim { it <= ' ' }
            keys.add(keyExpr.substring(1, keyExpr.length - 1).trim { it <= ' ' })
        }
        return PathSegments.ofObjectKeys(keys)
    }

    private fun toArrayIndices(indices: Array<String>): PathSegment {
        val intIndices = ArrayList<Int>(indices.size)

        for (index in indices) {
            val trimmed = index.trim { it <= ' ' }
            if (!integerExpr.matcher(trimmed).matches()) {
                throw IllegalArgumentException("Unrecognised index expression:" + trimmed)
            }
            intIndices.add(Integer.valueOf(trimmed))
        }

        return PathSegments.ofArrayIndices(intIndices)
    }
}
