package com.codepoetics.raffia.streaming

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.functions.Projector
import com.codepoetics.raffia.operations.Projectors
import com.codepoetics.raffia.writers.BasketWeavingWriter
import com.codepoetics.raffia.writers.Writers

/**
 * A token sent to a state machine which either throws them away, or collects them to
 * build a basket which is then projected and the results sent to a downstream state machine.
 */
internal sealed class ProjectorToken() {

    object DoNothing: ProjectorToken()
    data class StartProjecting(val projector: Projector<Basket>, val wrappedToken: Token) : ProjectorToken()
    data class ContinueProjecting(val wrappedToken: Token) : ProjectorToken()

}

internal typealias ProjectingInterpreter = PathAwareInterpreter<ProjectorToken>

/**
 * The interpreter which generates ProjectorTokens out of ordinary tokens, based on position and path-binding information
 */
internal val projectingInterpreter: ProjectingInterpreter = { state, token ->
    state.pathBindingState.let {
        when (it) {
            is PathBindingState.Complete ->
                if (state.atBindingPoint) {
                    if (token is Token.End) ProjectorToken.DoNothing
                    else ProjectorToken.StartProjecting(Projectors.id, token)
                } else ProjectorToken.ContinueProjecting(token)

            is PathBindingState.Conditional ->
                if (state.atBindingPoint) {
                    if (token is Token.End) ProjectorToken.DoNothing
                    else ProjectorToken.StartProjecting(
                            it.conditionalPath.head().createItemProjector(it.conditionalPath.tail()), token)
                } else ProjectorToken.ContinueProjecting(token)

            else -> ProjectorToken.DoNothing
        }
    }
}

/**
 * The current state of a projecting state machine. It's either ignoring inputs, or buffering them so a projector can
 * project them and flush the projected Baskets to the downstream consumer.
 */
internal sealed class ProjectorState<S> {

    abstract internal fun receive(downstreamStateMachine: StateMachine<S, Basket>, token: ProjectorToken): ProjectorState<S>

    data class DoingNothing<S>(var downstreamState: S) : ProjectorState<S>() {
        override fun receive(downstreamStateMachine: StateMachine<S, Basket>, token: ProjectorToken): ProjectorState<S> =
                when (token) {
                    is ProjectorToken.DoNothing -> this

                    is ProjectorToken.StartProjecting ->
                        Buffering(downstreamState, Writers.weavingTransient(), 0, token.projector)
                                .receiveDownstreamToken(downstreamStateMachine, token.wrappedToken)

                    is ProjectorToken.ContinueProjecting -> throw IllegalStateException("ContinueProjecting received while doing nothing")
                }
    }

    data class Buffering<S>(val downstreamState: S, var weavingState: BasketWeavingWriter, var depth: Int, var projector: Projector<Basket>) : ProjectorState<S>() {
        override fun receive(downstreamStateMachine: StateMachine<S, Basket>, token: ProjectorToken): ProjectorState<S> =
                when (token) {
                    is ProjectorToken.DoNothing -> throw IllegalStateException("DoNothing received while projecting")
                    is ProjectorToken.StartProjecting -> throw IllegalStateException("StartProjecting received while projecting")
                    is ProjectorToken.ContinueProjecting -> receiveDownstreamToken(downstreamStateMachine, token.wrappedToken)
                }

        private fun update(block: BasketWeavingWriter.() -> BasketWeavingWriter): Buffering<S> = apply {
            weavingState = weavingState.block()
        }

        internal fun receiveDownstreamToken(downstreamStateMachine: StateMachine<S, Basket>, token: Token): ProjectorState<S> = when (token) {
            is Token.BeginArray -> update {
                depth += 1
                beginArray()
            }

            is Token.BeginObject -> update {
                depth += 1
                beginObject()
            }

            is Token.End -> update {
                depth -= 1
                end()
            }

            is Token.Key -> update { key(token.key) }
            is Token.StringValue -> update { add(token.value) }
            is Token.NumberValue -> update { add(token.value) }
            is Token.TrueValue -> update { add(true) }
            is Token.FalseValue -> update { add(false) }
            is Token.NullValue -> update { addNull() }
        }.flushIfComplete(downstreamStateMachine)

        private fun flushIfComplete(downstreamStateMachine: StateMachine<S, Basket>): ProjectorState<S> =
                if (depth == 0) ProjectorState.DoingNothing(flush(downstreamStateMachine))
                else this

        private fun flush(downstreamStateMachine: StateMachine<S, Basket>): S =
                projector.project(weavingState.weave()).fold(downstreamState, downstreamStateMachine)
    }

    companion object {
        @JvmStatic
        fun <S> initial(downstreamState: S): ProjectorState<S> = DoingNothing(downstreamState)
    }

}