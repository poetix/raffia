package com.codepoetics.raffia.streaming

import com.codepoetics.raffia.functions.Updater
import com.codepoetics.raffia.writers.BasketWeavingWriter
import com.codepoetics.raffia.writers.Writers

/**
 * A token sent to a state machine which either passes tokens through to a downstream state machine, or collects them to
 * build a basket which is then updated before being written out to the downstream state machine.
 */
internal sealed class UpdaterToken() {

    data class PassThrough(val wrappedToken: Token) : UpdaterToken()
    data class StartUpdating(val updater: Updater, val wrappedToken: Token) : UpdaterToken()
    data class ContinueUpdating(val wrappedToken: Token) : UpdaterToken()

}

internal typealias UpdatingInterpreter = PathAwareInterpreter<UpdaterToken>

/**
 * The interpreter which generates UpdaterTokens out of ordinary tokens, based on position and path-binding information
 */
internal fun updatingInterpreter(updater: Updater): UpdatingInterpreter = { state, token ->
    state.pathBindingState.let {
        when (it) {
            is PathBindingState.Complete ->
                if (state.atBindingPoint) {
                    if (token is Token.End) UpdaterToken.PassThrough(token)
                    else UpdaterToken.StartUpdating(updater, token)
                } else UpdaterToken.ContinueUpdating(token)

            is PathBindingState.Conditional ->
                if (state.atBindingPoint) {
                    if (token is Token.End) UpdaterToken.PassThrough(token)
                    else UpdaterToken.StartUpdating(
                            it.conditionalPath.head().createItemUpdater(it.conditionalPath.tail(), updater), token)
                } else UpdaterToken.ContinueUpdating(token)

            else -> UpdaterToken.PassThrough(token)
        }
    }
}

/**
 * The current state of an updater. It's either writing things straight through, or buffering them so an updater can
 * update them before flushing the updated Basket to the downstream consumer.
 */
internal sealed class UpdaterState<S> {

    abstract internal fun receive(downstreamStateMachine: StateMachine<S, Token>, token: UpdaterToken): UpdaterState<S>

    data class PassingThrough<S>(var downstreamState: S) : UpdaterState<S>() {
        override fun receive(downstreamStateMachine: StateMachine<S, Token>, token: UpdaterToken): UpdaterState<S> =
                when (token) {
                    is UpdaterToken.PassThrough -> apply {
                        downstreamState = downstreamStateMachine(downstreamState, token.wrappedToken)
                    }

                    is UpdaterToken.StartUpdating ->
                        Buffering(downstreamState, Writers.weavingTransient(), 0, token.updater)
                                .receiveDownstreamToken(downstreamStateMachine, token.wrappedToken)

                    is UpdaterToken.ContinueUpdating -> throw IllegalStateException("ContinueUpdating received while passing through")
                }
    }

    data class Buffering<S>(val downstreamState: S, var weavingState: BasketWeavingWriter, var depth: Int, var updater: Updater) : UpdaterState<S>() {
        override fun receive(downstreamStateMachine: StateMachine<S, Token>, token: UpdaterToken): UpdaterState<S> =
                when (token) {
                    is UpdaterToken.PassThrough -> throw IllegalStateException("PassThrough received while updating")
                    is UpdaterToken.StartUpdating -> throw IllegalStateException("StartUpdating received while updating")
                    is UpdaterToken.ContinueUpdating -> receiveDownstreamToken(downstreamStateMachine, token.wrappedToken)
                }

        private fun update(block: BasketWeavingWriter.() -> BasketWeavingWriter) = apply {
            weavingState = weavingState.block()
        }

        internal fun receiveDownstreamToken(downstreamStateMachine: StateMachine<S, Token>, token: Token): UpdaterState<S> = when (token) {
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

        private fun flushIfComplete(downstreamStateMachine: StateMachine<S, Token>): UpdaterState<S> =
                if (depth == 0) UpdaterState.PassingThrough(flush(downstreamStateMachine))
                else this

        private fun flush(downstreamStateMachine: StateMachine<S, Token>): S =
                updater.update(weavingState.weave()).writeTo(downstreamState, downstreamStateMachine)
    }

    companion object {
        @JvmStatic
        fun <S> initial(downstreamState: S): UpdaterState<S> = PassingThrough(downstreamState)
    }

}