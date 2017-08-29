package com.codepoetics.raffia.paths.segments

import com.codepoetics.raffia.java.api.BasketPredicate
import com.codepoetics.raffia.paths.PathSegment

import java.util.ArrayList
import java.util.Collections

object PathSegments {

    val LOWER_UNBOUNDED = Integer.MIN_VALUE
    val UPPER_UNBOUNDED = Integer.MAX_VALUE

    fun ofArrayIndex(arrayIndex: Int): PathSegment {
        return ArrayIndexPathSegment(listOf(arrayIndex))
    }

    fun ofArrayIndices(first: Int, vararg remaining: Int): PathSegment {
        val indices = ArrayList<Int>(remaining.size + 1)
        indices.add(first)
        for (i in remaining) {
            indices.add(i)
        }
        return ofArrayIndices(indices)
    }

    fun ofArraySlice(startIndex: Int, endIndex: Int): PathSegment {
        return ArraySlicePathSegment(startIndex, endIndex)
    }

    fun ofArrayIndices(arrayIndices: Collection<Int>): PathSegment {
        return ArrayIndexPathSegment(arrayIndices)
    }

    fun ofWildcard(): PathSegment {
        return WildcardPathSegment()
    }

    fun ofObjectKey(objectKey: String): PathSegment {
        return ObjectKeyPathSegment(listOf(objectKey))
    }

    fun ofAny(objectKey: String): PathSegment {
        return DeepScanToObjectKeyPathSegment(objectKey)
    }

    fun itemMatching(representation: String, predicate: BasketPredicate): PathSegment {
        return MatchingItemPathSegment(representation, predicate)
    }

    fun ofObjectKeys(first: String, vararg remaining: String): PathSegment {
        val keys = ArrayList<String>(remaining.size + 1)
        keys.add(first)
        Collections.addAll(keys, *remaining)
        return ofObjectKeys(keys)
    }

    fun ofObjectKeys(keys: Collection<String>): PathSegment {
        return ObjectKeyPathSegment(keys)
    }
}
