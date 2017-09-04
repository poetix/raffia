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
    data class Key(var key: String) : Token()
    data class StringValue(var value: String) : Token()
    data class NumberValue(var value: BigDecimal) : Token()
}

typealias StateMachine<S, I> = (S, I) -> S

interface Filter<T> : BasketWriter<Filter<T>> {
    val result: T
}

class TokenFactory {

    private val keyToken: Token.Key = Token.Key("")
    private val stringValueToken: Token.StringValue = Token.StringValue("")
    private val numberValueToken: Token.NumberValue = Token.NumberValue(BigDecimal.ZERO)

    fun keyToken(key: String): Token = keyToken.apply { this.key = key }
    fun stringToken(value: String): Token = stringValueToken.apply {  this.value = value}
    fun numberToken(value: BigDecimal): Token = numberValueToken.apply { this.value = value }

}

/**
 * A writer which emits tokens, to be processed by a state machine.
 */
class InterpretingWriter<S, T>(
        val stateMachine: StateMachine<S, Token>,
        var state: S,
        val adapt: S.() -> T) : Filter<T> {

    private val keyToken: Token.Key = Token.Key("")
    private val stringValueToken: Token.StringValue = Token.StringValue("")
    private val numberValueToken: Token.NumberValue = Token.NumberValue(BigDecimal.ZERO)

    private fun keyToken(key: String): Token = keyToken.apply { this.key = key }
    private fun stringToken(value: String): Token = stringValueToken.apply {  this.value = value}
    private fun numberToken(value: BigDecimal): Token = numberValueToken.apply { this.value = value }

    private fun receive(token: Token): Filter<T> = apply { state = stateMachine(state, token) }

    override fun beginObject(): Filter<T> = receive(Token.BeginObject)
    override fun beginArray(): Filter<T> = receive(Token.BeginArray)
    override fun end(): Filter<T> = receive(Token.End)
    override fun key(key: String): Filter<T> = receive(keyToken(key))
    override fun add(value: String): Filter<T> = receive(stringToken(value))
    override fun add(value: BigDecimal): Filter<T> = receive(numberToken(value))
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
fun <S> Basket.writeTo(state: S, stateMachine: StateMachine<S, Token>, tokenFactory: TokenFactory): S {
    return when (this) {
        is Basket.StringBasket -> stateMachine(state, tokenFactory.stringToken(stringValue))
        is Basket.NumberBasket -> stateMachine(state, tokenFactory.numberToken(numberValue))
        is Basket.TrueBasket -> stateMachine(state, Token.TrueValue)
        is Basket.FalseBasket -> stateMachine(state, Token.FalseValue)
        is Basket.NullBasket -> stateMachine(state, Token.NullValue)
        is Basket.ArrayBasket -> stateMachine(contents.fold(stateMachine(state, Token.BeginArray)) { newState, basket ->
                basket.writeTo(newState, stateMachine, tokenFactory)
            }, Token.End)
        is Basket.ObjectBasket -> stateMachine(properties.fold(stateMachine(state, Token.BeginObject)) { newState, (key, value) ->
                value.writeTo(stateMachine(newState, tokenFactory.keyToken(key)), stateMachine, tokenFactory)
            }, Token.End)
    }
}

class ReusableStack<T>(val stack: MutableList<T> = mutableListOf<T>(), var stackPtr: Int = -1, inline val provider: () -> T) {
    val empty: Boolean get() = stackPtr == -1

    var current: T? = null

    inline fun push(initialise: T.() -> Unit): T {
        stackPtr += 1
        val item: T = if (stackPtr == stack.size) provider().also { stack.add(it) } else stack[stackPtr]
        item.initialise()
        current = item
        return item
    }

    fun pop() {
        if (stackPtr < 0) throw IllegalStateException("pop() called on empty stack")
        stackPtr -= 1
        current = if (stackPtr < 0) null else stack[stackPtr]
    }

    val contents: List<T> get() = stack.take(stackPtr + 1)

    val depth: Int get() = stackPtr

    fun clear() = apply {
        stack.clear()
    }
}