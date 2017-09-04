package com.codepoetics.raffia.streaming

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.baskets.ObjectEntry

enum class StreamWeaverStructType {
    OBJECT,
    ARRAY
}

data class StreamWeaverStruct(
        var type: StreamWeaverStructType,
        val contents: MutableList<Basket>,
        val entries: MutableList<ObjectEntry>) {

    val basket: Basket get() = when (type) {
        StreamWeaverStructType.OBJECT -> Basket.ofObject(entries)
        StreamWeaverStructType.ARRAY -> Basket.ofArray(contents)
    }
}

class StreamWeaverState(var key: String = "", var result: Basket = Basket.ofNull()) {
    val structStack = ReusableStack<StreamWeaverStruct> { StreamWeaverStruct(StreamWeaverStructType.OBJECT, mutableListOf(), mutableListOf()) }

    fun complete(basket: Basket): StreamWeaverState = apply {
        if (isComplete) result = basket
        else {
            structStack.current!!.apply {
                when (type) {
                    StreamWeaverStructType.OBJECT -> entries.add(ObjectEntry(key, basket))
                    StreamWeaverStructType.ARRAY -> contents.add(basket)
                }
            }
        }
    }

    val isComplete: Boolean get() = structStack.empty

    fun clear() = apply {
        structStack.clear()
    }
}

val streamWeaverStateMachine: StateMachine<StreamWeaverState, Token> = { state, token ->
    state.apply {
        when (token) {
            is Token.End -> {
                if (structStack.empty) throw IllegalArgumentException("End received while not building object or array")
                val basket = structStack.current!!.basket
                structStack.pop()
                complete(basket)
            }
            is Token.Key -> this.key = token.key
            is Token.BeginObject ->
                structStack.push {
                    type = StreamWeaverStructType.OBJECT
                    entries.clear()
                }
            is Token.BeginArray ->
                structStack.push {
                    type = StreamWeaverStructType.ARRAY
                    contents.clear()
                }
            is Token.NullValue -> complete(Basket.ofNull())
            is Token.TrueValue -> complete(Basket.ofBoolean(true))
            is Token.FalseValue -> complete(Basket.ofBoolean(false))
            is Token.NumberValue -> complete(Basket.ofNumber(token.value))
            is Token.StringValue -> complete(Basket.ofString(token.value))
        }
    }
}