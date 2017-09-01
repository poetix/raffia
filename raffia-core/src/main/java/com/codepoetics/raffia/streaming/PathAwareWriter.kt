package com.codepoetics.raffia.streaming

import com.codepoetics.raffia.paths.Path
import com.codepoetics.raffia.paths.PathSegment
import com.codepoetics.raffia.paths.PathSegmentMatchResult

/**
 * Represents the current position in the Basket's nested object structure
 */
sealed class Position(val depth: Int) {

    companion object {
        @JvmStatic
        val empty: Position = NoPosition
    }

    object NoPosition : Position(-1) {
        override fun exit(): Position = throw IllegalStateException("exit() called on empty position")
        override fun key(key: String): Position = throw IllegalStateException("key() called on empty position")
        override fun advance(): Position = this
        override fun checkAgainst(pathSegment: PathSegment): PathSegmentMatchResult = PathSegmentMatchResult.UNMATCHED
    }

    class ArrayIndex(val outer: Position, depth: Int, var index: Int) : Position(depth) {
        override fun exit(): Position = outer

        override fun key(key: String): Position = throw IllegalStateException("key() called on array index position")
        override fun advance(): Position = apply { this.index += 1 }
        override fun checkAgainst(pathSegment: PathSegment): PathSegmentMatchResult = pathSegment.matchesIndex(index)
    }

    class ObjectKey(val outer: Position, depth: Int, var key: String?) : Position(depth) {
        override fun exit(): Position = outer

        override fun key(key: String): Position = apply { this.key = key }
        override fun advance(): ObjectKey = apply { this.key = null }
        override fun checkAgainst(pathSegment: PathSegment): PathSegmentMatchResult =
                if (key == null) PathSegmentMatchResult.UNMATCHED
                else pathSegment.matchesKey(key!!)
    }

    abstract protected fun key(key: String): Position
    abstract protected fun advance(): Position

    protected fun enterArray(): Position = ArrayIndex(this, depth + 1, 0)
    protected fun enterObject(): Position = ObjectKey(this, depth + 1, null)

    abstract protected fun exit(): Position

    abstract internal fun checkAgainst(pathSegment: PathSegment): PathSegmentMatchResult

    override fun toString(): String = when (this) {
        is NoPosition -> ""
        is ArrayIndex -> "${outer.toString()}[$index]"
        is ObjectKey -> if (key == null) "${outer.toString()}.?" else "${outer.toString()}.$key"
    }

    fun receive(token: Token): Position = when (token) {
        is Token.Key -> key(token.key)
        is Token.BeginArray -> enterArray()
        is Token.BeginObject -> enterObject()
        is Token.End -> exit().advance()
        else -> advance()
    }
}

/**
 * Represents the state of path-binding, i.e. how much of a given path has been bound by the current Position
 */
sealed class PathBindingState(val outer: PathBindingState?, val atDepth: Int) {

    internal fun receive(position: Position): PathBindingState =
            if (position is Position.NoPosition)
                this
            else if (position.depth <= atDepth)
                outer!!.receive(position)
            else if (this is PathBindingState.Partial)
                comparePartial(remainingPath, position)
            else this

    private fun comparePartial(remainingPath: Path, position: Position): PathBindingState =
            if (remainingPath.head().isConditional)
                PathBindingState.Conditional(this, position.depth, remainingPath)
            else when (position.checkAgainst(remainingPath.head())) {
                PathSegmentMatchResult.UNMATCHED -> PathBindingState.Deviated(this, position.depth)
                PathSegmentMatchResult.MATCHED_UNBOUND -> this
                PathSegmentMatchResult.MATCHED_BOUND -> boundCase(remainingPath.tail(), position)
            }

    private fun boundCase(tail: Path, position: Position) =
            if (tail.isEmpty)
                PathBindingState.Complete(this, position.depth)
            else PathBindingState.Partial(this, position.depth, tail)

    companion object {
        @JvmStatic
        fun fromPath(path: Path): PathBindingState =
                if (path.isEmpty) Complete(null, -1)
                else Partial(null, -1, path)
    }

    class Conditional(outer: PathBindingState, atDepth: Int, val conditionalPath: Path) : PathBindingState(outer, atDepth)
    class Complete(outer: PathBindingState?, atDepth: Int) : PathBindingState(outer, atDepth)
    class Partial(outer: PathBindingState?, atDepth: Int, val remainingPath: Path) : PathBindingState(outer, atDepth)
    class Deviated(outer: PathBindingState, atDepth: Int) : PathBindingState(outer, atDepth)

}

/**
 * Combined state from position tracking and path binding.
 */
data class PositionTrackingState(var position: Position, var pathBindingState: PathBindingState) {
    internal val atBindingPoint: Boolean get() = position.depth == pathBindingState.atDepth

    fun receive(token: Token): Unit {
        position = position.receive(token)
        pathBindingState = pathBindingState.receive(position)
    }

    companion object {
        @JvmStatic
        fun fromPath(path: Path): PositionTrackingState = PositionTrackingState(Position.empty, PathBindingState.fromPath(path))
    }
}

/**
 * A position-aware interpreter keeps track of position and path-binding, and drives a downstream state machine.
 */
class PositionAwareInterpreterState<S>(var positionTrackingState: PositionTrackingState, var downstreamState: S) {

    companion object {
        @JvmStatic
        fun <S> fromPath(path: Path, downstreamState: S) =
                PositionAwareInterpreterState(PositionTrackingState.fromPath(path), downstreamState)
    }

}

/**
 * A path-aware interpreter generates tokens for a downstream state machine based on the current token and position-tracking state.
 */
typealias PathAwareInterpreter<I> = (PositionTrackingState, Token) -> I

/**
 * Given a path-aware interpreter and a downstream state machine, a position-aware state machine consumes tokens,
 * passes position-tracking state to the interpreter, and forwards the interpreted tokens to the downstream state.
 */
fun <S, I> positionAwareStateMachine(pathAwareInterpreter: PathAwareInterpreter<I>, downstreamStateMachine: StateMachine<S, I>)
        : StateMachine<PositionAwareInterpreterState<S>, Token> = { state, token ->
    state.apply {
        val downstreamToken = pathAwareInterpreter(positionTrackingState, token)

        positionTrackingState.receive(token)
        downstreamState = downstreamStateMachine(downstreamState, downstreamToken)
    }
}