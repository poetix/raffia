package com.codepoetics.raffia.streaming

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.functions.Updater
import com.codepoetics.raffia.lenses.Lens
import com.codepoetics.raffia.paths.Path
import com.codepoetics.raffia.paths.PathSegment
import com.codepoetics.raffia.paths.PathSegmentMatchResult
import com.codepoetics.raffia.writers.BasketWeavingWriter
import com.codepoetics.raffia.writers.BasketWriter
import com.codepoetics.raffia.writers.Writers
import java.math.BigDecimal

/**
 * A streaming token
 */
sealed class Token {
    object BeginObject: Token()
    object BeginArray: Token()
    object End: Token()
    object NullValue: Token()
    object TrueValue: Token()
    object FalseValue: Token()
    class Key(val key: String): Token()
    class StringValue(val value: String): Token()
    class NumberValue(val value: BigDecimal): Token()
}

typealias StateMachine<S, I> = (S, I) -> S

interface Filter<T>: BasketWriter<Filter<T>> {
    val result: T
}

/**
 * A writer which emits tokens, to be processed by a state machine.
 */
class InterpretingWriter<S, T>(val stateMachine: StateMachine<S, Token>, var state: S, val adapt: S.() -> T) : Filter<T> {

    private fun receive(token: Token): Filter<T> = apply { state = stateMachine(state, token) }

    override fun beginObject(): Filter<T> = receive(Token.BeginObject)
    override fun beginArray(): Filter<T> = receive(Token.BeginArray)
    override fun end(): Filter<T> = receive(Token.End)
    override fun key(key: String): Filter<T> = receive(Token.Key(key))
    override fun add(value: String): Filter<T> = receive(Token.StringValue(value))
    override fun add(value: BigDecimal): Filter<T> = receive(Token.NumberValue(value))
    override fun add(value: Boolean): Filter<T> = receive(if (value) Token.TrueValue else Token.FalseValue)
    override fun addNull(): Filter<T> = receive(Token.NullValue)

    override val result: T get() = state.adapt()
}

/**
 * A converter which works in the opposite direction, applying tokens to a writer.
 */
fun <T : BasketWriter<T>> inputWritingStateMachine(): StateMachine<T, Token> = { writer, token ->
    when(token) {
        is Token.BeginObject -> writer.beginObject()
        is Token.BeginArray -> writer.beginArray()
        is Token.End -> writer.end()
        is Token.Key -> writer.key(token.key)
        is Token.StringValue -> writer.add(token.value)
        is Token.NumberValue -> writer.add(token.value)
        is Token.TrueValue -> writer.add(true)
        is Token.FalseValue -> writer.add(false)
        is Token.NullValue -> writer.addNull()
    }
}

/**
 * An extension method which enables a Basket to write itself to a state machine which consumes tokens.
 */
fun <S> Basket.writeTo(state: S, stateMachine: StateMachine<S, Token>): S {
    fun S.receive(token: Token): S = stateMachine(this, token)
    return when(this) {
        is Basket.StringBasket -> state.receive(Token.StringValue(stringValue))
        is Basket.NumberBasket -> state.receive(Token.NumberValue(numberValue))
        is Basket.TrueBasket -> state.receive(Token.TrueValue)
        is Basket.FalseBasket -> state.receive(Token.FalseValue)
        is Basket.NullBasket -> state.receive(Token.NullValue)
        is Basket.ArrayBasket -> contents.fold(state.receive(Token.BeginArray)) { newState, basket ->
            basket.writeTo(newState, stateMachine)
        }.receive(Token.End)
        is Basket.ObjectBasket -> properties.fold(state.receive(Token.BeginObject)) { newState, (key, value) ->
            value.writeTo(newState.receive(Token.Key(key)), stateMachine)
        }.receive(Token.End)
    }
}

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

    class ArrayIndex(val outer: Position, depth: Int, var index: Int): Position(depth) {
        override fun exit(): Position = outer

        override fun key(key: String): Position = throw IllegalStateException("key() called on array index position")
        override fun advance(): Position = apply { this.index += 1 }
        override fun checkAgainst(pathSegment: PathSegment): PathSegmentMatchResult = pathSegment.matchesIndex(index)
    }

    class ObjectKey(val outer: Position, depth: Int, var key: String?): Position(depth) {
        override fun exit(): Position = outer

        override fun key(key: String): Position = apply { this.key = key }
        override fun advance(): ObjectKey = apply { this.key = null }
        override fun checkAgainst(pathSegment: PathSegment): PathSegmentMatchResult =
                if (key == null) PathSegmentMatchResult.UNMATCHED
                else pathSegment.matchesKey(key!!)
    }

    abstract fun key(key: String): Position
    abstract fun advance(): Position

    fun enterArray(): Position = ArrayIndex(this, depth + 1, 0)
    fun enterObject(): Position = ObjectKey(this, depth + 1, null)

    abstract fun exit(): Position

    abstract fun checkAgainst(pathSegment: PathSegment): PathSegmentMatchResult

    override fun toString(): String = when(this) {
        is NoPosition -> ""
        is ArrayIndex -> "${outer.toString()}[$index]"
        is ObjectKey -> if (key == null) "${outer.toString()}.?" else "${outer.toString()}.$key"
    }

}

/**
 * A state machine which updates the Position based on incoming tokens.
 */
val positionStateMachine: StateMachine<Position, Token> = { position, input ->
    when(input) {
        is Token.Key -> position.key(input.key)
        is Token.BeginArray -> position.enterArray()
        is Token.BeginObject -> position.enterObject()
        is Token.End -> position.exit().advance()
        else -> position.advance()
    }
}

/**
 * Represents the state of path-binding, i.e. how much of a given path has been bound by the current Position
 */
sealed class PathBindingState(val outer: PathBindingState?, val atDepth: Int) {

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
 * Some awkward logic here.
 */
object pathBindingStateMachine : StateMachine<PathBindingState, Position> {

    override fun invoke(state: PathBindingState, position: Position): PathBindingState =
        if (position is Position.NoPosition) state
        else if (position.depth <= state.atDepth) invoke(state.outer!!, position)
        else if (state is PathBindingState.Partial) comparePartial(state, state.remainingPath, position)
        else state

    private fun comparePartial(state: PathBindingState, remainingPath: Path, position: Position): PathBindingState =
        if (remainingPath.head().isConditional) PathBindingState.Conditional(state, position.depth, remainingPath)
        else when (position.checkAgainst(remainingPath.head())) {
            PathSegmentMatchResult.UNMATCHED -> PathBindingState.Deviated(state, position.depth)
            PathSegmentMatchResult.MATCHED_UNBOUND -> state
            PathSegmentMatchResult.MATCHED_BOUND -> boundCase(state, remainingPath.tail(), position)
        }

    private fun boundCase(state: PathBindingState, tail: Path, position: Position) =
            if (tail.isEmpty) PathBindingState.Complete(state, position.depth)
            else PathBindingState.Partial(state, position.depth, tail)
}

/**
 * Combined state from position tracking and path binding.
 */
class PositionTrackingState(var position: Position, var pathBindingState: PathBindingState) {
    val atBindingPoint: Boolean get() = position.depth == pathBindingState.atDepth

    companion object {
        @JvmStatic
        fun fromPath(path: Path): PositionTrackingState = PositionTrackingState(Position.empty, PathBindingState.fromPath(path))
    }
}

/**
 * We use the position state machine and path binding state machine together to move both states forward based on a single token.
 */
val positionTrackingStateMachine : StateMachine<PositionTrackingState, Token> = { state, token ->
    state.apply {
        position = positionStateMachine(position, token)
        pathBindingState = pathBindingStateMachine(pathBindingState, position)
    }
}

/**
 * A position-aware interpreter keeps track of position and path-binding, and drives a downstream state machine.
 */
class PositionAwareInterpreterState<S>(var positionTrackingState: PositionTrackingState, var downstreamState: S) {
    fun with(positionTrackingState: PositionTrackingState, downstreamState: S): PositionAwareInterpreterState<S> = apply {
        this.positionTrackingState = positionTrackingState
        this.downstreamState = downstreamState
    }

    companion object {
        @JvmStatic
        fun <S> fromPath(path: Path, downstreamState: S) = PositionAwareInterpreterState(PositionTrackingState.fromPath(path), downstreamState)
    }
}

/**
 * A path-aware interpreter generates tokens for a downstream state machine based on the current token and position-tracking state.
 */
typealias PathAwareInterpreter<I> = (PositionTrackingState, Token) -> I
typealias PositionAwareStateMachine<S> = StateMachine<PositionAwareInterpreterState<S>, Token>

/**
 * Given a path-aware interpreter and a downstream state machine, a position-aware state machine consumes tokens,
 * passes position-tracking state to the interpreter, and forwards the interpreted tokens to the downstream state.
 */
fun <S, I> positionAwareStateMachine(pathAwareInterpreter: PathAwareInterpreter<I>, downstreamStateMachine: StateMachine<S, I>)
    : PositionAwareStateMachine<S> = { state, input ->
        positionTrackingStateMachine(state.positionTrackingState, input).let { newPositionTrackingState ->
            state.with(
                    newPositionTrackingState,
                    downstreamStateMachine(
                            state.downstreamState,
                            pathAwareInterpreter(newPositionTrackingState, input)))
        }
    }

/**
 * A token sent to a state machine which either passes tokens through to a downstream state machine, or collects them to
 * build a basket which is then updated before being written out to the downstream state machine.
 */
sealed class UpdaterToken() {

    class PassThrough(val wrappedToken: Token): UpdaterToken()
    class StartUpdating(val wrappedToken: Token, val updater: Updater): UpdaterToken()
    class ContinueUpdating(val wrappedToken: Token): UpdaterToken()
    object EndUpdating: UpdaterToken()

}

typealias UpdatingInterpreter = PathAwareInterpreter<UpdaterToken>

/**
 * The interpreter which generates UpdaterTokens out of ordinary tokens, based on position and path-binding information
 */
fun updatingInterpreter(updater: Updater): UpdatingInterpreter = { state, token ->
    state.pathBindingState.let {
        when (it) {
            is PathBindingState.Complete ->
                if (state.atBindingPoint) UpdaterToken.StartUpdating(token, updater)
                else if (token is Token.End) UpdaterToken.EndUpdating
                else UpdaterToken.ContinueUpdating(token)

            is PathBindingState.Conditional ->
                if (state.atBindingPoint) UpdaterToken.StartUpdating(
                        token,
                        it.conditionalPath.head().createItemUpdater(it.conditionalPath.tail(), updater))
                else if (token is Token.End) UpdaterToken.EndUpdating
                else UpdaterToken.ContinueUpdating(token)

            else -> UpdaterToken.PassThrough(token)
        }
    }
}

/**
 * The current state of an updater. It's either writing things straight through, or buffering them so an updater can
 * update them before flushing the updated Basket to the downstream consumer.
 */
sealed class UpdaterState<S> {

    class PassingThrough<S>(var downstreamState: S): UpdaterState<S>() {
        fun passingThrough(downstreamState: S): UpdaterState<S> = apply { this.downstreamState = downstreamState }
    }

    class Buffering<S>(val downstreamState: S, var weavingState: BasketWeavingWriter, var updater: Updater): UpdaterState<S>() {
        fun buffer(token: Token): Buffering<S> = apply { weavingState = weavingStateMachine(weavingState, token) }
        fun flush(stateMachine: StateMachine<S, Token>): S =
                updater.update(weavingState.weave())
                        .writeTo(downstreamState, stateMachine)
    }

    companion object {
        private val weavingStateMachine = inputWritingStateMachine<BasketWeavingWriter>()

        @JvmStatic
        fun <S> initial(downstreamState: S): UpdaterState<S> = PassingThrough(downstreamState)
    }

}

/**
 * The updater state machine processes UpdaterTokens, and pushes ordinary Tokens down to the downstream state machine.
 */
class UpdaterStateMachine<S>(val downstreamStateMachine: StateMachine<S, Token>): StateMachine<UpdaterState<S>, UpdaterToken> {
    override fun invoke(state: UpdaterState<S>, input: UpdaterToken): UpdaterState<S> =
    when(input) {
        is UpdaterToken.PassThrough ->
            if (state is UpdaterState.PassingThrough) passThrough(state, input)
            else throw IllegalStateException("PassThrough received while updating")

        is UpdaterToken.StartUpdating ->
            if (state is UpdaterState.PassingThrough) startBuffering(state, input)
            else throw IllegalStateException("StartUpdating received while updating")

        is UpdaterToken.ContinueUpdating ->
            if (state is UpdaterState.Buffering) state.buffer(input.wrappedToken)
            else throw IllegalStateException("ContinueUpdating received while passing through")

        is UpdaterToken.EndUpdating ->
            if (state is UpdaterState.Buffering) UpdaterState.PassingThrough(state.flush(downstreamStateMachine))
            else throw IllegalStateException("EndUpdating received while passing through")
    }

    private fun startBuffering(state: UpdaterState.PassingThrough<S>, input: UpdaterToken.StartUpdating): UpdaterState.Buffering<S> =
            UpdaterState.Buffering(
                state.downstreamState,
                Writers.weavingTransient(),
                input.updater)
            .buffer(input.wrappedToken)

    private fun passThrough(state: UpdaterState.PassingThrough<S>, input: UpdaterToken.PassThrough) = state.passingThrough(downstreamStateMachine(state.downstreamState, input.wrappedToken))
}

fun <T: BasketWriter<T>> updatingFilter(lens: Lens, updater: Updater, writer: T): Filter<T> {
    val interpreter: UpdatingInterpreter = updatingInterpreter(updater)
    val downstreamStateMachine = UpdaterStateMachine(inputWritingStateMachine<T>())
    val positionAwareSM= positionAwareStateMachine(interpreter, downstreamStateMachine)
    val initialState = PositionAwareInterpreterState.fromPath(lens.path, UpdaterState.initial(writer))

    return InterpretingWriter(positionAwareSM, initialState) {
        downstreamState.let {
            if (it is UpdaterState.PassingThrough) it.downstreamState
            else throw IllegalStateException("Illegal terminal state")
        }
    }
}