package com.codepoetics.raffia.streaming

import com.codepoetics.raffia.functions.Updater
import com.codepoetics.raffia.functions.updater
import com.codepoetics.raffia.writers.BasketWeavingWriter
import com.codepoetics.raffia.writers.Writers

/**
 * A token sent to a state machine which either passes tokens through to a downstream state machine, or collects them to
 * build a basket which is then updated before being written out to the downstream state machine.
 */
internal sealed class UpdaterToken() {

    data class PassThrough(var wrappedToken: Token) : UpdaterToken()
    data class StartUpdating(var updater: Updater, var wrappedToken: Token) : UpdaterToken()
    data class ContinueUpdating(var wrappedToken: Token) : UpdaterToken()

}

/**
 * The interpreter which generates UpdaterTokens out of ordinary tokens, based on position and path-binding information
 */
internal class UpdatingInterpreter(val updater: Updater) : PathAwareInterpreter<UpdaterToken> {

    private val passThroughToken: UpdaterToken.PassThrough = UpdaterToken.PassThrough(Token.NullValue)
    private val startUpdatingToken: UpdaterToken.StartUpdating = UpdaterToken.StartUpdating(updater { it }, Token.NullValue)
    private val continueUpdatingToken: UpdaterToken.ContinueUpdating = UpdaterToken.ContinueUpdating(Token.NullValue)

    private fun passThrough(wrappedToken: Token) = passThroughToken.apply { this.wrappedToken = wrappedToken }
    private fun startUpdating(updater: Updater, wrappedToken: Token) = startUpdatingToken.apply {
        this.updater = updater
        this.wrappedToken = wrappedToken
    }

    private fun continueUpdating(wrappedToken: Token) = continueUpdatingToken.apply {
        this.wrappedToken = wrappedToken
    }

    override fun invoke(state: PositionTrackingState, token: Token): UpdaterToken =
            state.current.let {
                when (it.type) {
                    PathBindingType.COMPLETE ->
                        if (state.atBindingPoint) {
                            if (token is Token.End) passThrough(token)
                            else startUpdating(updater, token)
                        } else continueUpdating(token)

                    PathBindingType.CONDITIONAL ->
                        if (state.atBindingPoint) {
                            if (token is Token.End) passThrough(token)
                            else startUpdating(
                                    it.remainingPath.head().createItemUpdater(it.remainingPath.tail(), updater), token)
                        } else continueUpdating(token)

                    else -> passThrough(token)
                }
            }
}


/**
 * The current state of an updater. It's either writing things straight through, or buffering them so an updater can
 * update them before flushing the updated Basket to the downstream consumer.
 */
internal sealed class UpdaterState<S> {

    abstract internal fun receive(downstreamStateMachine: StateMachine<S, Token>, token: UpdaterToken): UpdaterState<S>

    data class PassingThrough<S>(var downstreamState: S, var streamWeaverState: StreamWeaverState) : UpdaterState<S>() {

        var cachedBuffering: Buffering<S>? = null

        private fun buffering(downstreamState: S, updater: Updater): Buffering<S> {
            cachedBuffering = if (cachedBuffering == null) {
                Buffering(downstreamState, streamWeaverState.clear(), 0, updater, this)
            } else {
                cachedBuffering!!.apply {
                    this.downstreamState = downstreamState
                    this.weavingState = weavingState
                    this.depth = 0
                    this.updater = updater
                }
            }
            return cachedBuffering!!
        }


        override fun receive(downstreamStateMachine: StateMachine<S, Token>, token: UpdaterToken): UpdaterState<S> =
                when (token) {
                    is UpdaterToken.PassThrough -> apply {
                        downstreamState = downstreamStateMachine(downstreamState, token.wrappedToken)
                    }

                    is UpdaterToken.StartUpdating ->
                        buffering(downstreamState, token.updater)
                                .receiveDownstreamToken(downstreamStateMachine, token.wrappedToken)

                    is UpdaterToken.ContinueUpdating -> throw IllegalStateException("ContinueUpdating received while passing through")
                }
    }

    data class Buffering<S>(var downstreamState: S, var weavingState: StreamWeaverState, var depth: Int, var updater: Updater, private val passingThrough: PassingThrough<S>) : UpdaterState<S>() {

        val tokenFactory = TokenFactory()

        private fun passingThrough(downstreamState: S): PassingThrough<S> = passingThrough.apply {
            this.downstreamState = downstreamState
        }

        override fun receive(downstreamStateMachine: StateMachine<S, Token>, token: UpdaterToken): UpdaterState<S> =
                when (token) {
                    is UpdaterToken.PassThrough -> throw IllegalStateException("PassThrough received while updating")
                    is UpdaterToken.StartUpdating -> throw IllegalStateException("StartUpdating received while updating")
                    is UpdaterToken.ContinueUpdating -> receiveDownstreamToken(downstreamStateMachine, token.wrappedToken)
                }

        internal fun receiveDownstreamToken(downstreamStateMachine: StateMachine<S, Token>, token: Token): UpdaterState<S> = apply {
            weavingState = streamWeaverStateMachine(weavingState, token)
        }.flushIfComplete(downstreamStateMachine)

        private fun flushIfComplete(downstreamStateMachine: StateMachine<S, Token>): UpdaterState<S> =
                if (weavingState.structStack.empty) passingThrough(flush(downstreamStateMachine))
                else this

        private fun flush(downstreamStateMachine: StateMachine<S, Token>): S =
                updater.update(weavingState.result).writeTo(downstreamState, downstreamStateMachine, tokenFactory)
    }

    companion object {
        @JvmStatic
        fun <S> initial(downstreamState: S): UpdaterState<S> = PassingThrough(downstreamState, StreamWeaverState())
    }

}