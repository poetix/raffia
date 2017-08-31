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

class JsonWriter private constructor(private val parent: JsonWriter?, private val context: StructContext, private val generator: JsonGenerator) : BasketWriter<JsonWriter> {

    private enum class StructContext {
        OBJECT,
        ARRAY,
        NONE
    }

    override fun beginObject(): JsonWriter {
        try {
            generator.writeStartObject()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return JsonWriter(this, StructContext.OBJECT, generator)
    }

    override fun beginArray(): JsonWriter {
        try {
            generator.writeStartArray()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return JsonWriter(this, StructContext.ARRAY, generator)
    }

    override fun end(): JsonWriter {
        try {
            when (context) {
                JsonWriter.StructContext.ARRAY -> {
                    generator.writeEndArray()
                    return flushOnCompletion(parent!!)
                }
                JsonWriter.StructContext.OBJECT -> {
                    generator.writeEndObject()
                    return flushOnCompletion(parent!!)
                }
                else -> throw IllegalStateException("exit called without matching startArray or startObject")
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    private fun flushOnCompletion(writer: JsonWriter): JsonWriter {
        if (writer.context == StructContext.NONE) {
            try {
                generator.flush()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

        }
        return writer
    }

    override fun key(key: String): JsonWriter {
        try {
            generator.writeFieldName(key)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return this
    }

    override fun add(value: String): JsonWriter {
        try {
            generator.writeString(value)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return flushOnCompletion(this)
    }

    override fun add(value: BigDecimal): JsonWriter {
        try {
            generator.writeNumber(value)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return flushOnCompletion(this)
    }

    override fun add(value: Boolean): JsonWriter {
        try {
            generator.writeBoolean(value)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return flushOnCompletion(this)
    }

    override fun addNull(): JsonWriter {
        try {
            generator.writeNull()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return flushOnCompletion(this)
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
            return JsonWriter(null, StructContext.NONE, generator)
        }
    }
}
