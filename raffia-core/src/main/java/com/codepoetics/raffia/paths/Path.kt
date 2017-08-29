package com.codepoetics.raffia.paths

interface Path {

    val isEmpty: Boolean
    fun head(): PathSegment
    fun tail(): Path
    fun prepend(segment: PathSegment): Path

}
