package com.codepoetics.raffia.jackson

import com.codepoetics.raffia.baskets.Basket
import com.codepoetics.raffia.writers.BasketWriter
import com.codepoetics.raffia.writers.writeTo
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator

import java.io.IOException
import java.io.OutputStream
import java.io.StringWriter
import java.io.Writer
import java.math.BigDecimal

class JsonWriter private constructor(private val generator: JsonGenerator) : BasketWriter<JsonWriter> {

    private enum class StructContext {
        OBJECT,
        ARRAY,
        NONE
    }

    private val contextStack = mutableListOf<StructContext>(StructContext.NONE)
    private var stackPtr = 0

    override fun beginObject(): JsonWriter = apply {
        generator.writeStartObject()

        stackPtr += 1
        if (stackPtr == contextStack.size) contextStack.add(StructContext.OBJECT) else contextStack[stackPtr] = StructContext.OBJECT
    }

    override fun beginArray(): JsonWriter = apply {
        generator.writeStartArray()

        stackPtr += 1
        if (stackPtr == contextStack.size) contextStack.add(StructContext.ARRAY) else contextStack[stackPtr] = StructContext.ARRAY
    }

    private inline fun update(block: JsonWriter.() -> Unit) = apply {
        block()
        if (stackPtr == 0) generator.flush()
    }

    override fun end(): JsonWriter =
        when (contextStack[stackPtr]) {
            JsonWriter.StructContext.ARRAY -> update {
                generator.writeEndArray()
                stackPtr -= 1
            }
            JsonWriter.StructContext.OBJECT -> update {
                generator.writeEndObject()
                stackPtr -= 1
            }
            else -> throw IllegalStateException("exit called without matching startArray or startObject")
        }

    override fun key(key: String): JsonWriter = apply {
        generator.writeFieldName(key)
    }

    override fun add(value: String): JsonWriter = update {
        generator.writeString(value)
    }

    override fun add(value: BigDecimal): JsonWriter = update {
        generator.writeNumber(value)
    }

    override fun add(value: Boolean): JsonWriter = update {
        generator.writeBoolean(value)
    }

    override fun addNull(): JsonWriter = update {
        generator.writeNull()
    }

    companion object {

        private val DEFAULT = JsonFactory()

        @JvmStatic
        fun writeBasketAsString(basket: Basket, jsonFactory: JsonFactory): String {
            val writer = StringWriter()
            writeBasketTo(basket, writer, jsonFactory)
            return writer.toString()
        }

        @JvmStatic
        fun writeBasketAsString(basket: Basket): String {
            val writer = StringWriter()
            writeBasketTo(basket, writer)
            return writer.toString()
        }

        @JvmStatic
        @JvmOverloads fun writeBasketTo(basket: Basket, outputStream: OutputStream, jsonFactory: JsonFactory = DEFAULT): JsonWriter {
            return basket.writeTo(writingTo(jsonFactory, outputStream))
        }

        @JvmStatic
        @JvmOverloads fun writeBasketTo(basket: Basket, writer: Writer, jsonFactory: JsonFactory = DEFAULT): JsonWriter {
            return basket.writeTo(writingTo(jsonFactory, writer))
        }

        @JvmStatic
        fun writingTo(outputStream: OutputStream): JsonWriter {
            return writingTo(DEFAULT, outputStream)
        }

        @JvmStatic
        fun writingTo(writer: Writer): JsonWriter {
            return writingTo(DEFAULT, writer)
        }

        @JvmStatic
        fun writingTo(factory: JsonFactory, outputStream: OutputStream): JsonWriter {
            try {
                return writingTo(factory.createGenerator(outputStream))
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

        }

        @JvmStatic
        fun writingTo(factory: JsonFactory, writer: Writer): JsonWriter {
            try {
                return writingTo(factory.createGenerator(writer))
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

        }

        @JvmStatic
        fun writingTo(generator: JsonGenerator): JsonWriter {
            return JsonWriter(generator)
        }
    }
}
