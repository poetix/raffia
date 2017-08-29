package com.codepoetics.raffia.paths

object Paths {

    private val EMPTY = EmptyPath()

    fun create(segments: List<PathSegment>): Path {
        var result: Path = EMPTY
        for (i in segments.indices.reversed()) {
            result = result.prepend(segments[i])
        }
        return result
    }

    private class EmptyPath : Path {

        override val isEmpty: Boolean
            get() = true

        override fun head(): PathSegment {
            throw UnsupportedOperationException("Called head on an empty path")
        }

        override fun tail(): Path {
            throw UnsupportedOperationException("Called tail on an empty path")
        }

        override fun prepend(segment: PathSegment): Path {
            return NonEmptyPath(segment, this)
        }

        override fun toString(): String {
            return "<empty path>"
        }

    }

    private class NonEmptyPath internal constructor(private val head: PathSegment, private val tail: Path) : Path {

        override val isEmpty: Boolean
            get() = false

        override fun head(): PathSegment {
            return head
        }

        override fun tail(): Path {
            return tail
        }

        override fun prepend(segment: PathSegment): Path {
            return NonEmptyPath(segment, this)
        }

        override fun toString(): String {
            var cursor: Path = this
            val stringBuilder = StringBuilder()
            while (!cursor.isEmpty) {
                stringBuilder.append(cursor.head().representation())
                cursor = cursor.tail()
            }
            return stringBuilder.toString()
        }
    }
}
