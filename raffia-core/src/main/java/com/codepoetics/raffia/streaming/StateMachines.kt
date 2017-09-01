package com.codepoetics.raffia.streaming

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.writers.BasketWriter
import java.math.BigDecimal

/**
 * A streaming token
 */
sealed class Token {
    object BeginObject : Token()
    object BeginArray : Token()
    object End : Token()
    object NullValue : Token()
    object TrueValue : Token()
    object FalseValue : Token()
    data class Key(val key: String) : Token()
    data class StringValue(val value: String) : Token()
    data class NumberValue(val value: BigDecimal) : Token()
}

typealias StateMachine<S, I> = (S, I) -> S

interface Filter<T> : BasketWriter<Filter<T>> {
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
    when (token) {
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
    return when (this) {
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