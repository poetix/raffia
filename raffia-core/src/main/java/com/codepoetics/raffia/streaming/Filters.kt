package com.codepoetics.raffia.streaming

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.functions.Updater
import com.codepoetics.raffia.lenses.Lens
import com.codepoetics.raffia.operations.ProjectionResult
import com.codepoetics.raffia.writers.BasketWriter
import java.util.ArrayList

object Filters {

    @JvmStatic
    fun <T : BasketWriter<T>> updating(lens: Lens, updater: Updater, writer: T): Filter<T> {
        val interpreter: UpdatingInterpreter = updatingInterpreter(updater)
        val inputWritingStateMachine = inputWritingStateMachine<T>()
        val downstreamStateMachine = { state: UpdaterState<T>, token: UpdaterToken -> state.receive(inputWritingStateMachine, token) }
        val positionAwareSM = positionAwareStateMachine(interpreter, downstreamStateMachine)
        val initialState = PositionAwareInterpreterState.fromPath(lens.path, UpdaterState.initial(writer))

        return InterpretingWriter(positionAwareSM, initialState) {
            downstreamState.let {
                if (it is UpdaterState.PassingThrough) it.downstreamState
                else throw IllegalStateException("Illegal terminal state")
            }
        }
    }

    @JvmStatic
    fun <S> projecting(lens: Lens, initial: S, basketReceivingStateMachine: StateMachine<S, Basket>): Filter<S> {
        val downstreamStateMachine = { state: ProjectorState<S>, token: ProjectorToken -> state.receive(basketReceivingStateMachine, token) }
        val positionAwareSM = positionAwareStateMachine(projectingInterpreter, downstreamStateMachine)
        val initialState = PositionAwareInterpreterState.fromPath(lens.path, ProjectorState.initial(initial))

        return InterpretingWriter(positionAwareSM, initialState) {
            downstreamState.let {
                if (it is ProjectorState.DoingNothing) it.downstreamState
                else throw IllegalStateException("Illegal terminal state")
            }
        }
    }

    @JvmStatic
    fun projecting(lens: Lens): Filter<ProjectionResult<Basket>> =
            projecting(lens, ProjectionResult.empty()) { result, basket -> result.add(ProjectionResult.ofSingle(basket)) }
}