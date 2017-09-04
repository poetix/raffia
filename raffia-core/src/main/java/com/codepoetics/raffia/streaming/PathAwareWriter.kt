package com.codepoetics.raffia.streaming

import com.codepoetics.raffia.paths.Path
import com.codepoetics.raffia.paths.PathSegment
import com.codepoetics.raffia.paths.PathSegmentMatchResult
import com.codepoetics.raffia.paths.Paths

enum class PositionType {
    NONE,
    ARRAY_INDEX,
    OBJECT_KEY
}

data class Index(var key: String = "", var index: Int = 0, var type: PositionType = PositionType.NONE)

/**
 * Represents the current position in the Basket's nested object structure
 */
class Position() {

    companion object {
        val none: Index = Index()
    }

    val stack = ReusableStack<Index> { Index() }

    val current: Index get() = stack.current ?: none

    val depth: Int get() = stack.depth

    private fun key(key: String): Position = apply {
        current.key = key
    }

    private inline fun withCurrent(block: Index.() -> Unit): Position = apply { current.block() }

    private fun advance(): Position = withCurrent {
        when (type) {
            PositionType.ARRAY_INDEX -> index += 1
            PositionType.OBJECT_KEY -> key = ""
            else -> Unit
        }
    }

    private fun enterArray(): Position = apply {
        stack.push {
            type = PositionType.ARRAY_INDEX
            index = 0
        }
    }

    private fun enterObject(): Position = apply {
        stack.push {
            type = PositionType.OBJECT_KEY
            key = ""
        }
    }

    private fun exit(): Position = apply {
        stack.pop()
    }

    fun checkAgainst(pathSegment: PathSegment): PathSegmentMatchResult =
            current.let {
                when (it.type) {
                    PositionType.ARRAY_INDEX -> pathSegment.matchesIndex(it.index)
                    PositionType.OBJECT_KEY -> if (it.key.isEmpty()) PathSegmentMatchResult.UNMATCHED else pathSegment.matchesKey(it.key)
                    else -> PathSegmentMatchResult.UNMATCHED
                }
            }

    override fun toString(): String = stack.contents.map {
        when (it.type) {
            PositionType.NONE -> ""
            PositionType.ARRAY_INDEX -> "[${it.index}]"
            PositionType.OBJECT_KEY -> if (it.key.isEmpty()) ".?" else ".${it.key}"
        }
    }.joinToString("")

    fun receive(token: Token): Position = when (token) {
        is Token.Key -> key(token.key)
        is Token.BeginArray -> enterArray()
        is Token.BeginObject -> enterObject()
        is Token.End -> exit().advance()
        else -> advance()
    }
}

enum class PathBindingType {
    PARTIAL,
    DEVIATED,
    COMPLETE,
    CONDITIONAL
}

/**
 * Represents the state of path-binding, i.e. how much of a given path has been bound by the current Position
 */
data class PathBindingState(var type: PathBindingType, var atDepth: Int, var remainingPath: Path)

/**
 * Combined state from position tracking and path binding.
 */
data class PositionTrackingState(var position: Position, val initial: PathBindingState) {

    private val pathBindingStack = ReusableStack<PathBindingState>() { PathBindingState(PathBindingType.PARTIAL, 0, Paths.empty()) }

    val current: PathBindingState get() = pathBindingStack.current ?: initial

    internal val atBindingPoint: Boolean get() = position.depth == current.atDepth

    fun receive(token: Token): Unit {
        position = position.receive(token)
        receive(position)
    }

    private fun receive(position: Position): Unit {
        if (position.current.type == PositionType.NONE) return

        while (position.depth <= current.atDepth) {
            pathBindingStack.pop()
        }

        if (current.type == PathBindingType.PARTIAL) {
            comparePartial(current.remainingPath, position)
        }
    }

    private fun comparePartial(remainingPath: Path, position: Position): Unit {
        if (remainingPath.head().isConditional) {
            pathBindingStack.push {
                type = PathBindingType.CONDITIONAL
                atDepth = position.depth
                this.remainingPath = remainingPath
            }
        } else when (position.checkAgainst(remainingPath.head())) {
            PathSegmentMatchResult.UNMATCHED -> pathBindingStack.push {
                type = PathBindingType.DEVIATED
                atDepth = position.depth
            }
            PathSegmentMatchResult.MATCHED_UNBOUND -> Unit
            PathSegmentMatchResult.MATCHED_BOUND -> boundCase(remainingPath.tail(), position)
        }
    }

    private fun boundCase(tail: Path, position: Position) {
        if (tail.isEmpty)
            pathBindingStack.push {
                type = PathBindingType.COMPLETE
                atDepth = position.depth
            }
        else pathBindingStack.push {
            type = PathBindingType.PARTIAL
            atDepth = position.depth
            remainingPath = tail
        }
    }

    companion object {
        @JvmStatic
        fun fromPath(path: Path): PositionTrackingState = PositionTrackingState(Position(),
                if (path.isEmpty) PathBindingState(PathBindingType.COMPLETE, -1, path)
                else PathBindingState(PathBindingType.PARTIAL, -1, path))

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